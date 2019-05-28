package org.openstreetmap.atlas.geography.atlas.items;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.GeometryPrintable;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.ReverseIdentifierFactory;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.LocationIterableProperties;
import org.openstreetmap.atlas.geography.geojson.GeoJsonFeature;
import org.openstreetmap.atlas.geography.geojson.GeoJsonType;
import org.openstreetmap.atlas.geography.geojson.GeoJsonUtils;
import org.openstreetmap.atlas.tags.LastEditTimeTag;
import org.openstreetmap.atlas.tags.LastEditUserIdentifierTag;
import org.openstreetmap.atlas.tags.LastEditUserNameTag;
import org.openstreetmap.atlas.tags.names.NameTag;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.time.Time;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * A located entity with tags
 *
 * @author matthieun
 * @author mgostintsev
 * @author Sid
 * @author hallahan
 */
public abstract class AtlasEntity
        implements AtlasObject, DiffViewFriendlyItem, GeometryPrintable, GeoJsonFeature
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

    /**
     * A method that creates properties for a GeoJSON Feature from the tags.
     *
     * @return A GeoJSON properties object that is to be put in a Feature.
     */
    @Override
    public JsonObject getGeoJsonProperties()
    {
        final JsonObject properties = new JsonObject();
        getTags().forEach(properties::addProperty);
        properties.addProperty(GeoJsonUtils.IDENTIFIER, getIdentifier());
        properties.addProperty(GeoJsonUtils.OSM_IDENTIFIER, getOsmIdentifier());
        properties.addProperty(GeoJsonUtils.ITEM_TYPE, String.valueOf(getType()));

        final Set<Relation> relations = relations();
        if (relations.size() > 0)
        {
            final JsonArray relationsArray = new JsonArray();
            properties.add("relations", relationsArray);
            for (final Relation relation : relations)
            {
                final JsonObject relationObject = relation.getGeoJsonPropertiesWithoutMembers();
                relationsArray.add(relationObject);
            }
        }

        return properties;
    }

    @Override
    public GeoJsonType getGeoJsonType()
    {
        return GeoJsonType.FEATURE;
    }

    /**
     * The value in the "name" attribute.
     *
     * @return an optional string representing the value of the name tag.
     */
    public Optional<String> getName()
    {
        return this.getTag(NameTag.KEY);
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

    @Override
    public String toDiffViewFriendlyString()
    {
        throw new UnsupportedOperationException(
                "This operation is not supported for type " + this.getClass().getName());
    }

    /**
     * @return The {@link LocationIterableProperties} for this {@link AtlasEntity}
     */
    public abstract LocationIterableProperties toGeoJsonBuildingBlock();

    protected String parentRelationsAsDiffViewFriendlyString()
    {
        final StringList relationIds = new StringList();
        for (final Relation relation : this.relations())
        {
            relationIds.add(relation.getIdentifier());
        }
        final String relationsString = relationIds.join(",");

        return relationsString;
    }

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
