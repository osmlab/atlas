package org.openstreetmap.atlas.geography.sharding;

import java.io.Serializable;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.geojson.GeoJson;
import org.openstreetmap.atlas.geography.geojson.GeoJsonType;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.collections.StringList;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Sharding strategy
 *
 * @author matthieun
 */
public interface Sharding extends Serializable, GeoJson
{
    int SHARDING_STRING_SPLIT = 2;
    int SLIPPY_ZOOM_MAXIMUM = 18;

    /**
     * Parse a sharding definition string
     *
     * @param sharding
     *            The definition string
     * @return The corresponding {@link Sharding} instance.
     */
    static Sharding forString(final String sharding)
    {
        final StringList split;
        split = StringList.split(sharding, "@");
        if (split.size() != SHARDING_STRING_SPLIT)
        {
            throw new CoreException(
                    "Invalid sharding string: {} (correct e.g. dynamic@/path/to/tree, slippy@9, etc.)",
                    sharding);
        }
        if ("slippy".equals(split.get(0)))
        {
            final int zoom;
            zoom = Integer.valueOf(split.get(1));
            if (zoom > SLIPPY_ZOOM_MAXIMUM)
            {
                throw new CoreException("Slippy Sharding zoom too high : {}, max is {}", zoom,
                        SLIPPY_ZOOM_MAXIMUM);
            }
            return new SlippyTileSharding(zoom);
        }
        if ("geohash".equals(split.get(0)))
        {
            final int precision;
            precision = Integer.valueOf(split.get(1));
            return new GeoHashSharding(precision);
        }
        if ("dynamic".equals(split.get(0)))
        {
            final String definition = split.get(1);
            return new DynamicTileSharding(new File(definition));
        }
        throw new CoreException("Sharding type {} is not recognized.", split.get(0));
    }

    @Override
    default JsonObject asGeoJson()
    {
        final JsonObject featureCollectionObject = new JsonObject();
        featureCollectionObject.addProperty("type", "FeatureCollection");
        final JsonArray features = new JsonArray();
        for (final Shard shard : this.shards(Rectangle.MAXIMUM))
        {
            final JsonObject featureObject = new JsonObject();
            featureObject.addProperty("type", "Feature");
            featureObject.add("geometry",
                    new PolyLine(shard.bounds().closedLoop()).asGeoJsonGeometry());
            final JsonObject propertiesObject = new JsonObject();
            propertiesObject.addProperty("shard", shard.getName());
            featureObject.add("properties", propertiesObject);
            features.add(featureObject);
        }
        featureCollectionObject.add("features", features);
        return featureCollectionObject;
    }

    @Override
    default GeoJsonType getGeoJsonType()
    {
        return GeoJsonType.FEATURE_COLLECTION;
    }

    /**
     * Get the neighboring shards for a given shard.
     *
     * @param shard
     *            The shard for which to get neighbors
     * @return The shards {@link Iterable}, neighboring the supplied shard
     */
    Iterable<Shard> neighbors(Shard shard);

    /**
     * Get a shard given its name
     * 
     * @param name
     *            The name of the shard
     * @return The corresponding shard
     */
    Shard shardForName(String name);

    /**
     * Generate shards for the whole planet. This needs to be deterministic!
     *
     * @return The shards {@link Iterable}, covering the whole planet.
     */
    default Iterable<Shard> shards()
    {
        return shards(Rectangle.MAXIMUM);
    }

    /**
     * Generate shards. This needs to be deterministic!
     *
     * @param surface
     *            The bounds to limit the shards.
     * @return The shards {@link Iterable}.
     */
    Iterable<Shard> shards(GeometricSurface surface);

    /**
     * Generate shards. This needs to be deterministic!
     *
     * @param location
     *            The location to find
     * @return The shards {@link Iterable} (In case the location falls right at the boundary between
     *         shards)
     */
    Iterable<Shard> shardsCovering(Location location);

    /**
     * Generate shards. This needs to be deterministic!
     *
     * @param polyLine
     *            The line intersecting the shards
     * @return The shards {@link Iterable}.
     */
    Iterable<Shard> shardsIntersecting(PolyLine polyLine);
}
