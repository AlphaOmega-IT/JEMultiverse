package de.jexcellence.multiverse.api;

import de.jexcellence.multiverse.database.entity.MVWorld;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

/**
 * Provides a contract for handling multiverse-related operations, such as
 * retrieving or spawning worlds.
 */
public interface IMultiverseAdapter {

	/**
	 * Retrieves a {@link MVWorld} representing the global world, if any.
	 *
	 * @return A {@link CompletableFuture} that completes with the global {@link MVWorld}.
	 */
	CompletableFuture<MVWorld> getGlobalMVWorld();

	/**
	 * Retrieves a {@link MVWorld} by world name.
	 *
	 * @param worldName The name of the target world.
	 * @return A {@link CompletableFuture} that completes with the requested {@link MVWorld}.
	 */
	CompletableFuture<MVWorld> getMVWorld(
		final @NotNull String worldName
	);

	/**
	 * Checks whether a specific world has a multiverse spawn configured.
	 *
	 * @param worldName The name of the target world.
	 * @return A {@link CompletableFuture} that completes with a boolean indicating if the spawn is set.
	 */
	CompletableFuture<Boolean> hasMultiverseSpawn(
		final @NotNull String worldName
	);

	/**
	 * Teleports the given player to a configured spawn, optionally sending a message.
	 *
	 * @param player  The player to be teleported.
	 * @param message A message key or text to send on successful teleport.
	 * @return A {@link CompletableFuture} that completes with true if successful, otherwise false.
	 */
	CompletableFuture<Boolean> spawn(
		final @NotNull Player player,
		final @NotNull String message
	);
}