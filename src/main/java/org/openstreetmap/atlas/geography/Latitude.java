package org.openstreetmap.atlas.geography;

import org.openstreetmap.atlas.utilities.scalars.Angle;

/**
 * A Latitude between -90 degrees and +90 degrees, both included.
 *
 * @author matthieun
 * @author tony
 */
public class Latitude extends Angle
{
    private static final long serialVersionUID = -1737858321716005802L;

    protected static final int MINIMUM_DM7 = -900_000_000;
    protected static final int MAXIMUM_DM7 = 900_000_000;

    public static final Latitude MINIMUM = Latitude.dm7(MINIMUM_DM7);
    public static final Latitude ZERO = Latitude.dm7(0L);
    public static final Latitude MAXIMUM = Latitude.dm7(MAXIMUM_DM7);

    /**
     * @param degrees
     *            A Latitude value in degrees
     * @return The built {@link Latitude} object using the degrees value
     */
    public static Latitude degrees(final double degrees)
    {
        return dm7(Math.round(degrees * DM7_PER_DEGREE));
    }

    /**
     * @param dm7
     *            A latitude value in degree of magnitude 7 (dm7)
     * @return The built {@link Latitude} object using the dm7 value
     */
    public static Latitude dm7(final long dm7)
    {
        if (dm7 < MINIMUM_DM7 || dm7 > MAXIMUM_DM7)
        {
            throw new IllegalArgumentException("Cannot have a latitude of " + dm7 / DM7_PER_DEGREE
                    + " degrees which is outside of " + MINIMUM_DM7 / DM7_PER_DEGREE
                    + " degrees -> " + MAXIMUM_DM7 / DM7_PER_DEGREE + " degrees.");
        }
        return new Latitude((int) dm7);
    }

    /**
     * @param radians
     *            A Latitude value in Radians
     * @return The built {@link Latitude} object using the Radians value
     */
    public static Latitude radians(final double radians)
    {
        return dm7(Math.round(radians * DM7_PER_RADIAN));
    }

    /**
     * If the given radian exceeds the latitude boundary, will return the boundary value.
     *
     * @param radians
     *            The radian of latitude
     * @return The adjusted latitude if exceeds the boundary, otherwise the normal latitude
     */
    public static Latitude radiansBounded(final double radians)
    {
        long dm7 = Math.round(radians * DM7_PER_RADIAN);
        if (dm7 < MINIMUM_DM7)
        {
            dm7 = MINIMUM_DM7;
        }
        if (dm7 > MAXIMUM_DM7)
        {
            dm7 = MAXIMUM_DM7;
        }
        return dm7(dm7);
    }

    /**
     * Constructor
     *
     * @param dm7
     *            The latitude value in dm7
     */
    protected Latitude(final int dm7)
    {
        super(dm7);
        if (dm7 < MINIMUM_DM7 || dm7 > MAXIMUM_DM7)
        {
            throw new IllegalArgumentException("Invalid Latitude microdegrees value: " + dm7);
        }
    }

    @Override
    public String toString()
    {
        return String.valueOf(this.asDegrees());
    }
}
