package org.openstreetmap.atlas.geography.atlas;

import java.util.function.Predicate;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
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
    private transient SpatialIndex<Node> nodeSpatialIndex;
    private transient SpatialIndex<Edge> edgeSpatialIndex;
    private transient SpatialIndex<Area> areaSpatialIndex;
    private transient SpatialIndex<Line> lineSpatialIndex;
    private transient SpatialIndex<Point> pointSpatialIndex;
    private transient SpatialIndex<Relation> relationSpatialIndex;

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
        return this.getAreaSpatialIndex().get(location.bounds());
    }

    @Override
    public Iterable<Area> areasCovering(final Location location, final Predicate<Area> matcher)
    {
        return Iterables.filter(this.getAreaSpatialIndex().get(location.bounds()), matcher);
    }

    @Override
    public Iterable<Area> areasIntersecting(final Polygon polygon)
    {
        final Iterable<Area> areas = this.getAreaSpatialIndex().get(polygon.bounds());
        if (polygon instanceof Rectangle)
        {
            return areas;
        }
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
            return location.bounds().overlaps(polyline);
        });
    }

    @Override
    public Iterable<Edge> edgesContaining(final Location location, final Predicate<Edge> matcher)
    {
        return Iterables.filter(this.getEdgeSpatialIndex().get(location.bounds()), matcher);
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
            return location.bounds().overlaps(polyline);
        });
    }

    @Override
    public Iterable<Line> linesContaining(final Location location, final Predicate<Line> matcher)
    {
        return Iterables.filter(this.getLineSpatialIndex().get(location.bounds()), matcher);
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
        return Iterables.filter(relations, relation ->
        {
            return relation.intersects(polygon);
        });
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
        if (this.areaSpatialIndex == null)
        {
            synchronized (AREA_LOCK)
            {
                if (this.areaSpatialIndex == null)
                {
                    getLogger().info("Re-Building Area Spatial Index...");
                    // Use a temporary index so the check above cannot be compromised.
                    final SpatialIndex<Area> temporaryIndex = newAreaSpatialIndex();
                    areas().forEach(area -> temporaryIndex.add(area));
                    this.areaSpatialIndex = temporaryIndex;
                }
            }
        }
    }

    /**
     * This method is useful for de-serialized Atlases. When an Atlas is serialized, the indices are
     * not saved (transient). When de-serialized, they will be null, until this method is called.
     */
    protected void buildEdgeSpatialIndexIfNecessary()
    {
        if (this.edgeSpatialIndex == null)
        {
            synchronized (EDGE_LOCK)
            {
                if (this.edgeSpatialIndex == null)
                {
                    getLogger().info("Re-Building Edge Spatial Index...");
                    // Use a temporary index so the check above cannot be compromised.
                    final SpatialIndex<Edge> temporaryIndex = newEdgeSpatialIndex();
                    edges().forEach(edge -> temporaryIndex.add(edge));
                    this.edgeSpatialIndex = temporaryIndex;
                }
            }
        }
    }

    /**
     * This method is useful for de-serialized Atlases. When an Atlas is serialized, the indices are
     * not saved (transient). When de-serialized, they will be null, until this method is called.
     */
    protected void buildLineSpatialIndexIfNecessary()
    {
        if (this.lineSpatialIndex == null)
        {
            synchronized (LINE_LOCK)
            {
                if (this.lineSpatialIndex == null)
                {
                    getLogger().info("Re-Building Line Spatial Index...");
                    // Use a temporary index so the check above cannot be compromised.
                    final SpatialIndex<Line> temporaryIndex = newLineSpatialIndex();
                    lines().forEach(line -> temporaryIndex.add(line));
                    this.lineSpatialIndex = temporaryIndex;
                }
            }
        }
    }

    /**
     * This method is useful for de-serialized Atlases. When an Atlas is serialized, the indices are
     * not saved (transient). When de-serialized, they will be null, until this method is called.
     */
    protected void buildNodeSpatialIndexIfNecessary()
    {
        if (this.nodeSpatialIndex == null)
        {
            synchronized (NODE_LOCK)
            {
                if (this.nodeSpatialIndex == null)
                {
                    getLogger().info("Re-Building Node Spatial Index...");
                    // Use a temporary index so the check above cannot be compromised.
                    final SpatialIndex<Node> temporaryIndex = newNodeSpatialIndex();
                    nodes().forEach(node -> temporaryIndex.add(node));
                    this.nodeSpatialIndex = temporaryIndex;
                }
            }
        }
    }

    /**
     * This method is useful for de-serialized Atlases. When an Atlas is serialized, the indices are
     * not saved (transient). When de-serialized, they will be null, until this method is called.
     */
    protected void buildPointSpatialIndexIfNecessary()
    {
        if (this.pointSpatialIndex == null)
        {
            synchronized (POINT_LOCK)
            {
                if (this.pointSpatialIndex == null)
                {
                    getLogger().info("Re-Building Point Spatial Index...");
                    // Use a temporary index so the check above cannot be compromised.
                    final SpatialIndex<Point> temporaryIndex = newPointSpatialIndex();
                    points().forEach(point -> temporaryIndex.add(point));
                    this.pointSpatialIndex = temporaryIndex;
                }
            }
        }
    }

    /**
     * This method is useful for de-serialized Atlases. When an Atlas is serialized, the indices are
     * not saved (transient). When de-serialized, they will be null, until this method is called.
     */
    protected void buildRelationSpatialIndexIfNecessary()
    {
        if (this.relationSpatialIndex == null)
        {
            synchronized (RELATION_LOCK)
            {
                if (this.relationSpatialIndex == null)
                {
                    getLogger().info("Re-Building Relation Spatial Index...");
                    // Use a temporary index so the check above cannot be compromised.
                    final SpatialIndex<Relation> temporaryIndex = newRelationSpatialIndex();
                    relations().forEach(relation -> temporaryIndex.add(relation));
                    this.relationSpatialIndex = temporaryIndex;
                }
            }
        }
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

    protected abstract Logger getLogger();

    /**
     * Create a new spatial index
     *
     * @return A newly created spatial index
     */
    private SpatialIndex<Area> newAreaSpatialIndex()
    {
        return new PackedSpatialIndex<Area, Long>(new RTree<>())
        {
            private static final long serialVersionUID = 6569644967280192054L;

            @Override
            protected Long compress(final Area item)
            {
                return item.getIdentifier();
            }

            @Override
            protected boolean isValid(final Area item, final Rectangle bounds)
            {
                return bounds.overlaps(item.asPolygon());
            }

            @Override
            protected Area restore(final Long packed)
            {
                return area(packed);
            }
        };
    }

    /**
     * Create a new spatial index
     *
     * @return A newly created spatial index
     */
    private SpatialIndex<Edge> newEdgeSpatialIndex()
    {
        return new PackedSpatialIndex<Edge, Long>(new RTree<>())
        {
            private static final long serialVersionUID = -7338204023386941100L;

            @Override
            protected Long compress(final Edge item)
            {
                return item.getIdentifier();
            }

            @Override
            protected boolean isValid(final Edge item, final Rectangle bounds)
            {
                return bounds.overlaps(item.asPolyLine());
            }

            @Override
            protected Edge restore(final Long packed)
            {
                return edge(packed);
            }
        };
    }

    /**
     * Create a new spatial index
     *
     * @return A newly created spatial index
     */
    private SpatialIndex<Line> newLineSpatialIndex()
    {
        return new PackedSpatialIndex<Line, Long>(new RTree<>())
        {
            private static final long serialVersionUID = -2370005868531024004L;

            @Override
            protected Long compress(final Line item)
            {
                return item.getIdentifier();
            }

            @Override
            protected boolean isValid(final Line item, final Rectangle bounds)
            {
                return bounds.overlaps(item.asPolyLine());
            }

            @Override
            protected Line restore(final Long packed)
            {
                return line(packed);
            }
        };
    }

    /**
     * Create a new spatial index
     *
     * @return A newly created spatial index
     */
    private SpatialIndex<Node> newNodeSpatialIndex()
    {
        return new PackedSpatialIndex<Node, Long>(new RTree<>())
        {
            private static final long serialVersionUID = -3524737478519081893L;

            @Override
            protected Long compress(final Node item)
            {
                return item.getIdentifier();
            }

            @Override
            protected boolean isValid(final Node item, final Rectangle bounds)
            {
                return bounds.fullyGeometricallyEncloses(item);
            }

            @Override
            protected Node restore(final Long packed)
            {
                return node(packed);
            }
        };
    }

    /**
     * Create a new spatial index
     *
     * @return A newly created spatial index
     */
    private SpatialIndex<Point> newPointSpatialIndex()
    {
        return new PackedSpatialIndex<Point, Long>(new RTree<>())
        {
            private static final long serialVersionUID = -9098544142517525524L;

            @Override
            protected Long compress(final Point item)
            {
                return item.getIdentifier();
            }

            @Override
            protected boolean isValid(final Point item, final Rectangle bounds)
            {
                return bounds.fullyGeometricallyEncloses(item);
            }

            @Override
            protected Point restore(final Long packed)
            {
                return point(packed);
            }
        };
    }

    /**
     * Create a new spatial index
     *
     * @return A newly created spatial index
     */
    private SpatialIndex<Relation> newRelationSpatialIndex()
    {
        return new PackedSpatialIndex<Relation, Long>(new RTree<>())
        {
            private static final long serialVersionUID = 6569644967280192054L;

            @Override
            protected Long compress(final Relation item)
            {
                return item.getIdentifier();
            }

            @Override
            protected boolean isValid(final Relation item, final Rectangle bounds)
            {
                return item.intersects(bounds);
            }

            @Override
            protected Relation restore(final Long packed)
            {
                return relation(packed);
            }
        };
    }
}
