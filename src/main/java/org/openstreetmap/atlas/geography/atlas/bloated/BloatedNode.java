package org.openstreetmap.atlas.geography.atlas.bloated;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Independent {@link Node} that contains its own data. At scale, use at your own risk.
 *
 * @author matthieun
 */
public class BloatedNode extends Node
{
    private static final long serialVersionUID = -8229589987121555419L;

    private long identifier;
    private Location location;
    private Map<String, String> tags;
    private SortedSet<Long> inEdgeIdentifiers;
    private SortedSet<Long> outEdgeIdentifiers;
    private Set<Long> relationIdentifiers;

    public static BloatedNode fromNode(final Node node)
    {
        return new BloatedNode(node.getIdentifier(), node.getLocation(), node.getTags(),
                node.inEdges().stream().map(Edge::getIdentifier)
                        .collect(Collectors.toCollection(TreeSet::new)),
                node.outEdges().stream().map(Edge::getIdentifier)
                        .collect(Collectors.toCollection(TreeSet::new)),
                node.relations().stream().map(Relation::getIdentifier).collect(Collectors.toSet()));
    }

    public BloatedNode(final long identifier, final Location location,
            final Map<String, String> tags, final SortedSet<Long> inEdgeIdentifiers,
            final SortedSet<Long> outEdgeIdentifiers, final Set<Long> relationIdentifiers)
    {
        super(new BloatedAtlas());
        this.identifier = identifier;
        this.location = location;
        this.tags = tags;
        this.inEdgeIdentifiers = inEdgeIdentifiers;
        this.outEdgeIdentifiers = outEdgeIdentifiers;
        this.relationIdentifiers = relationIdentifiers;
    }

    /**
     * Constructor to be used only in BloatedEdge and BloatedRelation. Used otherwise, and this
     * object will misbehave.
     *
     * @param identifier
     *            The feature identifier
     */
    protected BloatedNode(final Long identifier)
    {
        this(identifier, null, null, null, null, null);
    }

    @Override
    public boolean equals(final Object other) // NOSONAR
    {
        if (this == other)
        {
            return true;
        }
        if (other != null && this.getClass() == other.getClass())
        {
            final BloatedNode that = (BloatedNode) other;
            // Here override the Atlas equality check in Node.equals() as the BloatedAtlas is always
            // empty and unique.
            return this.getIdentifier() == that.getIdentifier();
        }
        return false;
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public Location getLocation()
    {
        return this.location;
    }

    @Override
    public Map<String, String> getTags()
    {
        return this.tags;
    }

    @Override
    public SortedSet<Edge> inEdges()
    {
        return this.inEdgeIdentifiers.stream().map(BloatedEdge::new)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public SortedSet<Edge> outEdges()
    {
        return this.outEdgeIdentifiers.stream().map(BloatedEdge::new)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public Set<Relation> relations()
    {
        throw new UnsupportedOperationException();
    }

    public BloatedNode withIdentifier(final long identifier)
    {
        this.identifier = identifier;
        return this;
    }

    public BloatedNode withInEdgeIdentifiers(final SortedSet<Long> inEdgeIdentifiers)
    {
        this.inEdgeIdentifiers = inEdgeIdentifiers;
        return this;
    }

    public BloatedNode withLocation(final Location location)
    {
        this.location = location;
        return this;
    }

    public BloatedNode withOutEdgeIdentifiers(final SortedSet<Long> outEdgeIdentifiers)
    {
        this.outEdgeIdentifiers = outEdgeIdentifiers;
        return this;
    }

    public BloatedNode withRelationIdentifiers(final Set<Long> relationIdentifiers)
    {
        this.relationIdentifiers = relationIdentifiers;
        return this;
    }

    public BloatedNode withTags(final Map<String, String> tags)
    {
        this.tags = tags;
        return this;
    }
}
