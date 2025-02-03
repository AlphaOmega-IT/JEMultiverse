package de.jexcellence.multiverse;

import de.jexcellence.commands.CommandFactory;
import de.jexcellence.jeplatform.JEPlatform;
import de.jexcellence.jeplatform.logger.JELogger;
import de.jexcellence.jeplatform.utility.teleportation.TeleportFactory;
import de.jexcellence.multiverse.api.MultiverseAdapter;
import de.jexcellence.multiverse.database.entity.MVWorld;
import de.jexcellence.multiverse.database.repository.MVWorldRepository;
import de.jexcellence.multiverse.ui.MultiverseEditorView;
import de.jexcellence.multiverse.utility.WorldManager;
import me.devnatan.inventoryframework.ViewFrame;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The main class for the Multiverse plugin, responsible for registering
 * commands, loading worlds, and managing the plugin lifecycle.
 */
public class Multiverse extends JavaPlugin {

  private final ExecutorService executor = Executors.newFixedThreadPool(5);
  private final Map<String, MVWorld> worlds = new HashMap<>();

  private MultiverseAdapter adapter;
  private CommandFactory commandFactory;

  private MVWorldRepository mvWorldRepository;
  private TeleportFactory teleportFactory;
  private JEPlatform platform;

  private ViewFrame viewFrame;

  /**
   * Called automatically when the plugin is loaded, before {@link #onEnable()}.
   * Initializes the internal {@link JEPlatform}.
   */
  @Override
  public void onLoad() {
    this.platform = new JEPlatform(this, true);
    this.getPlatformLogger().logInfo("Multiverse is starting...");
  }

  /**
   * Called automatically when the plugin is enabled.
   * Sets up repository, factories, commands, and loads existing worlds.
   */
  @Override
  public void onEnable() {
    this.adapter = new MultiverseAdapter(this);
    Bukkit.getServer().getServicesManager()
      .register(MultiverseAdapter.class, this.adapter, this, ServicePriority.Normal);

    this.commandFactory = new CommandFactory(this);
    this.teleportFactory = new TeleportFactory(this.platform);
    this.mvWorldRepository = new MVWorldRepository(this.executor, this.platform.getEntityManagerFactory());

    // Register commands and listeners
    this.commandFactory.registerAllCommandsAndListeners();

    // Create and register an inventory ViewFrame
    this.viewFrame = ViewFrame.create(this).defaultConfig(config -> {
      config.cancelOnClick();
      config.cancelOnDrag();
      config.cancelOnDrop();
      config.cancelOnPickup();
    }).with(new MultiverseEditorView()).register();

    // Load worlds from the repository
    new WorldManager(this).loadWorlds();

    this.getPlatformLogger().logInfo("Multiverse is enabled!");
  }

  /**
   * Called automatically when the plugin is disabled.
   * Shuts down the plugin gracefully.
   */
  @Override
  public void onDisable() {
    this.getPlatformLogger().logInfo("Multiverse is disabling...");
  }

  /**
   * Retrieves the platform's logger for logging messages.
   *
   * @return The {@link JELogger} instance.
   */
  public JELogger getPlatformLogger() {
    return this.platform.getLogger();
  }

  /**
   * Retrieves the {@link MVWorldRepository} used to manage worlds in storage.
   *
   * @return The {@link MVWorldRepository} instance.
   */
  public MVWorldRepository getMvWorldRepository() {
    return this.mvWorldRepository;
  }

  /**
   * Retrieves the {@link TeleportFactory} used for managing teleportation-related tasks.
   *
   * @return The {@link TeleportFactory} instance.
   */
  public TeleportFactory getTeleportFactory() {
    return this.teleportFactory;
  }

  /**
   * Retrieves the underlying {@link JEPlatform} instance for various platform-specific operations.
   *
   * @return The {@link JEPlatform} instance.
   */
  public JEPlatform getPlatform() {
    return this.platform;
  }

  /**
   * Retrieves the {@link ExecutorService} used to perform asynchronous operations.
   *
   * @return The {@link ExecutorService} instance.
   */
  public ExecutorService getExecutor() {
    return this.executor;
  }

  /**
   * Retrieves the {@link MultiverseAdapter} capable of handling various multiverse operations.
   *
   * @return The {@link MultiverseAdapter} instance.
   */
  public MultiverseAdapter getAdapter() {
    return this.adapter;
  }

  /**
   * Retrieves the {@link ViewFrame} used to manage inventory views.
   *
   * @return The {@link ViewFrame} instance.
   */
  public ViewFrame getViewFrame() {
    return this.viewFrame;
  }

  /**
   * Retrieves the map of loaded worlds keyed by their identifiers.
   *
   * @return A map of world identifier strings to {@link MVWorld} instances.
   */
  public Map<String, MVWorld> getWorlds() {
    return this.worlds;
  }

  /**
   * Retrieves the {@link CommandFactory} that manages the plugin's commands.
   *
   * @return The {@link CommandFactory} instance.
   */
  public CommandFactory getCommandFactory() {
    return this.commandFactory;
  }
}