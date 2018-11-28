package org.openstreetmap.atlas.geography.atlas.bloated;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Independent {@link Edge} that contains its own data. At scale, use at your own risk.
 *
 * @author matthieun
 */
public class BloatedLine extends Line implements BloatedEntity
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
    private Set<Long> relationIdentifiers;

    public static BloatedLine fromLine(final Line line)
    {
        return new BloatedLine(line.getIdentifier(), line.asPolyLine(), line.getTags(),
                line.relations().stream().map(Relation::getIdentifier).collect(Collectors.toSet()));
    }

    public static BloatedLine shallowFromLine(final Line line)
    {
        return new BloatedLine(line.getIdentifier()).withInitialBounds(line.asPolyLine().bounds());
    }

    BloatedLine(final long identifier)
    {
        this(identifier, null, null, null);
    }

    public BloatedLine(final Long identifier, final PolyLine polyLine,
            final Map<String, String> tags, final Set<Long> relationIdentifiers)
    {
        super(new BloatedAtlas());

        if (identifier == null)
        {
            throw new CoreException("Identifier can never be null.");
        }

        this.originalBounds = polyLine != null ? polyLine.bounds() : null;
        this.aggregateBounds = this.originalBounds;

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
        return this.aggregateBounds;
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
    public boolean isSuperShallow()
    {
        return this.polyLine == null && this.tags == null && this.relationIdentifiers == null;
    }

    @Override
    public Set<Relation> relations()
    {
        return this.relationIdentifiers == null ? null
                : this.relationIdentifiers.stream().map(BloatedRelation::new)
                        .collect(Collectors.toSet());
    }

    @Override
    public String toString()
    {
        return "BloatedLine [identifier=" + this.identifier + ", polyLine=" + this.polyLine
                + ", tags=" + this.tags + ", relationIdentifiers=" + this.relationIdentifiers + "]";
    }

    public BloatedLine withIdentifier(final long identifier)
    {
        this.identifier = identifier;
        return this;
    }

    public BloatedLine withPolyLine(final PolyLine polyLine)
    {
        this.polyLine = polyLine;
        if (this.originalBounds == null)
        {
            this.originalBounds = polyLine.bounds();
        }
        this.aggregateBounds = Rectangle.forLocated(this.originalBounds, polyLine.bounds());
        return this;
    }

    public BloatedLine withRelationIdentifiers(final Set<Long> relationIdentifiers)
    {
        this.relationIdentifiers = relationIdentifiers;
        return this;
    }

    public BloatedLine withTags(final Map<String, String> tags)
    {
        this.tags = tags;
        return this;
    }

    private BloatedLine withInitialBounds(final Rectangle bounds)
    {
        this.originalBounds = bounds;
        this.aggregateBounds = bounds;
        return this;
    }
}
