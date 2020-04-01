package org.openstreetmap.atlas.geography;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Random;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Snapper.SnappedLocation;
import org.openstreetmap.atlas.geography.converters.WkbLocationConverter;
import org.openstreetmap.atlas.geography.converters.WktLocationConverter;
import org.openstreetmap.atlas.geography.coordinates.EarthCenteredEarthFixedCoordinate;
import org.openstreetmap.atlas.geography.coordinates.GeodeticCoordinate;
import org.openstreetmap.atlas.geography.geojson.GeoJsonGeometry;
import org.openstreetmap.atlas.geography.geojson.GeoJsonType;
import org.openstreetmap.atlas.geography.geojson.GeoJsonUtils;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.scalars.Distance;

import com.google.gson.JsonObject;

/**
 * Location on the surface of the earth
 *
 * @author matthieun
 * @author mgostintsev
 */
public class Location
        implements Located, Iterable<Location>, Serializable, GeometryPrintable, GeoJsonGeometry
{
    public static final String TEST_1_COORDINATES = "37.335310,-122.009566";
    public static final String TEST_2_COORDINATES = "37.321628,-122.028464";
    public static final String TEST_3_COORDINATES = "37.317585,-122.052138";
    public static final String TEST_4_COORDINATES = "37.332451,-122.028932";
    public static final String TEST_5_COORDINATES = "37.390535,-122.031007";
    public static final String TEST_6_COORDINATES = "37.325440,-122.033948";
    public static final String TEST_7_COORDINATES = "37.3314171,-122.0304871";
    public static final String TEST_8_COORDINATES = "37.3214159,-122.0303831";
    // Quick-access locations, mostly used for testing.
    public static final Location TEST_1 = Location.forString(TEST_1_COORDINATES);
    public static final Location TEST_2 = Location.forString(TEST_2_COORDINATES);
    public static final Location TEST_3 = Location.forString(TEST_3_COORDINATES);
    public static final Location TEST_4 = Location.forString(TEST_4_COORDINATES);
    public static final Location TEST_5 = Location.forString(TEST_5_COORDINATES);
    public static final Location TEST_6 = Location.forString(TEST_6_COORDINATES);
    public static final Location TEST_7 = Location.forString(TEST_7_COORDINATES);
    public static final Location TEST_8 = Location.forString(TEST_8_COORDINATES);
    public static final Location STEVENS_CREEK = Location.forString("37.324233,-122.003467");
    public static final Location CROSSING_85_280 = Location.forString("37.332439,-122.055760");
    public static final Location CROSSING_85_17 = Location.forString("37.255731,-121.955918");
    public static final Location EIFFEL_TOWER = Location.forString("48.858241,2.294495");
    public static final Location COLOSSEUM = Location.forString("41.890224,12.492340");
    public static final Location CENTER = new Location(0L);
    private static final long serialVersionUID = 3770424147251047128L;
    private static final int INT_FULL_MASK = 0xFFFFFFFF;
    private static final long INT_FULL_MASK_AS_LONG = 0xFFFFFFFFL;
    private static final int INT_SIZE = 32;
    private static final int FACTOR_OF_3 = 3;
    private static final Random RANDOM = new Random();

    private final Latitude latitude;
    private final Longitude longitude;

    /**
     * @param locationString
     *            The {@link Location} as a {@link String} in "latitude(degrees),longitude(degrees)"
     *            format
     * @return The corresponding {@link Location}
     */
    public static Location forString(final String locationString)
    {
        final StringList split = StringList.split(locationString, ",");
        if (split.size() != 2)
        {
            throw new CoreException("Invalid Location String: {}", locationString);
        }
        final double latitude = Double.parseDouble(split.get(0));
        final double longitude = Double.parseDouble(split.get(1));
        return new Location(Latitude.degrees(latitude), Longitude.degrees(longitude));
    }

    /**
     * @param locationString
     *            The {@link Location} as a {@link String} in "longitude(degrees),latitude(degrees)"
     *            format
     * @return The corresponding {@link Location}
     */
    public static Location forStringLongitudeLatitude(final String locationString)
    {
        final StringList split = StringList.split(locationString, ",");
        if (split.size() != 2)
        {
            throw new CoreException("Invalid Location String: {}", locationString);
        }
        final double latitude = Double.parseDouble(split.get(1));
        final double longitude = Double.parseDouble(split.get(0));
        return new Location(Latitude.degrees(latitude), Longitude.degrees(longitude));
    }

    /**
     * @param wkt
     *            The {@link Location} as a Well Known Text (WKT) {@link String} format
     * @return The corresponding {@link Location}
     */
    public static Location forWkt(final String wkt)
    {
        return new WktLocationConverter().backwardConvert(wkt);
    }

    /**
     * @param bounds
     *            Bounds to constrain the result
     * @return A random location within the bounds
     */
    public static Location random(final Rectangle bounds)
    {
        final int latitude = RANDOM.ints((int) bounds.lowerLeft().getLatitude().asDm7(),
                (int) bounds.upperRight().getLatitude().asDm7()).iterator().next();
        final int longitude = RANDOM.ints((int) bounds.lowerLeft().getLongitude().asDm7(),
                (int) bounds.upperRight().getLongitude().asDm7()).iterator().next();
        return new Location(Latitude.dm7(latitude), Longitude.dm7(longitude));
    }

    /**
     * Build a {@link Location} from a {@link Latitude} and a {@link Longitude} objects.
     *
     * @param latitude
     *            The {@link Latitude} to use
     * @param longitude
     *            The {@link Longitude} to use
     */
    public Location(final Latitude latitude, final Longitude longitude)
    {
        if (latitude == null)
        {
            throw new CoreException("Latitude is null.");
        }
        if (longitude == null)
        {
            throw new CoreException("Longitude is null.");
        }
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Copy constructor for {@link Location}
     *
     * @param other
     *            the {@link Location} from which to copy
     */
    public Location(final Location other)
    {
        if (other == null)
        {
            throw new CoreException("Other Location was null");
        }
        this.latitude = other.latitude;
        this.longitude = other.longitude;
    }

    /**
     * Create a location from a dm7 latitude and dm7 longitude concatenated in a long
     *
     * @param concatenation
     *            The first 32 bits are for the dm7 latitude, and the last 32 bits are for the dm7
     *            longitude.
     */
    public Location(final long concatenation)
    {
        final int lon = (int) concatenation;
        final int lat = (int) (concatenation >>> INT_SIZE) & INT_FULL_MASK;
        this.longitude = Longitude.dm7(lon);
        this.latitude = Latitude.dm7(lat);
    }

    /**
     * @return A dm7 latitude and dm7 longitude concatenated in a long. The first 32 bits are for
     *         the dm7 latitude, and the last 32 bits are for the dm7 longitude.
     */
    public long asConcatenation()
    {
        long result = this.latitude.asDm7();
        result <<= INT_SIZE;
        result |= this.longitude.asDm7() & INT_FULL_MASK_AS_LONG;
        return result;
    }

    @Override
    public JsonObject asGeoJsonGeometry()
    {
        return GeoJsonUtils.geometry(GeoJsonType.POINT, GeoJsonUtils.coordinate(this));
    }

    @Override
    public Rectangle bounds()
    {
        return Rectangle.forCorners(this, this);
    }

    /**
     * Get a {@link Rectangle} around this {@link Location}
     *
     * @param extension
     *            The height of the 1/2 {@link Rectangle}. The height of the total {@link Rectangle}
     *            will be twice that. Same for width.
     * @return The {@link Rectangle} around this {@link Location}
     */
    public Rectangle boxAround(final Distance extension)
    {
        final Location north = this.shiftAlongGreatCircle(Heading.NORTH, extension);
        final Location south = this.shiftAlongGreatCircle(Heading.SOUTH, extension);
        final Location east = this.shiftAlongGreatCircle(Heading.EAST, extension);
        final Location west = this.shiftAlongGreatCircle(Heading.WEST, extension);
        return Rectangle.forLocations(north, south, east, west);
    }

    /**
     * @param that
     *            The other {@link Location} to compute the {@link Distance} to
     * @return The {@link Distance} between the two {@link Location}
     */
    public Distance distanceTo(final Location that)
    {
        // Do a quick check on Longitude
        if (this.getLongitude().isCloserViaAntimeridianTo(that.getLongitude()))
        {
            // Use the method that is not annoyed by the antimeridian
            return haversineDistanceTo(that);
        }
        return equirectangularDistanceTo(that);
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof Location)
        {
            final Location that = (Location) other;
            return this.getLatitude().equals(that.getLatitude())
                    && this.getLongitude().equals(that.getLongitude());
        }
        return false;
    }

    /**
     * An equirectangular approximation distance between two locations, better performance but less
     * accurate. It is especially not able to handle distances that cross the antimeridian. It would
     * compute the distance all the way around the world instead.
     *
     * @param that
     *            The other point to compute the distance to
     * @return The equirectangular distance
     * @see "http://www.movable-type.co.uk/scripts/latlong.html"
     */
    public Distance equirectangularDistanceTo(final Location that)
    {
        // convert to radians
        final double lat1 = this.getLatitude().asRadians();
        final double lon1 = this.getLongitude().asRadians();
        final double lat2 = that.getLatitude().asRadians();
        final double lon2 = that.getLongitude().asRadians();

        final double xAxis = (lon2 - lon1) * Math.cos((lat1 + lat2) / 2);
        final double yAxis = lat2 - lat1;

        return Distance.AVERAGE_EARTH_RADIUS.scaleBy(Math.sqrt(xAxis * xAxis + yAxis * yAxis));
    }

    @Override
    public GeoJsonType getGeoJsonType()
    {
        return GeoJsonType.POINT;
    }

    /**
     * @return This {@link Location}'s {@link Latitude}
     */
    public Latitude getLatitude()
    {
        return this.latitude;
    }

    /**
     * @return This {@link Location}'s {@link Longitude}
     */
    public Longitude getLongitude()
    {
        return this.longitude;
    }

    /**
     * @param other
     *            The other {@link Location} to test
     * @return True if this {@link Location} and the other {@link Location} to test are on the same
     *         East-West line.
     */
    public boolean hasSameLatitudeAs(final Location other)
    {
        return this.getLatitude().equals(other.getLatitude());
    }

    /**
     * @param other
     *            The other {@link Location} to test
     * @return True if this {@link Location} and the other {@link Location} to test are on the same
     *         North-South line.
     */
    public boolean hasSameLongitudeAs(final Location other)
    {
        return this.getLongitude().equals(other.getLongitude());
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.latitude == null ? 0 : this.latitude.hashCode());
        result = prime * result + (this.longitude == null ? 0 : this.longitude.hashCode());
        return result;
    }

    /**
     * This uses the ‘haversine’ formula to calculate the great-circle distance between two
     * locations, more calculation but more accurate
     *
     * @param that
     *            The other point to compute the distance to
     * @return The haversine distance
     * @see "http://www.movable-type.co.uk/scripts/latlong.html"
     */
    public Distance haversineDistanceTo(final Location that)
    {
        // convert to radians
        final double lat1 = this.getLatitude().asRadians();
        final double lon1 = this.getLongitude().asRadians();
        final double lat2 = that.getLatitude().asRadians();
        final double lon2 = that.getLongitude().asRadians();

        final double deltaLat = lat2 - lat1;
        final double deltaLon = lon2 - lon1;

        final double hav = Math.pow(Math.sin(deltaLat / 2), 2)
                + Math.cos(lat1) * Math.cos(lat2) * Math.pow(Math.sin(deltaLon / 2), 2);
        final double result = 2 * Math.atan2(Math.sqrt(hav), Math.sqrt(1 - hav));
        return Distance.AVERAGE_EARTH_RADIUS.scaleBy(result);
    }

    /**
     * This computes the initial heading (heading at the start point) of the segment on the surface
     * of earth between two locations
     *
     * @see "http://www.movable-type.co.uk/scripts/latlong.html"
     * @param that
     *            The other point to compute the heading to
     * @return The heading between two points
     */
    public Heading headingTo(final Location that)
    {
        if (this.equals(that))
        {
            throw new CoreException("Cannot compute some heading when two points are the same.");
        }

        // convert to radians
        final double lat1 = this.getLatitude().asRadians();
        final double lon1 = this.getLongitude().asRadians();
        final double lat2 = that.getLatitude().asRadians();
        final double lon2 = that.getLongitude().asRadians();

        final double deltaLon = lon2 - lon1;

        final double yAxis = Math.sin(deltaLon) * Math.cos(lat2);
        final double xAxis = Math.cos(lat1) * Math.sin(lat2)
                - Math.sin(lat1) * Math.cos(lat2) * Math.cos(deltaLon);
        return Heading.radians(Math.atan2(yAxis, xAxis));
    }

    /**
     * @param other
     *            The other {@link Location} to test
     * @return True if this {@link Location} is east of the other {@link Location}.
     */
    public boolean isEastOf(final Location other)
    {
        return this.getLongitude().isGreaterThan(other.getLongitude());
    }

    /**
     * @param other
     *            The other {@link Location} to test
     * @return True if this {@link Location} is east or on the same {@link Longitude} as the other
     *         {@link Location}.
     */
    public boolean isEastOfOrOnTheSameLatitudeAs(final Location other)
    {
        return this.getLongitude().isGreaterThanOrEqualTo(other.getLongitude());
    }

    /**
     * @param other
     *            The other {@link Location} to test
     * @return True if this {@link Location} is north of the other {@link Location}.
     */
    public boolean isNorthOf(final Location other)
    {
        return this.getLatitude().isGreaterThan(other.getLatitude());
    }

    /**
     * @param other
     *            The other {@link Location} to test
     * @return True if this {@link Location} is north or on the same {@link Latitude} as the other
     *         {@link Location}.
     */
    public boolean isNorthOfOrOnTheSameLatitudeAs(final Location other)
    {
        return this.getLatitude().isGreaterThanOrEqualTo(other.getLatitude());
    }

    @Override
    public Iterator<Location> iterator()
    {
        return Iterables.from(this).iterator();
    }

    /**
     * Midpoint along a Rhumb line between this point and that point
     *
     * @param that
     *            The other point to compute the midpoint between
     * @return The {@link Location} of the loxodromic midpoint
     * @see "http://www.movable-type.co.uk/scripts/latlong.html"
     */
    public Location loxodromicMidPoint(final Location that)
    {
        // Convert to Radians
        final double lat1 = this.getLatitude().asRadians();
        double lon1 = this.getLongitude().asRadians();
        final double lat2 = that.getLatitude().asRadians();
        final double lon2 = that.getLongitude().asRadians();

        // Crossing anti-meridian
        if (Math.abs(lon2 - lon1) > Math.PI)
        {
            lon1 += 2 * Math.PI;
        }

        final double pheta = (lat1 + lat2) / 2;
        final double phi1 = Math.tan(Math.PI / 4 + lat1 / 2);
        final double phi2 = Math.tan(Math.PI / 4 + lat2 / 2);
        final double phi3 = Math.tan(Math.PI / 4 + pheta / 2);
        double lambda = ((lon2 - lon1) * Math.log(phi3) + lon1 * Math.log(phi2)
                - lon2 * Math.log(phi1)) / Math.log(phi2 / phi1);

        // Locations on the same circle of latitude do not produce a finite lambda value above.
        // Locations at the same longitude (especially the antimeridian) should preserve their sign.
        // All other locations should be be normalized within [-180, +180).
        if (!Double.isFinite(lambda) || lon1 == lon2)
        {
            lambda = (lon1 + lon2) / 2;
        }
        else
        {
            lambda = (lambda + FACTOR_OF_3 * Math.PI) % (2 * Math.PI) - Math.PI;
        }

        return new Location(Latitude.radians(pheta), Longitude.radians(lambda));
    }

    /**
     * The half-way point along a great-circle path between this and that point
     *
     * @param that
     *            The other point to compute the midpoint between
     * @return The {@link Location} of the midpoint
     * @see "http://www.movable-type.co.uk/scripts/latlong.html"
     */
    public Location midPoint(final Location that)
    {
        // Convert to Radians
        final double lat1 = this.getLatitude().asRadians();
        final double lon1 = this.getLongitude().asRadians();
        final double lat2 = that.getLatitude().asRadians();
        final double lon2 = that.getLongitude().asRadians();

        final double longitudeDelta = lon2 - lon1;

        final double xBearing = Math.cos(lat2) * Math.cos(longitudeDelta);
        final double yBearing = Math.cos(lat2) * Math.sin(longitudeDelta);

        final double pheta = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt(
                (Math.cos(lat1) + xBearing) * (Math.cos(lat1) + xBearing) + yBearing * yBearing));
        double lambda = lon1 + Math.atan2(yBearing, Math.cos(lat1) + xBearing);

        // Normalize to -180/180
        lambda = (lambda + FACTOR_OF_3 * Math.PI) % (2 * Math.PI) - Math.PI;
        if (this.getLongitude().equals(Longitude.MAXIMUM)
                && that.getLongitude().equals(Longitude.MAXIMUM))
        {
            lambda *= -1;
        }
        return new Location(Latitude.radians(pheta), Longitude.radians(lambda));
    }

    /**
     * Shift a location along a great circle. Note that if the shifted location exceeds the boundary
     * of latitude (-90 to 90 degrees) or longitude (-180 to 180 degrees), it will use the boundary
     * instead
     *
     * @param initialHeading
     *            Initial heading
     * @param distance
     *            Distance along the great circle
     * @return The shifted location
     * @see "http://www.movable-type.co.uk/scripts/latlong.html"
     */
    public Location shiftAlongGreatCircle(final Heading initialHeading, final Distance distance)
    {
        if (Distance.ZERO.equals(distance))
        {
            return this;
        }
        // convert to radians
        final double latitude1 = this.getLatitude().asRadians();
        final double longitude1 = this.getLongitude().asRadians();
        final double bearing = initialHeading.asRadians();

        final double latitude2 = Math.asin(Math.sin(latitude1)
                * Math.cos(distance.asMillimeters() / Distance.AVERAGE_EARTH_RADIUS.asMillimeters())
                + Math.cos(latitude1)
                        * Math.sin(distance.asMillimeters()
                                / Distance.AVERAGE_EARTH_RADIUS.asMillimeters())
                        * Math.cos(bearing));
        final double longitude2 = longitude1 + Math.atan2(
                Math.sin(bearing)
                        * Math.sin(distance.asMillimeters()
                                / Distance.AVERAGE_EARTH_RADIUS.asMillimeters())
                        * Math.cos(latitude1),
                Math.cos(distance.asMillimeters() / Distance.AVERAGE_EARTH_RADIUS.asMillimeters())
                        - Math.sin(latitude1) * Math.sin(latitude2));
        return new Location(Latitude.radiansBounded(latitude2),
                Longitude.radiansBounded(longitude2));
    }

    /**
     * Snap this {@link Location} to a {@link MultiPolygon} using a {@link Snapper}
     *
     * @param shape
     *            The shape to snap to
     * @return The corresponding {@link SnappedLocation}
     */
    public SnappedLocation snapTo(final MultiPolygon shape)
    {
        return new Snapper().snap(this, shape);
    }

    /**
     * Snap this {@link Location} to a {@link PolyLine} using a {@link Snapper}
     *
     * @param shape
     *            The shape to snap to
     * @return The corresponding {@link SnappedLocation}
     */
    public SnappedLocation snapTo(final PolyLine shape)
    {
        return new Snapper().snap(this, shape);
    }

    public String toCompactString()
    {
        return this.getLatitude() + "," + this.getLongitude();
    }

    /**
     * @return the {@link EarthCenteredEarthFixedCoordinate} for this {@link Location}.
     */
    public EarthCenteredEarthFixedCoordinate toEarthCenteredEarthFixedCoordinate()
    {
        return new EarthCenteredEarthFixedCoordinate(this);
    }

    /**
     * @return the {@link GeodeticCoordinate} for this {@link Location}.
     */
    public GeodeticCoordinate toGeodeticCoordinate()
    {
        return new GeodeticCoordinate(this);
    }

    @Override
    public String toString()
    {
        return toWkt();
    }

    @Override
    public byte[] toWkb()
    {
        return new WkbLocationConverter().convert(this);
    }

    @Override
    public String toWkt()
    {
        return new WktLocationConverter().convert(this);
    }

    @Override
    public boolean within(final GeometricSurface surface)
    {
        return surface.fullyGeometricallyEncloses(this);
    }

    protected Point2D asAwtPoint()
    {
        return new Point((int) getLongitude().asDm7(), (int) getLatitude().asDm7());
    }
}
