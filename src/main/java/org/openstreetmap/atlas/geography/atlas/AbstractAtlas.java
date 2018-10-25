package org.openstreetmap.atlas.geography.atlas;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.pbf.OsmPbfLoader;
import org.openstreetmap.atlas.geography.index.PackedSpatialIndex;
import org.openstreetmap.atlas.geography.index.RTree;
import org.openstreetmap.atlas.geography.index.SpatialIndex;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of {@link Atlas} that covers common methods.
 *
 * @author matthieun
 * @author tony
 * @author mgostintsev
 */
public abstract class AbstractAtlas extends BareAtlas
{
    private static final long serialVersionUID = -1408393006815178776L;
    private static final Logger logger = LoggerFactory.getLogger(AbstractAtlas.class);

    protected static final long DEFAULT_NUMBER_OF_ITEMS = 1024;
    protected static final int HASH_MODULO_RATIO = 10;

    // Spatial index lock objects for thread protection. Even though it looks not necessary, those
    // locks are static to avoid issues when deserializing Atlas files. If non static, they might
    // end up being null in some cases right after deserialization.
    private static final Object NODE_LOCK = new Object();
    private static final Object EDGE_LOCK = new Object();
    private static final Object AREA_LOCK = new Object();
    private static final Object LINE_LOCK = new Object();
    private static final Object POINT_LOCK = new Object();
    private static final Object RELATION_LOCK = new Object();

    // Spatial indices
    // Transient: Those are not serialized, and re-generated on the fly
    // Volatile: This is to allow double checked locking to be safe in the
    // this.buildXXXXXSpatialIndexIfNecessary() methods.
    // http://www.cs.umd.edu/~pugh/java/memoryModel/DoubleCheckedLocking.html
    // See: "Fixing Double-Checked Locking using Volatile"
    private transient volatile SpatialIndex<Node> nodeSpatialIndex;
    private transient volatile SpatialIndex<Edge> edgeSpatialIndex;
    private transient volatile SpatialIndex<Area> areaSpatialIndex;
    private transient volatile SpatialIndex<Line> lineSpatialIndex;
    private transient volatile SpatialIndex<Point> pointSpatialIndex;
    private transient volatile SpatialIndex<Relation> relationSpatialIndex;

    /**
     * Create an {@link Atlas} from an OSM protobuf and save it to a resource.
     *
     * @param osmPbf
     *            The OSM protobuf
     * @param atlasResource
     *            The {@link WritableResource} to save the {@link Atlas} to
     * @return The created {@link Atlas}
     */
    public static Atlas createAndSaveOsmPbf(final Resource osmPbf,
            final WritableResource atlasResource)
    {
        final OsmPbfLoader loader = new OsmPbfLoader(osmPbf);
        loader.saveAtlas(atlasResource);
        return loader.read();
    }

    /**
     * Create an {@link Atlas} from an OSM protobuf that has already been sliced and save it to a
     * resource
     *
     * @param osmPbf
     *            The OSM protobuf
     * @param atlasResource
     *            The {@link WritableResource} to save the {@link Atlas} to
     * @return The created {@link Atlas}
     */
    public static Atlas createAndSaveOsmPbfWithSlicing(final Resource osmPbf,
            final WritableResource atlasResource)
    {
        final OsmPbfLoader loader = new OsmPbfLoader(osmPbf,
                AtlasLoadingOption.createOptionWithAllEnabled(null));
        loader.saveAtlas(atlasResource);
        return loader.read();
    }

    /**
     * Create from an OSM protobuf resource
     *
     * @param resource
     *            The OSM protobuf resource
     * @return The Atlas read from the pbf
     */
    public static Atlas forOsmPbf(final Resource resource)
    {
        final OsmPbfLoader loader = new OsmPbfLoader(resource);
        return loader.read();
    }

    @Override
    public Iterable<Area> areasCovering(final Location location)
    {
        final Iterable<Area> areas = this.getAreaSpatialIndex().get(location.bounds());
        return Iterables.stream(areas).filter(area ->
        {
            final Polygon areaPolygon = area.asPolygon();
            return areaPolygon.fullyGeometricallyEncloses(location);
        });
    }

