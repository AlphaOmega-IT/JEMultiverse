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

@Command
public class PMultiverse extends PlayerCommand {

  private final WorldManager worldManager;
  private final Multiverse multiverse;

  public PMultiverse(
    final @NotNull PMultiverseSection commandSection,
    final @NotNull Multiverse multiverse
  ) {
    super(commandSection);
    this.multiverse = multiverse;
    this.worldManager = new WorldManager(this.multiverse);
  }

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

    if (action.equals(PMultiverseAction.HELP)) {
      this.help(player);
      return;
    }

    final String worldIdentifier = this.stringParameter(args, 1);

    switch (action) {
      case CREATE -> handleCreate(player, worldIdentifier, args);
      case DELETE -> handleDelete(player, worldIdentifier);
      case EDIT -> handleEdit(player, worldIdentifier);
      case FORCE_CREATION -> handleForceCreation(player, worldIdentifier, args);
      case LOAD -> handleLoad(player, worldIdentifier, args);
      case TELEPORT -> handleTeleport(player, worldIdentifier);
      default -> this.help(player);
    }
  }

  private void handleCreate(
    Player player,
    String worldIdentifier,
    String[] args
  ) {
    if (
      this.hasNoPermission(player, PMultiversePermission.CREATE)
    ) return;

    this.worldManager.createWorld(worldIdentifier, this.enumParameterOrElse(args, 2, World.Environment.class, World.Environment.NORMAL),
      this.enumParameterOrElse(args, 3, MVWorldType.class, MVWorldType.DEFAULT), player, false
    );
  }

  private void handleDelete(
    Player player,
    String worldIdentifier
  ) {
    if (
      this.hasNoPermission(player, PMultiversePermission.DELETE)
    ) return;

    this.worldManager.deleteWorld(worldIdentifier, player);
  }

  private void handleEdit(Player player, String worldIdentifier) {
    if (
      this.hasNoPermission(player, PMultiversePermission.EDIT)
    ) return;

    this.multiverse.getViewFrame().open(
      MultiverseEditorView.class,
      player,
      Map.of(
        "plugin", this.multiverse,
        "identifier", worldIdentifier,
        "mvWorld", this.multiverse.getWorlds().getOrDefault(worldIdentifier, new MVWorld.Builder().build())
      )
    );
  }

  private void handleForceCreation(
    Player player,
    String worldIdentifier,
    String[] args
  ) {
    if (
      this.hasNoPermission(player, PMultiversePermission.FORCE_CREATION)
    ) return;

    this.worldManager.createWorld(worldIdentifier, this.enumParameterOrElse(args, 2, World.Environment.class, World.Environment.NORMAL),
      this.enumParameterOrElse(args, 3, MVWorldType.class, MVWorldType.DEFAULT), player, true
    );
  }

  private void handleLoad(
    Player player,
    String worldIdentifier,
    String[] args
  ) {
    if (
      this.hasNoPermission(player, PMultiversePermission.LOAD)
    ) return;

    this.handleForceCreation(player, worldIdentifier, args);
  }

  private void handleTeleport(
    Player player,
    String worldIdentifier
  ) {
    if (
      this.hasNoPermission(player, PMultiversePermission.TELEPORT)
    ) return;

    this.multiverse
      .getMvWorldRepository()
      .findByIdentifierAsync(worldIdentifier)
      .thenAcceptAsync(mvWorld -> {
        if (mvWorld == null) {
          new I18n.Builder("multiverse.world_doesnt_exist", player)
            .includingPrefix()
            .withPlaceholder("world_name", worldIdentifier)
            .build()
            .send()
          ;
          return;
        }
        this.worldManager.teleport(mvWorld, player);
      }, this.multiverse.getExecutor())
    ;
  }

  private void help(final @NotNull Player player) {
    new I18n.Builder("multiverse.help", player)
      .includingPrefix()
      .build()
      .send()
    ;
  }

  @Override
  protected List<String> onPlayerTabCompletion(
    final @NotNull Player player,
    final @NotNull String label,
    final String[] args
  ) {
    List<String> actionNames = Arrays.stream(PMultiverseAction.values())
        .map(action -> action.name().toLowerCase())
        .toList();

    List<String> completionsArg2;
    if (args.length > 0 && args[0].equalsIgnoreCase(PMultiverseAction.CREATE.name().toLowerCase())) {
      completionsArg2 = List.of(UUID.randomUUID().toString().substring(0, 8).replace("-", "_") + "_world");
    } else {
      completionsArg2 = Bukkit.getWorlds().stream()
          .map(World::getName)
          .toList();
    }

    List<String> environmentNames = Arrays.stream(World.Environment.values())
        .map(env -> env.name().toLowerCase())
        .toList();

    List<String> worldTypeNames = Arrays.stream(MVWorldType.values())
        .map(type -> type.name().toLowerCase())
        .toList();

    switch (args.length) {
      case 1:
        return StringUtil.copyPartialMatches(args[0].toLowerCase(), actionNames, new ArrayList<>());
      case 2:
        return StringUtil.copyPartialMatches(args[1].toLowerCase(), completionsArg2, new ArrayList<>());
      case 3:
        if (this.isCreateOrForceCreation(args[0])) {
          return StringUtil.copyPartialMatches(args[2].toLowerCase(), environmentNames, new ArrayList<>());
        }
        break;
      case 4:
        if (this.isCreateOrForceCreation(args[0])) {
          return StringUtil.copyPartialMatches(args[3].toLowerCase(), worldTypeNames, new ArrayList<>());
        }
        break;
      default:
        break;
    }

    return new ArrayList<>();
  }

  private boolean isCreateOrForceCreation(String action) {
    return action.equalsIgnoreCase(PMultiverseAction.CREATE.name().toLowerCase()) ||
        action.equalsIgnoreCase(PMultiverseAction.FORCE_CREATION.name().toLowerCase());
  }
}