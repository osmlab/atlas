package org.openstreetmap.atlas.utilities.scalars;

import java.io.Serializable;

import org.openstreetmap.atlas.geography.Heading;

/**
 * Angle, between -180 degrees (included) and 180 degrees (excluded). Precision is 10 microdegrees,
 * or one ten millionth of a degree. The term dm7 denotes a unit of one ten millionth of a degree.
 * Angle's degree circle starts from -180 and increases in the clock-wise direction towards 0 and
 * then it goes to 180 (excluded).
 *
 * @author matthieun
 * @author mkalender
 */
public class Angle implements Serializable
{
    private static final long serialVersionUID = -5120437813288084229L;

    // There are ten million microdegrees per degree
    protected static final int DM7_PER_DEGREE = 10_000_000;
    // An angle is >= -180 degrees
    protected static final int MINIMUM_DM7 = -1_800_000_000;
    // An angle is < 180 degrees
    protected static final int MAXIMUM_DM7 = 1_800_000_000;
    // There are approximately 57 degrees per radian
    public static final int DM7_PER_RADIAN = 572_957_795;
    // When precision is needed
    public static final double DM7_PER_RADIAN_DOUBLE = Double.valueOf(MAXIMUM_DM7) / Math.PI;
    // dm7 unit per microdegree
    protected static final int DM7_PER_MICRODEGREE = 10;
    // Threshold to print a dm7 value
    private static final int DM7_PRINT_THRESHOLD = 10_000;

    // This difference does not fit in an int!
    protected static final long REVOLUTION_DM7 = (long) MAXIMUM_DM7 - (long) MINIMUM_DM7;

    // Useful Angle constants
    public static final Angle MINIMUM = Angle.dm7(MINIMUM_DM7);
    public static final Angle NONE = Angle.dm7(0L);
    public static final Angle MAXIMUM = Angle.dm7(MAXIMUM_DM7 - 1);

    // The primitive store. It will always fit between MINIMUM_DM7 and MAXIMUM_DM7, so int is enough
    private final int dm7;

    /**
     * Create an Angle from an angle value. Any value outside of [-180,180[ degrees will be
     * translated to its equivalent within this range
     *
     * @param degrees
     *            The angle value in degrees
     * @return The Angle object corresponding to this value in degrees.
     */
    public static Angle degrees(final double degrees)
    {
        return dm7(Math.round(degrees * DM7_PER_DEGREE));
    }

    /**
     * Create an Angle from an angle value. Any value outside of [-180,180[ degrees will be
     * translated to its equivalent within this range
     *
     * @param dm7
     *            The angle value in dm7
     * @return The Angle object corresponding to this value in dm7.
     */
    public static Angle dm7(final long dm7)
    {
        long rollingMicroDegrees = dm7 % REVOLUTION_DM7;
        // Add a full 360 degrees until the number is within [-180,180[.
        if (rollingMicroDegrees < MINIMUM_DM7)
        {
            rollingMicroDegrees += REVOLUTION_DM7;
        }
        // Subtract a full 360 degrees until the number is within [-180,180[.
        if (rollingMicroDegrees >= MAXIMUM_DM7)
        {
            rollingMicroDegrees -= REVOLUTION_DM7;
        }
        // Store the angle as modulo 360 degrees
        return new Angle((int) rollingMicroDegrees);
    }

    /**
     * Create an Angle from an angle value. Any value outside of [-Pi,Pi[ degrees will be translated
     * to its equivalent within this range
     *
     * @param radians
     *            The angle value in radians
     * @return The Angle object corresponding to this value in radians.
     */
    public static Angle radians(final double radians)
    {
        return dm7(Math.round(radians * DM7_PER_RADIAN));
    }

    /**
     * Constructor.
     *
     * @param dm7
     *            An angle value in dm7, between MINIMUM_DM7 and MAXIMUM_DM7
     */
    protected Angle(final int dm7)
    {
        this.dm7 = assertDm7(dm7);
    }

    /**
     * Add another angle to this angle
     *
     * @param that
     *            The other {@link Angle} to add
     * @return The angle representing the sum
     */
    public final Angle add(final Angle that)
    {
        // The dm7 function takes care of maintaining the value inside the bounds.
        return Angle.dm7(this.getDm7() + that.getDm7());
    }

    /**
     * @return The value of this {@link Angle} in degrees.
     */
    public double asDegrees()
    {
        return (double) this.asDm7() / DM7_PER_DEGREE;
    }

    /**
     * @return The value of this {@link Angle} in one tenth of a microdegree. This returns a long
     *         instead of an int, because {@link Heading} (a sub-class of {@link Angle}) is based on
     *         0-360 degrees angles that might not fit within int at the dm7 level.
     */
    public long asDm7()
    {
        return this.dm7;
    }

    /**
     * @return the {@link Angle} object with positive values. E.g. -100 degree will be 100 degree.
     *         Note -180 degree will return 179.9999999
     */
    public Angle asPositiveAngle()
    {
        if (this.dm7 == MINIMUM_DM7)
        {
            return MAXIMUM;
        }
        return Angle.dm7(Math.abs(this.dm7));
    }

