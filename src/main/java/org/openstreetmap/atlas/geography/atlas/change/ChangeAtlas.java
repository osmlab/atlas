package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.AbstractAtlas;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.validators.AtlasValidator;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;

/**
 * Shallow atlas view that applies a set of change objects and presents the result without updating
 * the whole dataset.
 *
 * @author matthieun
 */
public class ChangeAtlas extends AbstractAtlas // NOSONAR
{
    private static final long serialVersionUID = -5741815439928958165L;
    private static final ChangeRelation NULL_PLACEHOLDER_RELATION = new ChangeRelation(null, null,
            null);
    private static final ChangeNode NULL_PLACEHOLDER_NODE = new ChangeNode(null, null, null);
    private static final ChangeEdge NULL_PLACEHOLDER_EDGE = new ChangeEdge(null, null, null);
    private static final ChangeArea NULL_PLACEHOLDER_AREA = new ChangeArea(null, null, null);
    private static final ChangeLine NULL_PLACEHOLDER_LINE = new ChangeLine(null, null, null);
    private static final ChangePoint NULL_PLACEHOLDER_POINT = new ChangePoint(null, null, null);

    private final Change change;
    private final Atlas source;
    private String name;

    private transient Rectangle bounds;
    private transient AtlasMetaData metaData;
    private transient Long numberOfNodes;
    private transient Long numberOfEdges;
    private transient Long numberOfAreas;
    private transient Long numberOfLines;
    private transient Long numberOfPoints;
    private transient Long numberOfRelations;

    // Computing relations in ChangeAtlas is very expensive, so we cache them here.
    private transient Map<Long, ChangeRelation> relationsCache;
    private transient Object relationsCacheLock = new Object();

    // Computing relations in ChangeAtlas is very expensive, so we cache them here.
    private transient Map<Long, ChangeNode> nodesCache;
    private transient Object nodesCacheLock = new Object();

    // Computing relations in ChangeAtlas is very expensive, so we cache them here.
    private transient Map<Long, ChangeEdge> edgesCache;
    private transient Object edgesCacheLock = new Object();

    // Computing relations in ChangeAtlas is very expensive, so we cache them here.
    private transient Map<Long, ChangeArea> areasCache;
    private transient Object areasCacheLock = new Object();

    // Computing relations in ChangeAtlas is very expensive, so we cache them here.
    private transient Map<Long, ChangeLine> linesCache;
    private transient Object linesCacheLock = new Object();

    // Computing relations in ChangeAtlas is very expensive, so we cache them here.
    private transient Map<Long, ChangePoint> pointsCache;
    private transient Object pointsCacheLock = new Object();

    private static void checkChanges(final Change... changes)
    {
        if (changes == null)
        {
            throw new CoreException("Change cannot be null in a ChangeAtlas.");
        }
        if (changes.length < 1)
        {
            throw new CoreException("ChangeAtlas has to have at least one Change.");
        }
    }

    private static void checkSource(final Atlas source)
    {
        if (source == null)
        {
            throw new CoreException("Source Atlas cannot be null in a ChangeAtlas.");
        }
    }

    public ChangeAtlas(final Atlas source, final Change... changes)
    {
        this(source, "", changes);
    }

    public ChangeAtlas(final Atlas source, final String name, final Change... changes)
    {
        checkSource(source);
        checkChanges(changes);
        this.change = Change.merge(changes);
        this.source = source;
        this.name = name == null || name.isEmpty() ? source.getName() : name;
        new AtlasValidator(this).validate();
    }

    public ChangeAtlas(final Change... changes)
    {
        this("", changes);
    }

    public ChangeAtlas(final String name, final Change... changes)
    {
        checkChanges(changes);
        final Change changeInternal = Change.merge(changes);
        boolean valid = false;
        Atlas sourceInternal = null;
        FeatureChange dummy = null;
        for (final FeatureChange featureChange : changeInternal.getFeatureChanges())
        {
            if (featureChange.getChangeType() == ChangeType.ADD)
            {
                if (!featureChange.afterViewIsFull())
                {
                    throw new CoreException(
                            "ChangeAtlas needs all ADD featureChanges to be full (no partial after view) to exist with no source Atlas.");
                }
                if (sourceInternal == null)
                {
                    final PackedAtlasBuilder builder = new PackedAtlasBuilder();
                    builder.addPoint(-1L, Location.CENTER, Maps.hashMap());
                    sourceInternal = builder.get();
                    dummy = FeatureChange
                            .remove(CompletePoint.shallowFrom(sourceInternal.point(-1L)));
                }
                valid = true;
            }
        }
        if (valid)
        {
            final ChangeBuilder changeBuilder = new ChangeBuilder();
            changeBuilder.addAll(changeInternal.changes());
            changeBuilder.add(dummy);
            this.change = changeBuilder.get();
            this.source = sourceInternal;
            this.name = name == null || name.isEmpty() ? sourceInternal.getName() : name;
            new AtlasValidator(this).validate();
        }
        else
        {
            throw new CoreException(
                    "ChangeAtlas needs at least a full ADD featureChange to exist with no source Atlas.");
        }
    }

