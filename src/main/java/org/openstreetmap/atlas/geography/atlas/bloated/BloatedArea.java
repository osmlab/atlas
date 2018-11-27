package org.openstreetmap.atlas.geography.atlas.bloated;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedAtlas.BloatedEntity;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Independent {@link Edge} that contains its own data. At scale, use at your own risk.
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
     * changed bounds, if preset. Each time with(Located) is called on this entity, it is recomputed
     * from the original bounds and the new Located bounds.
     */
    private Rectangle aggregateBounds;

    private long identifier;
    private Polygon polygon;
    private Map<String, String> tags;
    private Set<Long> relationIdentifiers;

    public static BloatedArea fromArea(final Area area)
    {
        return new BloatedArea(area.getIdentifier(), area.asPolygon(), area.getTags(),
                area.relations().stream().map(Relation::getIdentifier).collect(Collectors.toSet()));
    }

    public static BloatedArea shallowFromArea(final Area area)
    {
        return new BloatedArea(area.getIdentifier(), area.asPolygon());
    }

    BloatedArea(final long identifier)
    {
        this(identifier, null, null, null);
    }

    BloatedArea(final long identifier, final Polygon polygon)
    {
        this(identifier, polygon, null, null);
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
        this.aggregateBounds = polygon != null ? polygon.bounds() : null;

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

    public BloatedArea withTags(final Map<String, String> tags)
    {
        this.tags = tags;
        return this;
    }
}
