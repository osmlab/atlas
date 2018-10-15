package org.openstreetmap.atlas.utilities.scalars;

import java.io.Serializable;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * Time duration
 *
 * @author matthieun
 */
public final class Duration implements Serializable
{
    private static final long serialVersionUID = 8306012362496627267L;

    private static final long NANOSECONDS_PER_MILLISECONDS = 1_000_000;
    private static final long MILLISECONDS_PER_SECOND = 1000;
    private static final long SECONDS_PER_MINUTE = 60;
    private static final long MINUTES_PER_HOUR = 60;
    public static final Duration ONE_DAY = hours(24);
    public static final Duration ONE_HOUR = hours(1);
    public static final Duration ONE_MINUTE = minutes(1);
    public static final Duration ONE_SECOND = seconds(1);
    public static final Duration ZERO = milliseconds(0);
    public static final Duration MAXIMUM = milliseconds(Long.MAX_VALUE);

    private final long milliseconds;

    public static Duration hours(final double hours)
    {
        return minutes(hours * MINUTES_PER_HOUR);
    }

    public static Duration milliseconds(final long milliseconds)
    {
        return new Duration(milliseconds);
    }

    public static Duration minutes(final double minutes)
    {
        return seconds(minutes * SECONDS_PER_MINUTE);
    }

    public static Duration seconds(final double seconds)
    {
        return milliseconds(Math.round(seconds * MILLISECONDS_PER_SECOND));
    }

    private Duration(final long milliseconds)
    {
        if (milliseconds < 0)
        {
            throw new CoreException("Cannot have a negative duration");
        }
        this.milliseconds = milliseconds;
    }

    public Duration add(final Duration that)
    {
        return new Duration(this.milliseconds + that.milliseconds);
    }

    public double asHours()
    {
        return this.asMinutes() / MINUTES_PER_HOUR;
    }

    public long asMilliseconds()
    {
        return this.milliseconds;
    }

    public double asMinutes()
    {
        return this.asSeconds() / SECONDS_PER_MINUTE;
    }

    public double asSeconds()
    {
        return (double) this.asMilliseconds() / MILLISECONDS_PER_SECOND;
    }

    public Duration difference(final Duration that)
    {
        return new Duration(Math.abs(that.milliseconds - this.milliseconds));
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj instanceof Duration)
        {
            return this.asMilliseconds() == ((Duration) obj).asMilliseconds();
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Long.hashCode(this.milliseconds);
    }

    public Duration highest(final Duration other)
    {
        if (other == null || this.isMoreThanOrEqualsTo(other))
        {
            return this;
        }
        return other;
    }

    public boolean isCloseTo(final Duration that, final Duration safe)
    {
        return difference(that).isLessThanOrEqualsTo(safe);
    }

    public boolean isLessThan(final Duration that)
    {
        return this.milliseconds < that.milliseconds;
    }

    public boolean isLessThanOrEqualsTo(final Duration that)
    {
        return this.milliseconds <= that.milliseconds;
    }

    public boolean isMoreThan(final Duration that)
    {
        return this.milliseconds > that.milliseconds;
    }

    public boolean isMoreThanOrEqualsTo(final Duration that)
    {
        return this.milliseconds >= that.milliseconds;
    }

    public Duration lowest(final Duration other)
    {
        if (other == null || this.isLessThanOrEqualsTo(other))
        {
            return this;
        }
        return other;
    }

    public long millisecondsOfSecond()
    {
        return this.asMilliseconds() % MILLISECONDS_PER_SECOND;
    }

    public long nanosecondsOfSecond()
    {
        return this.millisecondsOfSecond() * NANOSECONDS_PER_MILLISECONDS;
    }

    public void sleep()
    {
        try
        {
            Thread.sleep(this.milliseconds);
        }
        catch (final InterruptedException e)
        {
            throw new RuntimeException("Could not sleep " + this, e);
        }
    }

    @Override
    public String toString()
    {
        if (this.milliseconds < MILLISECONDS_PER_SECOND)
        {
            return this.asMilliseconds() + " milliseconds";
        }
        if (this.milliseconds < MILLISECONDS_PER_SECOND * SECONDS_PER_MINUTE)
        {
            return String.format("%.3f seconds", this.asSeconds());
        }
        return String.format("%.3f minutes", this.asMinutes());
    }
}
