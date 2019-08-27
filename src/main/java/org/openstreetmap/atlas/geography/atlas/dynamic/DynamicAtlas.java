package org.openstreetmap.atlas.geography.atlas.dynamic;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.BareAtlas;
import org.openstreetmap.atlas.geography.atlas.dynamic.policy.DynamicAtlasPolicy;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * This is not thread safe!
 * <p>
 * An Atlas that is dynamically expanding by loading neighboring shards upon request of its
 * features.
 *
 * @author matthieun
 */
// NOSONAR here as the parent equals is enough
public class DynamicAtlas extends BareAtlas // NOSONAR
{
    private static final long serialVersionUID = -2858997785405677961L;

    // The current Atlas that will be swapped during expansion.
    private Atlas current;
    private final DynamicAtlasExpander expander;

    /**
     * @param dynamicAtlasExpansionPolicy
     *            Expansion policy for the dynamic atlas
     */
    public DynamicAtlas(final DynamicAtlasPolicy dynamicAtlasExpansionPolicy)
    {
        this.setName("DynamicAtlas(" + dynamicAtlasExpansionPolicy.getInitialShards().stream()
                .map(Shard::getName).collect(Collectors.toSet()) + ")");
        this.expander = new DynamicAtlasExpander(this, dynamicAtlasExpansionPolicy);
    }

    @Override
    public Area area(final long identifier)
    {
        final Iterator<DynamicArea> result = this.expander
                .expand(() -> Iterables.from(subArea(identifier)), this.expander::areaCovered,
                        this::newArea)
                .iterator();
        return result.hasNext() ? result.next() : null;
    }

    @Override
    public Iterable<Area> areas()
    {
        return this.expander.expand(() -> this.current.areas(), this.expander::areaCovered,
                this::newArea);
    }

    @Override
    public Iterable<Area> areasCovering(final Location location)
    {
        return this.expander.expand(() -> this.current.areasCovering(location),
                this.expander::areaCovered, this::newArea);
    }

    @Override
    public Iterable<Area> areasCovering(final Location location, final Predicate<Area> matcher)
    {
        return this.expander.expand(() -> this.current.areasCovering(location, matcher),
                this.expander::areaCovered, this::newArea);
    }

    @Override
    public Iterable<Area> areasIntersecting(final GeometricSurface surface)
    {
        return this.expander.expand(() -> this.current.areasIntersecting(surface),
                this.expander::areaCovered, this::newArea);
    }

    @Override
    public Iterable<Area> areasIntersecting(final GeometricSurface surface,
            final Predicate<Area> matcher)
    {
        return this.expander.expand(() -> this.current.areasIntersecting(surface, matcher),
                this.expander::areaCovered, this::newArea);
    }

    @Override
    public Iterable<Area> areasWithin(final GeometricSurface surface)
    {
        return this.expander.expand(() -> this.current.areasWithin(surface),
                this.expander::areaCovered, this::newArea);
    }

    @Override
    public Rectangle bounds()
    {
        return this.current.bounds();
    }

    @Override
    public Edge edge(final long identifier)
    {
        final Iterator<DynamicEdge> result = this.expander
                .expand(() -> Iterables.from(subEdge(identifier)), this.expander::lineItemCovered,
                        this::newEdge)
                .iterator();
        return result.hasNext() ? result.next() : null;
    }

    @Override
    public Iterable<Edge> edges()
    {
        return this.expander.expand(() -> this.current.edges(), this.expander::lineItemCovered,
                this::newEdge);
    }

    @Override
    public Iterable<Edge> edgesContaining(final Location location)
    {
        return this.expander.expand(() -> this.current.edgesContaining(location),
                this.expander::lineItemCovered, this::newEdge);
    }

    @Override
    public Iterable<Edge> edgesContaining(final Location location, final Predicate<Edge> matcher)
    {
        return this.expander.expand(() -> this.current.edgesContaining(location, matcher),
                this.expander::lineItemCovered, this::newEdge);
    }

