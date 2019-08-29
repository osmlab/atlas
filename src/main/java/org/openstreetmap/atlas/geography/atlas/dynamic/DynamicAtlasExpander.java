package org.openstreetmap.atlas.geography.atlas.dynamic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.GeometryPrintable;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.WktPrintable;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean.RelationBeanItem;
import org.openstreetmap.atlas.geography.atlas.dynamic.policy.DynamicAtlasPolicy;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StreamIterable;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
class DynamicAtlasExpander
{
    private static final Logger logger = LoggerFactory.getLogger(DynamicAtlasExpander.class);

    private final DynamicAtlas dynamicAtlas;

    private Set<Shard> shardsUsedForCurrent;
    private final Map<Shard, Atlas> loadedShards;
    private final Function<Shard, Optional<Atlas>> atlasFetcher;
    private final Sharding sharding;
    private final DynamicAtlasPolicy policy;
    // This is true when the loading of the initial shard has been completed
    private final boolean initialized;
    // This is true when, in case of deferred loading, the loading of the shards has been called
    // (unlocking further automatic loading later)
    private boolean isAlreadyLoaded = false;
    private boolean preemptiveLoadDone = false;
    // Number of times the udnerlying Multi-Atlas has been built.
    private int timesMultiAtlasWasBuiltUnderneath;

    DynamicAtlasExpander(final DynamicAtlas dynamicAtlas, final DynamicAtlasPolicy policy)
    {
        this.dynamicAtlas = dynamicAtlas;

        this.timesMultiAtlasWasBuiltUnderneath = 0;
        this.sharding = policy.getSharding();
        this.loadedShards = new HashMap<>();
        this.shardsUsedForCurrent = new HashSet<>();
        this.atlasFetcher = policy.getAtlasFetcher();
        // Still keep the policy
        this.policy = policy;
        this.addNewShards(policy.getInitialShards());
        this.initialized = true;
    }

    public DynamicAtlasPolicy getPolicy()
    {
        return this.policy;
    }

    boolean areaCovered(final Area area)
    {
        final Polygon polygon = area.asPolygon();
        final MultiPolygon initialShardsBounds = this.policy.getInitialShardsBounds();
        if (!this.policy.isExtendIndefinitely() && !(polygon.overlaps(initialShardsBounds)
                || initialShardsBounds.overlaps(polygon)))
        {
            // If the policy is to not extend indefinitely, then assume that the loading is not
            // necessary.
            return true;
        }
        final Iterable<? extends Shard> neededShards = this.sharding.shards(polygon);
        for (final Shard neededShard : neededShards)
        {
            if (!this.loadedShards.containsKey(neededShard))
            {
                newPolygon(polygon, area);
                return false;
            }
        }
        return true;
    }

