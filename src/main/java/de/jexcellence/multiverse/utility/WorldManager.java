package de.jexcellence.multiverse.utility;

import de.jexcellence.je18n.i18n.I18n;
import de.jexcellence.multiverse.Multiverse;
import de.jexcellence.multiverse.database.entity.MVWorld;
import de.jexcellence.multiverse.generator.plotgenerator.PlotBiomeProvider;
import de.jexcellence.multiverse.generator.plotgenerator.PlotChunkGenerator;
import de.jexcellence.multiverse.generator.voidgenerator.VoidBiomeProvider;
import de.jexcellence.multiverse.generator.voidgenerator.VoidChunkGenerator;
import de.jexcellence.multiverse.type.MVWorldType;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.codehaus.plexus.util.FileUtils;
import org.jetbrains.annotations.NotNull;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.function.Function;

/**
 * Manages world creation, deletion, loading, and teleportation operations for the Multiverse plugin.
 * <p>
 * This class handles asynchronous operations efficiently with proper thread management and provides
 * feedback to players via localized messages. It uses custom chunk generators and biome providers for different world types.
 * </p>
 */
public class WorldManager {
  private static final Pattern WORLD_IDENTIFIER_PATTERN = Pattern.compile("^[a-zA-Z0-9/._-]+$");
  private static final int WORLD_LOAD_BATCH_SIZE = 128;

  /**
   * Mapping for plot generator parameters. The materials represent the primary block, secondary block, and accent block respectively.
   */
  private static final Map<MVWorldType, Material[]> PLOT_GENERATOR_PARAMS = Map.of(
    MVWorldType.PLOT, new Material[]{Material.STONE, Material.COBBLESTONE_SLAB, Material.IRON_BLOCK}
  );

  private final Multiverse multiverse;

  /**
   * Constructs a new WorldManager with the specified Multiverse instance.
   *
   * @param multiverse the instance of the Multiverse plugin used for accessing repositories, scheduler, and configuration.
   */
  public WorldManager(final @NotNull Multiverse multiverse) {
    this.multiverse = multiverse;
  }

  /**
   * Asynchronously creates a new world based on provided parameters.
   *
   * @param identifier    the unique identifier/name for the world to be created.
   * @param environment   the environment of the world (e.g., NORMAL, NETHER, THE_END).
   * @param type          the type of world to be created (e.g., VOID, PLOT, DEFAULT).
   * @param player        the player initiating the world creation.
   * @param forceCreation if true, forces world creation even if a world with the same identifier exists.
   */
  public void createWorld(
    final @NotNull String identifier,
    final @NotNull World.Environment environment,
    final @NotNull MVWorldType type,
    final @NotNull Player player,
    final boolean forceCreation
  ) {
    final World existingWorld = Bukkit.getWorld(identifier);
    if (existingWorld != null && !forceCreation) {
      sendPlayerMessage(player, "multiverse.world_already_exists", Map.of("world_name", identifier));
      return;
    }

    sendPlayerMessage(player, "multiverse.preparing_world", Map.of("world_name", identifier));

    CompletableFuture.supplyAsync(() -> createWorldCreator(identifier, environment, type), multiverse.getExecutor())
      .thenComposeAsync(this::createBukkitWorld, multiverse.getExecutor())
      .whenCompleteAsync((world, throwable) -> handleWorldCreationResult(world, throwable, identifier, player, type, environment), multiverse.getExecutor());
  }

  /**
   * Creates a {@link WorldCreator} instance based on the world type.
   *
   * @param identifier  the world identifier.
   * @param environment the world environment.
   * @param type        the world type.
   * @return a configured {@link WorldCreator} instance.
   */
  private WorldCreator createWorldCreator(String identifier, World.Environment environment, MVWorldType type) {
    return switch (type) {
      case VOID -> new WorldCreator(identifier)
        .environment(environment)
        .generator(new VoidChunkGenerator())
        .biomeProvider(new VoidBiomeProvider())
        .keepSpawnLoaded(TriState.TRUE);
      case PLOT -> {
        Material[] materials = PLOT_GENERATOR_PARAMS.get(type);
        yield new WorldCreator(identifier)
          .environment(environment)
          .generator(new PlotChunkGenerator(30, 6, 30, materials[0], materials[1], materials[2]))
          .biomeProvider(new PlotBiomeProvider())
          .keepSpawnLoaded(TriState.TRUE);
      }
      case DEFAULT -> new WorldCreator(identifier)
        .environment(environment)
        .keepSpawnLoaded(TriState.TRUE);
    };
  }

