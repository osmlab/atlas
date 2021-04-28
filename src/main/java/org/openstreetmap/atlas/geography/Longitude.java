package org.openstreetmap.atlas.geography;

import org.openstreetmap.atlas.utilities.scalars.Angle;

/**
 * A Longitude between -180 degrees and +180 degrees, inclusive.
 *
 * @author matthieun
 * @author tony
 */
public class Longitude extends Angle
{
    public static final Longitude MINIMUM = Longitude.dm7(MINIMUM_DM7);
    public static final Longitude ZERO = Longitude.dm7(0L);
    public static final Longitude MAXIMUM = Longitude.dm7(MAXIMUM_DM7);
    public static final Longitude ANTIMERIDIAN_WEST = Longitude.MINIMUM;
    public static final Longitude ANTIMERIDIAN_EAST = Longitude.MAXIMUM;
    private static final long serialVersionUID = 4222162088144937632L;
    private boolean isMaximumDm7 = false;

    /**
     * @param degrees
     *            A Longitude value in degrees
     * @return The built {@link Longitude} object using the degrees value
     */
    public static Longitude degrees(final double degrees)
    {
        return dm7(Math.round(degrees * DM7_PER_DEGREE));
    }

    /**
     * @param dm7
     *            A longitude value in degree of magnitude 7 (dm7)
     * @return The built {@link Longitude} object using the dm7 value
     */
    public static Longitude dm7(final long dm7)
    {
        if (dm7 < MINIMUM_DM7 || dm7 > MAXIMUM_DM7)
        {
            throw new IllegalArgumentException("Cannot have a longitude of " + dm7 / DM7_PER_DEGREE
                    + " degrees which is outside of " + MINIMUM_DM7 / DM7_PER_DEGREE
                    + " degrees -> " + MAXIMUM_DM7 / DM7_PER_DEGREE + " degrees.");
        }
        // This constructor depends on the Angle Constructor, which allows for overriding the
        // "assertDm7" method.
        return new Longitude((int) dm7);
    }

    /**
     * @param radians
     *            A Longitude value in Radians
     * @return The built {@link Longitude} object using the Radians value
     */
    public static Longitude radians(final double radians)
    {
        return dm7(Math.round(radians * DM7_PER_RADIAN));
    }

    /**
     * If the given radian exceeds the longitude boundary, will return the boundary value.
     *
     * @param radians
     *            The radian of longitude
     * @return The adjusted longitude if exceeds the boundary, otherwise the normal longitude
     */
    public static Longitude radiansBounded(final double radians)
    {
        long dm7 = Math.round(radians * DM7_PER_RADIAN);
        if (dm7 < MINIMUM_DM7)
        {
            dm7 = MINIMUM_DM7;
        }
        if (dm7 >= MAXIMUM_DM7)
        {
            dm7 = MAXIMUM_DM7 - 1L;
        }
        return dm7(dm7);
    }

    /**
     * Constructor
     *
     * @param dm7
     *            The longitude value in dm7
     */
    protected Longitude(final int dm7)
    {
        super(dm7);
        if (dm7 == MAXIMUM_DM7)
        {
            this.isMaximumDm7 = true;
        }
    }

    @Override
    public long asDm7()
    {
        if (this.isMaximumDm7)
        {
            // This longitude was built with +180 and not -180, so make sure to return the same.
            return MAXIMUM_DM7;
        }
        else
        {
            return super.asDm7();
        }
    }

    /**
     * @param that
     *            The other {@link Longitude} to relate to
     * @return True if those two longitudes are closer to each other on the Antimeridian side than
     *         on the Greenwich side.
     */
    public boolean isCloserViaAntimeridianTo(final Longitude that)
    {
        // The difference in numerical longitude is larger than half a revolution
        return Math.abs(this.asDm7() - that.asDm7()) > REVOLUTION_DM7 / 2;
    }

    @Override
    public String toString()
    {
        return String.valueOf(this.asDegrees());
    }

    @Override
    protected int assertDm7(final int dm7)
    {
        if (dm7 < MINIMUM_DM7 || dm7 > MAXIMUM_DM7)
        {
            throw new IllegalArgumentException("Longitude dm7 value " + dm7 + " is invalid.");
        }
        return dm7;
    }
}
