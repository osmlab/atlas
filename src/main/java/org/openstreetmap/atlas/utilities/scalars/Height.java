package org.openstreetmap.atlas.utilities.scalars;

import java.io.Serializable;

/**
 * Distance wrapper to denote height above or below ground
 *
 * @author ajayaswal
 */

public class Height implements Serializable
{
    private static final long serialVersionUID = 8572975427687313909L;

    private final Distance distance;
    private final boolean aboveGround;

    public Height()
    {
        this.distance = Distance.ZERO;
        this.aboveGround = true;
    }

    public Height(final Double rawHeight)
    {
        this.distance = Distance.meters(Math.abs(rawHeight));
        this.aboveGround = rawHeight >= 0 ? true : false;
    }

    public double getAbsoluteHeightAsMeters()
    {
        return this.distance.asMeters();
    }

    public boolean isAboveGround()
    {
        return this.aboveGround;
    }

    @Override
    public String toString()
    {
        return String.format("%.1f meters ", this.distance, this.aboveGround ? "" : "underground");
    }
}