  /**
   * Creates a Bukkit world using the provided {@link WorldCreator} instance, scheduling the task on the main thread.
   *
   * @param worldCreator the {@link WorldCreator} instance that defines world creation parameters.
   * @return a {@link CompletableFuture} that resolves to the created {@link World} or completes exceptionally if an error occurs.
   */
  private CompletableFuture<World> createBukkitWorld(WorldCreator worldCreator) {
    CompletableFuture<World> future = new CompletableFuture<>();
    Bukkit.getScheduler().runTask(multiverse, () -> {
      try {
        World world = worldCreator.createWorld();
        if (world == null) throw new IllegalStateException("World creation returned null");
        // Set bedrock beneath the spawn location for a stable spawn area.
        world.getSpawnLocation().clone().subtract(0, 1, 0).toCenterLocation().getBlock().setType(Material.BEDROCK);
        future.complete(world);
      } catch (Exception e) {
        future.completeExceptionally(e);
      }
    });
    return future;
  }

  /**
   * Handles the result of the world creation process, notifying the player and updating the world repository.
   *
   * @param world       the created {@link World}, or null if creation failed.
   * @param throwable   the exception thrown during creation, or null if successful.
   * @param identifier  the world identifier.
   * @param player      the player that initiated world creation.
   * @param type        the world type.
   * @param environment the world environment.
   */
  private void handleWorldCreationResult(World world, Throwable throwable, String identifier, Player player, MVWorldType type, World.Environment environment) {
    if (throwable != null || world == null) {
      sendPlayerMessage(player, "multiverse.world_creation_failed", Map.of(
        "world_name", identifier,
        "exception", throwable != null ? throwable.getMessage() : "Unknown error"
      ));
      return;
    }

    try {
      MVWorld mvWorld = multiverse.getMvWorldRepository().create(
        new MVWorld.Builder(world, type, environment).build()
      );
      Bukkit.getScheduler().runTask(multiverse, () -> {
        multiverse.getWorlds().put(mvWorld.getIdentifier(), mvWorld);
        teleport(mvWorld, player);
      });
      sendPlayerMessage(player, "multiverse.world_created", Map.of(
        "world_name", identifier,
        "world_id", mvWorld.getId()
      ));
    } catch (Exception e) {
      multiverse.getPlatformLogger().logDebug("Failed to create MVWorld entry for " + identifier, e);
      sendPlayerMessage(player, "multiverse.world_creation_failed", Map.of(
        "world_name", identifier,
        "exception", e.getMessage()
      ));
      Bukkit.getScheduler().runTask(multiverse, () -> deleteWorld(identifier, player));
    }
  }

  /**
   * Loads existing worlds from the database and creates them in the game.
   * <p>
   * Only a batch of {@code WORLD_LOAD_BATCH_SIZE} worlds is loaded at once.
   * </p>
   */
  public void loadWorlds() {
    multiverse.getPlatformLogger().logInfo("Loading existing worlds...");
    multiverse.getMvWorldRepository().findAllAsync(0, WORLD_LOAD_BATCH_SIZE)
      .thenAcceptAsync(this::processLoadedWorlds, multiverse.getExecutor());
  }

