package de.jexcellence.multiverse.command.multiverse;

import de.jexcellence.commands.PlayerCommand;
import de.jexcellence.commands.utility.Command;
import de.jexcellence.je18n.i18n.I18n;
import de.jexcellence.multiverse.Multiverse;
import de.jexcellence.multiverse.database.entity.MVWorld;
import de.jexcellence.multiverse.type.MVWorldType;
import de.jexcellence.multiverse.ui.MultiverseEditorView;
import de.jexcellence.multiverse.utility.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The PMultiverse command class handles player interactions for multiverse-related commands.
 * Includes sub-commands for creation, deletion, editing, loading, teleporting, and more.
 */
@Command
public class PMultiverse extends PlayerCommand {

  private final WorldManager worldManager;
  private final Multiverse multiverse;

  /**
   * Constructs a new PMultiverse command.
   *
   * @param commandSection The command section containing settings for this command.
   * @param multiverse     The main plugin class instance.
   */
  public PMultiverse(
    final @NotNull PMultiverseSection commandSection,
    final @NotNull Multiverse multiverse
  ) {
    super(commandSection);
    this.multiverse = multiverse;
    this.worldManager = new WorldManager(this.multiverse);
  }

  /**
   * Handles the main invocation of this command by a player, parsing sub-commands.
   *
   * @param player The player invoking the command.
   * @param label  The command label used.
   * @param args   The command arguments provided by the player.
   */
  @Override
  protected void onPlayerInvocation(
    final @NotNull Player player,
    final @NotNull String label,
    final String[] args
  ) {
    if (
      this.hasNoPermission(player, PMultiversePermission.MULTIVERSE)
    ) return;

    if (args.length == 0) {
      //TODO
      return;
    }

    final PMultiverseAction action = this.enumParameterOrElse(args, 0, PMultiverseAction.class, PMultiverseAction.HELP);
    if (
      action.equals(PMultiverseAction.HELP)
    ) {
      this.help(player);
      return;
    }

    final String identifier = this.stringParameter(args, 1);
    this.handleAction(action, player, args, identifier);
  }

  /**
   * Creates a new world with the specified identifier if it does not already exist.
   *
   * @param player          The player requesting the world creation.
   * @param args            Additional command arguments.
   */
  private void handleCreate(
    final Player player,
    final String worldIdentifier,
    final String[] args
  ) {
    if (
      this.hasNoPermission(player, PMultiversePermission.CREATE)
    ) return;

    final World.Environment environment = this.getOptionalEnum(args, 2, World.Environment.class, World.Environment.NORMAL);
    final MVWorldType worldType = this.getOptionalEnum(args, 3, MVWorldType.class, MVWorldType.DEFAULT);

    this.worldManager.createWorld(worldIdentifier, environment, worldType, player, false);
  }

  /**
   * Deletes an existing world with the specified identifier.
   *
   * @param player          The player requesting the world deletion.
   * @param worldIdentifier The world identifier.
   */
  private void handleDelete(
    final Player player,
    final String worldIdentifier
  ) {
    if (
      this.hasNoPermission(player, PMultiversePermission.DELETE)
    ) return;

    this.worldManager.deleteWorld(worldIdentifier, player);
  }

  /**
   * Opens an edit interface for the specified world.
   *
   * @param player          The player requesting to edit the world.
   * @param worldIdentifier The world identifier.
   */
  private void handleEdit(
    final Player player,
    final String worldIdentifier
  ) {
    if (
      this.hasNoPermission(player, PMultiversePermission.EDIT)
    ) return;

    this.multiverse.getMvWorldRepository().findByIdentifierAsync(worldIdentifier).thenAcceptAsync(
      mvWorld -> {
        this.multiverse.getViewFrame().open(
          MultiverseEditorView.class, player,
          Map.of(
            "plugin", multiverse,
            "identifier", worldIdentifier,
            "mvWorld", mvWorld == null ? new MVWorld.Builder().build() : mvWorld
            )
        );
      }
    , this.multiverse.getExecutor());
  }

  /**
   * Forcibly creates a new world, bypassing any existing checks.
   *
   * @param player          The player requesting forced creation.
   * @param worldIdentifier The world identifier.
   * @param args            Additional command arguments.
   */
  private void handleForceCreation(
    final Player player,
    final String worldIdentifier,
    final String[] args
  ) {
    if (
      this.hasNoPermission(player, PMultiversePermission.FORCE_CREATION)
    ) return;

    final World.Environment environment = this.getOptionalEnum(args, 2, World.Environment.class, World.Environment.NORMAL);
    final MVWorldType worldType = this.getOptionalEnum(args, 3, MVWorldType.class, MVWorldType.DEFAULT);

    this.worldManager.createWorld(worldIdentifier, environment, worldType, player, true);
  }

