package org.openstreetmap.atlas.geography.atlas.items.complex.bignode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.items.AtlasItem;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Route;
import org.openstreetmap.atlas.geography.atlas.items.complex.ComplexEntity;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.routing.AStarRouter;
import org.openstreetmap.atlas.geography.atlas.routing.AllPathsRouter;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.LocationIterableProperties;
import org.openstreetmap.atlas.geography.geojson.GeoJsonObject;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * {@link BigNode} is a {@link ComplexEntity} that represents complex intersections, as one single
 * object. This is mostly used to model turn restrictions. A {@link BigNode} also provides the
 * different ways to traverse it as {@link Route} objects.
 *
 * @author matthieun
 * @author mgostintsev
 */
public class BigNode extends ComplexEntity
{
    /**
     * This denotes the type path through a {@link BigNode}. Note: other than the start and end
     * edges, only junction edges are allowed in the path.
     *
     * @author mgostintsev
     */
    public enum Path
    {
        SHORTEST,
        ALL
    }

    /**
     * This denotes the type of {@link BigNode}
     *
     * @author Sid
     */
    public enum Type
    {
        // Intersections represented by a single node
        SIMPLE,
        // Dual Carriage Way Intersections represented by a set of nodes
        DUAL_CARRIAGEWAY
    }

    private static final long serialVersionUID = 4102278807908010498L;

    // Maximum allowed nodes for a BigNode
    public static final int MAXIMUM_NODES = 20;

    // BigNode type
    private Type type;

    // All the nodes defining this BigNode
    private final Set<Node> nodes;

    // Lazily initialized sets of edges making up this BigNode
    private Set<Edge> edges;
    private Set<Edge> inEdges;
    private Set<Edge> outEdges;
    private Set<Edge> junctionEdges;

    // Outside of the start/end edge, only junction edges are allowed to be part of a path leading
    // in/out of a BigNode. Because we're building a new atlas to do the routing one, we can't use
    // contains for the edge in question, but rely on the identifier to make sure it's actually a
    // junction edge.
    private final Predicate<Edge> isJunctionEdge = edge -> this.junctionEdges().stream()
            .filter(junctionEdge -> junctionEdge.getIdentifier() == edge.getIdentifier())
            .count() > 0;

    /**
     * Construct a {@link Type#SIMPLE} {@link BigNode}
     *
     * @param source
     *            The source node
     */
    public BigNode(final Node source)
    {
        super(source);

        final Set<Node> nodes = new HashSet<>();
        nodes.add(source);

        this.nodes = nodes;
        this.type = Type.SIMPLE;
    }

    /**
     * Construct a {@link BigNode}
     *
     * @param source
     *            The source node
     * @param nodes
     *            All the nodes belonging to the {@link BigNode}
     * @param type
     *            Type of the {@link BigNode}
     */
    public BigNode(final Node source, final Set<Node> nodes, final Type type)
    {
        this(source, nodes);
        this.type = type;
    }

    /**
     * Construct a {@link BigNode}
     *
     * @param source
     *            The source {@link Node}
     * @param nodes
     *            All the {@link Node}s belonging to the {@link BigNode}
     */
    protected BigNode(final Node source, final Set<Node> nodes)
    {
        super(source);
        this.nodes = nodes;
    }

    /**
     * @return The set of all possible {@link Route}s in and out of this {@link BigNode}.
     *         {@link BigNode#shortestPaths} are a subset of this set. Note: this will NOT be cached
     *         by the {@link BigNode}, so be judicious in how many times this is called.
     */
    public Set<Route> allPaths()
    {
        final Atlas atlas = nodes().iterator().next().getAtlas();
        final Atlas bigNodeAtlas = buildBigNodeAtlas();

        // Find all the routes from all the in-edges to all the out-edges
        final Set<Route> allPaths = new HashSet<>();
        for (final Edge inEdge : inEdges())
        {
            for (final Edge outEdge : outEdges())
            {
                // Translate edges to bigNodeAtlas edges to get all routes
                final Set<Route> bigNodeRoutes = AllPathsRouter.allRoutes(
                        bigNodeAtlas.edge(inEdge.getIdentifier()),
                        bigNodeAtlas.edge(outEdge.getIdentifier()), this.isJunctionEdge);

                if (bigNodeRoutes.isEmpty())
                {
                    continue;
                }

                // Translate the result routes back to routes made of edges from the original Atlas
                for (final Route bigNodeRoute : bigNodeRoutes)
                {
                    Route route = Route.forEdge(atlas.edge(bigNodeRoute.start().getIdentifier()));
                    for (int index = 1; index < bigNodeRoute.size(); index++)
                    {
                        final long edgeIdentifier = bigNodeRoute.get(index).getIdentifier();
                        route = route.append(atlas.edge(edgeIdentifier));
                    }
                    allPaths.add(route);
                }
            }
        }

        return allPaths;
    }

