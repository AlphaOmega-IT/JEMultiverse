package de.jexcellence.multiverse.command.multiverse;

import me.blvckbytes.bukkitevaluable.section.IPermissionNode;
import org.jetbrains.annotations.NotNull;

/**
 * Represents permission nodes for all PMultiverse actions.
 * Each enum constant associates an internal name and a fallback permission node.
 */
public enum PMultiversePermission implements IPermissionNode {

	MULTIVERSE("command", "multiverse.command"),
	CREATE("commandCreate", "multiverse.command.create"),
	DELETE("commandDelete", "multiverse.command.delete"),
	EDIT("commandEdit", "multiverse.command.edit"),
	FORCE_CREATION("commandForceCreate", "multiverse.command.force_create"),
	HELP("commandHelp", "multiverse.command.help"),
	LIST("commandList", "multiverse.command.list"),
	LOAD("commandLoad", "multiverse.command.load"),
	TELEPORT("commandTeleport", "multiverse.command.teleport");

	private final String internalName;
	private final String fallbackNode;

	/**
	 * Constructs a new enum constant for PMultiverse permissions.
	 *
	 * @param internalName The internal name of the permission node.
	 * @param fallbackNode The fallback (full) permission node string.
	 */
	PMultiversePermission(
		final @NotNull String internalName,
		final @NotNull String fallbackNode
	) {
		this.internalName = internalName;
		this.fallbackNode = fallbackNode;
	}

	/**
	 * Retrieves the internal name of this permission node.
	 *
	 * @return A string representing the internal name.
	 */
	@Override
	public String getInternalName() {
		return this.internalName;
	}

	/**
	 * Retrieves the fallback permission node.
	 *
	 * @return A string representing the fallback node.
	 */
	@Override
	public String getFallbackNode() {
		return this.fallbackNode;
	}
}