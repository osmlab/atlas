package org.openstreetmap.atlas.utilities.scalars;

import java.io.Serializable;

/**
 * Product of two {@link Angle}s
 *
 * @author matthieun
 * @author jgage
 * @author cstaylor
 * @author rstack
 */
public final class Surface implements Serializable
{
    public static final Surface MINIMUM = Surface.forDm7Squared(0);
    public static final Surface MAXIMUM = Surface.forDm7Squared(Long.MAX_VALUE);

    /**
     * <pre>
     * dm7 = 10^-7 degrees
     * d = Angle in Radians * Radius of the Earth
     * Angle in Radians = d / Radius of the Earth
     * 1m / Radius of the Earth in meters = 1 / 6371000
     * 8.99 * 10^-6 degrees
     * 89.9 dm7 = 1m
     * 89.9^2 = 8082.01dm7^2
     * </pre>
     */
    public static final Surface UNIT_METER_SQUARED_ON_EARTH_SURFACE = Surface.forDm7Squared(8082L);
    private static final long serialVersionUID = 9085129200745439319L;

    private final long dm7Squared;

    public static Surface forAngles(final Angle angle1, final Angle angle2)
    {
        long dm71 = angle1.asDm7();
        if (dm71 < 0)
        {
            dm71 += Angle.REVOLUTION_DM7;
        }

        long dm72 = angle2.asDm7();
        if (dm72 < 0)
        {
            dm72 += Angle.REVOLUTION_DM7;
        }

        return new Surface(dm71 * dm72);
    }

    public static Surface forDm7Squared(final long dm7Squared)
    {
        return new Surface(dm7Squared);
    }

    private Surface(final long dm7Squared)
    {
        this.dm7Squared = dm7Squared;
    }

    public Surface add(final Surface other)
    {
        return Surface.forDm7Squared(this.asDm7Squared() + other.asDm7Squared());
    }

    public long asDm7Squared()
    {
        return this.dm7Squared;
    }

    public double asKilometerSquared()
    {
        final double result = asDm7Squared()
                / ((double) Angle.DM7_PER_RADIAN * (double) Angle.DM7_PER_RADIAN)
                * (Distance.AVERAGE_EARTH_RADIUS.asKilometers()
                        * Distance.AVERAGE_EARTH_RADIUS.asKilometers());
        return result;
    }

    public double asMeterSquared()
    {
        final double result = asDm7Squared()
                / ((double) Angle.DM7_PER_RADIAN * (double) Angle.DM7_PER_RADIAN)
                * (Distance.AVERAGE_EARTH_RADIUS.asMeters()
                        * Distance.AVERAGE_EARTH_RADIUS.asMeters());
        return result;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof Surface)
        {
            final Surface that = (Surface) other;
            return that.asDm7Squared() == this.asDm7Squared();
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(this.dm7Squared);
    }

    public boolean isLargerThan(final Surface other)
    {
        return this.asDm7Squared() > other.asDm7Squared();
    }

    public boolean isLargerThanOrEqualTo(final Surface other)
    {
        return this.asDm7Squared() >= other.asDm7Squared();
    }

    public boolean isLessThan(final Surface other)
    {
        return this.asDm7Squared() < other.asDm7Squared();
    }

    public boolean isLessThanOrEqualTo(final Surface other)
    {
        return this.asDm7Squared() <= other.asDm7Squared();
    }

    public Surface scaleBy(final double factor)
    {
        if (factor < 0)
        {
            throw new IllegalArgumentException("Scale factor must not be a negative number");
        }
        return Surface.forDm7Squared((long) (this.asDm7Squared() * factor));
    }

    public Surface subtract(final Surface other)
    {
        if (isLargerThanOrEqualTo(other))
        {
            return Surface.forDm7Squared(this.asDm7Squared() - other.asDm7Squared());
        }
        throw new IllegalArgumentException("Invalid surfaces for performing subtraction");
    }

    @Override
    public String toString()
    {
        return asDm7Squared() + " dm7^2";
    }
}
