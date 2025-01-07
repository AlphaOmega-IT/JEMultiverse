package de.jexcellence.multiverse.listener;

import de.jexcellence.multiverse.Multiverse;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.concurrent.CompletableFuture;

public class OnSpawn implements Listener {

  private final Multiverse multiverse;

  public OnSpawn(
      final @NotNull Multiverse multiverse
  ) {
    this.multiverse = multiverse;
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerSpawn(
      final PlayerSpawnLocationEvent event
  ) {
    this.handleSpawn(event.getPlayer()).thenAcceptAsync(event::setSpawnLocation, this.multiverse.getExecutor());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerRespawn(
      final PlayerRespawnEvent event
  ) {
    this.handleSpawn(event.getPlayer()).thenAcceptAsync(event::setRespawnLocation, this.multiverse.getExecutor());
  }

  private CompletableFuture<Location> handleSpawn(
      final @NotNull Player player
  ) {
    return this.multiverse.getMvWorldRepository().findByGlobalSpawnAsync().thenComposeAsync(
        mvWorld -> {
          if (mvWorld != null)
            return CompletableFuture.completedFuture(mvWorld.getSpawnLocation());

          return this.multiverse.getMvWorldRepository().findByIdentifierAsync(player.getWorld().getName()).thenApplyAsync(
              alternateWorld -> {
                if (alternateWorld == null)
                  return player.getWorld().getSpawnLocation();

                return alternateWorld.getSpawnLocation();
              }, this.multiverse.getExecutor()
          );
        }, this.multiverse.getExecutor()
    );
  }
}
