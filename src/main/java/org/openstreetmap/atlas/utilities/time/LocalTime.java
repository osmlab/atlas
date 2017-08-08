package org.openstreetmap.atlas.utilities.time;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.openstreetmap.atlas.utilities.scalars.Duration;

/**
 * Time class that uses the java.time package in 1.8+
 *
 * @author matthieun
 */
public class LocalTime
{
    private static final int NANO_PER_MILLI = 1_000_000;
    private static final String DEFAULT_FORMAT = "yyyyMMdd-HHmmss-zzz";

    // Duration from start of UNIX time
    private final Duration epoch;
    private final ZoneId timeZone;

    // This is lazily populated!
    private LocalDateTime dateTime = null;

    public static LocalTime now(final ZoneId offset)
    {
        return new LocalTime(Duration.milliseconds(System.currentTimeMillis()), offset);
    }

    public LocalTime(final Duration epoch, final ZoneId timeZone)
    {
        this.epoch = epoch;
        this.timeZone = timeZone;
    }

    public int day()
    {
        return getDateTime().getDayOfMonth();
    }

    public Duration elapsedBetween(final LocalTime time)
    {
        return Duration
                .milliseconds(Math.abs(time.epoch.asMilliseconds() - this.epoch.asMilliseconds()));
    }

    public Duration elapsedSince()
    {
        return Duration.milliseconds(
                now(this.timeZone).epoch.asMilliseconds() - this.epoch.asMilliseconds());
    }

    public String format(final DateTimeFormatter formatter)
    {
        return formatter.format(getDateTime());
    }

    public String format(final String pattern)
    {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return getDateTime().format(formatter);
    }

    public Duration getEpoch()
    {
        return this.epoch;
    }

    public int hour()
    {
        return getDateTime().getHour();
    }

    public int millisecond()
    {
        return (int) Math.round((double) getDateTime().getNano() / NANO_PER_MILLI);
    }

    public int minute()
    {
        return getDateTime().getMinute();
    }

    public int month()
    {
        return getDateTime().getMonthValue();
    }

    public int second()
    {
        return getDateTime().getSecond();
    }

    @Override
    public String toString()
    {
        return format(DEFAULT_FORMAT);
    }

    public Duration untilNow()
    {
        return Duration.milliseconds(System.currentTimeMillis()).difference(this.epoch);
    }

    public int year()
    {
        return getDateTime().getYear();
    }

    private LocalDateTime getDateTime()
    {
        if (this.dateTime == null)
        {
            this.dateTime = LocalDateTime
                    .ofInstant(Instant.ofEpochMilli(this.epoch.asMilliseconds()), this.timeZone);
        }
        return this.dateTime;
    }
}
