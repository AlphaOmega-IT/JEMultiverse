package de.jexcellence.multiverse.ui;

import de.jexcellence.je18n.i18n.I18n;
import de.jexcellence.jeplatform.utility.itemstack.ItemBuildable;
import de.jexcellence.multiverse.Multiverse;
import de.jexcellence.multiverse.database.entity.MVWorld;
import me.devnatan.inventoryframework.View;
import me.devnatan.inventoryframework.ViewConfigBuilder;
import me.devnatan.inventoryframework.context.OpenContext;
import me.devnatan.inventoryframework.context.RenderContext;
import me.devnatan.inventoryframework.state.State;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MultiverseEditorView extends View {

	private final State<Multiverse> sMultiverse = initialState("plugin");
	private final State<String> sIdentifier = initialState("identifier");
	private final State<MVWorld> sMvWorld = initialState("mvWorld");

	private MVWorld mvWorld;
	private String identifier;
	private Multiverse multiverse;

	@Override
	public void onInit(@NotNull ViewConfigBuilder config) {
		config.size(6).build();
	}

	@Override
	public void onFirstRender(@NotNull RenderContext render) {
		renderComponents(render, mvWorld);
	}

	@Override
	public void onOpen(@NotNull OpenContext open) {
		this.multiverse = sMultiverse.get(open);
		this.identifier = sIdentifier.get(open);
		this.mvWorld = sMvWorld.get(open);

		open.modifyConfig().title(
			new I18n.Builder(
				"multiverse_editor_ui.title", open.getPlayer()
			).build().display()
		);
	}

	private void renderComponents(
		final @NotNull RenderContext render,
		final @NotNull MVWorld mvWorld
	) {
		if (
			mvWorld.getId() == null
		) {
			render.slot(3, 5, createErrorItem(render, "multiverse_editor_ui.error.world_doesnt_exist"));
			return;
		}

		if (
			!mvWorld.getIdentifier().equals(render.getPlayer().getWorld().getName())
		) {
			render.slot(3, 5, createErrorItem(render, "multiverse_editor_ui.error.you_are_not_in_world"));
			return;
		}

		render.slot(
			3, 3,
			new ItemBuildable.Builder(Material.RED_BED)
				.setName(new I18n.Builder("multiverse_editor_ui.spawn_location.name", render.getPlayer())
					.build()
					.display())
				.setLore(new I18n.Builder("multiverse_editor_ui.spawn_location.lore", render.getPlayer())
					.withPlaceholder(
						"spawn_location", mvWorld
							.getSpawnLocation()
							.toString())
					.build()
					.display())
				.build()
		).onClick(() -> {
			mvWorld.setSpawnLocation(render.getPlayer().getLocation().toCenterLocation());
			this.multiverse.getMvWorldRepository().updateAsync(mvWorld);
			this.multiverse.getWorlds().put(mvWorld.getIdentifier(), mvWorld);
			new I18n.Builder("multiverse_editor_ui.spawn_location_set", render.getPlayer()).includingPrefix().build().send();
		}).closeOnClick();

		render.slot(
			4, 3,
			new ItemBuildable.Builder(Material.STRUCTURE_BLOCK)
				.setName(new I18n.Builder("multiverse_editor_ui.global_spawn.name", render.getPlayer())
					.build()
					.display())
				.setLore(new I18n.Builder("multiverse_editor_ui.global_spawn.lore", render.getPlayer())
					.withPlaceholder("is_global_spawn", mvWorld.isGlobalizedSpawn() ? "✓" : "✗")
					.build()
					.display())
				.build()
		).onClick(() -> {
			this.multiverse.getMvWorldRepository().findByGlobalSpawnAsync().thenAcceptAsync(
				globalMVWorld -> {
					if (
						globalMVWorld != null && !mvWorld.isGlobalizedSpawn()
					) {
						new I18n.Builder("multiverse_editor_ui.global_spawn_already_set", render.getPlayer())
							.includingPrefix()
							.withPlaceholder("world_name", globalMVWorld.getIdentifier())
							.build()
							.send()
						;
						return;
					}

					mvWorld.setGlobalizedSpawn(!mvWorld.isGlobalizedSpawn());
					this.multiverse.getMvWorldRepository().updateAsync(mvWorld);
					this.multiverse.getWorlds().put(mvWorld.getIdentifier(), mvWorld);
					new I18n.Builder("multiverse_editor_ui.global_spawn_set", render.getPlayer()).includingPrefix().withPlaceholder("is_global_spawn", mvWorld.isGlobalizedSpawn() ? "✓" : "✗").build().send();
				}
				, this.multiverse.getExecutor());
		}).closeOnClick();
	}

	private ItemStack createErrorItem(@NotNull RenderContext render, String errorKey) {
		return new ItemBuildable.Builder(Material.BARRIER)
			.setName(
				new I18n.Builder(
					errorKey + ".name", render.getPlayer()
				).build().display()
			)
			.setLore(
				new I18n.Builder(
					errorKey + ".lore", render.getPlayer()
				).withPlaceholder("world_name", this.identifier).build().display()
			).build()
		;
	}
}