    public GeoJsonObject asGeoJson()
    {
        return new GeoJsonBuilder().create(Iterables.from(asGeoJsonBigNode()));
    }

    public LocationIterableProperties asGeoJsonBigNode()
    {
        final List<Location> locations = new ArrayList<>();
        nodes().stream().forEach(node -> locations
                .addAll(Rectangle.forLocated(node.getLocation()).expand(Distance.meters(2))));
        return new LocationIterableProperties(new Polygon(locations),
                Maps.hashMap("BigNode", String.valueOf(getSource().getIdentifier())));
    }

    public Iterable<LocationIterableProperties> asGeoJsonRestrictedPath()
    {
        return this.turnRestrictions().stream()
                .map(turnRestriction -> new LocationIterableProperties(
                        turnRestriction.getRoute().asPolyLine(),
                        Maps.hashMap("highway", "motorway", "oneway", "yes", "route",
                                turnRestriction.getRoute().toString())))
                .collect(Collectors.toList());
    }

    @Override
    public Rectangle bounds()
    {
        // Override the regular bounds() method that only looks at the source
        return Rectangle.forLocated(nodesAndEdges());
    }

    /**
     * @return All the {@link Edge}s linked to this {@link BigNode}
     */
    public Set<Edge> edges()
    {
        if (this.edges == null)
        {
            this.edges = this.nodes.stream().flatMap(node ->
            {
                return node.connectedEdges().stream().filter(HighwayTag::isCarNavigableHighway);
            }).collect(Collectors.toSet());
        }
        return this.edges;
    }

    @Override
    public boolean equals(final Object other)
    {
        // Override the regular equals(Object) method that only looks at the source
        if (other instanceof BigNode)
        {
            return this.getSource().equals(((BigNode) other).getSource())
                    && this.nodes().equals(((BigNode) other).nodes());
        }
        return false;
    }

    public Set<Edge> exteriorEdges()
    {
        return exteriorEdgesStream().collect(Collectors.toSet());
    }

    @Override
    public List<ComplexEntityError> getAllInvalidations()
    {
        final List<ComplexEntityError> returnValue = new ArrayList<>();
        if (!isValid())
        {
            returnValue.add(new ComplexEntityError(this,
                    String.format("Too many nodes %d", nodes().size()), null));
        }
        return returnValue;
    }

    /**
     * @return the {@link Type} of {@link BigNode} this is. Can be null.
     */
    public Type getType()
    {
        return this.type;
    }

    @Override
    public int hashCode()
    {
        // For checkstyle
        return super.hashCode();
    }

    /**
     * @return All the {@link Edge}s that drive into the {@link BigNode}
     */
    public Set<Edge> inEdges()
    {
        if (this.inEdges == null)
        {
            this.inEdges = inEdgesStream().collect(Collectors.toSet());
        }
        return this.inEdges;
    }

    @Override
    public boolean isValid()
    {
        return nodes().size() <= MAXIMUM_NODES;
    }

    /**
     * @return All the {@link Edge}s that represent internal junctions to the {@link BigNode}
     */
    public Set<Edge> junctionEdges()
    {
        if (this.junctionEdges == null)
        {
            this.junctionEdges = junctionEdgesStream().collect(Collectors.toSet());
        }
        return this.junctionEdges;
    }

    /**
     * @return All the {@link Node}s forming this {@link BigNode}
     */
    public Set<Node> nodes()
    {
        return this.nodes;
    }

