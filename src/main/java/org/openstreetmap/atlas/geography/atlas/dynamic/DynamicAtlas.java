package org.openstreetmap.atlas.geography.atlas.dynamic;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.BareAtlas;
import org.openstreetmap.atlas.geography.atlas.dynamic.policy.DynamicAtlasPolicy;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StreamIterable;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is not thread safe!
 * <p>
 * An Atlas that is dynamically expanding by loading neighboring shards upon request of its
 * features.
 *
 * @author matthieun
 */
public class DynamicAtlas extends BareAtlas
{
    private static final long serialVersionUID = -2858997785405677961L;
    private static final Logger logger = LoggerFactory.getLogger(DynamicAtlas.class);

    // The current Atlas that will be swapped during expansion.
    private Atlas current;

    private final Map<Shard, Atlas> loadedShards;
    private final Function<Shard, Optional<Atlas>> atlasFetcher;
    private final Sharding sharding;
    private final DynamicAtlasPolicy policy;
    // This is true when the loading of the initial shard has been completed
    private final boolean initialized;
    // This is true when, in case of deferred loading, the loading of the shards has been called
    // (unlocking further automatic loading later)
    private boolean isAlreadyLoaded = false;

    /**
     * @param dynamicAtlasExpansionPolicy
     *            Expansion policy for the dynamic atlas
     */
    public DynamicAtlas(final DynamicAtlasPolicy dynamicAtlasExpansionPolicy)
    {
        this.setName("DynamicAtlas(" + dynamicAtlasExpansionPolicy.getInitialShards().stream()
                .map(Shard::getName).collect(Collectors.toSet()) + ")");
        this.sharding = dynamicAtlasExpansionPolicy.getSharding();
        this.loadedShards = new HashMap<>();
        this.atlasFetcher = dynamicAtlasExpansionPolicy.getAtlasFetcher();
        // Still keep the policy
        this.policy = dynamicAtlasExpansionPolicy;
        this.addNewShards(dynamicAtlasExpansionPolicy.getInitialShards());
        this.initialized = true;
    }

    @Override
    public Area area(final long identifier)
    {
        final Iterator<DynamicArea> result = expand(() -> Iterables.from(subArea(identifier)),
                this::areaCovered, this::newArea).iterator();
        return result.hasNext() ? result.next() : null;
    }

    @Override
    public Iterable<Area> areas()
    {
        return expand(() -> this.current.areas(), this::areaCovered, this::newArea);
    }

    @Override
    public Iterable<Area> areasCovering(final Location location)
    {
        newLocation(location);
        return expand(() -> this.current.areasCovering(location), this::areaCovered, this::newArea);
    }

    @Override
    public Iterable<Area> areasCovering(final Location location, final Predicate<Area> matcher)
    {
        newLocation(location);
        return expand(() -> this.current.areasCovering(location, matcher), this::areaCovered,
                this::newArea);
    }

    @Override
    public Iterable<Area> areasIntersecting(final Polygon polygon)
    {
        newPolygon(polygon);
        return expand(() -> this.current.areasIntersecting(polygon), this::areaCovered,
                this::newArea);
    }

    @Override
    public Iterable<Area> areasIntersecting(final Polygon polygon, final Predicate<Area> matcher)
    {
        newPolygon(polygon);
        return expand(() -> this.current.areasIntersecting(polygon, matcher), this::areaCovered,
                this::newArea);
    }

    @Override
    public Rectangle bounds()
    {
        return this.current.bounds();
    }

    @Override
    public Edge edge(final long identifier)
    {
        final Iterator<DynamicEdge> result = expand(() -> Iterables.from(subEdge(identifier)),
                this::lineItemCovered, this::newEdge).iterator();
        return result.hasNext() ? result.next() : null;
    }

    @Override
    public Iterable<Edge> edges()
    {
        return expand(() -> this.current.edges(), this::lineItemCovered, this::newEdge);
    }

    @Override
    public Iterable<Edge> edgesContaining(final Location location)
    {
        newLocation(location);
        return expand(() -> this.current.edgesContaining(location), this::lineItemCovered,
                this::newEdge);
    }

