package org.openstreetmap.atlas.geography.sharding;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.geojson.GeoJsonType;
import org.openstreetmap.atlas.utilities.collections.StringList;

import com.google.gson.JsonObject;

/**
 * Shard and country pair
 *
 * @author matthieun
 */
public class CountryShard implements Shard
{
    private static final long serialVersionUID = -4158215940506552768L;

    // This is the separator between the country code and the shard name: COUNTRY_Z-X-Y in case of a
    // SlippyTile for a Shard.
    public static final String COUNTRY_SHARD_SEPARATOR = "_";

    private final Shard shard;
    private final String country;

    public static CountryShard forName(final String name)
    {
        final StringList split = StringList.split(name, COUNTRY_SHARD_SEPARATOR);
        return new CountryShard(split.get(0), SlippyTile.forName(split.get(1)));
    }

    public CountryShard(final String country, final Shard shard)
    {
        if (shard == null || country == null)
        {
            throw new CoreException("Cannot have null parameters: Country = {} and Shard = {}",
                    country, shard);
        }
        this.shard = shard;
        this.country = country;
    }

    @Override
    public JsonObject asGeoJson()
    {
        return this.shard.asGeoJson();
    }

    @Override
    public Rectangle bounds()
    {
        return this.shard.bounds();
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof CountryShard)
        {
            final CountryShard that = (CountryShard) other;
            return this.getCountry().equals(that.getCountry())
                    && this.getShard().equals(that.getShard());
        }
        return false;
    }

    public String getCountry()
    {
        return this.country;
    }

    @Override
    public GeoJsonType getGeoJsonType()
    {
        return this.shard.getGeoJsonType();
    }

    @Override
    public String getName()
    {
        return this.country + COUNTRY_SHARD_SEPARATOR + this.shard.getName();
    }

    public Shard getShard()
    {
        return this.shard;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(this.shard).append(this.country).hashCode();
    }

    @Override
    public String toString()
    {
        return getName();
    }

    @Override
    public byte[] toWkb()
    {
        return this.shard.toWkb();
    }

    @Override
    public String toWkt()
    {
        return this.shard.toWkt();
    }
}
