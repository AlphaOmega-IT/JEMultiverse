package de.jexcellence.multiverse.database.entity;

import de.jexcellence.hibernate.entity.AbstractEntity;
import de.jexcellence.multiverse.database.converter.LocationConverter;
import de.jexcellence.multiverse.type.MVWorldType;
import jakarta.persistence.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

@Entity
@Table(name = "mv_world")
public class MVWorld extends AbstractEntity {

  @Column(name = "world_name", nullable = false, unique = true)
  private String identifier;

  @Enumerated(EnumType.STRING)
  @Column(name = "world_type", nullable = false)
  private MVWorldType type;

  @Enumerated(EnumType.STRING)
  @Column(name = "world_environment", nullable = false)
  private World.Environment environment;

  @Convert(converter = LocationConverter.class)
  @Column(name = "spawn_location", nullable = false, columnDefinition = "LONGTEXT")
  private Location spawnLocation;

  @Column(name = "is_globalized_spawn", nullable = false)
  private boolean isGlobalizedSpawn;

  @Column(name = "is_pvp_enabled", nullable = false)
  private boolean isPvPEnabled;

  @Column(name = "enter_permission", nullable = false)
  private String enterPermission;

  // Default Constructor for JPA
  protected MVWorld() {}

  private MVWorld(final Builder builder) {
    this.identifier = builder.identifier;
    this.type = builder.type;
    this.environment = builder.environment;
    this.spawnLocation = builder.spawnLocation;
    this.isGlobalizedSpawn = builder.isGlobalizedSpawn;
    this.isPvPEnabled = builder.isPvPEnabled;
    this.enterPermission = builder.enterPermission;
  }

  public static class Builder {
    private final String identifier;
    private final MVWorldType type;
    private final World.Environment environment;
    private Location spawnLocation;
    private boolean isGlobalizedSpawn;
    private boolean isPvPEnabled;
    private String enterPermission;

    public Builder() {
      this.identifier = "";
      this.type = null;
      this.environment = null;
    }

    public Builder(final @NotNull World world, final MVWorldType type, final World.Environment environment) {
      this.identifier = world.getName();
      this.type = type;
      this.environment = environment;
      this.spawnLocation = world.getSpawnLocation().toCenterLocation();
      this.isGlobalizedSpawn = false;
      this.isPvPEnabled = false;
      this.enterPermission = "";
    }

    public MVWorld build() { return new MVWorld(this); }

    public String getIdentifier() {
      return this.identifier;
    }

    public MVWorldType getType() {
      return this.type;
    }

    public World.Environment getEnvironment() {
      return this.environment;
    }

    public Location getSpawnLocation() {
      return this.spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
      this.spawnLocation = spawnLocation;
    }

    public boolean isGlobalizedSpawn() {
      return this.isGlobalizedSpawn;
    }

    public void setGlobalizedSpawn(boolean globalizedSpawn) {
      isGlobalizedSpawn = globalizedSpawn;
    }

    public boolean isPvPEnabled() {
      return this.isPvPEnabled;
    }

    public void setPvPEnabled(boolean pvPEnabled) {
      isPvPEnabled = pvPEnabled;
    }

    public String getEnterPermission() {
      return this.enterPermission;
    }

    public void setEnterPermission(String enterPermission) {
      this.enterPermission = enterPermission;
    }
  }

  public String getIdentifier() {
    return this.identifier;
  }

  public void setGlobalizedSpawn(boolean globalizedSpawn) {
    isGlobalizedSpawn = globalizedSpawn;
  }

  public boolean isGlobalizedSpawn() {
    return this.isGlobalizedSpawn;
  }

  public void setSpawnLocation(Location spawnLocation) {
    this.spawnLocation = spawnLocation;
  }

  public Location getSpawnLocation() {
    return this.spawnLocation;
  }

  public World.Environment getEnvironment() {
    return this.environment;
  }

  public MVWorldType getType() {
    return this.type;
  }
}
