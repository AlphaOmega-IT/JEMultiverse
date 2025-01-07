package de.jexcellence.multiverse;

import de.jexcellence.jeplatform.JEPlatform;
import de.jexcellence.jeplatform.inventory.InventoryFactory;
import de.jexcellence.jeplatform.logger.JELogger;
import de.jexcellence.jeplatform.utility.teleportation.TeleportFactory;
import de.jexcellence.multiverse.api.MultiverseAdapter;
import de.jexcellence.multiverse.command.multiverse.PMultiverse;
import de.jexcellence.multiverse.command.spawn.PSpawn;
import de.jexcellence.multiverse.config.PMultiverseSection;
import de.jexcellence.multiverse.config.PSpawnSection;
import de.jexcellence.multiverse.database.repository.MVWorldRepository;
import de.jexcellence.multiverse.listener.OnSpawn;
import de.jexcellence.multiverse.utility.WorldManager;
import jakarta.persistence.Converter;
import me.blvckbytes.bukkitcommands.CommandSectionFactory;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Converter(autoApply = true)
public class Multiverse extends JavaPlugin {

  private final ExecutorService executor = Executors.newFixedThreadPool(5);

  private MultiverseAdapter adapter;
  private CommandSectionFactory commandSectionFactory;

  private MVWorldRepository mvWorldRepository;
  private InventoryFactory inventoryFactory;
  private TeleportFactory teleportFactory;
  private JEPlatform platform;

  @Override
  public void onLoad() {
    this.getPlatformLogger().logInfo("Multiverse is starting...");
  }

  @Override
  public void onEnable() {
    this.adapter = new MultiverseAdapter(this);
    Bukkit.getServer().getServicesManager().register(MultiverseAdapter.class, this.adapter, this, ServicePriority.Normal);

    this.platform = new JEPlatform(this, true);

    this.commandSectionFactory = new CommandSectionFactory(this);
    this.inventoryFactory = new InventoryFactory(this.platform);
    this.teleportFactory = new TeleportFactory(this.platform);
    this.mvWorldRepository = new MVWorldRepository(this.executor, this.platform.getEntityManagerFactory());

    this.commandSectionFactory.createCommand(PSpawn.class, PSpawnSection.class, "pspawn.yml");
    this.commandSectionFactory.createCommand(PMultiverse.class, PMultiverseSection.class, "pmultiverse.yml");

    Bukkit.getPluginManager().registerEvents(new OnSpawn(this), this);

    new WorldManager(this).loadWorlds();
    this.getPlatformLogger().logInfo("Multiverse is enabled!");
  }

  @Override
  public void onDisable() {
    this.getPlatformLogger().logInfo("Multiverse is disabling...");
  }

  public JELogger getPlatformLogger() {
    return this.platform.getLogger();
  }

  public MVWorldRepository getMvWorldRepository() {
    return this.mvWorldRepository;
  }

  public InventoryFactory getInventoryFactory() {
    return this.inventoryFactory;
  }

  public TeleportFactory getTeleportFactory() {
    return this.teleportFactory;
  }

  public JEPlatform getPlatform() {
    return this.platform;
  }

  public ExecutorService getExecutor() {
    return this.executor;
  }

  public MultiverseAdapter getAdapter() {
    return this.adapter;
  }

  public CommandSectionFactory getCommandSectionFactory() {
    return this.commandSectionFactory;
  }
}