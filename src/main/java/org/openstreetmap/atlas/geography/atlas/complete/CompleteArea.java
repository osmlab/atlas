package org.openstreetmap.atlas.geography.atlas.complete;

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
public class CompleteArea extends Area implements CompleteEntity
{
    private static final long serialVersionUID = 309534717673911086L;

    private Rectangle bounds;
    private long identifier;
    private Polygon polygon;
    private Map<String, String> tags;
    private Set<Long> relationIdentifiers;

    public static CompleteArea from(final Area area)
    {
        return new CompleteArea(area.getIdentifier(), area.asPolygon(), area.getTags(),
                area.relations().stream().map(Relation::getIdentifier).collect(Collectors.toSet()));
    }

    public static CompleteArea shallowFrom(final Area area)
    {
        return new CompleteArea(area.getIdentifier(), area.asPolygon());
    }

    CompleteArea(final long identifier, final Polygon polygon)
    {
        this(identifier, polygon, null, null);
    }

    public CompleteArea(final Long identifier, final Polygon polygon,
            final Map<String, String> tags, final Set<Long> relationIdentifiers)
    {
        super(new EmptyAtlas());

        if (identifier == null)
        {
            throw new CoreException("Identifier can never be null.");
        }

        if (polygon == null)
        {
            throw new CoreException("Polygon can never be null.");
        }

        this.bounds = polygon.bounds();

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
        return this.bounds;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof CompleteArea)
        {
            final CompleteArea that = (CompleteArea) other;
            return CompleteEntity.basicEqual(this, that)
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
        return this.relationIdentifiers == null ? null : this.relationIdentifiers.stream()
                .map(CompleteRelation::new).collect(Collectors.toSet());
    }

    @Override
    public String toString()
    {
        return this.getClass().getSimpleName() + " [identifier=" + this.identifier + ", polygon="
                + this.polygon + ", tags=" + this.tags + ", relationIdentifiers="
                + this.relationIdentifiers + "]";
    }

    @Override
    public CompleteArea withAddedTag(final String key, final String value)
    {
        return withTags(CompleteEntity.addNewTag(getTags(), key, value));
    }

    @Override
    public CompleteArea withIdentifier(final long identifier)
    {
        this.identifier = identifier;
        return this;
    }

    public CompleteArea withPolygon(final Polygon polygon)
    {
        this.polygon = polygon;
        this.bounds = polygon.bounds();
        return this;
    }

    @Override
    public CompleteArea withRelationIdentifiers(final Set<Long> relationIdentifiers)
    {
        this.relationIdentifiers = relationIdentifiers;
        return this;
    }

    @Override
    public CompleteArea withRelations(final Set<Relation> relations)
    {
        this.relationIdentifiers = relations.stream().map(Relation::getIdentifier)
                .collect(Collectors.toSet());
        return this;
    }

    @Override
    public CompleteArea withRemovedTag(final String key)
    {
        return withTags(CompleteEntity.removeTag(getTags(), key));
    }

    @Override
    public CompleteArea withReplacedTag(final String oldKey, final String newKey,
            final String newValue)
    {
        return withRemovedTag(oldKey).withAddedTag(newKey, newValue);
    }

    @Override
    public CompleteArea withTags(final Map<String, String> tags)
    {
        this.tags = tags;
        return this;
    }
}