    @Override
    public Iterable<Edge> edgesIntersecting(final GeometricSurface surface)
    {
        return this.expander.expand(() -> this.current.edgesIntersecting(surface),
                this.expander::lineItemCovered, this::newEdge);
    }

    @Override
    public Iterable<Edge> edgesIntersecting(final GeometricSurface surface,
            final Predicate<Edge> matcher)
    {
        return this.expander.expand(() -> this.current.edgesIntersecting(surface, matcher),
                this.expander::lineItemCovered, this::newEdge);
    }

    @Override
    public Iterable<Edge> edgesWithin(final GeometricSurface surface)
    {
        return this.expander.expand(() -> this.current.edgesWithin(surface),
                this.expander::lineItemCovered, this::newEdge);
    }

    /**
     * @return The number of shards loaded by that {@link DynamicAtlas} at any time.
     */
    public int getNumberOfShardsLoaded()
    {
        return getShardsLoaded().size();
    }

    public DynamicAtlasPolicy getPolicy()
    {
        return this.expander.getPolicy();
    }

    /**
     * @return All the shards explored by this {@link DynamicAtlas} including the ones that yielded
     *         no Atlas.
     */
    public Set<Shard> getShardsExplored()
    {
        return this.expander.getLoadedShards().keySet();
    }

