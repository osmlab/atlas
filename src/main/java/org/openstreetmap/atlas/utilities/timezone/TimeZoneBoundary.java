package org.openstreetmap.atlas.utilities.timezone;

import java.util.TimeZone;

import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.utilities.scalars.Surface;

/**
 * This {@link TimeZoneBoundary} holds {@link TimeZone} and {@link Polygon} object, and will be
 * stored in spatial index directly for best time zone query performance
 *
 * @author tony
 */
public class TimeZoneBoundary implements Located
{
    private final TimeZone timeZone;
    private final Polygon polygon;

    public TimeZoneBoundary(final TimeZone timeZone, final Polygon polygon)
    {
        this.timeZone = timeZone;
        this.polygon = polygon;
    }

    /**
     * @return the area as degrees
     */
    public Surface area()
    {
        return this.polygon.bounds().surface();
    }

    @Override
    public Rectangle bounds()
    {
        return this.polygon.bounds();
    }

    public Polygon getPolygon()
    {
        return this.polygon;
    }

    public TimeZone getTimeZone()
    {
        return this.timeZone;
    }

    @Override
    public String toString()
    {
        return this.timeZone.getID() + " " + this.bounds();
    }
}