    @Override
    public Area area(final long identifier)
    {
        final Supplier<ChangeArea> creator = () -> entityFor(identifier, ItemType.AREA,
                () -> this.source.area(identifier),
                (sourceEntity, overrideEntity) -> new ChangeArea(this, (Area) sourceEntity,
                        (Area) overrideEntity));
        return getFromCacheOrCreate(this.areasCache, cache -> this.areasCache = cache,
                this.areasCacheLock, NULL_PLACEHOLDER_AREA, identifier, creator);
    }

    @Override
    public Iterable<Area> areas()
    {
        return entitiesFor(ItemType.AREA, this::area, this.source.areas());
    }

    @Override
    public synchronized Rectangle bounds()
    {
        if (this.bounds == null)
        {
            // Stream it to make sure the "Iterable" signature is used here (vs. Located, which
            // would stack overflow).
            this.bounds = Rectangle.forLocated(Iterables.stream(this));
        }
        return this.bounds;
    }

    @Override
    public Edge edge(final long identifier)
    {
        /*
         * If the edge was not found in this atlas, return null. Additionally, we then check to see
         * if this edge is missing a start or end node (which may have been removed by a
         * FeatureChange). In this case, we also want to "remove" the edge by returning null.
         */
        final Predicate<ChangeEdge> nullableEdge = edge -> edge.start() == null
                || edge.end() == null;

        final Supplier<ChangeEdge> creator = () -> entityFor(identifier, ItemType.EDGE,
                () -> this.source.edge(identifier),
                (sourceEntity, overrideEntity) -> new ChangeEdge(this, (Edge) sourceEntity,
                        (Edge) overrideEntity));

        return getFromCacheOrCreate(this.edgesCache, cache -> this.edgesCache = cache,
                this.edgesCacheLock, NULL_PLACEHOLDER_EDGE, identifier, creator,
                Optional.of(nullableEdge));
    }

    @Override
    public Iterable<Edge> edges()
    {
        return entitiesFor(ItemType.EDGE, this::edge, this.source.edges());
    }

    @Override
    public String getName()
    {
        if (this.name == null)
        {
            return super.getName();
        }
        return this.name;
    }

    @Override
    public Line line(final long identifier)
    {
        final Supplier<ChangeLine> creator = () -> entityFor(identifier, ItemType.LINE,
                () -> this.source.line(identifier),
                (sourceEntity, overrideEntity) -> new ChangeLine(this, (Line) sourceEntity,
                        (Line) overrideEntity));
        return getFromCacheOrCreate(this.linesCache, cache -> this.linesCache = cache,
                this.linesCacheLock, NULL_PLACEHOLDER_LINE, identifier, creator);
    }

    @Override
    public Iterable<Line> lines()
    {
        return entitiesFor(ItemType.LINE, this::line, this.source.lines());
    }

    @Override
    public synchronized AtlasMetaData metaData()
    {
        if (this.metaData == null)
        {
            AtlasMetaData sourceMetaData = this.source.metaData();
            if (sourceMetaData == null)
            {
                sourceMetaData = new AtlasMetaData();
            }
            final AtlasSize size = new AtlasSize(this);
            this.metaData = sourceMetaData.copyWithNewSize(size).copyWithNewOriginal(false);
        }
        return this.metaData;
    }

    @Override
    public Node node(final long identifier)
    {
        final Supplier<ChangeNode> creator = () -> entityFor(identifier, ItemType.NODE,
                () -> this.source.node(identifier),
                (sourceEntity, overrideEntity) -> new ChangeNode(this, (Node) sourceEntity,
                        (Node) overrideEntity));
        return getFromCacheOrCreate(this.nodesCache, cache -> this.nodesCache = cache,
                this.nodesCacheLock, NULL_PLACEHOLDER_NODE, identifier, creator);
    }

