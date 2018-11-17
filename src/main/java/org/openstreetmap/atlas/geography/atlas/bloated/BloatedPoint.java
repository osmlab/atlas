package org.openstreetmap.atlas.geography.atlas.bloated;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedAtlas.BloatedEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Independent {@link Edge} that contains its own data. At scale, use at your own risk.
 *
 * @author matthieun
 */
public class BloatedPoint extends Point implements BloatedEntity
{
    private static final long serialVersionUID = 309534717673911086L;

    private Rectangle bounds;

    private long identifier;
    private Location location;
    private Map<String, String> tags;
    private Set<Long> relationIdentifiers;

    public static BloatedPoint fromPoint(final Point point)
    {
        return new BloatedPoint(point.getIdentifier(), point.getLocation(), point.getTags(), point
                .relations().stream().map(Relation::getIdentifier).collect(Collectors.toSet()));
    }

    public static BloatedPoint shallowFromPoint(final Point point)
    {
        return new BloatedPoint(point.getIdentifier()).withBounds(point.getLocation().bounds());
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
            throw new CoreException("Identifier is the only parameter that cannot be null.");
        }

        this.bounds = location == null ? null : location.bounds();

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
    public Set<Relation> relations()
    {
        return this.relationIdentifiers == null ? null
                : this.relationIdentifiers.stream().map(BloatedRelation::new)
                        .collect(Collectors.toSet());
    }

    public BloatedPoint withIdentifier(final long identifier)
    {
        this.identifier = identifier;
        return this;
    }

    public BloatedPoint withLocation(final Location location)
    {
        this.location = location;
        this.bounds = location.bounds();
        return this;
    }

    public BloatedPoint withRelationIdentifiers(final Set<Long> relationIdentifiers)
    {
        this.relationIdentifiers = relationIdentifiers;
        return this;
    }

    public BloatedPoint withTags(final Map<String, String> tags)
    {
        this.tags = tags;
        return this;
    }

    private BloatedPoint withBounds(final Rectangle bounds)
    {
        this.bounds = bounds;
        return this;
    }
}