    @Override
    public Iterable<Area> areasCovering(final Location location, final Predicate<Area> matcher)
    {
        return Iterables.filter(areasCovering(location), matcher);
    }

    @Override
    public Iterable<Area> areasIntersecting(final Polygon polygon)
    {
        final Iterable<Area> areas = this.getAreaSpatialIndex().get(polygon.bounds());
        return Iterables.filter(areas, area ->
        {
            final Polygon areaPolygon = area.asPolygon();
            return polygon.overlaps(areaPolygon);
        });
    }

    @Override
    public Iterable<Area> areasIntersecting(final Polygon polygon, final Predicate<Area> matcher)
    {
        return Iterables.filterTranslate(areasIntersecting(polygon), item -> item, matcher);
    }

    @Override
    public Iterable<Edge> edgesContaining(final Location location)
    {
        final Iterable<Edge> edges = this.getEdgeSpatialIndex().get(location.bounds());
        return Iterables.filter(edges, edge ->
        {
            final PolyLine polyline = edge.asPolyLine();
            return polyline.contains(location);
        });
    }

    @Override
    public Iterable<Edge> edgesContaining(final Location location, final Predicate<Edge> matcher)
    {
        return Iterables.filter(edgesContaining(location), matcher);
    }

    @Override
    public Iterable<Edge> edgesIntersecting(final Polygon polygon)
    {
        final Iterable<Edge> edges = this.getEdgeSpatialIndex().get(polygon.bounds());
        return Iterables.filter(edges, edge ->
        {
            final PolyLine polyline = edge.asPolyLine();
            return polygon.overlaps(polyline);
        });
    }

    @Override
    public Iterable<Edge> edgesIntersecting(final Polygon polygon, final Predicate<Edge> matcher)
    {
        return Iterables.filter(edgesIntersecting(polygon), matcher);
    }

    public SpatialIndex<Area> getAreaSpatialIndex()
    {
        buildAreaSpatialIndexIfNecessary();
        return this.areaSpatialIndex;
    }

    public SpatialIndex<Edge> getEdgeSpatialIndex()
    {
        buildEdgeSpatialIndexIfNecessary();
        return this.edgeSpatialIndex;
    }

    public SpatialIndex<Line> getLineSpatialIndex()
    {
        buildLineSpatialIndexIfNecessary();
        return this.lineSpatialIndex;
    }

    public SpatialIndex<Node> getNodeSpatialIndex()
    {
        buildNodeSpatialIndexIfNecessary();
        return this.nodeSpatialIndex;
    }

    public SpatialIndex<Point> getPointSpatialIndex()
    {
        buildPointSpatialIndexIfNecessary();
        return this.pointSpatialIndex;
    }

    public SpatialIndex<Relation> getRelationSpatialIndex()
    {
        buildRelationSpatialIndexIfNecessary();
        return this.relationSpatialIndex;
    }

    @Override
    public Iterable<Line> linesContaining(final Location location)
    {
        final Iterable<Line> lines = this.getLineSpatialIndex().get(location.bounds());
        return Iterables.filter(lines, line ->
        {
            final PolyLine polyline = line.asPolyLine();
            return polyline.contains(location);
        });
    }

    @Override
    public Iterable<Line> linesContaining(final Location location, final Predicate<Line> matcher)
    {
        return Iterables.filter(linesContaining(location), matcher);
    }

    @Override
    public Iterable<Line> linesIntersecting(final Polygon polygon)
    {
        final Iterable<Line> lines = this.getLineSpatialIndex().get(polygon.bounds());
        return Iterables.filter(lines, line ->
        {
            final PolyLine polyline = line.asPolyLine();
            return polygon.overlaps(polyline);
        });
    }

    @Override
    public Iterable<Line> linesIntersecting(final Polygon polygon, final Predicate<Line> matcher)
    {
        return Iterables.filter(linesIntersecting(polygon), matcher);
    }

