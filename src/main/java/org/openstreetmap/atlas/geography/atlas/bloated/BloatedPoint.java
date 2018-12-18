package org.openstreetmap.atlas.geography.atlas.bloated;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Independent {@link Point} that contains its own data. At scale, use at your own risk.
 *
 * @author matthieun
 */
public class BloatedPoint extends Point implements BloatedEntity
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
    private Location location;
    private Map<String, String> tags;
    private Set<Long> relationIdentifiers;

    public static BloatedPoint from(final Point point)
    {
        return new BloatedPoint(point.getIdentifier(), point.getLocation(), point.getTags(), point
                .relations().stream().map(Relation::getIdentifier).collect(Collectors.toSet()));
    }

    public static BloatedPoint shallowFrom(final Point point)
    {
        return new BloatedPoint(point.getIdentifier())
                .withInitialBounds(point.getLocation().bounds());
    }

    BloatedPoint(final long identifier)
    {
        this(identifier, null, null, null);
    }

    public BloatedPoint(final Long identifier, final Location location,
            final Map<String, String> tags, final Set<Long> relationIdentifiers)
    {
        super(new BloatedAtlas());

        if (identifier == null)
        {
            throw new CoreException("Identifier can never be null.");
        }

        this.originalBounds = location != null ? location.bounds() : null;
        this.aggregateBounds = this.originalBounds;

        this.identifier = identifier;
        this.location = location;
        this.tags = tags;
        this.relationIdentifiers = relationIdentifiers;
    }

    @Override
    public Rectangle bounds()
    {
        return this.aggregateBounds;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof BloatedPoint)
        {
            final BloatedPoint that = (BloatedPoint) other;
            return BloatedEntity.basicEqual(this, that)
                    && Objects.equals(this.getLocation(), that.getLocation());
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
    public int hashCode()
    {
        return super.hashCode();
    }

    @Override
    public boolean isSuperShallow()
    {
        return this.location == null && this.tags == null && this.relationIdentifiers == null;
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
        return "BloatedPoint [identifier=" + this.identifier + ", location=" + this.location
                + ", tags=" + this.tags + ", relationIdentifiers=" + this.relationIdentifiers + "]";
    }

    public BloatedPoint withAddedTag(final String key, final String value)
    {
        return withTags(BloatedEntity.addNewTag(getTags(), key, value));
    }

    public BloatedPoint withAggregateBoundsExtendedUsing(final Rectangle bounds)
    {
        if (this.aggregateBounds == null)
        {
            this.aggregateBounds = bounds;
        }
        this.aggregateBounds = Rectangle.forLocated(this.aggregateBounds, bounds);
        return this;
    }

    public BloatedPoint withIdentifier(final long identifier)
    {
        this.identifier = identifier;
        return this;
    }

    public BloatedPoint withLocation(final Location location)
    {
        this.location = location;
        if (this.originalBounds == null)
        {
            this.originalBounds = location.bounds();
        }
        this.aggregateBounds = Rectangle.forLocated(this.originalBounds, location.bounds());
        return this;
    }

    public BloatedPoint withRelationIdentifiers(final Set<Long> relationIdentifiers)
    {
        this.relationIdentifiers = relationIdentifiers;
        return this;
    }

    public BloatedPoint withRelations(final Set<Relation> relations)
    {
        this.relationIdentifiers = relations.stream().map(Relation::getIdentifier)
                .collect(Collectors.toSet());
        return this;
    }

    public BloatedPoint withRemovedTag(final String key)
    {
        return withTags(BloatedEntity.removeTag(getTags(), key));
    }

    public BloatedPoint withReplacedTag(final String oldKey, final String newKey,
            final String newValue)
    {
        return withRemovedTag(oldKey).withAddedTag(newKey, newValue);
    }

    public BloatedPoint withTags(final Map<String, String> tags)
    {
        this.tags = tags;
        return this;
    }

    private BloatedPoint withInitialBounds(final Rectangle bounds)
    {
        this.originalBounds = bounds;
        this.aggregateBounds = bounds;
        return this;
    }
}