    /**
     * @return The value of this {@link Angle} in positive radians.
     */
    public double asPositiveRadians()
    {
        return asRadians() < 0 ? asRadians() + 2 * Math.PI : asRadians();
    }

    /**
     * @return The value of this {@link Angle} in radians. Can be negative or positive.
     */
    public double asRadians()
    {
        return this.asDm7() / DM7_PER_RADIAN_DOUBLE;
    }

    /**
     * Returns the difference between two {@link Angle}s. Returned {@link Angle} will never be
     * negative.
     *
     * @param that
     *            {@link Angle} to compare against
     * @return Positive difference value between given {@link Angle}s
     */
    public final Angle difference(final Angle that)
    {
        return this.subtract(that).asPositiveAngle();
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof Angle)
        {
            final Angle that = (Angle) other;
            // This is specifically to make sure that the two angles are compared on their actual
            // dm7 value, rather than what the asDm7() method returns. This is to allow angle
            // sub-classes to have multiple ways to return the value of one single angle: example is
            // Longitude, with two ways of representing the Angle -180 degrees, with -180 and +180.
            // Another example is Heading, which is an Angle, but displays its values in [0, 360[
            // degrees.
            return this.getDm7() == that.getDm7();
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(this.dm7);
    }

    /**
     * Compares this {@link Angle} with another {@link Angle}.
     *
     * @param other
     *            Another {@link Angle} to compare against
     * @return true if this {@link Angle} is greater than other {@link Angle}
     */
    public final boolean isGreaterThan(final Angle other)
    {
        if (other.getClass() == this.getClass())
        {
            return this.asDm7() > other.asDm7();
        }
        return this.getDm7() > other.getDm7();
    }

    /**
     * Compares this {@link Angle} with another {@link Angle}.
     *
     * @param other
     *            Another {@link Angle} to compare against
     * @return true if this {@link Angle} is greater than or equal to other {@link Angle}
     */
    public final boolean isGreaterThanOrEqualTo(final Angle other)
    {
        if (other.getClass() == this.getClass())
        {
            return this.asDm7() >= other.asDm7();
        }
        return this.getDm7() >= other.getDm7();
    }

    /**
     * Compares this {@link Angle} with another {@link Angle}.
     *
     * @param other
     *            Another {@link Angle} to compare against
     * @return true if this {@link Angle} is less than other {@link Angle}
     */
    public final boolean isLessThan(final Angle other)
    {
        if (other.getClass() == this.getClass())
        {
            return this.asDm7() < other.asDm7();
        }
        return this.getDm7() < other.getDm7();
    }

    /**
     * Compares this {@link Angle} with another {@link Angle}.
     *
     * @param other
     *            Another {@link Angle} to compare against
     * @return true if this {@link Angle} is less than or equal to other {@link Angle}
     */
    public final boolean isLessThanOrEqualTo(final Angle other)
    {
        if (other.getClass() == this.getClass())
        {
            return this.asDm7() <= other.asDm7();
        }
        return this.getDm7() <= other.getDm7();
    }

    /**
     * @return The average distance on Earth's surface of this angle taken from the center of Earth.
     */
    public Distance onEarth()
    {
        return Distance.AVERAGE_EARTH_RADIUS.scaleBy(this.asPositiveRadians());
    }

    /**
     * Subtracts an {@link Angle} from this.
     *
     * @param that
     *            The angle to subtract
     * @return The angle resulting from the subtraction
     */
    public final Angle subtract(final Angle that)
    {
        // The dm7 function takes care of maintaining the value inside the bounds.
        return Angle.dm7(this.getDm7() - that.getDm7());
    }

    @Override
    public String toString()
    {
        if (Math.abs(this.getDm7()) >= DM7_PRINT_THRESHOLD)
        {
            return this.asDegrees() + " degrees";
        }
        return this.asDm7() + " tenths of microdegrees";
    }

    /**
     * Resolving strategy for any invalid or out of bounds angle value. Sub-classes need to override
     * this method to change the resolution strategy. The default strategy is throwing an exception
     * if the dm7 value is smaller than -180 degrees or larger than or equal to 180 degrees.
     *
     * @param dm7
     *            The proposed dm7 value
     * @return The corrected dm7 value
     */
    protected int assertDm7(final int dm7)
    {
        if (dm7 < MINIMUM_DM7 || dm7 >= MAXIMUM_DM7)
        {
            throw new IllegalArgumentException("Angle dm7 value " + dm7 + " is invalid.");
        }
        return dm7;
    }

    /**
     * @return dm7 value as is. Classes that extend {@link Angle} might override
     *         {@link Angle#asDm7()}, but {@link Angle#getDm7()}. Therefore, this method is used for
     *         comparisons and calculations.
     */
    private long getDm7()
    {
        return this.dm7;
    }
}
