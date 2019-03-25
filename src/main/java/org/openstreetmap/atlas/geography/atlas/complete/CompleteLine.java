package org.openstreetmap.atlas.geography.atlas.complete;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Independent {@link Line} that contains its own data. At scale, use at your own risk.
 *
 * @author matthieun
 */
public class CompleteLine extends Line implements CompleteLineItem
{
    private static final long serialVersionUID = 309534717673911086L;

    private Rectangle bounds;
    private long identifier;
    private PolyLine polyLine;
    private Map<String, String> tags;
    private Set<Long> relationIdentifiers;

    public static CompleteLine from(final Line line)
    {
        return new CompleteLine(line.getIdentifier(), line.asPolyLine(), line.getTags(),
                line.relations().stream().map(Relation::getIdentifier).collect(Collectors.toSet()));
    }

    public static CompleteLine shallowFrom(final Line line)
    {
        return new CompleteLine(line.getIdentifier(), line.asPolyLine());
    }

    CompleteLine(final long identifier, final PolyLine polyLine)
    {
        this(identifier, polyLine, null, null);
    }

    public CompleteLine(final Long identifier, final PolyLine polyLine,
            final Map<String, String> tags, final Set<Long> relationIdentifiers)
    {
        super(new EmptyAtlas());

        if (identifier == null)
        {
            throw new CoreException("Identifier can never be null.");
        }

        if (polyLine == null)
        {
            throw new CoreException("PolyLine can never be null");
        }

        this.bounds = polyLine.bounds();

        this.identifier = identifier;
        this.polyLine = polyLine;
        this.tags = tags;
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
        return this.bounds;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof CompleteLine)
        {
            final CompleteLine that = (CompleteLine) other;
            return CompleteEntity.basicEqual(this, that)
                    && Objects.equals(this.asPolyLine(), that.asPolyLine());
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
        return this.tags == null && this.relationIdentifiers == null;
    }

    @Override
    public Set<Relation> relations()
    {
        /*
         * Disregard the CompleteRelation bounds parameter (Rectangle.MINIMUM) here. We must provide
         * bounds to the CompleteRelation constructor to satisfy the API contract. However, the
         * bounds provided here do not reflect the true bounds of the relation with this identifier.
         * We would need an atlas context to actually compute the proper bounds. Effectively, the
         * CompleteRelations returned by the method are just wrappers around an identifier.
         */
        return this.relationIdentifiers == null ? null
                : this.relationIdentifiers.stream()
                        .map(relationIdentifier -> new CompleteRelation(relationIdentifier,
                                Rectangle.MINIMUM))
                        .collect(Collectors.toSet());
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + " [identifier=" + this.identifier + ", polyLine="
                + this.polyLine + ", tags=" + this.tags + ", relationIdentifiers="
                + this.relationIdentifiers + "]";
    }

    @Override
    public CompleteLine withAddedTag(final String key, final String value)
    {
        return withTags(CompleteEntity.addNewTag(getTags(), key, value));
    }

    public CompleteLine withBoundsExtendedBy(final Rectangle bounds)
    {
        if (this.bounds == null)
        {
            this.bounds = bounds;
        }
        this.bounds = Rectangle.forLocated(this.bounds, bounds);
        return this;
    }

    @Override
    public CompleteLine withIdentifier(final long identifier)
    {
        this.identifier = identifier;
        return this;
    }

    @Override
    public CompleteLine withPolyLine(final PolyLine polyLine)
    {
        this.polyLine = polyLine;
        this.bounds = polyLine.bounds();
        return this;
    }

    @Override
    public CompleteLine withRelationIdentifiers(final Set<Long> relationIdentifiers)
    {
        this.relationIdentifiers = relationIdentifiers;
        return this;
    }

    @Override
    public CompleteLine withRelations(final Set<Relation> relations)
    {
        this.relationIdentifiers = relations.stream().map(Relation::getIdentifier)
                .collect(Collectors.toSet());
        return this;
    }

    @Override
    public CompleteLine withRemovedTag(final String key)
    {
        return withTags(CompleteEntity.removeTag(getTags(), key));
    }

    @Override
    public CompleteLine withReplacedTag(final String oldKey, final String newKey,
            final String newValue)
    {
        return withRemovedTag(oldKey).withAddedTag(newKey, newValue);
    }

    @Override
    public CompleteLine withTags(final Map<String, String> tags)
    {
        this.tags = tags;
        return this;
    }
}
