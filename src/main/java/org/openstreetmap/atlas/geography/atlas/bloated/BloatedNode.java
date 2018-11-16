package org.openstreetmap.atlas.geography.atlas.bloated;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedAtlas.BloatedEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Independent {@link Node} that contains its own data. At scale, use at your own risk.
 *
 * @author matthieun
 */
public class BloatedNode extends Node implements BloatedEntity
{
    private static final long serialVersionUID = -8229589987121555419L;

    private Rectangle bounds;

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

    public static BloatedNode shallowFromNode(final Node node)
    {
        return new BloatedNode(node.getIdentifier()).withBounds(node.getLocation().bounds());
    }

    BloatedNode(final long identifier)
    {
        this(identifier, null, null, null, null, null);
    }

    public BloatedNode(final Long identifier, final Location location,
            final Map<String, String> tags, final SortedSet<Long> inEdgeIdentifiers,
            final SortedSet<Long> outEdgeIdentifiers, final Set<Long> relationIdentifiers)
    {
        super(new BloatedAtlas());

        if (identifier == null)
        {
            throw new CoreException("Identifier is the only parameter that cannot be null.");
        }

        this.bounds = location == null ? null : location.bounds();

        this.identifier = identifier;
        this.location = location;
        this.tags = tags;
        this.inEdgeIdentifiers = inEdgeIdentifiers;
        this.outEdgeIdentifiers = outEdgeIdentifiers;
        this.relationIdentifiers = relationIdentifiers;
    }

    @Override
    public Rectangle bounds()
    {
        return this.bounds;
    }

    @Override
    public boolean equals(final Object other)
    {
        return BloatedAtlas.equals(this, other);
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
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public SortedSet<Edge> inEdges()
    {
        return this.inEdgeIdentifiers == null ? null
                : this.inEdgeIdentifiers.stream().map(BloatedEdge::new)
                        .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public SortedSet<Edge> outEdges()
    {
        return this.outEdgeIdentifiers == null ? null
                : this.outEdgeIdentifiers.stream().map(BloatedEdge::new)
                        .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public Set<Relation> relations()
    {
        return this.relationIdentifiers == null ? null
                : this.relationIdentifiers.stream().map(BloatedRelation::new)
                        .collect(Collectors.toSet());
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
        this.bounds = location.bounds();
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

    private BloatedNode withBounds(final Rectangle bounds)
    {
        this.bounds = bounds;
        return this;
    }
}
