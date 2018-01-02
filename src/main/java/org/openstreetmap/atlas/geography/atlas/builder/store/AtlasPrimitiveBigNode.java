package org.openstreetmap.atlas.geography.atlas.builder.store;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.BigNode;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.BigNode.Path;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.BigNode.Type;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.RestrictedPath;

/**
 * A serializable Big Node thats useful in spark jobs
 *
 * @author Sid
 */
public class AtlasPrimitiveBigNode extends AtlasPrimitiveEntity
{
    private static final long serialVersionUID = -1722511104597663348L;
    // All the nodes defining this BigNode
    private final Set<AtlasPrimitiveLocationItem> nodes;
    // Set of connected edges
    private final Set<AtlasPrimitiveEdge> edges;
    // All the possible paths in then out of this BigNode
    private final Set<AtlasPrimitiveRoute> paths;
    // All the restricted paths of this BigNode
    private final Set<AtlasPrimitiveRouteIdentifier> restrictedPaths;
    // Type of Big Node
    private final Type type;

    public static AtlasPrimitiveBigNode from(final BigNode bigNode)
    {
        return from(bigNode, false, Path.SHORTEST);
    }

    public static AtlasPrimitiveBigNode from(final BigNode bigNode,
            final boolean storeRestrictedPaths, final Path pathType)
    {
        final Set<AtlasPrimitiveLocationItem> nodes = bigNode.nodes().stream()
                .map(node -> AtlasPrimitiveLocationItem.from(node)).collect(Collectors.toSet());
        final Set<AtlasPrimitiveEdge> edges = bigNode.edges().stream()
                .map(edge -> AtlasPrimitiveEdge.from(edge)).collect(Collectors.toSet());

        // All the paths in then out of this BigNode - the type (shortest/all/etc.) of path is
        // configurable
        final Set<AtlasPrimitiveRoute> paths;
        switch (pathType)
        {
            case SHORTEST:
                paths = bigNode.shortestPaths().stream().map(AtlasPrimitiveRoute::from)
                        .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());
                break;
            case ALL:
                paths = bigNode.allPaths().stream().map(AtlasPrimitiveRoute::from)
                        .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toSet());
                break;
            default:
                throw new CoreException("Invalid Path Type: {}", pathType);
        }

        // All the restricted paths of this BigNode - will use all possible paths under the covers
        // to capture as many restrictions as possible.
        final Set<AtlasPrimitiveRouteIdentifier> restrictedPaths = storeRestrictedPaths ? bigNode
                .turnRestrictions().stream().map(RestrictedPath::getRoute)
                .map(AtlasPrimitiveRoute::from).filter(Optional::isPresent).map(Optional::get)
                .map(AtlasPrimitiveRouteIdentifier::from).collect(Collectors.toSet())
                : Collections.emptySet();
        return new AtlasPrimitiveBigNode(bigNode.getIdentifier(), nodes, edges, paths,
                restrictedPaths, bigNode.getTags(), bigNode.getType());
    }

    public AtlasPrimitiveBigNode(final long identifier, final Set<AtlasPrimitiveLocationItem> nodes,
            final Set<AtlasPrimitiveEdge> edges, final Set<AtlasPrimitiveRoute> paths,
            final Set<AtlasPrimitiveRouteIdentifier> restrictedPaths,
            final Map<String, String> tags, final Type type)
    {
        super(identifier, tags);
        this.nodes = nodes;
        this.edges = edges;
        this.paths = paths;
        this.type = type;
        this.restrictedPaths = restrictedPaths;
    }

    @Override
    public Rectangle bounds()
    {
        return Rectangle.forLocated(this.nodes);
    }

    public Set<AtlasPrimitiveEdge> edges()
    {
        return this.edges;
    }

    public Set<AtlasPrimitiveRouteIdentifier> getRestrictedPaths()
    {
        return this.restrictedPaths;
    }

    public Set<AtlasPrimitiveEdge> inEdges()
    {
        return edges().stream().filter(edge -> !nodesContain(edge.start()))
                .collect(Collectors.toSet());
    }

    public Set<AtlasPrimitiveEdge> junctionEdges()
    {
        return edges().stream()
                .filter(edge -> nodesContain(edge.start()) && nodesContain(edge.end()))
                .collect(Collectors.toSet());
    }

    public Set<Location> nodeLocations()
    {
        return this.nodes().stream().map(AtlasPrimitiveLocationItem::getLocation)
                .collect(Collectors.toSet());
    }

    public Set<AtlasPrimitiveLocationItem> nodes()
    {
        return this.nodes;
    }

    public boolean nodesContain(final Location location)
    {
        return this.nodes.stream().filter(node -> node.getLocation().equals(location)).count() > 0;
    }

    public Set<AtlasPrimitiveEdge> outEdges()
    {
        return edges().stream().filter(edge -> !this.nodeLocations().contains(edge.end()))
                .collect(Collectors.toSet());
    }

    public Set<AtlasPrimitiveRoute> paths()
    {
        return this.paths;
    }

    @Override
    public String toString()
    {
        return "[AtlasPrimitiveBigNode: nodes="
                + nodes().stream().map(node -> node.getIdentifier()).collect(Collectors.toSet())
                + "]";
    }

    public Type type()
    {
        return this.type;
    }
}