    @Override
    public Iterable<Edge> edgesContaining(final Location location, final Predicate<Edge> matcher)
    {
        newLocation(location);
        return expand(() -> this.current.edgesContaining(location, matcher), this::lineItemCovered,
                this::newEdge);
    }

    @Override
    public Iterable<Edge> edgesIntersecting(final Polygon polygon)
    {
        newPolygon(polygon);
        return expand(() -> this.current.edgesIntersecting(polygon), this::lineItemCovered,
                this::newEdge);
    }

    @Override
    public Iterable<Edge> edgesIntersecting(final Polygon polygon, final Predicate<Edge> matcher)
    {
        newPolygon(polygon);
        return expand(() -> this.current.edgesIntersecting(polygon, matcher), this::lineItemCovered,
                this::newEdge);
    }

    @Override
    public Line line(final long identifier)
    {
        final Iterator<DynamicLine> result = expand(() -> Iterables.from(subLine(identifier)),
                this::lineItemCovered, this::newLine).iterator();
        return result.hasNext() ? result.next() : null;
    }

    @Override
    public Iterable<Line> lines()
    {
        return expand(() -> this.current.lines(), this::lineItemCovered, this::newLine);
    }

    @Override
    public Iterable<Line> linesContaining(final Location location)
    {
        newLocation(location);
        return expand(() -> this.current.linesContaining(location), this::lineItemCovered,
                this::newLine);
    }

    @Override
    public Iterable<Line> linesContaining(final Location location, final Predicate<Line> matcher)
    {
        newLocation(location);
        return expand(() -> this.current.linesContaining(location, matcher), this::lineItemCovered,
                this::newLine);
    }

    @Override
    public Iterable<Line> linesIntersecting(final Polygon polygon)
    {
        newPolygon(polygon);
        return expand(() -> this.current.linesIntersecting(polygon), this::lineItemCovered,
                this::newLine);
    }

    @Override
    public Iterable<Line> linesIntersecting(final Polygon polygon, final Predicate<Line> matcher)
    {
        newPolygon(polygon);
        return expand(() -> this.current.linesIntersecting(polygon, matcher), this::lineItemCovered,
                this::newLine);
    }

    public void loadShards()
    {
        this.policy.getShardSetChecker().accept(nonNullShards());
        final List<Atlas> nonNullAtlasShards = getNonNullAtlasShards();
        if (!nonNullAtlasShards.isEmpty())
        {
            if (nonNullAtlasShards.size() == 1)
            {
                this.current = nonNullAtlasShards.get(0);
            }
            else
            {
                this.current = new MultiAtlas(nonNullAtlasShards);
            }
            if (this.initialized)
            {
                this.isAlreadyLoaded = true;
            }
        }
        else
        {
            throw new CoreException("Cannot load shards with no data!");
        }
    }

    @Override
    public AtlasMetaData metaData()
    {
        return this.current.metaData();
    }

    @Override
    public Node node(final long identifier)
    {
        final Iterator<DynamicNode> result = expand(() -> Iterables.from(subNode(identifier)),
                this::locationItemCovered, this::newNode).iterator();
        return result.hasNext() ? result.next() : null;
    }

    @Override
    public Iterable<Node> nodes()
    {
        return expand(() -> this.current.nodes(), this::locationItemCovered, this::newNode);
    }

    @Override
    public Iterable<Node> nodesAt(final Location location)
    {
        newLocation(location);
        return expand(() -> this.current.nodesAt(location), this::locationItemCovered,
                this::newNode);
    }

    @Override
    public Iterable<Node> nodesWithin(final Polygon polygon)
    {
        newPolygon(polygon);
        return expand(() -> this.current.nodesWithin(polygon), this::locationItemCovered,
                this::newNode);
    }

    @Override
    public Iterable<Node> nodesWithin(final Polygon polygon, final Predicate<Node> matcher)
    {
        newPolygon(polygon);
        return expand(() -> this.current.nodesWithin(polygon, matcher), this::locationItemCovered,
                this::newNode);
    }

    @Override
    public long numberOfAreas()
    {
        return this.current.numberOfAreas();
    }

    @Override
    public long numberOfEdges()
    {
        return this.current.numberOfEdges();
    }

    @Override
    public long numberOfLines()
    {
        return this.current.numberOfLines();
    }

