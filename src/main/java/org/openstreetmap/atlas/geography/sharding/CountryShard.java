package org.openstreetmap.atlas.geography.sharding;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.geojson.GeoJsonType;
import org.openstreetmap.atlas.geography.sharding.converters.StringToShardConverter;
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

    private final Shard shard;
    private final String country;

    public static CountryShard forName(final String name)
    {
        final StringList split = StringList.split(name, Shard.SHARD_DATA_SEPARATOR, 2);
        return new CountryShard(split.get(0), new StringToShardConverter().convert(split.get(1)));
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

    public CountryShard(final String country, final String shardString)
    {
        if (shardString == null || country == null)
        {
            throw new CoreException("Cannot have null parameters: Country = {} and Shard = {}",
                    country, shardString);
        }
        this.country = country;
        this.shard = new StringToShardConverter().convert(shardString);
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
        return this.country + Shard.SHARD_DATA_SEPARATOR + this.shard.getName();
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
        return "[CountryShard: country = " + this.country + ", shard = " + this.shard.toString()
                + "]";
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
