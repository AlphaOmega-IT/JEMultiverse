package de.jexcellence.multiverse.generator.voidgenerator;

import org.bukkit.HeightMap;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * A {@link ChunkGenerator} that generates void worlds, containing no blocks
 * or surface. Used alongside {@link VoidBiomeProvider}.
 */
public class VoidChunkGenerator extends ChunkGenerator {

  /**
   * Generates noise for the chunk, but in a void world, no terrain is created.
   *
   * @param worldInfo Information about the current world.
   * @param random    The random generator.
   * @param chunkX    The X-coordinate for the chunk.
   * @param chunkZ    The Z-coordinate for the chunk.
   * @param chunkData The data structure to place blocks into.
   */
  @Override
  public void generateNoise(
    @NotNull final WorldInfo worldInfo,
    @NotNull final Random random,
    final int chunkX,
    final int chunkZ,
    @NotNull final ChunkData chunkData
  ) {
    // No terrain generation for void
  }

  /**
   * Generates the surface layer of this chunk. In a void world, there's no surface.
   */
  @Override
  public void generateSurface(
    @NotNull final WorldInfo worldInfo,
    @NotNull final Random random,
    final int chunkX,
    final int chunkZ,
    @NotNull final ChunkData chunkData
  ) {
    // No surface to generate
  }

  /**
   * Generates the bedrock layer for this chunk. In a void world, there's no bedrock.
   */
  @Override
  public void generateBedrock(
    @NotNull final WorldInfo worldInfo,
    @NotNull final Random random,
    final int chunkX,
    final int chunkZ,
    @NotNull final ChunkData chunkData
  ) {
    // No bedrock generation
  }

  /**
   * Generates caves for this chunk. Not applicable for a void world, so does nothing.
   */
  @Override
  public void generateCaves(
    @NotNull final WorldInfo worldInfo,
    @NotNull final Random random,
    final int chunkX,
    final int chunkZ,
    @NotNull final ChunkData chunkData
  ) {
    // No caves in a void world
  }

  /**
   * Provides a default {@link BiomeProvider} for the void world.
   *
   * @param worldInfo The world information.
   * @return A {@link VoidBiomeProvider} instance that always returns THE_VOID biome.
   */
  @Override
  public @Nullable BiomeProvider getDefaultBiomeProvider(
    @NotNull final WorldInfo worldInfo
  ) {
    return new VoidBiomeProvider();
  }

  /**
   * Retrieves the base height used by features like getHighestBlockYAt, etc.
   *
   * @param worldInfo  The world information.
   * @param random     The random generator.
   * @param x          X-coordinate within the chunk.
   * @param z          Z-coordinate within the chunk.
   * @param heightMap  The height map in use.
   * @return The integer value representing the base height.
   */
  @Override
  public int getBaseHeight(
    @NotNull final WorldInfo worldInfo,
    @NotNull final Random random,
    final int x,
    final int z,
    @NotNull final HeightMap heightMap
  ) {
    return 96;
  }

  /**
   * Retrieves the list of {@link BlockPopulator} for the world, if any.
   *
   * @param world The current {@link World} object.
   * @return A list of block populators.
   */
  @Override
  public @NotNull List<BlockPopulator> getDefaultPopulators(
    final @NotNull World world
  ) {
    return Collections.singletonList(new VoidBlockPopulator());
  }

  /**
   * Returns the fixed spawn location of the void world, typically at (0, 96, 0).
   *
   * @param world  The current world.
   * @param random The random generator.
   * @return The spawn {@link Location} object.
   */
  @Override
  public @Nullable Location getFixedSpawnLocation(
    @NotNull final World world,
    @NotNull final Random random
  ) {
    return new Location(world, 0.0, 96.0, 0.0);
  }

  /**
   * Determines whether to generate noise in this chunk. Disabled for a void world.
   *
   * @return False for all coordinates.
   */
  @Override
  public boolean shouldGenerateNoise(
    @NotNull final WorldInfo worldInfo,
    @NotNull final Random random,
    final int chunkX,
    final int chunkZ
  ) {
    return false;
  }

  /**
   * Determines whether to generate a surface layer in this chunk. Disabled for a void world.
   *
   * @return False for all coordinates.
   */
  @Override
  public boolean shouldGenerateSurface(
    @NotNull final WorldInfo worldInfo,
    @NotNull final Random random,
    final int chunkX,
    final int chunkZ
  ) {
    return false;
  }

  /**
   * Determines whether to generate caves in this chunk. Disabled for a void world.
   *
   * @return False for all coordinates.
   */
  @Override
  public boolean shouldGenerateCaves(
    @NotNull final WorldInfo worldInfo,
    @NotNull final Random random,
    final int chunkX,
    final int chunkZ
  ) {
    return false;
  }

  /**
   * Determines whether to generate decorations in this chunk. Disabled for a void world.
   *
   * @return False for all coordinates.
   */
  @Override
  public boolean shouldGenerateDecorations(
    @NotNull final WorldInfo worldInfo,
    @NotNull final Random random,
    final int chunkX,
    final int chunkZ
  ) {
    return false;
  }

  /**
   * Determines whether to generate mobs in this world. Set to true.
   *
   * @return True, allowing mob generation in a void world if configured by the server.
   */
  @Override
  public boolean shouldGenerateMobs() {
    return true;
  }

  /**
   * Determines whether to generate mobs in this chunk. By default, returns true.
   *
   * @return True, enabling standard mob spawning rules.
   */
  @Override
  public boolean shouldGenerateMobs(
    @NotNull final WorldInfo worldInfo,
    @NotNull final Random random,
    final int chunkX,
    final int chunkZ
  ) {
    return true;
  }

  /**
   * Determines whether to generate structures in this chunk. Disabled for a void world.
   *
   * @return False for all coordinates.
   */
  @Override
  public boolean shouldGenerateStructures(
    @NotNull final WorldInfo worldInfo,
    @NotNull final Random random,
    final int chunkX,
    final int chunkZ
  ) {
    return false;
  }
}