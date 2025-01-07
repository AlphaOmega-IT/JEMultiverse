package de.jexcellence.multiverse.database.repository;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.jexcellence.hibernate.repository.AbstractCRUDRepository;
import de.jexcellence.multiverse.database.entity.MVWorld;
import jakarta.persistence.EntityManagerFactory;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class MVWorldRepository extends AbstractCRUDRepository<MVWorld, Long> {

  private final ExecutorService executor;
  private final Cache<String, MVWorld> cache;

  public MVWorldRepository(
      final ExecutorService executor,
      final EntityManagerFactory entityManagerFactory
  ) {
    super(entityManagerFactory, MVWorld.class);
    this.cache = Caffeine.newBuilder().maximumSize(64).expireAfterWrite(30, TimeUnit.MINUTES).build();
    this.executor = executor;
  }

  public List<MVWorld> findAll(
      final int pageNumber,
      final int pageSize
  ) {
    if (! this.cache.asMap().values().isEmpty())
      return this.cache.asMap().values().stream().toList();

    List<MVWorld> foundWorlds = super.findAll(pageNumber, pageSize);
    foundWorlds.forEach(mvWorld -> this.cache.put(mvWorld.getIdentifier(), mvWorld));
    return foundWorlds;
  }

  public MVWorld findByIdentifier(
      final @NotNull String identifier
  ) {
    MVWorld cachedWorld = this.cache.getIfPresent(identifier);

    if (cachedWorld == null) {
      MVWorld foundWorld = super.findByAttributes(Map.of("identifier", identifier));
      if (foundWorld != null)
        this.cache.put(foundWorld.getIdentifier(), foundWorld);
      return foundWorld;
    }

    return cachedWorld;
  }

  public MVWorld findByGlobalSpawn() {
    for (MVWorld mvWorld : this.cache.asMap().values())
      if (mvWorld.isGlobalizedSpawn())
        return mvWorld;

    MVWorld foundWorld = super.findByAttributes(Map.of("isGlobalizedSpawn", true));
    if (foundWorld != null)
      this.cache.put(foundWorld.getIdentifier(), foundWorld);
    return foundWorld;
  }

  public CompletableFuture<MVWorld> findByIdentifierAsync(
      final @NotNull String identifier
  ) {
    MVWorld cachedWorld = this.cache.getIfPresent(identifier);

    if (cachedWorld == null) {
      CompletableFuture<MVWorld> foundWorld = super.findByAttributesAsync(Map.of("identifier", identifier));
      return foundWorld.thenApplyAsync(mvWorld -> {
        if (mvWorld != null)
          this.cache.put(mvWorld.getIdentifier(), mvWorld);

        return mvWorld;
      }, this.executor);
    }

    return CompletableFuture.completedFuture(cachedWorld);
  }

  public CompletableFuture<MVWorld> findByGlobalSpawnAsync() {
    for (MVWorld mvWorld : this.cache.asMap().values())
      if (mvWorld.isGlobalizedSpawn())
        return CompletableFuture.completedFuture(mvWorld);

    CompletableFuture<MVWorld> foundWorld = super.findByAttributesAsync(Map.of("isGlobalizedSpawn", true));
    return foundWorld.thenApplyAsync(mvWorld -> {
      if (mvWorld != null)
        this.cache.put(mvWorld.getIdentifier(), mvWorld);

      return mvWorld;
    }, this.executor);
  }

  @Override
  public MVWorld create(MVWorld entity) {
    MVWorld mvWorld = super.create(entity);
    this.cache.put(mvWorld.getIdentifier(), mvWorld);
    return mvWorld;
  }

  @Override
  public MVWorld update(MVWorld entity) {
    MVWorld mvWorld = super.update(entity);
    this.cache.put(mvWorld.getIdentifier(), mvWorld);
    return mvWorld;
  }

  @Override
  public void delete(Long aLong) {
    super.delete(aLong);
    this.cache.asMap().values().removeIf(world -> world.getId().equals(aLong));
  }
}
