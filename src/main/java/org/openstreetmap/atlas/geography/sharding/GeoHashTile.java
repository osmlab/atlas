package org.openstreetmap.atlas.geography.sharding;

import org.locationtech.spatial4j.context.SpatialContext;
import org.locationtech.spatial4j.io.GeohashUtils;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.geojson.GeoJsonType;
import org.openstreetmap.atlas.geography.sharding.converters.RectangleToSpatial4JRectangleConverter;
import org.openstreetmap.atlas.utilities.collections.Iterables;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gson.JsonObject;

/**
 * @author matthieun
 */
public class GeoHashTile implements Shard
{
    public static final int MAXIMUM_PRECISION = 12;
    public static final GeoHashTile ROOT = new GeoHashTile("");

    static final char[] GEOHASH_CHARACTERS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u',
            'v', 'w', 'x', 'y', 'z' };
    static final BiMap<Integer, Character> GEOHASH_CHARACTER_MAP = HashBiMap.create();

    private static final long serialVersionUID = 525101912087621541L;
    private static final RectangleToSpatial4JRectangleConverter RECTANGLE_TO_SPATIAL_4_J_RECTANGLE_CONVERTER = new RectangleToSpatial4JRectangleConverter();

    static
    {
        for (int index = 0; index < GEOHASH_CHARACTERS.length; index++)
        {
            GEOHASH_CHARACTER_MAP.put(index, GEOHASH_CHARACTERS[index]);
        }
    }

    private final String value;
    private Rectangle bounds;

    public static Iterable<GeoHashTile> allTiles(final int precision)
    {
        validatePrecision(precision);
        if (precision == 0)
        {
            return Iterables.iterable(GeoHashTile.ROOT);
        }
        return new GeoHashTileIterable(precision);
    }

    public static Iterable<GeoHashTile> allTiles(final int precision,
            final GeometricSurface surface)
    {
        validatePrecision(precision);
        if (precision == 0)
        {
            return Iterables.iterable(GeoHashTile.ROOT);
        }
        return new GeoHashTileIterable(precision, surface);
    }

    public static GeoHashTile covering(final Location location, final int precision)
    {
        validatePrecision(precision);
        return new GeoHashTile(GeohashUtils.encodeLatLon(location.getLatitude().asDegrees(),
                location.getLongitude().asDegrees(), precision));
    }

    public static GeoHashTile forName(final String value)
    {
        return new GeoHashTile(value);
    }

    public static long numberTilesAtPrecision(final int precision)
    {
        if (precision == 0)
        {
            return 1L;
        }
        return (long) Math.pow((double) GEOHASH_CHARACTERS.length, (double) precision);
    }

    public static void validatePrecision(final int precision)
    {
        if (precision > MAXIMUM_PRECISION)
        {
            throw new CoreException("Cannot have precision {} > {}", precision, MAXIMUM_PRECISION);
        }
        if (precision < 0)
        {
            throw new CoreException("Cannot have precision {} < 0", precision);
        }
    }

    public GeoHashTile(final String value)
    {
        this.value = value;
    }

    @Override
    public JsonObject asGeoJson()
    {
        return bounds().asGeoJson();
    }

    @Override
    public Rectangle bounds()
    {
        if (this.bounds == null)
        {
            if (this.value.isEmpty())
            {
                this.bounds = Rectangle.MAXIMUM;
            }
            else
            {
                this.bounds = RECTANGLE_TO_SPATIAL_4_J_RECTANGLE_CONVERTER
                        .convert(GeohashUtils.decodeBoundary(this.value, SpatialContext.GEO));
            }
        }
        return this.bounds;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof GeoHashTile)
        {
            return this.value.equals(((GeoHashTile) other).getName());
        }
        return false;
    }

    @Override
    public GeoJsonType getGeoJsonType()
    {
        return bounds().getGeoJsonType();
    }

    @Override
    public String getName()
    {
        return this.value;
    }

    public int getPrecision()
    {
        return this.value.length();
    }

    @Override
    public int hashCode()
    {
        return this.value.hashCode();
    }

    public Iterable<Shard> neighbors()
    {
        return Iterables.stream(GeoHashTile.allTiles(this.getPrecision(), bounds()))
                .filter(tile -> !this.equals(tile)).map(tile -> (Shard) tile).collect();
    }

    public char[] toCharArray()
    {
        return this.value.toCharArray();
    }

    @Override
    public String toString()
    {
        return "[GeoHashTile: value = " + this.value + "]";
    }

    @Override
    public byte[] toWkb()
    {
        return bounds().toWkb();
    }

    @Override
    public String toWkt()
    {
        return bounds().toWkt();
    }
}
