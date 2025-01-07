package de.jexcellence.multiverse.api;

import de.jexcellence.multiverse.Multiverse;
import de.jexcellence.multiverse.database.entity.MVWorld;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class MultiverseAdapter implements IMultiverseAdapter {
	
	private final Multiverse multiverse;
	
	public MultiverseAdapter(
			final @NotNull Multiverse multiverse
	) {
		this.multiverse = multiverse;
	}
	
	@Override
	public CompletableFuture<MVWorld> getGlobalMVWorld() {
		return this.multiverse.getMvWorldRepository().findByGlobalSpawnAsync();
	}
	
	@Override
	public CompletableFuture<MVWorld> getMVWorld(final @NotNull String worldName) {
		return this.multiverse.getMvWorldRepository().findByIdentifierAsync(worldName);
	}
	
	@Override
	public CompletableFuture<Boolean> hasMultiverseSpawn(final @NotNull String worldName) {
		return this.multiverse.getMvWorldRepository().findByIdentifierAsync(worldName).thenApplyAsync(Objects::nonNull);
	}
	
	@Override
	public CompletableFuture<Boolean> spawn(
			final @NotNull Player player,
			final @NotNull String message
	) {
		return this.getGlobalMVWorld().thenApplyAsync(mvWorld -> {
			if (mvWorld == null) {
				this.getMVWorld(player.getWorld().getName()).thenApplyAsync(alternateWorld -> {
					if (alternateWorld == null)
						return false;
					
					this.multiverse.getTeleportFactory().teleport(player, alternateWorld.getSpawnLocation(), message, Map.of("world_name", alternateWorld.getIdentifier()));
					return true;
				}, this.multiverse.getExecutor());
			}
			
			if (mvWorld != null) {
				this.multiverse.getTeleportFactory().teleport(player, mvWorld.getSpawnLocation(), message, Map.of("world_name", mvWorld.getIdentifier()));
				return true;
			}
			
			return false;
		}, this.multiverse.getExecutor());
	}
}
