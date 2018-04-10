package org.openstreetmap.atlas.geography.atlas.items;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.ReverseIdentifierFactory;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.LocationIterableProperties;
import org.openstreetmap.atlas.tags.LastEditTimeTag;
import org.openstreetmap.atlas.tags.LastEditUserIdentifierTag;
import org.openstreetmap.atlas.tags.LastEditUserNameTag;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.time.Time;

/**
 * A located entity with tags
 *
 * @author matthieun
 * @author mgostintsev
 * @author Sid
 */
public abstract class AtlasEntity implements AtlasObject
{
    private static final long serialVersionUID = -6072525057489468736L;

    // The atlas this item belongs to
    private final Atlas atlas;

    protected AtlasEntity(final Atlas atlas)
    {
        this.atlas = atlas;
    }

    /**
     * Utility function to test if an entity's tags match some given tag keys.
     *
     * @param matches
     *            The given tag keys to match
     * @return True if at least one tag of the entity matches one of the given keys
     */
    public boolean containsKey(final Iterable<String> matches)
    {
        final Map<String, String> tags = this.getTags();
        for (final String candidate : matches)
        {
            if (tags.containsKey(candidate))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Utility function to test if an entity's tags start with some given tag keys.
     *
     * @param matches
     *            The given tag keys to match
     * @return True if at least one tag of the entity starts with one of the given keys
     */
    public boolean containsKeyStartsWith(final Iterable<String> matches)
    {
        final Map<String, String> tags = this.getTags();
        for (final String candidate : matches)
        {
            for (final String key : tags.keySet())
            {
                if (key.startsWith(candidate))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * For comparison, we require the AtlasEntities to be from the same instance of Atlas. Edges
     * with same attributes from two instances of Atlas (with same data) are considered different
     */
    @Override
    public boolean equals(final Object other)
    {
        if (this == other)
        {
            return true;
        }
        if (other != null && this.getClass() == other.getClass())
        {
            final AtlasEntity that = (AtlasEntity) other;
            // Do not call atlas.equals() which would browse all the items and create a stack
            // overflow
            return this.getAtlas() == that.getAtlas()
                    && this.getIdentifier() == that.getIdentifier();
        }
        return false;
    }

    @Override
    public Atlas getAtlas()
    {
        return this.atlas;
    }

    @Override
    public long getOsmIdentifier()
    {
        return new ReverseIdentifierFactory().getOsmIdentifier(this.getIdentifier());
    }

    @Override
    public Optional<String> getTag(final String key)
    {
        return Optional.ofNullable(getTags().get(key));
    }

    public abstract ItemType getType();

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(getIdentifier()).append(getClass()).hashCode();
    }

    /**
     * Return true if the entity intersects the geometricSurface. If it is a {@link LocationItem},
     * the polygon fully encloses it. For a {@link LineItem} the geometricSurface overlaps it. For
     * an {@link Area} the geometricSurface overlaps it. For a relation, at least one member of the
     * relation returns true to this method.
     *
     * @param surface
     *            The {@link GeometricSurface} to test
     * @return True if it intersects
     */
    public abstract boolean intersects(GeometricSurface surface);

    /**
     * @return If available, the {@link Time} at which the entity was last edited.
     */
    public Optional<Time> lastEdit()
    {
        final String tag = this.tag(LastEditTimeTag.KEY);
        if (tag == null)
        {
            return Optional.empty();
        }
        return Optional.of(new Time(Duration.milliseconds(Long.valueOf(tag))));
    }

    /**
     * @return If available, the identifier of the last user.
     */
    public Optional<String> lastUserIdentifier()
    {
        final String tag = this.tag(LastEditUserIdentifierTag.KEY);
        if (tag == null)
        {
            return Optional.empty();
        }
        return Optional.of(tag);
    }

    /**
     * @return If available, the name of the last user.
     */
    public Optional<String> lastUserName()
    {
        final String tag = this.tag(LastEditUserNameTag.KEY);
        if (tag == null)
        {
            return Optional.empty();
        }
        return Optional.of(tag);
    }

    /**
     * @return All the relations this {@link AtlasEntity} is member of.
     */
    public abstract Set<Relation> relations();

    /**
     * @return The {@link LocationIterableProperties} for this {@link AtlasEntity}
     */
    public abstract LocationIterableProperties toGeoJsonBuildingBlock();

    protected String tagString()
    {
        final StringBuilder builder = new StringBuilder();
        final Map<String, String> tags = getTags();
        int index = 0;
        builder.append("[Tags: ");
        for (final String key : tags.keySet())
        {
            final String value = tags.get(key);
            builder.append("[");
            builder.append(key);
            builder.append(" => ");
            builder.append(value);
            builder.append("]");
            if (index < tags.size() - 1)
            {
                builder.append(", ");
            }
            index++;
        }
        builder.append("]");
        return builder.toString();
    }

}
