package org.openstreetmap.atlas.geography.atlas.complete;

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
public class CompletePoint extends Point implements CompleteLocationItem
{
    private static final long serialVersionUID = 309534717673911086L;

    private Rectangle bounds;
    private long identifier;
    private Location location;
    private Map<String, String> tags;
    private Set<Long> relationIdentifiers;

    public static CompletePoint from(final Point point)
    {
        return new CompletePoint(point.getIdentifier(), point.getLocation(), point.getTags(), point
                .relations().stream().map(Relation::getIdentifier).collect(Collectors.toSet()));
    }

    public static CompletePoint shallowFrom(final Point point)
    {
        return new CompletePoint(point.getIdentifier())
                .withInitialBounds(point.getLocation().bounds());
    }

    CompletePoint(final long identifier)
    {
        this(identifier, null, null, null);
    }

    public CompletePoint(final Long identifier, final Location location,
            final Map<String, String> tags, final Set<Long> relationIdentifiers)
    {
        super(new EmptyAtlas());

        if (identifier == null)
        {
            throw new CoreException("Identifier can never be null.");
        }

        this.bounds = location != null ? location.bounds() : null;

        this.identifier = identifier;
        this.location = location;
        this.tags = tags;
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
        if (other instanceof CompletePoint)
        {
            final CompletePoint that = (CompletePoint) other;
            return CompleteEntity.basicEqual(this, that)
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
        return this.relationIdentifiers == null ? null : this.relationIdentifiers.stream()
                .map(CompleteRelation::new).collect(Collectors.toSet());
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + " [identifier=" + this.identifier + ", location="
                + this.location + ", tags=" + this.tags + ", relationIdentifiers="
                + this.relationIdentifiers + "]";
    }

    @Override
    public CompletePoint withAddedTag(final String key, final String value)
    {
        return withTags(CompleteEntity.addNewTag(getTags(), key, value));
    }

    @Override
    public CompletePoint withIdentifier(final long identifier)
    {
        this.identifier = identifier;
        return this;
    }

    @Override
    public CompletePoint withLocation(final Location location)
    {
        this.location = location;
        this.bounds = location.bounds();
        return this;
    }

    @Override
    public CompletePoint withRelationIdentifiers(final Set<Long> relationIdentifiers)
    {
        this.relationIdentifiers = relationIdentifiers;
        return this;
    }

    @Override
    public CompletePoint withRelations(final Set<Relation> relations)
    {
        this.relationIdentifiers = relations.stream().map(Relation::getIdentifier)
                .collect(Collectors.toSet());
        return this;
    }

    @Override
    public CompletePoint withRemovedTag(final String key)
    {
        return withTags(CompleteEntity.removeTag(getTags(), key));
    }

    @Override
    public CompletePoint withReplacedTag(final String oldKey, final String newKey,
            final String newValue)
    {
        return withRemovedTag(oldKey).withAddedTag(newKey, newValue);
    }

    @Override
    public CompletePoint withTags(final Map<String, String> tags)
    {
        this.tags = tags;
        return this;
    }

    private CompletePoint withInitialBounds(final Rectangle bounds)
    {
        this.bounds = bounds;
        return this;
    }
}