    @Override
    public Iterable<Node> nodes()
    {
        return entitiesFor(ItemType.NODE, this::node, this.source.nodes());
    }

    @Override
    public synchronized long numberOfAreas()
    {
        if (this.numberOfAreas == null)
        {
            this.numberOfAreas = Iterables.size(areas());
        }
        return this.numberOfAreas;
    }

    @Override
    public synchronized long numberOfEdges()
    {
        if (this.numberOfEdges == null)
        {
            this.numberOfEdges = Iterables.size(edges());
        }
        return this.numberOfEdges;
    }

    @Override
    public synchronized long numberOfLines()
    {
        if (this.numberOfLines == null)
        {
            this.numberOfLines = Iterables.size(lines());
        }
        return this.numberOfLines;
    }

    @Override
    public synchronized long numberOfNodes()
    {
        if (this.numberOfNodes == null)
        {
            this.numberOfNodes = Iterables.size(nodes());
        }
        return this.numberOfNodes;
    }

    @Override
    public synchronized long numberOfPoints()
    {
        if (this.numberOfPoints == null)
        {
            this.numberOfPoints = Iterables.size(points());
        }
        return this.numberOfPoints;
    }

    @Override
    public synchronized long numberOfRelations()
    {
        if (this.numberOfRelations == null)
        {
            this.numberOfRelations = Iterables.size(relations());
        }
        return this.numberOfRelations;
    }

    @Override
    public Point point(final long identifier)
    {
        final Supplier<ChangePoint> creator = () -> entityFor(identifier, ItemType.POINT,
                () -> this.source.point(identifier),
                (sourceEntity, overrideEntity) -> new ChangePoint(this, (Point) sourceEntity,
                        (Point) overrideEntity));
        return getFromCacheOrCreate(this.pointsCache, cache -> this.pointsCache = cache,
                this.pointsCacheLock, NULL_PLACEHOLDER_POINT, identifier, creator);
    }

    @Override
    public Iterable<Point> points()
    {
        return entitiesFor(ItemType.POINT, this::point, this.source.points());
    }

    @Override
    public Relation relation(final long identifier)
    {
        /*
         * If the relation was not found in this atlas, return null. Additionally, we check to see
         * if the relation has no members. If so, it is considered empty and is dropped from the
         * atlas. This logic, combined with the logic in ChangeRelation.membersFor, will
         * automatically handle removing non-empty but shallow relations as well.
         */
        final Predicate<ChangeRelation> nullableRelation = relationCandidate -> relationCandidate
                .members().isEmpty();

        final Supplier<ChangeRelation> creator = () -> entityFor(identifier, ItemType.RELATION,
                () -> this.source.relation(identifier),
                (sourceEntity, overrideEntity) -> new ChangeRelation(this, (Relation) sourceEntity,
                        (Relation) overrideEntity));

        return getFromCacheOrCreate(this.relationsCache, cache -> this.relationsCache = cache,
                this.relationsCacheLock, NULL_PLACEHOLDER_RELATION, identifier, creator,
                Optional.of(nullableRelation));
    }

    @Override
    public Iterable<Relation> relations()
    {
        return entitiesFor(ItemType.RELATION, this::relation, this.source.relations());
    }

    public ChangeAtlas withName(final String name)
    {
        this.name = name;
        return this;
    }

    /**
     * Get the {@link Iterable} of entities corresponding to the right type. This takes care of
     * surfacing only the ones not deleted, or if added or modified, the new ones.
     *
     * @param <M>
     *            The {@link AtlasEntity} subclass.
     * @param itemType
     *            The type of entity
     * @param entityForIdentifier
     *            A function that creates a new object from its identifier.
     * @param sourceEntities
     *            All the corresponding entities from the source atlas.
     * @return All the corresponding entities in this atlas.
     */
    private <M extends AtlasEntity> Iterable<M> entitiesFor(final ItemType itemType,
            final LongFunction<M> entityForIdentifier, final Iterable<M> sourceEntities)
    {
        return new MultiIterable<>(
                this.change.getFeatureChanges().stream()
                        .filter(featureChange -> featureChange.getItemType() == itemType
                                && featureChange.getChangeType() == ChangeType.ADD)
                        .map(featureChange -> entityForIdentifier
                                .apply(featureChange.getIdentifier()))
                        .filter(Objects::nonNull).collect(Collectors.toList()),
                Iterables.stream(sourceEntities)
                        .filter(entity -> !this.change.changeFor(itemType, entity.getIdentifier())
                                .isPresent())
                        .map(entity -> entityForIdentifier.apply(entity.getIdentifier()))
                        .filter(Objects::nonNull).collect());
    }

