package de.jexcellence.multiverse.command.spawn;

import me.blvckbytes.bukkitevaluable.section.IPermissionNode;
import org.jetbrains.annotations.NotNull;

public enum PSpawnPermission implements IPermissionNode {

	SET_SPAWN("setSpawn", "multiverse.spawn.set"),
	SPAWN("spawn", "multiverse.spawn");

	private final String internalName;
	private final String fallbackNode;

	PSpawnPermission(
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
