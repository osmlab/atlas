package org.openstreetmap.atlas.geography.atlas.complete;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Independent {@link Edge} that contains its own data. At scale, use at your own risk.
 *
 * @author matthieun
 */
public class CompleteEdge extends Edge implements CompleteLineItem
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
     * changed bounds, if present. Each time with(Located) is called on this entity, it is
     * recomputed from the original bounds and the new Located bounds.
     */
    private Rectangle aggregateBounds;

    private long identifier;
    private PolyLine polyLine;
    private Map<String, String> tags;
    private Long startNodeIdentifier;
    private Long endNodeIdentifier;
    private Set<Long> relationIdentifiers;

    public static CompleteEdge from(final Edge edge)
    {
        return new CompleteEdge(edge.getIdentifier(), edge.asPolyLine(), edge.getTags(),
                edge.start().getIdentifier(), edge.end().getIdentifier(),
                edge.relations().stream().map(Relation::getIdentifier).collect(Collectors.toSet()));
    }

    public static CompleteEdge shallowFrom(final Edge edge)
    {
        return new CompleteEdge(edge.getIdentifier()).withInitialBounds(edge.asPolyLine().bounds());
    }

    CompleteEdge(final long identifier)
    {
        this(identifier, null, null, null, null, null);
    }

    public CompleteEdge(final Long identifier, final PolyLine polyLine,
            final Map<String, String> tags, final Long startNodeIdentifier,
            final Long endNodeIdentifier, final Set<Long> relationIdentifiers)
    {
        super(new EmptyAtlas());

        if (identifier == null)
        {
            throw new CoreException("Identifier can never be null.");
        }

        this.originalBounds = polyLine != null ? polyLine.bounds() : null;
        this.aggregateBounds = this.originalBounds;

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
        return this.endNodeIdentifier == null ? null : new CompleteNode(this.endNodeIdentifier);
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof CompleteEdge)
        {
            final CompleteEdge that = (CompleteEdge) other;
            return CompleteEntity.basicEqual(this, that)
                    && Objects.equals(this.asPolyLine(), that.asPolyLine())
                    && CompleteEntity.equalThroughGet(this.start(), that.start(),
                            Node::getIdentifier)
                    && CompleteEntity.equalThroughGet(this.end(), that.end(), Node::getIdentifier);
        }
        return false;
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
    public boolean isSuperShallow()
    {
        return this.polyLine == null && this.tags == null && this.startNodeIdentifier == null
                && this.endNodeIdentifier == null && this.relationIdentifiers == null;
    }

    @Override
    public Set<Relation> relations()
    {
        return this.relationIdentifiers == null ? null
                : this.relationIdentifiers.stream().map(CompleteRelation::new)
                        .collect(Collectors.toSet());
    }

    @Override
    public Node start()
    {
        return this.startNodeIdentifier == null ? null : new CompleteNode(this.startNodeIdentifier);
    }

    @Override
    public String toString()
    {
        return "BloatedEdge [identifier=" + this.identifier + ", startNodeIdentifier="
                + this.startNodeIdentifier + ", endNodeIdentifier=" + this.endNodeIdentifier
                + ", polyLine=" + this.polyLine + ", tags=" + this.tags + ", relationIdentifiers="
                + this.relationIdentifiers + "]";
    }

    @Override
    public CompleteEdge withAddedTag(final String key, final String value)
    {
        return withTags(CompleteEntity.addNewTag(getTags(), key, value));
    }

    @Override
    public CompleteEdge withAggregateBoundsExtendedUsing(final Rectangle bounds)
    {
        if (this.aggregateBounds == null)
        {
            this.aggregateBounds = bounds;
        }
        this.aggregateBounds = Rectangle.forLocated(this.aggregateBounds, bounds);
        return this;
    }

    public CompleteEdge withEndNodeIdentifier(final Long endNodeIdentifier)
    {
        this.endNodeIdentifier = endNodeIdentifier;
        return this;
    }

    @Override
    public CompleteEdge withIdentifier(final long identifier)
    {
        this.identifier = identifier;
        return this;
    }

    @Override
    public CompleteEdge withPolyLine(final PolyLine polyLine)
    {
        this.polyLine = polyLine;
        if (this.originalBounds == null)
        {
            this.originalBounds = polyLine.bounds();
        }
        this.aggregateBounds = Rectangle.forLocated(this.originalBounds, polyLine.bounds());
        return this;
    }

    @Override
    public CompleteEdge withRelationIdentifiers(final Set<Long> relationIdentifiers)
    {
        this.relationIdentifiers = relationIdentifiers;
        return this;
    }

    @Override
    public CompleteEdge withRelations(final Set<Relation> relations)
    {
        this.relationIdentifiers = relations.stream().map(Relation::getIdentifier)
                .collect(Collectors.toSet());
        return this;
    }

    @Override
    public CompleteEdge withRemovedTag(final String key)
    {
        return withTags(CompleteEntity.removeTag(getTags(), key));
    }

    @Override
    public CompleteEdge withReplacedTag(final String oldKey, final String newKey,
            final String newValue)
    {
        return withRemovedTag(oldKey).withAddedTag(newKey, newValue);
    }

    public CompleteEdge withStartNodeIdentifier(final Long startNodeIdentifier)
    {
        this.startNodeIdentifier = startNodeIdentifier;
        return this;
    }

    @Override
    public CompleteEdge withTags(final Map<String, String> tags)
    {
        this.tags = tags;
        return this;
    }

    private CompleteEdge withInitialBounds(final Rectangle bounds)
    {
        this.originalBounds = bounds;
        this.aggregateBounds = bounds;
        return this;
    }
}
