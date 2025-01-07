package de.jexcellence.multiverse.command.multiverse;

import de.jexcellence.je18n.i18n.I18n;
import de.jexcellence.multiverse.Multiverse;
import de.jexcellence.multiverse.config.PMultiverseSection;
import de.jexcellence.multiverse.type.MVWorldType;
import de.jexcellence.multiverse.ui.MultiverseEditorUI;
import de.jexcellence.multiverse.utility.WorldManager;
import me.blvckbytes.bukkitcommands.PlayerCommand;
import me.blvckbytes.bukkitevaluable.section.PermissionsSection;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class PMultiverse extends PlayerCommand {

  private static final String FILE_PATH = "pmultiverse.yml";

  private final WorldManager worldManager;
  private final Multiverse multiverse;
  private final PermissionsSection permissionsSection;

  public PMultiverse(
      final @NotNull PMultiverseSection commandSection,
      final @NotNull Multiverse multiverse
  ) {
    super(commandSection);
    this.multiverse = multiverse;
    this.permissionsSection = this.multiverse.getCommandSectionFactory().getPermission(FILE_PATH);
    this.worldManager = new WorldManager(this.multiverse);
  }

  @Override
  protected void onPlayerInvocation(
      final Player player,
      final String label,
      final String[] args
  ) {
    if (!this.permissionsSection.hasPermission(player, PMultiversePermission.MULTIVERSE)) {
      this.permissionsSection.sendMissingMessage(player, PMultiversePermission.MULTIVERSE);
      return;
    }

    if (args.length == 0) {
      return;
    }

    final PMultiverseAction action = this.enumParameterOrElse(args, 0, PMultiverseAction.class, PMultiverseAction.HELP);

    if (action.equals(PMultiverseAction.HELP)) {
      this.help(player);
      return;
    }

    final String worldIdentifier = this.stringParameter(args, 1);

    switch (action) {
      case CREATE -> this.handleCreate(player, worldIdentifier, args);
      case DELETE -> this.handleDelete(player, worldIdentifier);
      case EDIT -> this.handleEdit(player, worldIdentifier);
      case FORCE_CREATION -> this.handleForceCreation(player, worldIdentifier, args);
      case LOAD -> this.handleLoad(player, worldIdentifier, args);
      case TELEPORT -> this.handleTeleport(player, worldIdentifier);
    }
  }

  private void handleCreate(Player player, String worldName, String[] args) {
    if (this.hasNoPermission(player, PMultiversePermission.CREATE)) return;

    World.Environment environment = this.enumParameterOrElse(args, 2, World.Environment.class, World.Environment.NORMAL);
    MVWorldType worldType = this.enumParameterOrElse(args, 3, MVWorldType.class, MVWorldType.DEFAULT);

    this.worldManager.createWorld(worldName, environment, worldType, player, false);
  }

  private void handleDelete(Player player, String worldName) {
    if (this.hasNoPermission(player, PMultiversePermission.DELETE)) return;

    this.worldManager.deleteWorld(worldName, player);
  }

  private void handleEdit(Player player, String worldName) {
    if (this.hasNoPermission(player, PMultiversePermission.EDIT)) return;

    new MultiverseEditorUI(this.multiverse, worldName)
        .get(player)
        .ifPresent(ui -> ui.display(player));
  }

  private void handleForceCreation(Player player, String worldName, String[] args) {
    if (this.hasNoPermission(player, PMultiversePermission.FORCE_CREATION)) return;

    World.Environment environment = this.enumParameterOrElse(args, 2, World.Environment.class, World.Environment.NORMAL);
    MVWorldType worldType = this.enumParameterOrElse(args, 3, MVWorldType.class, MVWorldType.DEFAULT);

    this.worldManager.createWorld(worldName, environment, worldType, player, true);
  }

  private void handleLoad(Player player, String worldName, String[] args) {
    if (this.hasNoPermission(player, PMultiversePermission.LOAD)) return;

    this.handleForceCreation(player, worldName, args);
  }

  private void handleTeleport(Player player, String worldName) {
    if (this.hasNoPermission(player, PMultiversePermission.TELEPORT)) return;

    this.multiverse.getMvWorldRepository()
        .findByIdentifierAsync(worldName)
        .thenAcceptAsync(mvWorld -> {
          Optional.ofNullable(mvWorld).ifPresentOrElse(
              world -> this.worldManager.teleport(world, player),
              () -> new I18n.Builder("multiverse.world_doesnt_exist", player)
                  .includingPrefix()
                  .withPlaceholder("world_name", worldName)
                  .build()
                  .send()
          );
        }, this.multiverse.getExecutor());
  }

  private void help(@NotNull Player player) {
    new I18n.Builder("multiverse.help", player)
        .build()
        .send();
  }

  private boolean hasNoPermission(Player player, PMultiversePermission permission) {
    if (!this.permissionsSection.hasPermission(player, permission)) {
      this.permissionsSection.sendMissingMessage(player, permission);
      return true;
    }
    return false;
  }

  @Override
  protected List<String> onTabComplete(
      final CommandSender sender,
      final String label,
      final String[] args
  ) {
    List<String> actionNames = Arrays.stream(PMultiverseAction.values())
        .map(action -> action.name().toLowerCase())
        .collect(Collectors.toList());

    List<String> completionsArg2;
    if (args.length > 0 && args[0].equalsIgnoreCase(PMultiverseAction.CREATE.name().toLowerCase())) {
      completionsArg2 = List.of(UUID.randomUUID().toString().substring(0, 8).replace("-", "_") + "_world");
    } else {
      completionsArg2 = Bukkit.getWorlds().stream()
          .map(World::getName)
          .collect(Collectors.toList());
    }

    List<String> environmentNames = Arrays.stream(World.Environment.values())
        .map(env -> env.name().toLowerCase())
        .collect(Collectors.toList());

    List<String> worldTypeNames = Arrays.stream(MVWorldType.values())
        .map(type -> type.name().toLowerCase())
        .collect(Collectors.toList());

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