    void buildUnderlyingMultiAtlas()
    {
        final Time buildTime = Time.now();
        final Set<Shard> nonNullShards = nonNullShards();
        if (this.shardsUsedForCurrent.equals(nonNullShards))
        {
            // Same Multi-Atlas, let's not reload.
            return;
        }
        final List<Atlas> nonNullAtlasShards = getNonNullAtlasShards();
        if (!nonNullAtlasShards.isEmpty())
        {
            this.policy.getShardSetChecker().accept(nonNullShards());
            if (nonNullAtlasShards.size() == 1)
            {
                this.dynamicAtlas.swapCurrentAtlas(nonNullAtlasShards.get(0));
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("{}: Loading MultiAtlas with {}", this.dynamicAtlas.getName(),
                            nonNullShards().stream().map(Shard::getName)
                                    .collect(Collectors.toList()));
                }
                this.dynamicAtlas.swapCurrentAtlas(new MultiAtlas(nonNullAtlasShards));
                this.timesMultiAtlasWasBuiltUnderneath++;
            }
            this.shardsUsedForCurrent = nonNullShards;
            if (this.initialized)
            {
                this.isAlreadyLoaded = true;
            }
        }
        else
        {
            throw new CoreException("Cannot load shards with no data!");
        }
        logger.trace("{}: Built underlying MultiAtlas in {}", this.dynamicAtlas.getName(),
                buildTime.elapsedSince());
    }

    /**
     * Expand the Atlas if needed. This method loops through the provided {@link Iterable}, then
     * checks if each entity found warrants loading another neighboring {@link Shard}. If it does,
     * it loads all the necessary {@link Shard}s and retries looping through the new
     * {@link Iterable}. Once everything is included, then the final {@link Iterable} is returned.
     *
     * @param entitiesSupplier
     *            The {@link Supplier} of the {@link Iterable} of items that will be called as long
     *            as there are overlaps to new shards. There is a need of a supplier here so that
     *            the {@link Iterable} is re-built every time with the latest Atlas.
     * @param entityCoveredPredicate
     *            The function that decides if an entity is already covered or not.
     * @param mapper
     *            What to do with the result. This is to replace the regular items with
     *            DynamicItems.
     * @return The {@link Iterable} of DynamicItems
     */
    <V extends AtlasEntity, T> Iterable<T> expand(final Supplier<Iterable<V>> entitiesSupplier,
            final Predicate<V> entityCoveredPredicate, final Function<V, T> mapper)
    {
        StreamIterable<V> result = Iterables.stream(entitiesSupplier.get())
                .filter(Objects::nonNull);
        final boolean shouldStopExploring = this.policy.isDeferLoading()
                && !this.policy.isExtendIndefinitely() && this.preemptiveLoadDone;
        while (!shouldStopExploring && !entitiesCovered(result, entityCoveredPredicate))
        {
            result = Iterables.stream(entitiesSupplier.get()).filter(Objects::nonNull);
        }
        return result.map(mapper).collect();
    }

    Map<Shard, Atlas> getLoadedShards()
    {
        return this.loadedShards;
    }

    /**
     * @return The number of times that {@link DynamicAtlas} has (re-)built its {@link MultiAtlas}
     *         underneath.
     */
    int getTimesMultiAtlasWasBuiltUnderneath()
    {
        return this.timesMultiAtlasWasBuiltUnderneath;
    }

    boolean lineItemCovered(final LineItem item)
    {
        final PolyLine polyLine = item.asPolyLine();
        final MultiPolygon initialShardsBounds = this.policy.getInitialShardsBounds();
        if (!this.policy.isExtendIndefinitely() && !initialShardsBounds.overlaps(polyLine))
        {
            // If the policy is to not extend indefinitely, then assume that the loading is not
            // necessary.
            return true;
        }
        final Iterable<? extends Shard> neededShards = this.sharding.shardsIntersecting(polyLine);
        for (final Shard neededShard : neededShards)
        {
            if (!this.loadedShards.containsKey(neededShard))
            {
                newPolyLine(polyLine, item);
                return false;
            }
        }
        return true;
    }

    boolean locationItemCovered(final LocationItem item)
    {
        final Location location = item.getLocation();
        final MultiPolygon initialShardsBounds = this.policy.getInitialShardsBounds();
        if (!this.policy.isExtendIndefinitely()
                && !initialShardsBounds.fullyGeometricallyEncloses(location))
        {
            // If the policy is to not extend indefinitely, then assume that the loading is not
            // necessary.
            return true;
        }
        final Iterable<? extends Shard> neededShards = this.sharding.shardsCovering(location);
        for (final Shard neededShard : neededShards)
        {
            if (!this.loadedShards.containsKey(neededShard))
            {
                newLocation(location, item);
                return false;
            }
        }
        return true;
    }

    /**
     * Do a preemptive load of the {@link DynamicAtlas} as far as the {@link DynamicAtlasPolicy}
     * allows.
     * <p>
     * In some very specific cases, where the {@link DynamicAtlasPolicy} allows expansion only if
     * new shards intersect at least one feature that crosses the initial set of shards, it is
     * possible that expanding only one time misses out some shard candidates. This happens when
     * some feature intersects the initial shards but does not have any shape point inside any
     * initial shard. This way, the initial shards do not contain that feature even though they
     * intersect it. That feature is discovered as we load the neighboring shards which contain that
     * feature. If that said feature also intersects a third neighboring shard, then that third
     * neighboring shard becomes eligible for expansion, as that specific feature crosses it and the
     * initial shards. To work around that case, the preemptive load will do a multi-staged loading.
     */
    void preemptiveLoad()
    {
        if (!this.policy.isDeferLoading())
        {
            logger.warn(
                    "{}: Skipping preemptive loading as it is useful only when the DynamicAtlasPolicy is deferLoading = true.",
                    this.dynamicAtlas.getName());
            return;
        }
        if (this.preemptiveLoadDone)
        {
            return;
        }
        // Loop through the entities to find potential shards to add
        browseForPotentialNewShards();
        // Load all the shards into a multiAtlas
        buildUnderlyingMultiAtlas();
        // Record the current list of shards
        Set<Shard> currentShards = new HashSet<>(this.loadedShards.keySet());
        // Loop through the entities again to find potential shards to add. This can still happen if
        // a way intersects the initial shard without shapepoints inside the initial shards, and was
        // revealed by loading a new neighboring shard. At that point, if that way also intersects a
        // third shard which was not loaded before, that third shard might become now eligible.
        browseForPotentialNewShards();
        browseForPotentialNewShardsFromAggressiveRelations();
        // Repeat the same process as long as we find some of those third party shards.
        while (!this.loadedShards.keySet().equals(currentShards))
        {
            if (logger.isInfoEnabled())
            {
                final Set<Shard> missingShards = new HashSet<>(this.loadedShards.keySet());
                missingShards.removeAll(currentShards);
                logger.info("{}: Preemptive load found new unexpected 2nd degree shard(s): {}",
                        this.dynamicAtlas.getName(),
                        missingShards.stream().map(Shard::getName).collect(Collectors.toList()));
            }

            // Load all the shards into a multiAtlas
            buildUnderlyingMultiAtlas();
            // Record the current list of shards
            currentShards = new HashSet<>(this.loadedShards.keySet());
            // Loop through the entities again to find potential shards to add.
            browseForPotentialNewShards();
            browseForPotentialNewShardsFromAggressiveRelations();
        }
        this.preemptiveLoadDone = true;
    }

    boolean relationCovered(final Relation relation)
    {
        final Set<Long> parentRelationIdentifierTree = new HashSet<>();
        parentRelationIdentifierTree.add(relation.getIdentifier());
        return relationCoveredInternal(relation, parentRelationIdentifierTree);
    }

    private void addNewShardLog(final Shard shard)
    {
        if (logger.isInfoEnabled())
        {
            final Atlas loaded = this.loadedShards.get(shard);
            if (loaded == null)
            {
                logger.info("{}: Loading new shard {} found no new Atlas.",
                        this.dynamicAtlas.getName(), shard.getName());
            }
            else
            {
                logger.info("{}: Loading new shard {} found a new Atlas {} of size {}",
                        this.dynamicAtlas.getName(), shard.getName(), loaded.getName(),
                        loaded.size());
            }
        }
    }

    private void addNewShards(final Iterable<? extends Shard> shards)
    {
        final Set<Shard> initialNonEmptyLoadedShards = nonNullShards();
        for (final Shard shard : shards)
        {
            if (!this.loadedShards.containsKey(shard))
            {
                this.loadedShards.put(shard, this.atlasFetcher.apply(shard).orElse(null));
                addNewShardLog(shard);
            }
        }
        final List<Atlas> nonNullAtlasShards = getNonNullAtlasShards();
        if (!nonNullAtlasShards.isEmpty())
        {
            if (shouldBuildUnderlyingMultiAtlasWhenAddingNewShards(initialNonEmptyLoadedShards))
            {
                // Load the new current atlas only if it is the first time, or it is not the
                // first time, and the policy is not to defer loading.
                buildUnderlyingMultiAtlas();
            }
        }
        else
        {
            // There should always be a non-null atlas in that list, coming from the initial Shard.
            throw new CoreException("{}: There is no data to load for initial shard!",
                    this.dynamicAtlas.getName());
        }
    }

    private boolean areaCoversInitialShardBounds(final Area area)
    {
        return this.policy.getInitialShardsBounds().overlaps(area.asPolygon());
    }

    private void browseForPotentialNewShards()
    {
        // Look at regular entities
        this.dynamicAtlas.entities();
    }

    private void browseForPotentialNewShardsFromAggressiveRelations()
    {
        // In case we want to aggressively explore relations, we constrain it to only when the
        // policy is to not extend indefinitely, and to defer loading.
        if (this.policy.isAggressivelyExploreRelations() && !this.policy.isExtendIndefinitely()
                && this.policy.isDeferLoading())
        {
            // Get all the neighboring shards
            final Set<Shard> onlyNeighboringShards = new HashSet<>();
            this.loadedShards.keySet().forEach(
                    shard -> this.sharding.neighbors(shard).forEach(onlyNeighboringShards::add));
            onlyNeighboringShards.removeAll(this.loadedShards.keySet());
            if (logger.isTraceEnabled())
            {
                final Set<String> shardNames = onlyNeighboringShards.stream().map(Shard::getName)
                        .collect(Collectors.toSet());
                final String wktCollection = WktPrintable.toWktCollection(onlyNeighboringShards);
                logger.trace("{}: Aggressively exploring relations in shards {} - {}",
                        this.dynamicAtlas.getName(), shardNames, wktCollection);
            }
            // For each of those shards, load the Atlas individually and find the relation and its
            // members if it is there too.
            final Set<Shard> neighboringShardsContainingRelation = neighboringShardsContainingInitialRelation(
                    onlyNeighboringShards);
            // Add the neighboring shards as new shards to be loaded.
            if (!neighboringShardsContainingRelation.isEmpty())
            {
                addNewShards(neighboringShardsContainingRelation);
            }
        }
    }

    /**
     * @param entities
     *            The items to test for full coverage by the current shards
     * @param entityCoveredPredicate
     *            The function that decides if an entity is already covered or not.
     * @return False if any of the items is not fully covered by the current shards
     */
    private <V extends AtlasEntity> boolean entitiesCovered(final Iterable<V> entities,
            final Predicate<V> entityCoveredPredicate)
    {
        return Iterables.stream(entities)
                .filter(entity -> this.policy.getAtlasEntitiesToConsiderForExpansion().test(entity))
                .allMatch(entityCoveredPredicate);
    }

    private List<Atlas> getNonNullAtlasShards()
    {
        return this.loadedShards.values().stream().filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean lineItemCoversInitialShardBounds(final LineItem lineItem)
    {
        return this.policy.getInitialShardsBounds().overlaps(lineItem.asPolyLine());
    }

    private boolean loadedShardsfullyGeometricallyEncloseLocation(final Location location)
    {
        return Iterables.stream(this.sharding.shardsCovering(location))
                .allMatch(this.loadedShards::containsKey);
    }

    private boolean loadedShardsfullyGeometricallyEnclosePolyLine(final PolyLine polyLine)
    {
        return Iterables.stream(this.sharding.shardsIntersecting(polyLine))
                .allMatch(this.loadedShards::containsKey);
    }

    private boolean loadedShardsfullyGeometricallyEnclosePolygon(final Polygon polygon)
    {
        return Iterables.stream(this.sharding.shards(polygon))
                .allMatch(this.loadedShards::containsKey);
    }

    private boolean locationItemCoversInitialShardBounds(final LocationItem locationItem)
    {
        return this.policy.getInitialShardsBounds()
                .fullyGeometricallyEncloses(locationItem.getLocation());
    }

    private boolean neighboringAtlasContainingInitialRelation(final Atlas atlas)
    {
        for (final Relation newRelation : atlas.relations())
        {
            final Relation currentRelation = this.dynamicAtlas
                    .subRelation(newRelation.getIdentifier());
            if (currentRelation != null
                    && this.policy.getAtlasEntitiesToConsiderForExpansion().test(currentRelation)
                    && relationCoversInitialShardBounds(currentRelation))
            {
                final RelationBean newMembers = newRelation.members().asBean();
                final RelationBean currentMembers = currentRelation.members().asBean();
                for (final RelationBeanItem newMember : newMembers)
                {
                    if (!currentMembers.contains(newMember))
                    {
                        newShapeLog(newRelation, currentRelation);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Set<Shard> neighboringShardsContainingInitialRelation(
            final Set<Shard> neighboringShardCandidates)
    {
        final Set<Shard> neighboringShardsContainingRelation = new HashSet<>();
        neighboringShardCandidates
                .forEach(shard -> this.policy.getAtlasFetcher().apply(shard).ifPresent(atlas ->
                {
                    if (neighboringAtlasContainingInitialRelation(atlas))
                    {
                        neighboringShardsContainingRelation.add(shard);
                    }
                }));
        return neighboringShardsContainingRelation;
    }

    private void newLocation(final Location location, final LocationItem... source)
    {
        if (!loadedShardsfullyGeometricallyEncloseLocation(location))
        {
            newShapeLog(location, source);
            addNewShards(this.sharding.shardsCovering(location));
        }
    }

    private void newPolyLine(final PolyLine polyLine, final LineItem... source)
    {
        if (!loadedShardsfullyGeometricallyEnclosePolyLine(polyLine))
        {
            newShapeLog(polyLine, source);
            addNewShards(this.sharding.shardsIntersecting(polyLine));
        }
    }

    private void newPolygon(final Polygon polygon, final AtlasEntity... source)
    {
        if (!loadedShardsfullyGeometricallyEnclosePolygon(polygon))
        {
            newShapeLog(polygon, source);
            addNewShards(this.sharding.shards(polygon));
        }
    }

    private void newShapeLog(final GeometryPrintable geometry, final AtlasEntity... source)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("{}: Triggering new shard load for {}{}", this.dynamicAtlas.getName(),
                    source.length > 0
                            ? "Atlas " + new StringList(Iterables.stream(Iterables.asList(source))
                                    .map(item -> item.getType() + " " + item.getIdentifier()))
                                            .join(", ")
                                    + " with shape "
                            : "",
                    geometry.toWkt());
        }
    }

    private Set<Shard> nonNullShards()
    {
        return new HashSet<>(this.loadedShards.keySet().stream()
                .filter(shard -> this.loadedShards.get(shard) != null).collect(Collectors.toSet()));
    }

    // NOSONAR here as complexity 16 is ok.
    private boolean relationCoveredInternal(final Relation relation, // NOSONAR
            final Set<Long> parentRelationIdentifierTree)
    {
        final RelationMemberList members = relation.members();
        boolean result = true;
        for (final RelationMember member : members)
        {
            final AtlasEntity entity = member.getEntity();
            if (entity instanceof Area)
            {
                if (!areaCovered((Area) entity))
                {
                    result = false;
                }
            }
            else if (entity instanceof LineItem)
            {
                if (!lineItemCovered((LineItem) entity))
                {
                    result = false;
                }
            }
            else if (entity instanceof LocationItem)
            {
                if (!locationItemCovered((LocationItem) entity))
                {
                    result = false;
                }
            }
            else if (entity instanceof Relation)
            {
                result = relationMemberCoveredInternal(relation, (Relation) entity,
                        parentRelationIdentifierTree);
            }
            else
            {
                throw new CoreException("Unknown Relation Member Type: {}",
                        entity.getClass().getName());
            }
        }
        return result;
    }

    private boolean relationCoversInitialShardBounds(final Relation relation)
    {
        final Set<Long> parentRelationIdentifierTree = new HashSet<>();
        parentRelationIdentifierTree.add(relation.getIdentifier());
        return relationCoversInitialShardBoundsInternal(relation, parentRelationIdentifierTree);
    }

    // NOSONAR here as complexity 16 is ok.
    private boolean relationCoversInitialShardBoundsInternal(final Relation relation, // NOSONAR
            final Set<Long> parentRelationIdentifierTree)
    {
        final RelationMemberList members = relation.members();
        boolean result = false;
        for (final RelationMember member : members)
        {
            final AtlasEntity entity = member.getEntity();
            if (entity instanceof Area)
            {
                if (areaCoversInitialShardBounds((Area) entity))
                {
                    result = true;
                }
            }
            else if (entity instanceof LineItem)
            {
                if (lineItemCoversInitialShardBounds((LineItem) entity))
                {
                    result = true;
                }
            }
            else if (entity instanceof LocationItem)
            {
                if (locationItemCoversInitialShardBounds((LocationItem) entity))
                {
                    result = true;
                }
            }
            else if (entity instanceof Relation)
            {
                result = relationMemberCoversInitialShardBoundsInternal(relation, (Relation) entity,
                        parentRelationIdentifierTree);
            }
            else
            {
                throw new CoreException("Unknown Relation Member Type: {}",
                        entity.getClass().getName());
            }
        }
        return result;
    }

    private boolean relationMemberCoveredInternal(final Relation parentRelation,
            final Relation relation, final Set<Long> parentRelationIdentifierTree)
    {
        boolean result = true;
        final long newIdentifier = relation.getIdentifier();
        if (parentRelationIdentifierTree.contains(newIdentifier))
        {
            logger.error(
                    "Skipping! Unable to expand on relation which has a loop: {}. Parent tree: {}",
                    parentRelation, parentRelationIdentifierTree);
        }
        else
        {
            final Set<Long> newParentRelationIdentifierTree = new HashSet<>();
            newParentRelationIdentifierTree.addAll(parentRelationIdentifierTree);
            newParentRelationIdentifierTree.add(newIdentifier);
            if (!relationCoveredInternal(relation, newParentRelationIdentifierTree))
            {
                result = false;
            }
        }
        return result;
    }

    private boolean relationMemberCoversInitialShardBoundsInternal(final Relation parentRelation,
            final Relation relation, final Set<Long> parentRelationIdentifierTree)
    {
        boolean result = false;
        final long newIdentifier = relation.getIdentifier();
        if (parentRelationIdentifierTree.contains(newIdentifier))
        {
            logger.error(
                    "Skipping! Unable to expand on relation which has a loop: {}. Parent tree: {}",
                    parentRelation, parentRelationIdentifierTree);
        }
        else
        {
            final Set<Long> newParentRelationIdentifierTree = new HashSet<>();
            newParentRelationIdentifierTree.addAll(parentRelationIdentifierTree);
            newParentRelationIdentifierTree.add(newIdentifier);
            if (relationCoversInitialShardBoundsInternal(relation, newParentRelationIdentifierTree))
            {
                result = true;
            }
        }
        return result;
    }

    private boolean shouldBuildUnderlyingMultiAtlasWhenAddingNewShards(
            final Set<Shard> initialNonEmptyLoadedShards)
    {
        final boolean thereAreNewViableShards = !initialNonEmptyLoadedShards
                .equals(nonNullShards());
        // If DynamicAtlas is not initialized yet, it means this call is within the constructor.
        // We always load the initial shards first.
        final boolean dynamicAtlasNotInitializedYet = !this.initialized;
        // This is either:
        // 1. The opposite waiting for a preemptive load call
        // OR
        // 2. The preemptive load call has already happened and we are in a subsequent call.
        final boolean loadingIsNotDeferredOrItIsAndAlreadyHappened = !this.policy.isDeferLoading()
                || this.isAlreadyLoaded;

        final boolean shouldBuildUnderlyingMultiAtlas = thereAreNewViableShards // NOSONAR
                && (dynamicAtlasNotInitializedYet || loadingIsNotDeferredOrItIsAndAlreadyHappened);
        return shouldBuildUnderlyingMultiAtlas;
    }
}
