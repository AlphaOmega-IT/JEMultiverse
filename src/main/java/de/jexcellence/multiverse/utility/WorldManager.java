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

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class WorldManager {

  private final Multiverse multiverse;

  public WorldManager(
      final @NotNull Multiverse multiverse
  ) {
    this.multiverse = multiverse;
  }

  /**
   * Asynchronously creates a new world based on the specified parameters.
   *
   * @param identifier    The name of the world to be created.
   * @param type          The type of the world to be created.
   * @param player        The player initiating the world creation.
   * @param forceCreation Whether to force create the world if it already exists.
   */
  public void createWorld(
      final @NotNull String identifier,
      final @NotNull World.Environment environment,
      final @NotNull MVWorldType type,
      final @NotNull Player player,
      final boolean forceCreation
  ) {
    World foundWorld = Bukkit.getWorld(identifier);

    if (
        foundWorld != null && !forceCreation
    ) {
      new I18n.Builder("multiverse.world_already_exists", player)
          .includingPrefix()
          .withPlaceholder("world_name", identifier)
          .build()
          .send();
      return;
    }

    new I18n.Builder("multiverse.preparing_world", player).includingPrefix().withPlaceholder("world_name", identifier).build().send();

    CompletableFuture.supplyAsync(() -> switch (type) {
      case VOID -> new WorldCreator(
          identifier + (environment.name().equalsIgnoreCase(World.Environment.NETHER.name()) ? "_nether" : "-") + type.name()
      ).environment(environment).generator(new VoidChunkGenerator()).biomeProvider(new VoidBiomeProvider()).keepSpawnLoaded(TriState.TRUE);
      case PLOT -> new WorldCreator(
          identifier + "-" + type)
          .environment(environment)
          .generator(new PlotChunkGenerator(30, 6, 30, Material.STONE, Material.COBBLESTONE_SLAB, Material.IRON_BLOCK))
          .biomeProvider(new PlotBiomeProvider())
          .keepSpawnLoaded(TriState.TRUE);
      case DEFAULT -> new WorldCreator(identifier).environment(environment).keepSpawnLoaded(TriState.TRUE);
    }).thenComposeAsync(worldCreator -> {
      CompletableFuture<World> generatedWorld = new CompletableFuture<>();

      Bukkit.getScheduler().runTask(this.multiverse, () -> {
        try {
          World createdWorld = worldCreator.createWorld();

          if (createdWorld == null)
            throw new IllegalStateException("World can not be null");

          createdWorld.getSpawnLocation().clone().subtract(0, 1, 0).toCenterLocation().getBlock().setType(Material.BEDROCK);
          generatedWorld.complete(createdWorld);
        } catch (
            final Exception exception
        ) {
          generatedWorld.completeExceptionally(exception);
        }
      });

      return generatedWorld;
    }, this.multiverse.getExecutor()).whenCompleteAsync((world, throwable) -> {
      if (throwable != null || world == null) {
        new I18n.Builder("multiverse.world_creation_failed", player)
            .includingPrefix().withPlaceholders(Map.of(
                "world_name", identifier, "exception", throwable == null ? "{}" : throwable.getMessage()
            )).build().send();
        return;
      }

      try {
        MVWorld mvWorld = new MVWorld.Builder(
            world, type, environment
        ).build();

        mvWorld = this.multiverse.getMvWorldRepository().create(mvWorld);
        new I18n.Builder("multiverse.world_created", player).includingPrefix().withPlaceholders(Map.of("world_name", identifier, "world_id", mvWorld.getId())).build().send();

        this.multiverse.getTeleportFactory().teleport(player, mvWorld.getSpawnLocation(), "multiverse.teleported", Map.of("world_name", identifier));
      } catch (
          final Exception exception
      ) {
        new I18n.Builder("multiverse.world_creation_failed", player)
            .includingPrefix().withPlaceholders(Map.of(
                "world_name", identifier, "exception", exception.getMessage()
            )).build().send();

        //delete the generated world folder again...
        Bukkit.getScheduler().runTask(this.multiverse, () -> this.deleteWorld(identifier, player));
      }
    }, this.multiverse.getExecutor());
  }

  /**
   * This method will preload all existing worlds of the database and the folder, to ensure it contains the correct world generators.
   * Loads the first 128 worlds from a folder...
   */
  public void loadWorlds() {
    this.multiverse.getPlatformLogger().logInfo("Loading existing worlds...");

    final String regex = "^[a-zA-Z0-9/._-]+$";
    this.multiverse.getMvWorldRepository().findAllAsync(0, 128).thenAcceptAsync(
        foundWorlds -> {
          if (foundWorlds == null || foundWorlds.isEmpty()) {
            this.multiverse.getPlatformLogger().logInfo("No existing worlds within the database found.");
            return;
          }

          foundWorlds.forEach(mvWorld -> {
            if (Pattern.compile(regex).matcher(mvWorld.getIdentifier()).matches()) {
              WorldCreator worldCreator = new WorldCreator(mvWorld.getIdentifier()).environment(mvWorld.getEnvironment()).keepSpawnLoaded(TriState.TRUE);

              if (mvWorld.getType().equals(MVWorldType.VOID))
                worldCreator.generator(new VoidChunkGenerator()).biomeProvider(new VoidBiomeProvider());

              //TODO PLOT

              final World world = worldCreator.createWorld();

              assert world != null;

              this.multiverse.getPlatformLogger().logInfo("Loaded World: " + world.getName() + " with type: " + mvWorld.getType().name());
              return;
            }

            this.multiverse.getPlatformLogger().logDebug("Could not load world: " + mvWorld.getIdentifier() + " as it does not match the regex pattern.");
          });
        }, this.multiverse.getExecutor()
    );
  }

  /**
   * Asynchronously deletes the specified world if it exists and is empty of players. Sends appropriate messages to the player based on the result of the
   * deletion attempt.
   *
   * @param identifier the name of the world to be deleted
   * @param player     the player initiating the world deletion
   */
  public void deleteWorld(
      final @NotNull String identifier,
      final @NotNull Player player
  ) {
    final World world = Bukkit.getWorld(identifier);

    if (world == null) {
      new I18n.Builder("multiverse.world_doesnt_exist", player).includingPrefix().withPlaceholder("world_name", identifier).build().send();
      return;
    }

    new I18n.Builder("multiverse.deleting_world", player).includingPrefix().withPlaceholder("world_name", identifier).build().send();
    if (
        !world.getEntitiesByClass(Player.class).isEmpty()
    ) {
      new I18n.Builder("multiverse.world_contains_players", player).includingPrefix().withPlaceholder("world_name", identifier).build().send();
      return;
    }

    try {
      Bukkit.unloadWorld(world, false);

      this.multiverse.getMvWorldRepository().findByIdentifierAsync(identifier).thenAcceptAsync(
          mvWorld -> {
            if (mvWorld == null) {
              new I18n.Builder("multiverse.failed_to_delete_world_in_database", player).includingPrefix().withPlaceholder("world_name", identifier).build().send();
              return;
            }

            this.multiverse.getMvWorldRepository().delete(mvWorld.getId());
            new I18n.Builder("multiverse.world_deleted", player).includingPrefix().withPlaceholder("world_name", identifier).build().send();
          }, this.multiverse.getExecutor()
      );

      FileUtils.deleteDirectory(world.getWorldFolder());
      this.multiverse.getPlatformLogger().logDebug("Deleted world folder: " + world.getWorldFolder().getAbsolutePath());
    } catch (
        final Exception exception
    ) {
      this.multiverse.getPlatformLogger().logDebug("Failed to delete world: " + identifier, exception);
      new I18n.Builder("multiverse.failed_to_delete_world", player).includingPrefix().withPlaceholder("world_name", identifier).build().send();
    }
  }

  /**
   * Teleports the specified player to the specified world.
   *
   * @param mvWorld the world to teleport the player to
   * @param player  the player to teleport
   */
  public void teleport(
      final @NotNull MVWorld mvWorld,
      final @NotNull Player player
  ) {
    this.multiverse.getTeleportFactory().teleport(
        player, mvWorld.getSpawnLocation(), "multiverse.teleported", Map.of("world_name", mvWorld.getIdentifier())
    );
  }
}