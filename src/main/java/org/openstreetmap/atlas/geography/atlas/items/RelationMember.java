package org.openstreetmap.atlas.geography.atlas.items;

import org.apache.commons.lang3.StringUtils;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.geojson.GeoJsonFeature;
import org.openstreetmap.atlas.geography.geojson.GeoJsonType;
import org.openstreetmap.atlas.geography.geojson.GeoJsonUtils;

import com.google.gson.JsonObject;

/**
 * A {@link Relation} member. It has a role and an {@link AtlasEntity}.
 *
 * @author matthieun
 */
public class RelationMember implements Comparable<RelationMember>, Located, GeoJsonFeature
{
    private final String role;
    private final AtlasEntity entity;
    private final long relationIdentifier;

    public RelationMember(final String role, final AtlasEntity entity,
            final long relationIdentifier)
    {
        this.role = role;
        this.entity = entity;
        this.relationIdentifier = relationIdentifier;
    }

    @Override
    public Rectangle bounds()
    {
        return this.entity.bounds();
    }

    @Override
    public JsonObject asGeoJsonGeometry()
    {
        return this.entity.asGeoJsonGeometry();
    }

    @Override
    public JsonObject asGeoJson()
    {
        return GeoJsonUtils.feature(this.asGeoJsonGeometry(), this.getGeoJsonProperties());
    }

    @Override
    public int compareTo(final RelationMember other)
    {
        // Order by type first, then by identifier, then by role
        final int itemTypeValue1 = this.getEntity().getType().getValue();
        final int itemTypeValue2 = other.getEntity().getType().getValue();
        final int deltaTypeValue = itemTypeValue1 - itemTypeValue2;
        if (deltaTypeValue > 0)
        {
            return 1;
        }
        else if (deltaTypeValue < 0)
        {
            return -1;
        }
        else
        {
            final long identifier1 = this.getEntity().getIdentifier();
            final long identifier2 = other.getEntity().getIdentifier();
            final long delta = identifier1 - identifier2;
            if (delta > 0)
            {
                return 1;
            }
            else if (delta < 0)
            {
                return -1;
            }
            else
            {
                final String thisRole = this.getRole();
                final String otherRole = other.getRole();
                if (thisRole == null && otherRole == null)
                {
                    return 0;
                }
                if (thisRole == null)
                {
                    return -1;
                }
                if (otherRole == null)
                {
                    return 1;
                }
                return thisRole.compareTo(otherRole);
            }
        }
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof RelationMember)
        {
            final RelationMember that = (RelationMember) other;
            return StringUtils.equals(this.getRole(), that.getRole())
                    && this.getRelationIdentifier() == that.getRelationIdentifier()
                    && this.entity.getIdentifier() == that.getEntity().getIdentifier();
        }
        return false;
    }

    @Override
    public GeoJsonType getGeoJsonType()
    {
        return GeoJsonType.FEATURE;
    }

    @Override
    public JsonObject getGeoJsonProperties()
    {
        final JsonObject properties = this.entity.getGeoJsonProperties();
        properties.addProperty("role", this.role);
        return properties;
    }

    /**
     * @return The {@link AtlasEntity} pointed out by this relation. null if the {@link Atlas} that
     *         created it has a sliced {@link Relation}.
     */
    public AtlasEntity getEntity()
    {
        return this.entity;
    }

    public long getRelationIdentifier()
    {
        return this.relationIdentifier;
    }

    public String getRole()
    {
        return this.role;
    }

    @Override
    public int hashCode()
    {
        return this.role.hashCode() + this.entity.hashCode()
                + Long.hashCode(this.relationIdentifier);
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("{Member: ID = ");
        builder.append(this.getEntity().getIdentifier());
        builder.append(", Type = ");
        builder.append(this.getEntity().getType());
        builder.append(", Role = ");
        builder.append(this.getRole());
        builder.append("}");
        return builder.toString();
    }
}