    @Override
    public Iterable<Node> nodesAt(final Location location)
    {
        return this.getNodeSpatialIndex().get(location.bounds());
    }

    @Override
    public Iterable<Node> nodesWithin(final Polygon polygon)
    {
        final Iterable<Node> nodes = this.getNodeSpatialIndex().get(polygon.bounds());
        if (polygon instanceof Rectangle)
        {
            return nodes;
        }
        return Iterables.filter(nodes,
                node -> polygon.fullyGeometricallyEncloses(node.getLocation()));
    }

    @Override
    public Iterable<Node> nodesWithin(final Polygon polygon, final Predicate<Node> matcher)
    {
        return Iterables.filter(nodesWithin(polygon), matcher);
    }

    @Override
    public Iterable<Point> pointsAt(final Location location)
    {
        return this.getPointSpatialIndex().get(location.bounds());
    }

    @Override
    public Iterable<Point> pointsWithin(final Polygon polygon)
    {
        final Iterable<Point> points = this.getPointSpatialIndex().get(polygon.bounds());
        if (polygon instanceof Rectangle)
        {
            return points;
        }
        return Iterables.filter(points,
                point -> polygon.fullyGeometricallyEncloses(point.getLocation()));
    }

    @Override
    public Iterable<Point> pointsWithin(final Polygon polygon, final Predicate<Point> matcher)
    {
        return Iterables.filterTranslate(pointsWithin(polygon), item -> item, matcher);
    }

    @Override
    public Iterable<Relation> relationsWithEntitiesIntersecting(final Polygon polygon)
    {
        final Iterable<Relation> relations = this.getRelationSpatialIndex().get(polygon.bounds());
        return Iterables.filter(relations, relation -> relation.intersects(polygon));
    }

    @Override
    public Iterable<Relation> relationsWithEntitiesIntersecting(final Polygon polygon,
            final Predicate<Relation> matcher)
    {
        return Iterables.filter(relationsWithEntitiesIntersecting(polygon), matcher);
    }

    @Override
    public void save(final WritableResource writableResource)
    {
        throw new CoreException("{} does not support saving. Consider using {} instead.",
                this.getClass().getName(), PackedAtlas.class.getName());
    }

    /**
     * This method is useful for de-serialized Atlases. When an Atlas is serialized, the indices are
     * not saved (transient). When de-serialized, they will be null, until this method is called.
     */
    protected void buildAreaSpatialIndexIfNecessary()
    {
        buildSpatialIndexIfNecessary(AREA_LOCK, ItemType.AREA, this::newAreaSpatialIndex,
                () -> this.areaSpatialIndex,
                newSpatialIndex -> this.areaSpatialIndex = newSpatialIndex);
    }

    /**
     * This method is useful for de-serialized Atlases. When an Atlas is serialized, the indices are
     * not saved (transient). When de-serialized, they will be null, until this method is called.
     */
    protected void buildEdgeSpatialIndexIfNecessary()
    {
        buildSpatialIndexIfNecessary(EDGE_LOCK, ItemType.EDGE, this::newEdgeSpatialIndex,
                () -> this.edgeSpatialIndex,
                newSpatialIndex -> this.edgeSpatialIndex = newSpatialIndex);
    }

    /**
     * This method is useful for de-serialized Atlases. When an Atlas is serialized, the indices are
     * not saved (transient). When de-serialized, they will be null, until this method is called.
     */
    protected void buildLineSpatialIndexIfNecessary()
    {
        buildSpatialIndexIfNecessary(LINE_LOCK, ItemType.LINE, this::newLineSpatialIndex,
                () -> this.lineSpatialIndex,
                newSpatialIndex -> this.lineSpatialIndex = newSpatialIndex);
    }

    /**
     * This method is useful for de-serialized Atlases. When an Atlas is serialized, the indices are
     * not saved (transient). When de-serialized, they will be null, until this method is called.
     */
    protected void buildNodeSpatialIndexIfNecessary()
    {
        buildSpatialIndexIfNecessary(NODE_LOCK, ItemType.NODE, this::newNodeSpatialIndex,
                () -> this.nodeSpatialIndex,
                newSpatialIndex -> this.nodeSpatialIndex = newSpatialIndex);
    }