    /**
     * @return All the {@link Node}s and {@link Edge}s forming this {@link BigNode}
     */
    public Iterable<AtlasItem> nodesAndEdges()
    {
        return new MultiIterable<>(nodes(), edges());
    }

    /**
     * @return All the {@link Edge}s that drive out of the {@link BigNode}.
     */
    public Set<Edge> outEdges()
    {
        if (this.outEdges == null)
        {
            this.outEdges = outEdgesStream().collect(Collectors.toSet());
        }
        return this.outEdges;
    }

    /**
     * Set the {@link Type} for this {@link BigNode}.
     *
     * @param type
     *            The {@link Type} to set
     */
    public void setType(final Type type)
    {
        this.type = type;
    }

    /**
     * @return The set of shortest {@link Route}s in/out of this {@link BigNode}. Note: this will
     *         NOT be cached by the {@link BigNode}, so be judicious in how many times this is
     *         called.
     */
    public Set<Route> shortestPaths()
    {
        final Atlas atlas = nodes().iterator().next().getAtlas();
        final Atlas bigNodeAtlas = buildBigNodeAtlas();

        // Find all the routes from all the in-edges to all the out-edges
        final Set<Route> shortestPaths = new HashSet<>();
        for (final Edge inEdge : inEdges())
        {
            for (final Edge outEdge : outEdges())
            {
                // Translate edges to bigNodeAtlas edges to get the route
                final Route bigNodeRoute = AStarRouter.dijkstra(bigNodeAtlas, Distance.ONE_METER)
                        .route(bigNodeAtlas.edge(inEdge.getIdentifier()),
                                bigNodeAtlas.edge(outEdge.getIdentifier()));
                if (bigNodeRoute == null)
                {
                    continue;
                }

                // Translate the result route back to a route made of edges from the original Atlas
                Route route = Route.forEdge(atlas.edge(bigNodeRoute.start().getIdentifier()));
                for (int index = 1; index < bigNodeRoute.size(); index++)
                {
                    final long edgeIdentifier = bigNodeRoute.get(index).getIdentifier();
                    route = route.append(atlas.edge(edgeIdentifier));
                }
                shortestPaths.add(route);
            }
        }

        return shortestPaths;
    }

    @Override
    public String toString()
    {
        return "[BigNode: nodes="
                + nodes().stream().map(Node::getIdentifier).collect(Collectors.toSet()) + "]";
    }

    /**
     * @return All the paths in this {@link BigNode} that overlap at least one restricted path.
     */
    public Set<RestrictedPath> turnRestrictions()
    {
        return this.allPaths().stream().filter(Route::isTurnRestriction)
                .map(route -> new RestrictedPath(this, route)).collect(Collectors.toSet());
    }

    protected Stream<Edge> exteriorEdgesStream()
    {
        return edges().stream()
                .filter(edge -> !nodes().contains(edge.end()) || !nodes().contains(edge.start()));
    }

    protected Stream<Edge> inEdgesStream()
    {
        return edges().stream().filter(edge -> !nodes().contains(edge.start()));
    }

    protected Stream<Edge> junctionEdgesStream()
    {
        return edges().stream()
                .filter(edge -> nodes().contains(edge.start()) && nodes().contains(edge.end()));
    }

    protected Stream<Edge> outEdgesStream()
    {
        return edges().stream().filter(edge -> !nodes().contains(edge.end()));
    }

    /**
     * @return an {@link Atlas} made up of {@link Node}s and {@link Edge}s comprising this
     *         {@link BigNode}, in order to run routing algorithms on it.
     */
    private Atlas buildBigNodeAtlas()
    {
        final Set<Edge> edges = edges();
        final Set<Node> extendedNodes = new HashSet<>();
        edges.forEach(edge ->
        {
            extendedNodes.add(edge.start());
            extendedNodes.add(edge.end());
        });

        final PackedAtlasBuilder builder = new PackedAtlasBuilder()
                .withSizeEstimates(new AtlasSize(edges.size(), extendedNodes.size(), 0, 0, 0, 0));
        extendedNodes.forEach(
                node -> builder.addNode(node.getIdentifier(), node.getLocation(), node.getTags()));
        edges.forEach(
                edge -> builder.addEdge(edge.getIdentifier(), edge.asPolyLine(), edge.getTags()));
        return builder.get();
    }

}
