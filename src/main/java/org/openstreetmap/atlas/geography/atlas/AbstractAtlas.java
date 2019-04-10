package org.openstreetmap.atlas.geography.atlas;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.GeometricSurface;
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
import org.openstreetmap.atlas.geography.atlas.raw.creation.RawAtlasGenerator;
import org.openstreetmap.atlas.geography.atlas.raw.sectioning.WaySectionProcessor;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.RawAtlasCountrySlicer;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
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
     * Create an {@link Atlas} from an OSM protobuf and save it to a resource. Skip slicing.
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
        Atlas atlas = new RawAtlasGenerator(osmPbf).build();
        atlas = new WaySectionProcessor(atlas, AtlasLoadingOption.createOptionWithNoSlicing())
                .run();
        atlas.save(atlasResource);
        return atlas;
    }

    /**
     * Create an {@link Atlas} from an OSM protobuf that has already been sliced and save it to a
     * resource
     *
     * @param osmPbf
     *            The OSM protobuf
     * @param atlasResource
     *            The {@link WritableResource} to save the {@link Atlas} to
     * @param boundaryMap
     *            The {@link CountryBoundaryMap} to use for country-slicing
     * @return The created {@link Atlas}
     */
    public static Atlas createAndSaveOsmPbfWithSlicing(final Resource osmPbf,
            final WritableResource atlasResource, final CountryBoundaryMap boundaryMap)
    {
        Atlas atlas = new RawAtlasGenerator(osmPbf).build();
        final AtlasLoadingOption loadingOption = AtlasLoadingOption
                .createOptionWithAllEnabled(boundaryMap);
        loadingOption.setAdditionalCountryCodes(boundaryMap.getLoadedCountries());
        atlas = new RawAtlasCountrySlicer(loadingOption).slice(atlas);
        atlas = new WaySectionProcessor(atlas,
                AtlasLoadingOption.createOptionWithAllEnabled(boundaryMap)).run();
        atlas.save(atlasResource);
        return atlas;
    }

    /**
     * Create from an OSM protobuf resource. Skip slicing.
     *
     * @param resource
     *            The OSM protobuf resource
     * @return The Atlas read from the pbf
     */
    public static Atlas forOsmPbf(final Resource resource)
    {
        Atlas atlas = new RawAtlasGenerator(resource).build();
        atlas = new WaySectionProcessor(atlas, AtlasLoadingOption.createOptionWithNoSlicing())
                .run();
        return atlas;
    }

    @Override
    public Iterable<Area> areasCovering(final Location location)
    {
        return Iterables.stream(this.getAreaSpatialIndex().get(location.bounds())).filter(area ->
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
    public Iterable<Area> areasIntersecting(final GeometricSurface surface)
    {
        return Iterables.stream(this.getAreaSpatialIndex().get(surface.bounds())).filter(area ->
        {
            final Polygon areaPolygon = area.asPolygon();
            return surface.overlaps(areaPolygon);
        });
    }

    @Override
    public Iterable<Area> areasIntersecting(final GeometricSurface surface,
            final Predicate<Area> matcher)
    {
        return Iterables.filterTranslate(areasIntersecting(surface), item -> item, matcher);
    }

    @Override
    public Iterable<Area> areasWithin(final GeometricSurface surface)
    {
        return Iterables.stream(this.getAreaSpatialIndex().get(surface.bounds())).filter(area ->
        {
            final Polygon areaPolygon = area.asPolygon();
            return surface.fullyGeometricallyEncloses(areaPolygon);
        });
    }

    @Override
    public Iterable<Edge> edgesContaining(final Location location)
    {
        return Iterables.stream(this.getEdgeSpatialIndex().get(location.bounds())).filter(edge ->
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
    public Iterable<Edge> edgesIntersecting(final GeometricSurface surface)
    {
        return Iterables.stream(this.getEdgeSpatialIndex().get(surface.bounds())).filter(edge ->
        {
            final PolyLine polyline = edge.asPolyLine();
            return surface.overlaps(polyline);
        });
    }

    @Override
    public Iterable<Edge> edgesIntersecting(final GeometricSurface surface,
            final Predicate<Edge> matcher)
    {
        return Iterables.filter(edgesIntersecting(surface), matcher);
    }

    @Override
    public Iterable<Edge> edgesWithin(final GeometricSurface surface)
    {
        return Iterables.stream(this.getEdgeSpatialIndex().get(surface.bounds())).filter(edge ->
        {
            final PolyLine polyline = edge.asPolyLine();
            return surface.fullyGeometricallyEncloses(polyline);
        });
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
        return Iterables.stream(this.getLineSpatialIndex().get(location.bounds())).filter(line ->
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
    public Iterable<Line> linesIntersecting(final GeometricSurface surface)
    {
        return Iterables.stream(this.getLineSpatialIndex().get(surface.bounds())).filter(line ->
        {
            final PolyLine polyline = line.asPolyLine();
            return surface.overlaps(polyline);
        });
    }

    @Override
    public Iterable<Line> linesIntersecting(final GeometricSurface surface,
            final Predicate<Line> matcher)
    {
        return Iterables.filter(linesIntersecting(surface), matcher);
    }

    @Override
    public Iterable<Line> linesWithin(final GeometricSurface surface)
    {
        return Iterables.stream(this.getLineSpatialIndex().get(surface.bounds())).filter(line ->
        {
            final PolyLine polyline = line.asPolyLine();
            return surface.fullyGeometricallyEncloses(polyline);
        });
    }

    @Override
    public Iterable<Node> nodesAt(final Location location)
    {
        return this.getNodeSpatialIndex().get(location.bounds());
    }

    @Override
    public Iterable<Node> nodesWithin(final GeometricSurface surface)
    {
        final Iterable<Node> nodes = this.getNodeSpatialIndex().get(surface.bounds());
        if (surface instanceof Rectangle)
        {
            return nodes;
        }
        return Iterables.filter(nodes,
                node -> surface.fullyGeometricallyEncloses(node.getLocation()));
    }

    @Override
    public Iterable<Node> nodesWithin(final GeometricSurface surface, final Predicate<Node> matcher)
    {
        return Iterables.filter(nodesWithin(surface), matcher);
    }

    @Override
    public Iterable<Point> pointsAt(final Location location)
    {
        return this.getPointSpatialIndex().get(location.bounds());
    }

    @Override
    public Iterable<Point> pointsWithin(final GeometricSurface surface)
    {
        final Iterable<Point> points = this.getPointSpatialIndex().get(surface.bounds());
        if (surface instanceof Rectangle)
        {
            return points;
        }
        return Iterables.filter(points,
                point -> surface.fullyGeometricallyEncloses(point.getLocation()));
    }

    @Override
    public Iterable<Point> pointsWithin(final GeometricSurface surface,
            final Predicate<Point> matcher)
    {
        return Iterables.filterTranslate(pointsWithin(surface), item -> item, matcher);
    }

    @Override
    public Iterable<Relation> relationsWithEntitiesIntersecting(final GeometricSurface surface)
    {
        final Iterable<Relation> relations = this.getRelationSpatialIndex().get(surface.bounds());
        return Iterables.filter(relations, relation -> relation.intersects(surface));
    }

    @Override
    public Iterable<Relation> relationsWithEntitiesIntersecting(final GeometricSurface surface,
            final Predicate<Relation> matcher)
    {
        return Iterables.filter(relationsWithEntitiesIntersecting(surface), matcher);
    }

    @Override
    public Iterable<Relation> relationsWithEntitiesWithin(final GeometricSurface surface)
    {
        final Iterable<Relation> relations = this.getRelationSpatialIndex().get(surface.bounds());
        return Iterables.filter(relations, relation -> relation.within(surface));
    }

    @Override
    public void save(final WritableResource writableResource)
    {
        throw new CoreException(
                "{} does not support saving. Consider using {} instead. A {} can be had using Atlas.cloneToPackedAtlas()",
                this.getClass().getName(), PackedAtlas.class.getName(),
                PackedAtlas.class.getName());
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