    /**
     * This method is useful for de-serialized Atlases. When an Atlas is serialized, the indices are
     * not saved (transient). When de-serialized, they will be null, until this method is called.
     */
    protected void buildPointSpatialIndexIfNecessary()
    {
        buildSpatialIndexIfNecessary(POINT_LOCK, ItemType.POINT, this::newPointSpatialIndex,
                () -> this.pointSpatialIndex,
                newSpatialIndex -> this.pointSpatialIndex = newSpatialIndex);
    }

    /**
     * This method is useful for de-serialized Atlases. When an Atlas is serialized, the indices are
     * not saved (transient). When de-serialized, they will be null, until this method is called.
     */
    protected void buildRelationSpatialIndexIfNecessary()
    {
        buildSpatialIndexIfNecessary(RELATION_LOCK, ItemType.RELATION,
                this::newRelationSpatialIndex, () -> this.relationSpatialIndex,
                newSpatialIndex -> this.relationSpatialIndex = newSpatialIndex);
    }

    /**
     * @return The spatial index as new (meaning empty). This has to be used only in the protected
     *         constructors, to not conflict with the thread safe methods that re-build spatial
     *         indices as needed.
     */
    protected SpatialIndex<Area> getAsNewAreaSpatialIndex()
    {
        if (this.areaSpatialIndex == null)
        {
            this.areaSpatialIndex = newAreaSpatialIndex();
        }
        return this.areaSpatialIndex;
    }

    /**
     * @return The spatial index as new (meaning empty). This has to be used only in the protected
     *         constructors, to not conflict with the thread safe methods that re-build spatial
     *         indices as needed.
     */
    protected SpatialIndex<Edge> getAsNewEdgeSpatialIndex()
    {
        if (this.edgeSpatialIndex == null)
        {
            this.edgeSpatialIndex = newEdgeSpatialIndex();
        }
        return this.edgeSpatialIndex;
    }

    /**
     * @return The spatial index as new (meaning empty). This has to be used only in the protected
     *         constructors, to not conflict with the thread safe methods that re-build spatial
     *         indices as needed.
     */
    protected SpatialIndex<Line> getAsNewLineSpatialIndex()
    {
        if (this.lineSpatialIndex == null)
        {
            this.lineSpatialIndex = newLineSpatialIndex();
        }
        return this.lineSpatialIndex;
    }

    /**
     * @return The spatial index as new (meaning empty). This has to be used only in the protected
     *         constructors, to not conflict with the thread safe methods that re-build spatial
     *         indices as needed.
     */
    protected SpatialIndex<Node> getAsNewNodeSpatialIndex()
    {
        if (this.nodeSpatialIndex == null)
        {
            this.nodeSpatialIndex = newNodeSpatialIndex();
        }
        return this.nodeSpatialIndex;
    }

    /**
     * @return The spatial index as new (meaning empty). This has to be used only in the protected
     *         constructors, to not conflict with the thread safe methods that re-build spatial
     *         indices as needed.
     */
    protected SpatialIndex<Point> getAsNewPointSpatialIndex()
    {
        if (this.pointSpatialIndex == null)
        {
            this.pointSpatialIndex = newPointSpatialIndex();
        }
        return this.pointSpatialIndex;
    }

    /**
     * @return The spatial index as new (meaning empty). This has to be used only in the protected
     *         constructors, to not conflict with the thread safe methods that re-build spatial
     *         indices as needed.
     */
    protected SpatialIndex<Relation> getAsNewRelationSpatialIndex()
    {
        if (this.relationSpatialIndex == null)
        {
            this.relationSpatialIndex = newRelationSpatialIndex();
        }
        return this.relationSpatialIndex;
    }