    /**
     * Build a "Change" feature for this {@link ChangeAtlas} by querying the change object for
     * matching features. Use the source atlas otherwise.
     *
     * @param <M>
     *            The type of the feature to be built. Has to extend {@link AtlasEntity}.
     * @param identifier
     *            The feature identifier
     * @param itemType
     *            The feature type
     * @param sourceSupplier
     *            A supplier function that creates the entity from the source. Can return null if
     *            the source atlas does not contain that feature.
     * @param entityConstructorFromSource
     *            A function that takes the updated feature from the {@link Change} object, and
     *            constructs a new ChangeItem from it, that attaches to this Atlas.
     * @return The ChangeItem that corresponds to that feature. Can be a ChangeNode, ChangeEdge,
     *         etc. It links back to this Atlas.
     */
    private <M extends AtlasEntity> M entityFor(final long identifier, final ItemType itemType,
            final Supplier<AtlasEntity> sourceSupplier,
            final BiFunction<AtlasEntity, AtlasEntity, M> entityConstructorFromSource)
    {
        final Optional<FeatureChange> itemChangeOption = this.change.changeFor(itemType,
                identifier);
        final AtlasEntity sourceItem = sourceSupplier.get();
        if (itemChangeOption.isPresent())
        {
            // That Entity is affected by a change
            final FeatureChange itemChange = itemChangeOption.get();
            if (ChangeType.REMOVE == itemChange.getChangeType())
            {
                return null;
            }
            else
            {
                // Create the ChangeItem from the change object (the override). The source item
                // might be null (In case of an ADD which is a create and not a modify)
                return entityConstructorFromSource.apply(sourceItem, itemChange.getAfterView());
            }
        }
        else
        {
            if (sourceItem != null)
            {
                // Create the ChangeItem from the untouched source; the override is null
                return entityConstructorFromSource.apply(sourceItem, null);
            }
        }
        return null;
    }

    private <E> E getFromCacheOrCreate(final Map<Long, E> cache,
            final Consumer<Map<Long, E>> cacheSetter, final Object lock, final E nullPlaceholder,
            final Long identifier, final Supplier<E> creator)
    {
        return getFromCacheOrCreate(cache, cacheSetter, lock, nullPlaceholder, identifier, creator,
                Optional.empty());
    }

    /**
     * @param <E>
     *            The type of the entity returned. Intended to be a {@link ChangeArea},
     *            {@link ChangeNode}, etc.
     * @param cache
     *            The cache to use to retrieve the entity
     * @param cacheSetter
     *            A function that will set the cache not null in case it was null.
     * @param lock
     *            The synchronization lock used for that specific type
     * @param nullPlaceholder
     *            What placeholder in the cache specifies a null object at some identifier
     * @param identifier
     *            The identifier to return
     * @param creator
     *            A {@link Supplier} that provides the correct object for the specified identifier
     *            above
     * @param entityNullable
     *            A predicate that decides if a non null object should still return null. Example a
     *            relation with no members.
     * @return
     */
    private <E> E getFromCacheOrCreate(final Map<Long, E> cache,
            final Consumer<Map<Long, E>> cacheSetter, final Object lock, final E nullPlaceholder,
            final Long identifier, final Supplier<E> creator,
            final Optional<Predicate<E>> entityNullable)
    {
        // Get or create the cache (in case it was null)
        ChangeEntity.getOrCreateCache(cache, cacheSetter, lock, ConcurrentHashMap::new);
        E result;
        if (cache.containsKey(identifier))
        {
            // Retrieve an existing object
            result = cache.get(identifier);
            result = result == nullPlaceholder ? null : result;
        }
        else
        {
            // Create a new object
            result = creator.get();
            if (result == null || entityNullable.isPresent() && entityNullable.get().test(result))
            {
                // If the created object is null, or nullable, use the null placeholder
                cache.put(identifier, nullPlaceholder);
                result = null;
            }
            else
            {
                cache.put(identifier, result);
            }
        }
        return result;
    }

}
