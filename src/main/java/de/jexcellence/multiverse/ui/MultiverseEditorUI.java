package de.jexcellence.multiverse.ui;

import de.jexcellence.je18n.i18n.I18n;
import de.jexcellence.jeplatform.inventory.*;
import de.jexcellence.jeplatform.inventory.filter.IUIFilter;
import de.jexcellence.jeplatform.utility.itemstack.ItemBuildable;
import de.jexcellence.multiverse.Multiverse;
import de.jexcellence.multiverse.database.entity.MVWorld;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class MultiverseEditorUI implements IInventoryProvider {

	private final Multiverse multiverse;
	private final CompletableFuture<MVWorld> multiverseWorld;
	private final String identifier;

	public MultiverseEditorUI(
			final @NotNull Multiverse multiverse,
			final @NotNull String identifier
	) {
		this.multiverse = multiverse;
		this.identifier = identifier;
		this.multiverseWorld = this.multiverse.getMvWorldRepository().findByIdentifierAsync(this.identifier);
	}

	@Override
	public Optional<JEInventory> get(@NotNull final Player player) {
		return Optional.of(new JEInventory.Builder(this.multiverse.getInventoryFactory(), this)
				.id("multiverse_world_editor")
				.size(6, 9)
				.title(new I18n.Builder("multiverse_editor_ui.title", player)
						.build()
						.display())
				.build());
	}

	@Override
	public void init(
			@NotNull final Player player,
			@NotNull final IInvContents contents
	) {
		contents.fill(UIButton.empty());

		multiverseWorld
				.thenAcceptAsync(mvWorld -> {
					Bukkit
							.getScheduler()
							.runTask(this.multiverse, () -> {
								try {
									if (mvWorld == null) {
										contents.putItem(2, 4, UIButton.empty(new ItemBuildable.Builder(Material.BARRIER)
												.setName(new I18n.Builder("multiverse_editor_ui.error.world_doesnt_exist.name", player)
														.build()
														.display())
												.setLore(new I18n.Builder("multiverse_editor_ui.error.world_doesnt_exist.lore", player)
														.withPlaceholder("world_name", this.identifier)
														.build()
														.display())
												.build()));
										return;
									}

									if (!mvWorld.getIdentifier().equals(player.getWorld().getName())) {
										contents.putItem(2, 4, UIButton.empty(new ItemBuildable.Builder(Material.BARRIER)
												.setName(new I18n.Builder("multiverse_editor_ui.error.you_are_not_in_world.name", player)
														.build()
														.display())
												.setLore(new I18n.Builder("multiverse_editor_ui.error.you_are_not_in_world.lore", player)
														.withPlaceholder("world_name", mvWorld.getIdentifier())
														.build()
														.display())
												.build()));
										return;
									}

									this.initializeUIButtons(player, contents, mvWorld);
								} catch (Exception e) {
									this.handleError(player, e);
								}
							});
				}, this.multiverse.getExecutor())
				.exceptionally(ex -> {
					return null;
				});
	}

	@Override
	public void update(
			@NotNull final Player player,
			@NotNull final IInvContents contents
	) {
		//NOT NEEDED
	}

	@Override
	public void dispose(@NotNull final Player player) {
		//NOT NEEDED
	}

	@Override
	public IInvPagination pagination(
			@NotNull final Player player,
			@NotNull final IInvContents contents
	) {
		return contents.pagination();
	}

	@Override
	public IUIFilter filter(
			@NotNull final Player player,
			@NotNull final IInvContents contents
	) {
		return contents.filter();
	}

	@Override
	public void handleError(
			@NotNull final Player player,
			@NotNull final Exception e
	) {
		this.multiverse
				.getPlatformLogger()
				.logDebug("Error while handling MultiverseEditorUI", e);
		player.closeInventory();
	}

	private void initializeUIButtons(
			final @NotNull Player player,
			final @NotNull IInvContents contents,
			final @NotNull MVWorld mvWorld
	) {
		contents.putItem(1, 1, UIButton.from(new ItemBuildable.Builder(Material.RED_BED)
				.setName(new I18n.Builder("multiverse_editor_ui.spawn_location.name", player)
						.build()
						.display())
				.setLore(new I18n.Builder("multiverse_editor_ui.spawn_location.lore", player)
						.withPlaceholder(
								"spawn_location", mvWorld
										.getSpawnLocation()
										.toString())
						.build()
						.display())
				.build(), "edit_spawn_location", event -> {
			mvWorld.setSpawnLocation(player.getLocation().toCenterLocation());
			this.updateMVWorld(mvWorld);

			new I18n.Builder("multiverse_editor_ui.spawn_location_set", player)
					.withPlaceholder("location", mvWorld.getSpawnLocation())
					.build()
					.send()
			;

			player.closeInventory();
		}));

		contents.putItem(2, 1, UIButton.from(new ItemBuildable.Builder(Material.STRUCTURE_BLOCK)
				.setName(new I18n.Builder("multiverse_editor_ui.global_spawn.name", player)
						.build()
						.display())
				.setLore(new I18n.Builder("multiverse_editor_ui.global_spawn.lore", player)
						.withPlaceholder("is_global_spawn", mvWorld.isGlobalizedSpawn() ? "✓" : "✗")
						.build()
						.display())
				.build(), "edit_global_spawn", event -> {

			this.multiverse
					.getMvWorldRepository()
					.findByGlobalSpawnAsync()
					.thenAcceptAsync(globalSpawnWorld -> {
						player.closeInventory();
						if (globalSpawnWorld != null && !mvWorld.isGlobalizedSpawn()) {
							new I18n.Builder("multiverse_editor_ui.global_spawn_already_set", player)
									.withPlaceholder("world_name", globalSpawnWorld.getIdentifier())
									.includingPrefix()
									.build()
									.send()
							;
							return;
						}

						this.updateGlobalSpawn(player, mvWorld);
					}, this.multiverse.getExecutor())
			;

			this.updateGlobalSpawn(player, mvWorld);
		}));
	}

	private void updateMVWorld(
			final @NotNull MVWorld mvWorld
	) {
		CompletableFuture.runAsync(() -> this.multiverse.getMvWorldRepository().update(mvWorld), this.multiverse.getExecutor());
	}

	private void updateGlobalSpawn(
			final @NotNull Player player,
			final @NotNull MVWorld mvWorld
	) {
		mvWorld.setGlobalizedSpawn(!mvWorld.isGlobalizedSpawn());
		this.updateMVWorld(mvWorld);

		new I18n.Builder("multiverse_editor_ui.global_spawn_set", player)
				.withPlaceholder("is_global_spawn", mvWorld.isGlobalizedSpawn() ? "✓" : "✗")
				.includingPrefix()
				.build()
				.send()
		;
	}
}