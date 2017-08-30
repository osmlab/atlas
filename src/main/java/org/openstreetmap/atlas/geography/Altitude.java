package org.openstreetmap.atlas.geography;

import java.io.Serializable;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.tags.ElevationTag;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * This is the height or elevation (usually defined in meters) above the Earth ellipsoid. The Earth
 * ellipsoid is a mathematical surface defined by a semi-major axis and a semi-minor axis. The most
 * common values for these two parameters are defined by the World Geodetic Standard 1984 (WGS-84).
 * The WGS-84 ellipsoid is intended to correspond to mean sea level. An {@link Altitude} of zero
 * corresponds roughly to sea level, with positive values increasing away from the Earth’s center.
 * Altitude values range from the center of the Earth (see {@link Distance#AVERAGE_EARTH_RADIUS}) to
 * positive infinity. For more detail, see
 * <a href= "http://danceswithcode.net/engineeringnotes/geodetic_to_ecef/geodetic_to_ecef.html">
 * here</a>.
 * <p>
 * Please also note that this is NOT the same elevation (height above sea level) as referenced by
 * the {@link ElevationTag} in OSM.
 *
 * @author mgostintsev
 */
public final class Altitude implements Serializable
{
    private static final long serialVersionUID = -9064525655677062110L;

    public static final Altitude MEAN_SEA_LEVEL = Altitude.meters(0);

    private final Distance distance;

    // The altitude will be negative in the range between the center of the earth and sea level:
    // [-AVERAGE_EARTH_RADIUS to 0). Even though the underlying altitude is negative, the
    // representation will be positive to make use of the Distance functionality.
    private boolean isNegative = false;

    public static Altitude meters(final double meters)
    {
        return new Altitude(meters);
    }

    private Altitude(final double meters)
    {
        if (meters < 0)
        {
            if (-meters > Distance.AVERAGE_EARTH_RADIUS.asMeters())
            {
                throw new CoreException("Cannot have an altitude below the center of the Earth.");
            }
            this.isNegative = true;
            this.distance = Distance.meters(-meters);
        }
        else
        {
            this.distance = Distance.meters(meters);
        }
    }

    public double asMeters()
    {
        return this.isNegative ? -this.distance.asMeters() : this.distance.asMeters();
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof Altitude)
        {
            final Altitude that = (Altitude) other;
            return this.asMeters() == that.asMeters();
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Double.hashCode(this.asMeters());
    }

    @Override
    public String toString()
    {
        return String.valueOf(this.asMeters());
    }
}