    @Override
    public long numberOfNodes()
    {
        return this.current.numberOfNodes();
    }

    @Override
    public long numberOfPoints()
    {
        return this.current.numberOfPoints();
    }

    @Override
    public long numberOfRelations()
    {
        return this.current.numberOfRelations();
    }

    @Override
    public Point point(final long identifier)
    {
        final Iterator<DynamicPoint> result = expand(() -> Iterables.from(subPoint(identifier)),
                this::locationItemCovered, this::newPoint).iterator();
        return result.hasNext() ? result.next() : null;
    }

    @Override
    public Iterable<Point> points()
    {
        return expand(() -> this.current.points(), this::locationItemCovered, this::newPoint);
    }

    @Override
    public Iterable<Point> pointsAt(final Location location)
    {
        newLocation(location);
        return expand(() -> this.current.pointsAt(location), this::locationItemCovered,
                this::newPoint);
    }

    @Override
    public Iterable<Point> pointsWithin(final Polygon polygon)
    {
        newPolygon(polygon);
        return expand(() -> this.current.pointsWithin(polygon), this::locationItemCovered,
                this::newPoint);
    }

    @Override
    public Iterable<Point> pointsWithin(final Polygon polygon, final Predicate<Point> matcher)
    {
        newPolygon(polygon);
        return expand(() -> this.current.pointsWithin(polygon, matcher), this::locationItemCovered,
                this::newPoint);
    }

    /**
     * In case of an Atlas shard with neighbors containing data that might intersect it (example is
     * a way from a neighboring shard intersecting the initial shard, but without any shapepoints in
     * the initial shard) this method allows dual loading of neighboring shards. Once to load the
     * neighboring shards if any, and to discover such features if any, then to load the other
     * shards those features might intersect.
     */
    public void preemptiveLoad()
    {
        Set<Shard> currentShards = null;
        while (!this.loadedShards.keySet().equals(currentShards))
        {
            currentShards = new HashSet<>(this.loadedShards.keySet());
            this.entities();
            this.loadShards();
        }
    }

    @Override
    public Relation relation(final long identifier)
    {
        final Iterator<DynamicRelation> result = expand(
                () -> Iterables.from(subRelation(identifier)), this::relationCovered,
                this::newRelation).iterator();
        return result.hasNext() ? result.next() : null;
    }

    @Override
    public Iterable<Relation> relations()
    {
        return expand(() -> this.current.relations(), this::relationCovered, this::newRelation);
    }

    @Override
    public Iterable<Relation> relationsWithEntitiesIntersecting(final Polygon polygon)
    {
        newPolygon(polygon);
        return expand(() -> this.current.relationsWithEntitiesIntersecting(polygon),
                this::relationCovered, this::newRelation);
    }

    @Override
    public Iterable<Relation> relationsWithEntitiesIntersecting(final Polygon polygon,
            final Predicate<Relation> matcher)
    {
        newPolygon(polygon);
        return expand(() -> this.current.relationsWithEntitiesIntersecting(polygon, matcher),
                this::relationCovered, this::newRelation);
    }

    @Override
    public void save(final WritableResource writableResource)
    {
        throw new CoreException("DynamicAtlas cannot be saved");
    }

    protected Area subArea(final long identifier)
    {
        return this.current.area(identifier);
    }

    protected Edge subEdge(final long identifier)
    {
        return this.current.edge(identifier);
    }

    protected Line subLine(final long identifier)
    {
        return this.current.line(identifier);
    }

    protected Node subNode(final long identifier)
    {
        return this.current.node(identifier);
    }

    protected Point subPoint(final long identifier)
    {
        return this.current.point(identifier);
    }

    protected Relation subRelation(final long identifier)
    {
        return this.current.relation(identifier);
    }