    /**
     * @return All the shards explored by that {@link DynamicAtlas} which yielded some non null
     *         Atlas.
     */
    public Set<Shard> getShardsLoaded()
    {
        return this.expander.getLoadedShards().entrySet().stream()
                .filter(entry -> entry.getValue() != null).map(Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * @return The number of times that {@link DynamicAtlas} has (re-)built its {@link MultiAtlas}
     *         underneath.
     */
    public int getTimesMultiAtlasWasBuiltUnderneath()
    {
        return this.expander.getTimesMultiAtlasWasBuiltUnderneath();
    }

    @Override
    public Line line(final long identifier)
    {
        final Iterator<DynamicLine> result = this.expander
                .expand(() -> Iterables.from(subLine(identifier)), this.expander::lineItemCovered,
                        this::newLine)
                .iterator();
        return result.hasNext() ? result.next() : null;
    }

    @Override
    public Iterable<Line> lines()
    {
        return this.expander.expand(() -> this.current.lines(), this.expander::lineItemCovered,
                this::newLine);
    }

    @Override
    public Iterable<Line> linesContaining(final Location location)
    {
        return this.expander.expand(() -> this.current.linesContaining(location),
                this.expander::lineItemCovered, this::newLine);
    }

    @Override
    public Iterable<Line> linesContaining(final Location location, final Predicate<Line> matcher)
    {
        return this.expander.expand(() -> this.current.linesContaining(location, matcher),
                this.expander::lineItemCovered, this::newLine);
    }

    @Override
    public Iterable<Line> linesIntersecting(final GeometricSurface surface)
    {
        return this.expander.expand(() -> this.current.linesIntersecting(surface),
                this.expander::lineItemCovered, this::newLine);
    }

    @Override
    public Iterable<Line> linesIntersecting(final GeometricSurface surface,
            final Predicate<Line> matcher)
    {
        return this.expander.expand(() -> this.current.linesIntersecting(surface, matcher),
                this.expander::lineItemCovered, this::newLine);
    }

    @Override
    public Iterable<Line> linesWithin(final GeometricSurface surface)
    {
        return this.expander.expand(() -> this.current.linesWithin(surface),
                this.expander::lineItemCovered, this::newLine);
    }

    @Override
    public AtlasMetaData metaData()
    {
        return this.current.metaData();
    }

    @Override
    public Node node(final long identifier)
    {
        final Iterator<DynamicNode> result = this.expander
                .expand(() -> Iterables.from(subNode(identifier)),
                        this.expander::locationItemCovered, this::newNode)
                .iterator();
        return result.hasNext() ? result.next() : null;
    }

    @Override
    public Iterable<Node> nodes()
    {
        return this.expander.expand(() -> this.current.nodes(), this.expander::locationItemCovered,
                this::newNode);
    }

    @Override
    public Iterable<Node> nodesAt(final Location location)
    {
        return this.expander.expand(() -> this.current.nodesAt(location),
                this.expander::locationItemCovered, this::newNode);
    }

    @Override
    public Iterable<Node> nodesWithin(final GeometricSurface surface)
    {
        return this.expander.expand(() -> this.current.nodesWithin(surface),
                this.expander::locationItemCovered, this::newNode);
    }

    @Override
    public Iterable<Node> nodesWithin(final GeometricSurface surface, final Predicate<Node> matcher)
    {
        return this.expander.expand(() -> this.current.nodesWithin(surface, matcher),
                this.expander::locationItemCovered, this::newNode);
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
        final Iterator<DynamicPoint> result = this.expander
                .expand(() -> Iterables.from(subPoint(identifier)),
                        this.expander::locationItemCovered, this::newPoint)
                .iterator();
        return result.hasNext() ? result.next() : null;
    }

    @Override
    public Iterable<Point> points()
    {
        return this.expander.expand(() -> this.current.points(), this.expander::locationItemCovered,
                this::newPoint);
    }

    @Override
    public Iterable<Point> pointsAt(final Location location)
    {
        return this.expander.expand(() -> this.current.pointsAt(location),
                this.expander::locationItemCovered, this::newPoint);
    }

    @Override
    public Iterable<Point> pointsWithin(final GeometricSurface surface)
    {
        return this.expander.expand(() -> this.current.pointsWithin(surface),
                this.expander::locationItemCovered, this::newPoint);
    }

    @Override
    public Iterable<Point> pointsWithin(final GeometricSurface surface,
            final Predicate<Point> matcher)
    {
        return this.expander.expand(() -> this.current.pointsWithin(surface, matcher),
                this.expander::locationItemCovered, this::newPoint);
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
    public void preemptiveLoad()
    {
        this.expander.preemptiveLoad();
    }

    @Override
    public Relation relation(final long identifier)
    {
        final Iterator<DynamicRelation> result = this.expander
                .expand(() -> Iterables.from(subRelation(identifier)),
                        this.expander::relationCovered, this::newRelation)
                .iterator();
        return result.hasNext() ? result.next() : null;
    }

    @Override
    public Iterable<Relation> relations()
    {
        return this.expander.expand(() -> this.current.relations(), this.expander::relationCovered,
                this::newRelation);
    }

    @Override
    public Iterable<Relation> relationsWithEntitiesIntersecting(final GeometricSurface surface)
    {
        return this.expander.expand(() -> this.current.relationsWithEntitiesIntersecting(surface),
                this.expander::relationCovered, this::newRelation);
    }

    @Override
    public Iterable<Relation> relationsWithEntitiesIntersecting(final GeometricSurface surface,
            final Predicate<Relation> matcher)
    {
        return this.expander.expand(
                () -> this.current.relationsWithEntitiesIntersecting(surface, matcher),
                this.expander::relationCovered, this::newRelation);
    }

    @Override
    public Iterable<Relation> relationsWithEntitiesWithin(final GeometricSurface surface)
    {
        return this.expander.expand(() -> this.current.relationsWithEntitiesWithin(surface),
                this.expander::relationCovered, this::newRelation);
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

    synchronized void swapCurrentAtlas(final Atlas current)
    {
        this.current = current;
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

    private DynamicNode newNode(final Node node)
    {
        return new DynamicNode(this, node.getIdentifier());
    }

    private DynamicPoint newPoint(final Point point)
    {
        return new DynamicPoint(this, point.getIdentifier());
    }

    private DynamicRelation newRelation(final Relation relation)
    {
        return new DynamicRelation(this, relation.getIdentifier());
    }
}
