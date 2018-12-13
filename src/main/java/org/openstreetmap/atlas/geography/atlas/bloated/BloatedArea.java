package org.openstreetmap.atlas.geography.atlas.bloated;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Independent {@link Area} that contains its own data. At scale, use at your own risk.
 *
 * @author matthieun
 */
public class BloatedArea extends Area implements BloatedEntity
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
    private Polygon polygon;
    private Map<String, String> tags;
    private Set<Long> relationIdentifiers;

    public static BloatedArea from(final Area area)
    {
        return new BloatedArea(area.getIdentifier(), area.asPolygon(), area.getTags(),
                area.relations().stream().map(Relation::getIdentifier).collect(Collectors.toSet()));
    }

    public static BloatedArea shallowFrom(final Area area)
    {
        return new BloatedArea(area.getIdentifier()).withInitialBounds(area.asPolygon().bounds());
    }

    BloatedArea(final long identifier)
    {
        this(identifier, null, null, null);
    }

    public BloatedArea(final Long identifier, final Polygon polygon, final Map<String, String> tags,
            final Set<Long> relationIdentifiers)
    {
        super(new BloatedAtlas());

        if (identifier == null)
        {
            throw new CoreException("Identifier can never be null.");
        }

        this.originalBounds = polygon != null ? polygon.bounds() : null;
        this.aggregateBounds = this.originalBounds;

        this.identifier = identifier;
        this.polygon = polygon;
        this.tags = tags;
        this.relationIdentifiers = relationIdentifiers;
    }

    @Override
    public Polygon asPolygon()
    {
        return this.polygon;
    }

    @Override
    public Rectangle bounds()
    {
        return this.aggregateBounds;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof BloatedArea)
        {
            final BloatedArea that = (BloatedArea) other;
            return BloatedEntity.basicEqual(this, that)
                    && Objects.equals(this.asPolygon(), that.asPolygon());
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
        return this.polygon == null && this.tags == null && this.relationIdentifiers == null;
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
        return "BloatedArea [identifier=" + this.identifier + ", polygon=" + this.polygon
                + ", tags=" + this.tags + ", relationIdentifiers=" + this.relationIdentifiers + "]";
    }

    public BloatedArea withAddedTag(final String key, final String value)
    {
        return withTags(BloatedEntity.addNewTag(getTags(), key, value));
    }

    public BloatedArea withAggregateBoundsExtendedUsing(final Rectangle bounds)
    {
        if (this.aggregateBounds == null)
        {
            this.aggregateBounds = bounds;
        }
        this.aggregateBounds = Rectangle.forLocated(this.aggregateBounds, bounds);
        return this;
    }

    public BloatedArea withIdentifier(final long identifier)
    {
        this.identifier = identifier;
        return this;
    }

    public BloatedArea withPolygon(final Polygon polygon)
    {
        this.polygon = polygon;
        if (this.originalBounds == null)
        {
            this.originalBounds = polygon.bounds();
        }
        this.aggregateBounds = Rectangle.forLocated(this.originalBounds, polygon.bounds());
        return this;
    }

    public BloatedArea withRelationIdentifiers(final Set<Long> relationIdentifiers)
    {
        this.relationIdentifiers = relationIdentifiers;
        return this;
    }

    public BloatedArea withRelations(final Set<Relation> relations)
    {
        this.relationIdentifiers = relations.stream().map(Relation::getIdentifier)
                .collect(Collectors.toSet());
        return this;
    }

    public BloatedArea withRemovedTag(final String key)
    {
        return withTags(BloatedEntity.removeTag(getTags(), key));
    }

    public BloatedArea withReplacedTag(final String oldKey, final String newKey,
            final String newValue)
    {
        return withRemovedTag(oldKey).withAddedTag(newKey, newValue);
    }

    public BloatedArea withTags(final Map<String, String> tags)
    {
        this.tags = tags;
        return this;
    }

    private BloatedArea withInitialBounds(final Rectangle bounds)
    {
        this.originalBounds = bounds;
        this.aggregateBounds = bounds;
        return this;
    }
}
