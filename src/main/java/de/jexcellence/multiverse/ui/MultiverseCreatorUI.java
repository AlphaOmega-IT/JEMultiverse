package de.jexcellence.multiverse.ui;

import de.jexcellence.je18n.i18n.I18n;
import de.jexcellence.jeplatform.inventory.*;
import de.jexcellence.jeplatform.inventory.filter.IUIFilter;
import de.jexcellence.multiverse.Multiverse;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MultiverseCreatorUI implements IInventoryProvider {
	
	private final Multiverse multiverse;
	
	public MultiverseCreatorUI(
			final @NotNull Multiverse multiverse
	) {
		this.multiverse = multiverse;
	}
	
	@Override
	public Optional<JEInventory> get(@NotNull final Player player) {
		return Optional.of(new JEInventory.Builder(this.multiverse.getInventoryFactory(), this)
				                   .id("multiverse_world_creator")
				                   .size(1, 9)
				                   .title(new I18n.Builder("multiverse_creator_ui.title", player)
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
}
