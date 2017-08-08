package org.openstreetmap.atlas.utilities.time;

import java.time.ZoneOffset;

import org.openstreetmap.atlas.utilities.scalars.Duration;

/**
 * UTC Time
 *
 * @author matthieun
 */
public class Time extends LocalTime
{
    public static Time now()
    {
        return new Time(Duration.milliseconds(System.currentTimeMillis()));
    }

    public Time(final Duration epoch)
    {
        super(epoch, ZoneOffset.UTC);
    }
}
