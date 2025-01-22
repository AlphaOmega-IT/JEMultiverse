package de.jexcellence.multiverse.command.spawn;
import de.jexcellence.commands.PlayerCommand; import de.jexcellence.commands.utility.Command; import de.jexcellence.je18n.i18n.I18n; import de.jexcellence.multiverse.Multiverse; import org.bukkit.entity.Player; import org.jetbrains.annotations.NotNull;

import java.util.List;

/**

 The PSpawn command class handles teleporting players to a spawn location
 within the Multiverse plugin. */
@Command
public class PSpawn extends PlayerCommand {
	private final Multiverse multiverse;

	/**
	 * Constructs a new PSpawn command instance.
	 *
	 * @param commandSection The command section configuration.
	 * @param multiverse     A reference to the primary plugin class.
	 */
	public PSpawn(final @NotNull PSpawnSection commandSection, final @NotNull Multiverse multiverse) {
		super(commandSection);
		this.multiverse = multiverse;
	}

	/**
	 * Handles the player's invocation of this command, performing spawn teleportation.
	 *
	 * @param player The player who invoked the command.
	 * @param label  The command label used.
	 * @param args   The command arguments supplied by the player.
	 */
	@Override
	protected void onPlayerInvocation(final @NotNull Player player, final @NotNull String label, final String[] args) {
		if (this.hasNoPermission(player, PSpawnPermission.SPAWN)) {
			return;
		}
		this.multiverse.getAdapter().spawn(player, "spawn.teleporting_to_spawn").whenCompleteAsync((result, throwable) -> {
			if (!result) {
				new I18n.Builder("spawn.spawn_not_found", player).includingPrefix().build().send();
			}
		}, this.multiverse.getExecutor());
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
	protected List<String> onPlayerTabCompletion(final @NotNull Player player, final @NotNull String label, final String[] args) {
		return List.of();
	}
}