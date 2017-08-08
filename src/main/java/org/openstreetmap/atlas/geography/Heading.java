package org.openstreetmap.atlas.geography;

import org.openstreetmap.atlas.utilities.scalars.Angle;

/**
 * A Heading, that is between 0 included and 360 degrees excluded, using the standard 0 = north, 90
 * = east, 180 = south and 270 = west. Heading's degree circle starts from 0 and increases in the
 * clock-wise direction. Heading's circle is shifted forward 180 degrees (with
 * {@link Heading#DELTA_DM7}) compared to Angle's degree circle.
 *
 * @author matthieun
 */
public class Heading extends Angle
{
    private static final long serialVersionUID = -7621572408790801458L;

    public static final Heading NORTH = Heading.dm7(0L);
    public static final Heading SOUTH = Heading.dm7(1_800_000_000L);
    public static final Heading EAST = Heading.dm7(900_000_000L);
    public static final Heading WEST = Heading.dm7(2_700_000_000L);

    /**
     * Delta between {@link Angle}'s degree circle and {@link Heading}'s degree circle
     */
    protected static final int DELTA_DM7 = 1_800_000_000;

    /**
     * @param degrees
     *            A heading value in degrees
     * @return The built {@link Heading} object using the degrees value
     */
    public static Heading degrees(final double degrees)
    {
        return dm7(Math.round(degrees * DM7_PER_DEGREE));
    }

    /**
     * @param dm7
     *            A heading value in degree of magnitude 7 (dm7)
     * @return The built {@link Heading} object using the dm7 value
     */
    public static Heading dm7(final long dm7)
    {
        // Roll dm7 value from 0->360 to the -180->180 that the angle expects.
        // Heading's circle is 180 degree ahead of Angle's
        long rollingDm7 = (dm7 - DELTA_DM7) % REVOLUTION_DM7;

        // After the roll operation, dm7 value could fall into (-180, -360) degrees.
        // This addition of 360 degrees will shift that degree back into (0, 180)
        if (rollingDm7 < MINIMUM_DM7)
        {
            rollingDm7 += REVOLUTION_DM7;
        }

        // After the roll operation, dm7 value could fall into [180, 360) degrees.
        // This subtraction of 360 degrees will shift that degree back into [-180, 0)
        if (rollingDm7 >= MAXIMUM_DM7)
        {
            rollingDm7 -= REVOLUTION_DM7;
        }
        return new Heading((int) rollingDm7);
    }

    /**
     * @param radians
     *            A heading value in Radians
     * @return The built {@link Heading} object using the Radians value
     */
    public static Heading radians(final double radians)
    {
        return dm7(Math.round(radians * DM7_PER_RADIAN));
    }

    protected Heading(final int dm7)
    {
        super(dm7);
    }

    @Override
    public long asDm7()
    {
        // Override to scale back to 0->360
        return super.asDm7() + DELTA_DM7;
    }

    @Override
    public String toString()
    {
        return String.valueOf(this.asDegrees());
    }
}
