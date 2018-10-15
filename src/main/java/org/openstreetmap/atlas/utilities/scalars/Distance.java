package org.openstreetmap.atlas.utilities.scalars;

import java.io.Serializable;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * A class providing conversion between different length units like feet, kilometers, miles etc.
 *
 * @author tony
 */
public final class Distance implements Serializable
{
    private static final long serialVersionUID = 3728783948477892064L;

    protected static final long INCHES_PER_FOOT = 12L;
    protected static final long FEET_PER_MILE = 5280;
    protected static final double METERS_PER_FOOT = 0.3048;
    protected static final double METERS_PER_KILOMETER = 1000;
    protected static final long MILLIMETERS_PER_METER = 1000;
    protected static final long METERS_PER_NAUTICAL_MILE = 1852;

    public static final Distance AVERAGE_EARTH_RADIUS = Distance.kilometers(6371);
    public static final Distance ZERO = Distance.millimeters(0);
    public static final Distance TEN_MILES = Distance.miles(10);
    public static final Distance FIFTEEN_HUNDRED_FEET = Distance.feet(1500);
    public static final Distance ONE_METER = Distance.meters(1);
    public static final Distance MAXIMUM = Distance.millimeters(Long.MAX_VALUE);

    /**
     * @see "https://en.wikipedia.org/wiki/Territorial_waters"
     */
    public static final Distance SEA_TERRITORY_ZONE = Distance.nauticalMiles(12);

    private static final int DISTANCE_PRINTING_METERS_THRESHOLD = 1000;

    private final double millimeters;

    public static Distance feet(final double feet)
    {
        return Distance.meters(feet * METERS_PER_FOOT);
    }

    public static Distance feetAndInches(final double feet, final double inches)
    {
        return Distance.feet(feet).add(Distance.inches(inches));
    }

    public static Distance inches(final double inches)
    {
        return Distance.feet(inches / INCHES_PER_FOOT);
    }

    public static Distance kilometers(final double kilometers)
    {
        return Distance.meters(kilometers * METERS_PER_KILOMETER);
    }

    public static Distance meters(final double meters)
    {
        return Distance.millimeters(meters * MILLIMETERS_PER_METER);
    }

    public static Distance miles(final double miles)
    {
        return Distance.feet(miles * FEET_PER_MILE);
    }

    public static Distance millimeters(final double millimeters)
    {
        return new Distance(millimeters);
    }

    public static Distance nauticalMiles(final double nauticalMiles)
    {
        return Distance.meters(nauticalMiles * METERS_PER_NAUTICAL_MILE);
    }

    private Distance(final double millimeters)
    {
        if (millimeters < 0)
        {
            throw new CoreException("Cannot have a negative distance.");
        }
        this.millimeters = millimeters;
    }

    public Distance add(final Distance that)
    {
        return Distance.millimeters(this.asMillimeters() + that.asMillimeters());
    }

    public double asFeet()
    {
        return asMeters() / METERS_PER_FOOT;
    }

    public double asKilometers()
    {
        return asMeters() / METERS_PER_KILOMETER;
    }

    public double asMeters()
    {
        return asMillimeters() / MILLIMETERS_PER_METER;
    }

    public double asMiles()
    {
        return asFeet() / FEET_PER_MILE;
    }

    public double asMillimeters()
    {
        return this.millimeters;
    }

    public double asNauticalMiles()
    {
        return asMeters() / METERS_PER_NAUTICAL_MILE;
    }

    public Distance difference(final Distance that)
    {
        return Distance.millimeters(Math.abs(this.asMillimeters() - that.asMillimeters()));
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj instanceof Distance)
        {
            return this.asMillimeters() == ((Distance) obj).asMillimeters();
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Double.hashCode(this.millimeters);
    }

    public boolean isGreaterThan(final Distance that)
    {
        return this.asMillimeters() > that.asMillimeters();
    }

    public boolean isGreaterThanOrEqualTo(final Distance that)
    {
        return this.asMillimeters() >= that.asMillimeters();
    }

    public boolean isLessThan(final Distance that)
    {
        return this.asMillimeters() < that.asMillimeters();
    }

    public boolean isLessThanOrEqualTo(final Distance that)
    {
        return this.asMillimeters() <= that.asMillimeters();
    }

    public Distance scaleBy(final double multiplier)
    {
        if (multiplier < 0)
        {
            throw new IllegalArgumentException(
                    "Cannot scale a distance by a negative multiplier: " + multiplier);
        }
        return Distance.millimeters(Math.round(this.asMillimeters() * multiplier));
    }

    public Distance scaleBy(final Ratio ratio)
    {
        return scaleBy(ratio.asRatio());
    }

    public Distance substract(final Distance that)
    {
        final double delta = this.asMillimeters() - that.asMillimeters();
        return Distance.millimeters(Math.max(delta, 0));
    }

    @Override
    public String toString()
    {
        if (asMeters() < DISTANCE_PRINTING_METERS_THRESHOLD)
        {
            return String.format("%.1f meters (%.1f feet)", asMeters(), asFeet());
        }
        return String.format("%.1f km (%.1f miles)", asKilometers(), asMiles());
    }
}