    private void addNewShards(final Iterable<? extends Shard> shards)
    {
        final Set<Shard> initialNonEmptyLoadedShards = nonNullShards();
        for (final Shard shard : shards)
        {
            if (!this.loadedShards.containsKey(shard))
            {
                this.loadedShards.put(shard, this.atlasFetcher.apply(shard).orElse(null));
                if (logger.isInfoEnabled())
                {
                    final Atlas loaded = this.loadedShards.get(shard);
                    if (loaded == null)
                    {
                        logger.info("{}: Loading new shard {} found no new Atlas.", this.getName(),
                                shard.getName());
                    }
                    else
                    {
                        logger.info("{}: Loading new shard {} found a new Atlas {} of size {}",
                                this.getName(), shard.getName(), loaded.getName(),
                                loaded.size().toString());
                    }
                }
            }
        }
        final List<Atlas> nonNullAtlasShards = getNonNullAtlasShards();
        if (!nonNullAtlasShards.isEmpty())
        {
            if (!initialNonEmptyLoadedShards.equals(nonNullShards()))
            {
                // New Atlas files came in
                if (!this.initialized || !this.policy.isDeferLoading() || this.isAlreadyLoaded)
                {
                    // Load the new current atlas only if it is the first time, or it is not the
                    // first time, and the policy is not to defer loading.
                    loadShards();
                }
            }
        }
        else
        {
            // There should always be a non-null atlas in that list, coming from the initial Shard.
            throw new CoreException("{}: There is no data to load for initial shard!",
                    this.getName());
        }
    }

    private boolean areaCovered(final Area area)
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
    private <V extends AtlasEntity, T> Iterable<T> expand(
            final Supplier<Iterable<V>> entitiesSupplier, final Predicate<V> entityCoveredPredicate,
            final Function<V, T> mapper)
    {
        StreamIterable<V> result = Iterables.stream(entitiesSupplier.get())
                .filter(Objects::nonNull);
        while (!entitiesCovered(result, entityCoveredPredicate))
        {
            result = Iterables.stream(entitiesSupplier.get()).filter(Objects::nonNull);
        }
        return result.map(mapper).collect();
    }