  /**
   * Processes the list of loaded {@link MVWorld} records, creating in-game worlds.
   *
   * @param foundWorlds the list of {@link MVWorld} objects obtained from the database.
   */
  private void processLoadedWorlds(final List<MVWorld> foundWorlds) {
    if (foundWorlds == null || foundWorlds.isEmpty()) {
      multiverse.getPlatformLogger().logInfo("No existing worlds found in database");
      return;
    }

    foundWorlds.forEach(mvWorld -> {
      if (!WORLD_IDENTIFIER_PATTERN.matcher(mvWorld.getIdentifier()).matches()) {
        multiverse.getPlatformLogger().logDebug("Skipping invalid world: " + mvWorld.getIdentifier());
        return;
      }
      Bukkit.getScheduler().runTask(multiverse, () -> {
        WorldCreator creator = new WorldCreator(mvWorld.getIdentifier())
          .environment(mvWorld.getEnvironment())
          .keepSpawnLoaded(TriState.TRUE);
        switch (mvWorld.getType()) {
          case VOID:
            creator.generator(new VoidChunkGenerator()).biomeProvider(new VoidBiomeProvider());
            break;
          case PLOT:
            Material[] materials = PLOT_GENERATOR_PARAMS.get(MVWorldType.PLOT);
            creator.generator(new PlotChunkGenerator(30, 6, 30, materials[0], materials[1], materials[2]))
              .biomeProvider(new PlotBiomeProvider());
            break;
          // DEFAULT does not require a custom generator.
        }
        World world = creator.createWorld();
        if (world != null) {
          multiverse.getWorlds().put(mvWorld.getIdentifier(), mvWorld);
          multiverse.getPlatformLogger().logInfo("Loaded world: " + world.getName() + " (" + mvWorld.getType() + ")");
        }
      });
    });
  }

  /**
   * Asynchronously deletes the specified world if it exists and is not populated by players.
   * <p>
   * Notifies the initiating player with success or failure messages.
   * </p>
   *
   * @param identifier the unique identifier of the world to delete.
   * @param player     the player who initiated the deletion.
   */
  public void deleteWorld(final @NotNull String identifier, final @NotNull Player player) {
    final World world = Bukkit.getWorld(identifier);
    if (world == null) {
      sendPlayerMessage(player, "multiverse.world_does_not_exist", Map.of("world_name", identifier));
      return;
    }
    sendPlayerMessage(player, "multiverse.deleting_world", Map.of("world_name", identifier));
    if (!world.getEntitiesByClass(Player.class).isEmpty()) {
      sendPlayerMessage(player, "multiverse.world_contains_players", Map.of("world_name", identifier));
      return;
    }
    try {
      Bukkit.unloadWorld(world, false);
      multiverse.getMvWorldRepository().findByIdentifierAsync(identifier)
        .thenAcceptAsync(mvWorld -> {
          if (mvWorld == null) {
            new I18n.Builder("multiverse.failed_to_delete_world_in_database", player)
              .includingPrefix()
              .withPlaceholder("world_name", identifier)
              .build()
              .send();
            return;
          }
          multiverse.getMvWorldRepository().delete(mvWorld.getId());
          new I18n.Builder("multiverse.world_deleted", player)
            .includingPrefix()
            .withPlaceholder("world_name", identifier)
            .build()
            .send();
        }, multiverse.getExecutor());
      FileUtils.deleteDirectory(world.getWorldFolder());
      multiverse.getWorlds().remove(identifier);
      multiverse.getPlatformLogger().logDebug("Deleted world folder: " + world.getWorldFolder().getAbsolutePath());
    } catch (final Exception exception) {
      multiverse.getPlatformLogger().logDebug("Failed to delete world: " + identifier, exception);
      new I18n.Builder("multiverse.failed_to_delete_world", player)
        .includingPrefix()
        .withPlaceholder("world_name", identifier)
        .build()
        .send();
    }
  }

  /**
   * Sends a localized message to the specified player.
   *
   * @param player       the player to receive the message.
   * @param key          the localization key.
   * @param placeholders a map of placeholder names to their values.
   */
  private void sendPlayerMessage(Player player, String key, Map<String, Object> placeholders) {
    new I18n.Builder(key, player)
      .includingPrefix()
      .withPlaceholders(placeholders)
      .build()
      .send();
  }

  /**
   * Teleports the player to the spawn location of the specified world.
   *
   * @param mvWorld the {@link MVWorld} representing the target world.
   * @param player  the player to teleport.
   */
  public void teleport(final @NotNull MVWorld mvWorld, final @NotNull Player player) {
    multiverse.getTeleportFactory().teleport(
      player,
      mvWorld.getSpawnLocation(),
      "multiverse.teleported",
      Map.of("world_name", mvWorld.getIdentifier())
    );
  }
}