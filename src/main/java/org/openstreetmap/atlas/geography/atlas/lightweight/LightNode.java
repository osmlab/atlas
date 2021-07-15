package org.openstreetmap.atlas.geography.atlas.lightweight;

import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.complete.EmptyAtlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * A lightweight node with only basic information
 *
 * @author Taylor Smock
 */
public class LightNode extends Node implements LightLocationItem<LightNode>
{
    private static final byte HASH_BYTE = 31;
    private final long identifier;
    private final Location location;
    private final long[] inEdgeIdentifiers;
    private final long[] outEdgeIdentifiers;
    private final long[] relationIdentifiers;

    /**
     * Create a new LightNode from a Node
     *
     * @param node
     *            The node to copy from
     * @return A LightNode
     */
    static LightNode from(final Node node)
    {
        return new LightNode(node);
    }

    /**
     * Create a LightNode with just an identifier
     *
     * @param identifier
     *            The identifier
     */
    LightNode(final long identifier)
    {
        this(identifier, null);
    }

    /**
     * Create a LightNode from an identifier and a location
     *
     * @param identifier
     *            The identifier
     * @param location
     *            The location
     */
    LightNode(final long identifier, final Location location)
    {
        super(new EmptyAtlas());
        this.identifier = identifier;
        this.location = location;
        this.inEdgeIdentifiers = EMPTY_LONG_ARRAY;
        this.outEdgeIdentifiers = EMPTY_LONG_ARRAY;
        this.relationIdentifiers = EMPTY_LONG_ARRAY;
    }

    /**
     * Create a new LightNode from another Node
     *
     * @param from
     *            The ndoe to copy from
     */
    LightNode(final Node from)
    {
        super(new EmptyAtlas());
        this.identifier = from.getIdentifier();
        this.location = from.getLocation();
        this.inEdgeIdentifiers = from.inEdges().stream().map(Edge::getIdentifier).distinct()
                .mapToLong(Long::longValue).toArray();
        this.outEdgeIdentifiers = from.outEdges().stream().map(Edge::getIdentifier).distinct()
                .mapToLong(Long::longValue).toArray();
        this.relationIdentifiers = from.relations().stream().map(Relation::getIdentifier).distinct()
                .mapToLong(Long::longValue).toArray();
    }

    @Override
    public boolean equals(final Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other == null || this.getClass() != other.getClass())
        {
            return false;
        }
        final var lightNode = (LightNode) other;
        if (this.location != null && lightNode.location != null)
        {
            return this.identifier == lightNode.identifier
                    && this.location.equals(lightNode.location)
                    && Arrays.equals(this.inEdgeIdentifiers, lightNode.inEdgeIdentifiers)
                    && Arrays.equals(this.outEdgeIdentifiers, lightNode.outEdgeIdentifiers)
                    && Arrays.equals(this.relationIdentifiers, lightNode.relationIdentifiers);
        }
        else if (this.location == null && lightNode.location == null)
        {
            return this.identifier == lightNode.identifier
                    && Arrays.equals(this.inEdgeIdentifiers, lightNode.inEdgeIdentifiers)
                    && Arrays.equals(this.outEdgeIdentifiers, lightNode.outEdgeIdentifiers)
                    && Arrays.equals(this.relationIdentifiers, lightNode.relationIdentifiers);
        }
        return false;
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
    }

    /**
     * Get the identifiers for in edges
     *
     * @return The identifiers for in edges
     */
    public long[] getInEdgeIdentifiers()
    {
        return this.inEdgeIdentifiers.clone();
    }

    @Override
    public Location getLocation()
    {
        return this.location;
    }

    /**
     * Get the identifiers for out edges
     *
     * @return The identifiers for out edges
     */
    public long[] getOutEdgeIdentifiers()
    {
        return this.outEdgeIdentifiers.clone();
    }

    @Override
    public long[] getRelationIdentifiers()
    {
        return this.relationIdentifiers.clone();
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = HASH_BYTE * result + Long.hashCode(this.identifier);
        if (this.location != null)
        {
            result = HASH_BYTE * result + this.location.hashCode();
        }
        result = HASH_BYTE * result + Arrays.hashCode(this.inEdgeIdentifiers);
        result = HASH_BYTE * result + Arrays.hashCode(this.outEdgeIdentifiers);
        result = HASH_BYTE * result + Arrays.hashCode(this.relationIdentifiers);
        return result;
    }

    /**
     * Please note that the edges returned from this method should *only* be used for identifiers.
     *
     * @see Node#inEdges()
     * @return A set of identifier only edges
     */
    @Override
    public SortedSet<Edge> inEdges()
    {
        return LongStream.of(this.inEdgeIdentifiers).mapToObj(LightEdge::new)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Please note that the edges returned from this method should *only* be used for identifiers.
     *
     * @see Node#outEdges()
     * @return A set of identifier only edges
     */
    @Override
    public SortedSet<Edge> outEdges()
    {
        return LongStream.of(this.outEdgeIdentifiers).mapToObj(LightEdge::new)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    /**
     * Please note that the relations returned from this method should *only* be used for
     * identifiers.
     *
     * @see Node#relations()
     * @return A set of identifier only relations
     */
    @Override
    public Set<Relation> relations()
    {
        return LongStream.of(this.getRelationIdentifiers()).mapToObj(LightRelation::new)
                .collect(Collectors.toSet());
    }
}
