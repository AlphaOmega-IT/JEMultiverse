package de.jexcellence.multiverse.command.spawn;

import de.jexcellence.je18n.i18n.I18n;
import de.jexcellence.multiverse.Multiverse;
import de.jexcellence.multiverse.config.PSpawnSection;
import me.blvckbytes.bukkitcommands.PlayerCommand;
import me.blvckbytes.bukkitevaluable.section.PermissionsSection;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PSpawn extends PlayerCommand {
	
	private static final String FILE_PATH = "pspawn.yml";
	
	private final Multiverse multiverse;
	private final PermissionsSection permissionsSection;
	
	public PSpawn(
			final @NotNull PSpawnSection commandSection,
			final @NotNull Multiverse multiverse
	) {
		super(commandSection);
		this.multiverse = multiverse;
		this.permissionsSection = this.multiverse.getCommandSectionFactory().getPermission(FILE_PATH);
	}
	
	@Override
	protected void onPlayerInvocation(
			final Player player,
			final String label,
			final String[] args
	) {
		if (! this.permissionsSection.hasPermission(player, PSpawnPermission.SPAWN)) {
			this.permissionsSection.sendMissingMessage(player, PSpawnPermission.SPAWN);
			return;
		}
		
		this.multiverse
				.getAdapter()
				.spawn(player, "spawn.teleporting_to_spawn")
				.whenCompleteAsync((result, throwable) -> {
					if (result)
						return;
					
					new I18n.Builder("spawn.spawn_not_found", player)
							.includingPrefix()
							.build()
							.send()
					;
				}, this.multiverse.getExecutor())
		;
	}
	
	@Override
	protected List<String> onTabComplete(
			final CommandSender sender,
			final String label,
			final String[] args
	) {
		return List.of();
	}
}
