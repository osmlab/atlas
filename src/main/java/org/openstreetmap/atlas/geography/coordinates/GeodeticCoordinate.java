package org.openstreetmap.atlas.geography.coordinates;

import java.io.Serializable;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openstreetmap.atlas.geography.Altitude;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;

/**
 * Geodetic coordinate system representation, consisting of a {@link Latitude}, {@link Longitude}
 * and {@link Altitude} - commonly referred to as LLA. Note: Units are generally expressed in polar
 * coordinates and meters (for {@link Altitude}}.
 *
 * @author mgostintsev
 */
public class GeodeticCoordinate implements Serializable
{
    private static final long serialVersionUID = 4614378421580938085L;

    private final Latitude latitude;
    private final Longitude longitude;
    private final Altitude altitude;

    /**
     * Default constructor.
     *
     * @param latitude
     *            latitude
     * @param longitude
     *            longitude
     * @param altitude
     *            altitude
     */
    public GeodeticCoordinate(final Latitude latitude, final Longitude longitude,
            final Altitude altitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    /**
     * Creates a {@link GeodeticCoordinate} at the given {@link Location}, at mean sea level.
     *
     * @param location
     *            The {@link Location} of the coordinate.
     */
    public GeodeticCoordinate(final Location location)
    {
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
        this.altitude = Altitude.MEAN_SEA_LEVEL;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof GeodeticCoordinate)
        {
            final GeodeticCoordinate that = (GeodeticCoordinate) other;
            return this.getLatitude().equals(that.getLatitude())
                    && this.getLongitude().equals(that.getLongitude())
                    && this.getAltitude().equals(that.getAltitude());
        }
        return false;
    }

    public Altitude getAltitude()
    {
        return this.altitude;
    }

    public Latitude getLatitude()
    {
        return this.latitude;
    }

    public Longitude getLongitude()
    {
        return this.longitude;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(this.getLatitude()).append(this.getLongitude())
                .append(this.getAltitude()).hashCode();
    }

    @Override
    public String toString()
    {
        return "(" + this.getLatitude() + ", " + this.getLongitude() + ", " + this.getAltitude()
                + ")";
    }
}
