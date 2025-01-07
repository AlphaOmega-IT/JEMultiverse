package de.jexcellence.multiverse.command.multiverse;

import me.blvckbytes.bukkitevaluable.section.IPermissionNode;
import org.jetbrains.annotations.NotNull;

public enum PMultiversePermission implements IPermissionNode {

	MULTIVERSE("command", "multiverse.command"),
	CREATE("commandCreate", "multiverse.command.create"),
	DELETE("commandDelete", "multiverse.command.delete"),
	EDIT("commandEdit", "multiverse.command.edit"),
	FORCE_CREATION("CommandForceCreate", "multiverse.command.force_create"),
	HELP("commandHelp", "multiverse.command.help"),
	LIST("commandList", "multiverse.command.list"),
	LOAD("commandLoad", "multiverse.command.load"),
	TELEPORT("commandTeleport", "multiverse.command.teleport")
	;

	private final String internalName;
	private final String fallbackNode;

	PMultiversePermission(
			final @NotNull String internalName,
			final @NotNull String fallbackNode
	) {
		this.internalName = internalName;
		this.fallbackNode = fallbackNode;
	}
	
	@Override
	public String getInternalName() {
		return this.internalName;
	}
	
	@Override
	public String getFallbackNode() {
		return this.fallbackNode;
	}
}
