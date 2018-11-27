package org.openstreetmap.atlas.geography.atlas.bloated;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedAtlas.BloatedEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Independent {@link Edge} that contains its own data. At scale, use at your own risk.
 *
 * @author matthieun
 */
public class BloatedEdge extends Edge implements BloatedEntity
{
    private static final long serialVersionUID = 309534717673911086L;

    /*
     * We need to store the original entity bounds at creation-time. This is so multiple consecutive
     * with(Located) calls can update the aggregate bounds without including the bounds from the
     * overwritten change.
     */
    private Rectangle originalBounds;

    /*
     * This is the aggregate feature bounds. It is a super-bound of the original bounds and the
     * changed bounds, if preset. Each time with(Located) is called on this entity, it is recomputed
     * from the original bounds and the new Located bounds.
     */
    private Rectangle aggregateBounds;

    private long identifier;
    private PolyLine polyLine;
    private Map<String, String> tags;
    private Long startNodeIdentifier;
    private Long endNodeIdentifier;
    private Set<Long> relationIdentifiers;

    public static BloatedEdge fromEdge(final Edge edge)
    {
        return new BloatedEdge(edge.getIdentifier(), edge.asPolyLine(), edge.getTags(),
                edge.start().getIdentifier(), edge.end().getIdentifier(),
                edge.relations().stream().map(Relation::getIdentifier).collect(Collectors.toSet()));
    }

    public static BloatedEdge shallowFromEdge(final Edge edge)
    {
        return new BloatedEdge(edge.getIdentifier(), edge.asPolyLine().bounds());
    }

    BloatedEdge(final long identifier)
    {
        this(identifier, null, null, null, null, null);
    }

    BloatedEdge(final long identifier, final PolyLine polyLine)
    {
        this(identifier, polyLine, null, null, null, null);
    }

    public BloatedEdge(final Long identifier, final PolyLine polyLine,
            final Map<String, String> tags, final Long startNodeIdentifier,
            final Long endNodeIdentifier, final Set<Long> relationIdentifiers)
    {
        super(new BloatedAtlas());

        if (identifier == null)
        {
            throw new CoreException("Identifier can never be null.");
        }

        this.originalBounds = polyLine != null ? polyLine.bounds() : null;
        this.aggregateBounds = polyLine != null ? polyLine.bounds() : null;

        this.identifier = identifier;
        this.polyLine = polyLine;
        this.tags = tags;
        this.startNodeIdentifier = startNodeIdentifier;
        this.endNodeIdentifier = endNodeIdentifier;
        this.relationIdentifiers = relationIdentifiers;
    }

    @Override
    public PolyLine asPolyLine()
    {
        return this.polyLine;
    }

    @Override
    public Rectangle bounds()
    {
        return this.aggregateBounds;
    }

    @Override
    public Node end()
    {
        return this.endNodeIdentifier == null ? null : new BloatedNode(this.endNodeIdentifier);
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
    public Set<Relation> relations()
    {
        return this.relationIdentifiers == null ? null
                : this.relationIdentifiers.stream().map(BloatedRelation::new)
                        .collect(Collectors.toSet());
    }

    @Override
    public Node start()
    {
        return this.startNodeIdentifier == null ? null : new BloatedNode(this.startNodeIdentifier);
    }

    public BloatedEdge withEndNodeIdentifier(final Long endNodeIdentifier)
    {
        this.endNodeIdentifier = endNodeIdentifier;
        return this;
    }

    public BloatedEdge withIdentifier(final long identifier)
    {
        this.identifier = identifier;
        return this;
    }

    public BloatedEdge withPolyLine(final PolyLine polyLine)
    {
        this.polyLine = polyLine;
        if (this.originalBounds == null)
        {
            this.originalBounds = polyLine.bounds();
        }
        this.aggregateBounds = Rectangle.forLocated(this.originalBounds, polyLine.bounds());
        return this;
    }

    public BloatedEdge withRelationIdentifiers(final Set<Long> relationIdentifiers)
    {
        this.relationIdentifiers = relationIdentifiers;
        return this;
    }

    public BloatedEdge withStartNodeIdentifier(final Long startNodeIdentifier)
    {
        this.startNodeIdentifier = startNodeIdentifier;
        return this;
    }

    public BloatedEdge withTags(final Map<String, String> tags)
    {
        this.tags = tags;
        return this;
    }
}
