package de.jexcellence.multiverse.command.spawn;

import me.blvckbytes.bukkitevaluable.section.IPermissionNode;
import org.jetbrains.annotations.NotNull;

/**
 * Represents permission nodes for the PSpawn command.
 * Each enum constant associates an internal name and a fallback permission node.
 */
public enum PSpawnPermission implements IPermissionNode {

	SPAWN("command", "spawn.command"),
	SET_SPAWN("commandSet", "spawn.command.set")
	;

	private final String internalName;
	private final String fallbackNode;

	/**
	 * Constructs a new enum constant for spawn-related permissions.
	 *
	 * @param internalName The internal name of the permission node.
	 * @param fallbackNode The fallback (full) permission node string.
	 */
	PSpawnPermission(
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