    private List<Atlas> getNonNullAtlasShards()
    {
        return this.loadedShards.values().stream().filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean lineItemCovered(final LineItem item)
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

    private boolean loadedShardsfullyGeometricallyEncloseLocation(final Location location)
    {
        return Iterables.stream(this.sharding.shardsCovering(location))
                .allMatch(this.loadedShards::containsKey);
    }

    private boolean loadedShardsfullyGeometricallyEnclosePolygon(final Polygon polygon)
    {
        return Iterables.stream(this.sharding.shards(polygon))
                .allMatch(this.loadedShards::containsKey);
    }

    private boolean loadedShardsfullyGeometricallyEnclosePolyLine(final PolyLine polyLine)
    {
        return Iterables.stream(this.sharding.shardsIntersecting(polyLine))
                .allMatch(this.loadedShards::containsKey);
    }

    private boolean locationItemCovered(final LocationItem item)
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

    private DynamicArea newArea(final Area area)
    {
        return new DynamicArea(this, area.getIdentifier());
    }

    private DynamicEdge newEdge(final Edge edge)
    {
        return new DynamicEdge(this, edge.getIdentifier());
    }

    private DynamicLine newLine(final Line line)
    {
        return new DynamicLine(this, line.getIdentifier());
    }

    private void newLocation(final Location location, final LocationItem... source)
    {
        if (!loadedShardsfullyGeometricallyEncloseLocation(location))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("{}: Triggering new shard load for {}{}", this.getName(),
                        source.length > 0
                                ? "Atlas "
                                        + new StringList(Iterables.stream(Iterables.asList(source))
                                                .map(item -> item.getType() + " "
                                                        + item.getIdentifier())).join(", ")
                                        + " with shape "
                                : "",
                        location.toWkt());
            }
            addNewShards(this.sharding.shardsCovering(location));
        }
    }

    private DynamicNode newNode(final Node node)
    {
        return new DynamicNode(this, node.getIdentifier());
    }

    private DynamicPoint newPoint(final Point point)
    {
        return new DynamicPoint(this, point.getIdentifier());
    }

    private void newPolygon(final Polygon polygon, final AtlasEntity... source)
    {
        if (!loadedShardsfullyGeometricallyEnclosePolygon(polygon))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("{}: Triggering new shard load for {}{}", this.getName(),
                        source.length > 0
                                ? "Atlas "
                                        + new StringList(Iterables.stream(Iterables.asList(source))
                                                .map(item -> item.getType() + " "
                                                        + item.getIdentifier())).join(", ")
                                        + " with shape "
                                : "",
                        polygon.toWkt());
            }
            addNewShards(this.sharding.shards(polygon));
        }
    }

    private void newPolyLine(final PolyLine polyLine, final LineItem... source)
    {
        if (!loadedShardsfullyGeometricallyEnclosePolyLine(polyLine))
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("{}: Triggering new shard load for {}{}", this.getName(),
                        source.length > 0
                                ? "Atlas "
                                        + new StringList(Iterables.stream(Iterables.asList(source))
                                                .map(item -> item.getType() + " "
                                                        + item.getIdentifier())).join(", ")
                                        + " with shape "
                                : "",
                        polyLine.toWkt());
            }
            addNewShards(this.sharding.shardsIntersecting(polyLine));
        }
    }

    private DynamicRelation newRelation(final Relation relation)
    {
        return new DynamicRelation(this, relation.getIdentifier());
    }

    private Set<Shard> nonNullShards()
    {
        return new HashSet<>(this.loadedShards.keySet().stream()
                .filter(shard -> this.loadedShards.get(shard) != null).collect(Collectors.toSet()));
    }

    private boolean relationCovered(final Relation relation)
    {
        final Set<Long> parentRelationIdentifierTree = new HashSet<>();
        parentRelationIdentifierTree.add(relation.getIdentifier());
        return relationCoveredInternal(relation, parentRelationIdentifierTree);
    }

    private boolean relationCoveredInternal(final Relation relation,
            final Set<Long> parentRelationIdentifierTree)
    {
        final RelationMemberList knownMembers = relation.members();
        boolean result = true;
        boolean loop = false;
        for (final RelationMember member : knownMembers)
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
                final long newIdentifier = entity.getIdentifier();
                if (parentRelationIdentifierTree.contains(newIdentifier))
                {
                    logger.error(
                            "Skipping! Unable to expand on relation which has a loop: {}. Parent tree: {}",
                            relation, parentRelationIdentifierTree);
                    loop = true;
                    result = true;
                }
                else
                {
                    final Set<Long> newParentRelationIdentifierTree = new HashSet<>();
                    newParentRelationIdentifierTree.addAll(parentRelationIdentifierTree);
                    newParentRelationIdentifierTree.add(newIdentifier);
                    if (!relationCoveredInternal((Relation) entity,
                            newParentRelationIdentifierTree))
                    {
                        result = false;
                    }
                }
            }
            else
            {
                throw new CoreException("Unknown Relation Member Type: {}",
                        entity.getClass().getName());
            }
        }
        if (this.policy.isAggressivelyExploreRelations() && !loop)
        {
            // Get all the neighboring shards
            final Set<Shard> onlyNeighboringShards = new HashSet<>();
            this.loadedShards.keySet().forEach(
                    shard -> this.sharding.neighbors(shard).forEach(onlyNeighboringShards::add));
            onlyNeighboringShards.removeAll(this.loadedShards.keySet());
            // For each of those shards, load the Atlas individually and find the relation and its
            // members if it is there too.
            final Set<Shard> neighboringShardsContainingRelation = new HashSet<>();
            onlyNeighboringShards
                    .forEach(shard -> this.policy.getAtlasFetcher().apply(shard).ifPresent(atlas ->
                    {
                        final Relation newRelation = atlas.relation(relation.getIdentifier());
                        if (newRelation != null)
                        {
                            final RelationMemberList newMembers = newRelation.members();
                            for (final RelationMember newMember : newMembers)
                            {
                                if (!knownMembers.contains(newMember))
                                {
                                    neighboringShardsContainingRelation.add(shard);
                                    if (logger.isDebugEnabled())
                                    {
                                        logger.debug("{}: Triggering new shard load for {}{}",
                                                this.getName(),
                                                "Atlas " + relation.getType() + " containing ",
                                                newMember);
                                    }
                                    break;
                                }
                            }
                        }
                    }));
            // Add the neighboring shards as new shards to be loaded.
            if (!neighboringShardsContainingRelation.isEmpty())
            {
                result = false;
                addNewShards(neighboringShardsContainingRelation);
            }
        }
        return result;
    }
}
