package org.openstreetmap.atlas.geography.atlas.items;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.ReverseIdentifierFactory;
import org.openstreetmap.atlas.tags.HighwayTag;

/**
 * A unidirectional edge that belongs to an Atlas.
 *
 * @author matthieun
 */
public abstract class Edge extends LineItem implements Comparable<Edge>
{
    private static final long serialVersionUID = -4426003484206550921L;
    private static final ReverseIdentifierFactory reverseIdentifierFactory = new ReverseIdentifierFactory();

    /**
     * If the way is bidirectional in OSM, we will put two edges in atlas for two traffic
     * directions, each of them is one way. The master edge will have positive identifier and same
     * traffic direction as OSM.
     *
     * @param identifier
     *            Edge identifier
     * @return True if the edge identifier is positive
     */
    public static boolean isMasterEdgeIdentifier(final long identifier)
    {
        return identifier > 0;
    }

    protected Edge(final Atlas atlas)
    {
        super(atlas);
    }

    /**
     * Compare two edges on their identifier.
     */
    @Override
    public int compareTo(final Edge other)
    {
        final long difference = this.getIdentifier() - other.getIdentifier();
        return difference > 0 ? 1 : difference < 0 ? -1 : 0;
    }

    /**
     * @return All the {@link Edge}s connected to the end {@link Node}s of this {@link Edge}, except
     *         self. If this {@link Edge} is a two-way road, then the reversed {@link Edge} will be
     *         included in the set.
     */
    public Set<Edge> connectedEdges()
    {
        final Set<Edge> result = new HashSet<>();
        for (final Edge edge : this.end().connectedEdges())
        {
            if (!this.equals(edge))
            {
                result.add(edge);
            }
        }
        for (final Edge edge : this.start().connectedEdges())
        {
            if (!this.equals(edge))
            {
                result.add(edge);
            }
        }
        return result;
    }

    public Set<Node> connectedNodes()
    {
        final Set<Node> result = new HashSet<>();
        result.add(this.start());
        result.add(this.end());
        return result;
    }

    /**
     * @return The same {@link Edge} but with the tags interpreted with this {@link Edge}'s
     *         direction. For example, if this {@link Edge} is backwards from its OSM way, and the
     *         way has a maxspeed:backward tag, here it will be translated into a maxspeed tag. Also
     *         the maxspeed:forward tag will be filtered out (it will be used by the reverse edge).
     */
    public Edge directionalized()
    {
        return new DirectionalizedEdge(this);
    }

    /**
     * @return The {@link Node} at the end of this {@link Edge}
     */
    public abstract Node end();

    /**
     * @return the master for this {@link Edge}, which may or may not be the master.
     */
    public Edge getMasterEdge()
    {
        return this.isMasterEdge() ? this : this.reversed().get();
    }

    public long getMasterEdgeIdentifier()
    {
        return Math.abs(this.getIdentifier());
    }

    @Override
    public ItemType getType()
    {
        return ItemType.EDGE;
    }

    /**
     * @return {@code true} if there is a reverse edge to this one
     */
    public boolean hasReverseEdge()
    {
        return this.getAtlas().edge(-this.getIdentifier()) != null;
    }

    /**
     * @return The {@link HighwayTag} of the Edge, if it is present. Return HighwayTag.NO if it is
     *         not.
     */
    public HighwayTag highwayTag()
    {
        final Optional<HighwayTag> result = HighwayTag.highwayTag(this);
        if (result.isPresent())
        {
            return result.get();
        }
        else
        {
            return HighwayTag.NO;
        }
    }

    /**
     * @return All the {@link Edge}s connected and pointing to the start {@link Node} of this
     *         {@link Edge}. If this {@link Edge} is a two-way road, then the reversed {@link Edge}
     *         will be included in the set.
     */
    public Set<Edge> inEdges()
    {
        return this.start().inEdges();
    }

    /**
     * @param candidates
     *            Set of edges and nodes to test connectivity to.
     * @return True if the edge is directly connected at its end to at least one of the candidate
     *         items
     */
    public boolean isConnectedAtEndTo(final Set<? extends AtlasItem> candidates)
    {
        for (final AtlasItem item : candidates)
        {
            if (item instanceof Node)
            {
                if (end().equals(item))
                {
                    return true;
                }
            }
            if (item instanceof Edge)
            {
                if (end().equals(((Edge) item).start()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @param candidates
     *            Set of edges and nodes to test connectivity to.
     * @return True if the edge is directly connected at its start to at least one of the candidate
     *         items
     */
    public boolean isConnectedAtStartTo(final Set<? extends AtlasItem> candidates)
    {
        for (final AtlasItem item : candidates)
        {
            if (item instanceof Node)
            {
                if (start().equals(item))
                {
                    return true;
                }
            }
            if (item instanceof Edge)
            {
                if (start().equals(((Edge) item).end()))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Checks if edge is a master edge, by verifying that its identifier is a master edge
     * identifier.
     *
     * @return True if the edge's identifier is a master edge identifier
     */
    public boolean isMasterEdge()
    {
        return isMasterEdgeIdentifier(this.getIdentifier());
    }

    /**
     * @param candidate
     *            candidate for reverseEdge
     * @return {@code true} if candidate is the reverse of this {@link Edge}
     */
    public boolean isReversedEdge(final Edge candidate)
    {
        return this.getIdentifier() == -candidate.getIdentifier();
    }

    /**
     * @return {@code true} if the {@link Edge} is a way-sectioned road.
     */
    public boolean isWaySectioned()
    {
        return reverseIdentifierFactory.getWaySectionIndex(this.getIdentifier()) != 0;
    }

    /**
     * @return All the {@link Edge}s connected and pointing out of the end {@link Node} of this
     *         {@link Edge}. If this {@link Edge} is a two-way road, then the reversed {@link Edge}
     *         will be included in the set.
     */
    public Set<Edge> outEdges()
    {
        return this.end().outEdges();
    }

    /**
     * @return An {@link Edge} that is reversed to this one if it exists, empty otherwise.
     */
    public Optional<Edge> reversed()
    {
        final Edge edge = this.getAtlas().edge(-this.getIdentifier());
        if (edge != null)
        {
            return Optional.of(edge);
        }
        return Optional.empty();
    }

    public abstract Node start();

    @Override
    public String toDiffViewFriendlyString()
    {
        final String relationsString = this.parentRelationsAsDiffViewFriendlyString();

        return "[Edge" + ": id=" + this.getIdentifier() + ", startNode=" + start().getIdentifier()
                + ", endNode=" + end().getIdentifier() + ", polyLine=" + this.asPolyLine().toWkt()
                + ", relations=(" + relationsString + "), " + tagString() + "]";
    }

    @Override
    public String toString()
    {
        return "[Edge" + ": id=" + this.getIdentifier() + ", startNode=" + start().getIdentifier()
                + ", endNode=" + end().getIdentifier() + ", polyLine=" + this.asPolyLine().toWkt()
                + ", " + tagString() + "]";
    }
}
