package de.jexcellence.multiverse.api;

import de.jexcellence.multiverse.database.entity.MVWorld;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface IMultiverseAdapter {

	CompletableFuture<MVWorld> getGlobalMVWorld();
	
	CompletableFuture<MVWorld> getMVWorld(
			final @NotNull String worldName
	);
	
	CompletableFuture<Boolean> hasMultiverseSpawn(
			final @NotNull String worldName
	);
	
	CompletableFuture<Boolean> spawn(
			final @NotNull Player player,
			final @NotNull String message
	);
}
