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

/**
 * Listener class for handling player spawn and respawn events.
 * It determines the spawn location for players based on the multiverse configuration.
 */
public class OnSpawn implements Listener {

  private final Multiverse multiverse;

  /**
   * Constructs an OnSpawn listener with the specified Multiverse instance.
   *
   * @param multiverse the Multiverse instance used to access world configurations
   */
  public OnSpawn(final @NotNull Multiverse multiverse) {
    this.multiverse = multiverse;
  }

  /**
   * Handles the PlayerSpawnLocationEvent to set the spawn location for a player.
   *
   * @param event the PlayerSpawnLocationEvent triggered when a player spawns
   */
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerSpawn(final PlayerSpawnLocationEvent event) {
    this.handleSpawn(event.getPlayer()).thenAcceptAsync(event::setSpawnLocation).join();
  }

  /**
   * Handles the PlayerRespawnEvent to set the respawn location for a player.
   *
   * @param event the PlayerRespawnEvent triggered when a player respawns
   */
  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlayerRespawn(final PlayerRespawnEvent event) {
    this.handleSpawn(event.getPlayer()).thenAcceptAsync(event::setRespawnLocation).join();
  }

  /**
   * Determines the spawn location for a player asynchronously.
   * It first checks for a global spawn location, and if not found, uses the player's current world spawn location.
   *
   * @param player the player whose spawn location is being determined
   * @return a CompletableFuture containing the determined spawn location
   */
  private CompletableFuture<Location> handleSpawn(final @NotNull Player player) {
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