  /**
   * Loads an existing world, optionally applying additional parameters.
   *
   * @param player          The player requesting the load.
   * @param worldIdentifier The world identifier.
   * @param args            Additional command arguments.
   */
  private void handleLoad(
    final Player player,
    final String worldIdentifier,
    final String[] args
  ) {
    if (
      this.hasNoPermission(player, PMultiversePermission.LOAD)
    ) return;

    this.worldManager.loadWorlds();
  }

  /**
   * Teleports a player to the specified world.
   *
   * @param player          The player to teleport.
   * @param worldIdentifier The target world identifier.
   */
  private void handleTeleport(
    final Player player,
    final String worldIdentifier
  ) {
    if (
      this.hasNoPermission(player, PMultiversePermission.TELEPORT)
    ) return;

    this.multiverse.getMvWorldRepository().findByIdentifierAsync(worldIdentifier).thenAcceptAsync(
      mvWorld -> this.worldManager.teleport(mvWorld, player)
    , this.multiverse.getExecutor());
  }

  /**
   * Displays help information about this command to the player.
   *
   * @param player The player requesting help.
   */
  private void help(final @NotNull Player player) {
    // Implementation of help message
  }

  /**
   * Provides tab-completion suggestions for this command.
   *
   * @param player The player requesting tab-completion.
   * @param label  The command label used.
   * @param args   The current command arguments.
   * @return A list of possible completions.
   */
  @Override
  protected List<String> onPlayerTabCompletion(
    final @NotNull Player player,
    final @NotNull String label,
    final String[] args
  ) {
    final List<String> completions = new ArrayList<>();

    if (args.length == 1) {
      completions.addAll(getActionCompletions(args[0]));
    } else if (args.length == 2) {
      if (isCreateOrForceAction(args))
        completions.add("world_" + UUID.randomUUID().toString().substring(8));
      else
        completions.addAll(getWorldNameCompletions(args[1]));
    } else if (args.length == 3 && isCreateOrForceAction(args)) {
      completions.addAll(getEnvironmentCompletions(args[2]));
    } else if (args.length == 4 && isCreateOrForceAction(args)) {
      completions.addAll(getWorldTypeCompletions(args[3]));
    } else {
      return List.of();
    }

    return completions;
  }

  private void handleAction(
    final PMultiverseAction action,
    final Player player,
    final String[] args,
    final String identifier
  ) {
    switch (action) {
      case CREATE -> handleCreate(player, identifier, args);
      case DELETE -> handleDelete(player, identifier);
      case EDIT -> handleEdit(player, identifier);
      case FORCE_CREATION -> handleForceCreation(player, identifier, args);
      case LOAD -> handleLoad(player, identifier, args);
      case TELEPORT, TP -> handleTeleport(player, identifier);
	    default -> help(player);
    }
  }

  private <T extends Enum<T>> T getOptionalEnum(
    final String[] args,
    final int index,
    final Class<T> enumClazz,
    final T defaultValue
  ) {
    return
      args.length > index ?
        this.enumParameterOrElse(args, index, enumClazz, defaultValue) :
        defaultValue;
  }

  private List<String> getActionCompletions(final String input) {
    return StringUtil.copyPartialMatches(
      input.toLowerCase(),
      Arrays.stream(PMultiverseAction.values()).map(PMultiverseAction::name).map(String::toLowerCase).toList(),
      new ArrayList<>()
    );
  }

  private List<String> getWorldNameCompletions(final String input) {
    return StringUtil.copyPartialMatches(
      input.toLowerCase(),
      Bukkit.getWorlds().stream().map(World::getName).toList(),
      new ArrayList<>()
    );
  }

  private List<String> getEnvironmentCompletions(final String input) {
    return StringUtil.copyPartialMatches(
      input.toLowerCase(),
      Arrays.stream(World.Environment.values()).map(Enum::name).map(String::toLowerCase).toList(),
      new ArrayList<>()
    );
  }

  private List<String> getWorldTypeCompletions(final String input) {
    return StringUtil.copyPartialMatches(
      input.toLowerCase(),
      Arrays.stream(MVWorldType.values()).map(MVWorldType::name).map(String::toLowerCase).toList(),
      new ArrayList<>()
    );
  }

  private boolean isCreateOrForceAction(final String[] args) {
    return args.length > 0 &&
      (args[0].equalsIgnoreCase(PMultiverseAction.CREATE.name()) ||
        args[0].equalsIgnoreCase(PMultiverseAction.FORCE_CREATION.name()));
  }
}