package de.jexcellence.multiverse.generator.voidgenerator;

import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides the THE_VOID biome for a void world generation.
 */
public class VoidBiomeProvider extends BiomeProvider {

	/**
	 * Retrieves the biome at a given location in the world.
	 *
	 * @param info The world info for the current world.
	 * @param x    The X-coordinate in the chunk.
	 * @param y    The Y-coordinate in the chunk.
	 * @param z    The Z-coordinate in the chunk.
	 * @return A {@link Biome} object representing the chosen biome.
	 */
	@Override
	public @NotNull Biome getBiome(
		@NotNull final WorldInfo info,
		final int x,
		final int y,
		final int z
	) {
		return Biome.THE_VOID;
	}

	/**
	 * Returns a list of biomes appropriate for the given {@link WorldInfo}.
	 *
	 * @param info The world info for the current world.
	 * @return A list of biomes supported by this provider.
	 */
	@Override
	public @NotNull List<Biome> getBiomes(
		@NotNull final WorldInfo info
	) {
		return new ArrayList<>(List.of(Biome.THE_VOID));
	}
}