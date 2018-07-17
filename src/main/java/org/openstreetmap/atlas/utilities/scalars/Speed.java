package org.openstreetmap.atlas.utilities.scalars;

import java.io.Serializable;

/**
 * A class provides the conversion between different speed units like kph and mph
 *
 * @author tony
 * @author Sid
 */
public final class Speed implements Serializable
{
    private static final long serialVersionUID = 3268649594426387264L;

    public static final String MILES_PER_HOUR = "mph";
    public static final String KILOMETERS_PER_HOUR = "kph";
    public static final String NAUTICAL_MILES_PER_HOUR = "knots";

    private final Distance distance;
    private final Duration duration;

    public static Speed distancePerDuration(final Distance distance, final Duration duration)
    {
        return new Speed(distance, duration);
    }

    public static Speed kilometersPerHour(final double kph)
    {
        return new Speed(Distance.kilometers(kph), Duration.ONE_HOUR);
    }

    public static Speed knots(final double knots)
    {
        return new Speed(Distance.nauticalMiles(knots), Duration.ONE_HOUR);
    }

    public static Speed metersPerSecond(final double mps)
    {
        return new Speed(Distance.meters(mps), Duration.ONE_SECOND);
    }

    public static Speed milesPerHour(final double mph)
    {
        return new Speed(Distance.miles(mph), Duration.ONE_HOUR);
    }

    private Speed(final Distance distance, final Duration duration)
    {
        this.distance = distance;
        this.duration = duration;
    }

    public Speed add(final Speed that)
    {
        return Speed.kilometersPerHour(this.asKilometersPerHour() + that.asKilometersPerHour());
    }

    public double asKilometersPerHour()
    {
        return this.distance.asKilometers() / this.duration.asHours();
    }

    public double asKnots()
    {
        return this.distance.asNauticalMiles() / this.duration.asHours();
    }

    public double asMetersPerSecond()
    {
        return this.distance.asMeters() / this.duration.asSeconds();
    }

    public double asMilesPerHour()
    {
        return this.distance.asMiles() / this.duration.asHours();
    }

    public Speed difference(final Speed that)
    {
        return Speed.kilometersPerHour(
                Math.abs(this.asKilometersPerHour() - that.asKilometersPerHour()));
    }

    public Duration asDuration(final Distance distance)
    {
        return Duration.seconds(distance.asMeters() / this.asMetersPerSecond());
    }

    @Override
    public boolean equals(final Object object)
    {
        if (object instanceof Speed)
        {
            final Speed that = (Speed) object;
            return this.asKilometersPerHour() == that.asKilometersPerHour();
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Double.hashCode(asKilometersPerHour());
    }

    public boolean isFasterThan(final Speed that)
    {
        return this.asKilometersPerHour() > that.asKilometersPerHour();
    }

    /**
     * @param that
     *            The other {@link Speed} to compare
     * @return True if this speed is faster than or equals to another speed, note this comparison
     *         may only track a few digits after decimal depends on the initialization methods you
     *         choose
     *         <p>
     *         e.g.
     *         Speed.kilometersPerHour(1.000008).isFasterThanOrEqualTo(Speed.kilometersPerHour(1.
     *         000009)) will return true
     */
    public boolean isFasterThanOrEqualTo(final Speed that)
    {
        return this.asKilometersPerHour() >= that.asKilometersPerHour();
    }

    public boolean isSlowerThan(final Speed that)
    {
        return this.asKilometersPerHour() < that.asKilometersPerHour();
    }

    /**
     * @param that
     *            The other {@link Speed} to compare
     * @return True if this speed is slower than or equals to another speed, note this comparison
     *         will only track a few digits after decimal depends on the initialization methods you
     *         choose
     *         <p>
     *         e.g.
     *         Speed.kilometersPerHour(1.000009).isSlowerThanOrEqualTo(Speed.kilometersPerHour(1.
     *         000008)) will return true
     */
    public boolean isSlowerThanOrEqualTo(final Speed that)
    {
        return this.asKilometersPerHour() <= that.asKilometersPerHour();
    }

    public Speed subtract(final Speed that)
    {
        final double delta = this.asKilometersPerHour() - that.asKilometersPerHour();
        return Speed.kilometersPerHour(Math.max(delta, 0));
    }

    @Override
    public String toString()
    {
        return String.format("%.1f kph (%.1f mph)", asKilometersPerHour(), asMilesPerHour());
    }
}