    /**
     * Implementation of double-checked locking with volatile global variable as suggested by sonar
     *
     * @see "https://rules.sonarsource.com/java/tag/multi-threading/RSPEC-2168"
     * @param lock
     *            An object to lock on. Needs to be a global static variable.
     * @param type
     *            The type of the Spatial Index object to create
     * @param newIndexSupplier
     *            A function that returns a new built-out index
     * @param globalIndexSupplier
     *            A function that returns the existing global index
     * @param globalIndexConsumer
     *            A function that resets the existing global index
     */
    @SuppressWarnings("unchecked")
    private <M extends AtlasEntity> void buildSpatialIndexIfNecessary(final Object lock,
            final ItemType type, final Supplier<SpatialIndex<M>> newIndexSupplier,
            final Supplier<SpatialIndex<M>> globalIndexSupplier,
            final Consumer<SpatialIndex<M>> globalIndexConsumer)
    {
        SpatialIndex<M> localIndex = globalIndexSupplier.get();
        if (localIndex == null)
        {
            // Here lock is a global static variable. Sonar cannot see it here, hence the trailing
            // comment.
            synchronized (lock) // NOSONAR
            {
                localIndex = globalIndexSupplier.get();
                if (localIndex == null)
                {
                    logger.info("Re-Building {} Spatial Index...", type);
                    final SpatialIndex<M> temporaryIndex = newIndexSupplier.get();
                    Iterables.stream(this.entities(type, type.getMemberClass()))
                            .map(entity -> (M) entity).forEach(temporaryIndex::add);
                    globalIndexConsumer.accept(temporaryIndex);
                }
            }
        }
    }

    /**
     * Create a new spatial index
     *
     * @return A newly created spatial index
     */
    private SpatialIndex<Area> newAreaSpatialIndex()
    {
        return newSpatialIndex((item, bounds) -> bounds.overlaps(item.asPolygon()), this::area);
    }

    /**
     * Create a new spatial index
     *
     * @return A newly created spatial index
     */
    private SpatialIndex<Edge> newEdgeSpatialIndex()
    {
        return newSpatialIndex((item, bounds) -> bounds.overlaps(item.asPolyLine()), this::edge);
    }

    /**
     * Create a new spatial index
     *
     * @return A newly created spatial index
     */
    private SpatialIndex<Line> newLineSpatialIndex()
    {
        return newSpatialIndex((item, bounds) -> bounds.overlaps(item.asPolyLine()), this::line);
    }

    /**
     * Create a new spatial index
     *
     * @return A newly created spatial index
     */
    private SpatialIndex<Node> newNodeSpatialIndex()
    {
        return newSpatialIndex((item, bounds) -> bounds.fullyGeometricallyEncloses(item),
                this::node);
    }

    /**
     * Create a new spatial index
     *
     * @return A newly created spatial index
     */
    private SpatialIndex<Point> newPointSpatialIndex()
    {
        return newSpatialIndex((item, bounds) -> bounds.fullyGeometricallyEncloses(item),
                this::point);
    }

    /**
     * Create a new spatial index
     *
     * @return A newly created spatial index
     */
    private SpatialIndex<Relation> newRelationSpatialIndex()
    {
        return newSpatialIndex((item, bounds) -> item.intersects(bounds), this::relation);
    }

    /**
     * @param memberValidForBounds
     *            A function that decides if a member is included in bounds or not.
     * @param memberFromIdentifier
     *            A function that re-builds a member from its identifier.
     * @return A {@link SpatialIndex} tailored to the specified type
     */
    private <M extends AtlasEntity> SpatialIndex<M> newSpatialIndex(
            final BiFunction<M, Rectangle, Boolean> memberValidForBounds,
            final Function<Long, M> memberFromIdentifier)
    {
        return new PackedSpatialIndex<M, Long>(new RTree<>())
        {
            private static final long serialVersionUID = 6569644967280192054L;

            @Override
            protected Long compress(final M item)
            {
                return item.getIdentifier();
            }

            @Override
            protected boolean isValid(final M item, final Rectangle bounds)
            {
                return memberValidForBounds.apply(item, bounds);
            }

            @Override
            protected M restore(final Long packed)
            {
                return memberFromIdentifier.apply(packed);
            }
        };
    }
}
