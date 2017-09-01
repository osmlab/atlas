package org.openstreetmap.atlas.geography.coordinates;

import java.io.Serializable;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.converters.GeodeticEarthCenteredEarthFixedConverter;

/**
 * Earth-Fixed, Earth-Centered (ECEF) coordinate system representation. This is a right-handed
 * Cartesian coordinate system (think X, Y and Z) with the origin at the Earth's center. Note: Units
 * are generally expressed in meters.
 *
 * @author mgostintsev
 */
public class EarthCenteredEarthFixedCoordinate implements Serializable
{
    private static final long serialVersionUID = -3091871010423428109L;

    private static final GeodeticEarthCenteredEarthFixedConverter COORDINATE_CONVERTER = new GeodeticEarthCenteredEarthFixedConverter();

    private final double xValue;
    private final double yValue;
    private final double zValue;

    /**
     * Constructs an {@link EarthCenteredEarthFixedCoordinate} at (0,0,0).
     */
    public EarthCenteredEarthFixedCoordinate()
    {
        this.xValue = 0;
        this.yValue = 0;
        this.zValue = 0;
    }

    /**
     * Constructs an {@link EarthCenteredEarthFixedCoordinate} at given (x,y,z).
     *
     * @param xValue
     *            x-value
     * @param yValue
     *            y-value
     * @param zValue
     *            z-value
     */
    public EarthCenteredEarthFixedCoordinate(final double xValue, final double yValue,
            final double zValue)
    {
        this.xValue = xValue;
        this.yValue = yValue;
        this.zValue = zValue;
    }

    /**
     * Constructs an {@link EarthCenteredEarthFixedCoordinate} at given {@link Location}.
     *
     * @param location
     *            The {@link Location} of the coordinate
     */
    public EarthCenteredEarthFixedCoordinate(final Location location)
    {
        final EarthCenteredEarthFixedCoordinate coordinate = COORDINATE_CONVERTER
                .apply(location.toGeodeticCoordinate());
        this.xValue = coordinate.getX();
        this.yValue = coordinate.getY();
        this.zValue = coordinate.getZ();
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof EarthCenteredEarthFixedCoordinate)
        {
            final EarthCenteredEarthFixedCoordinate that = (EarthCenteredEarthFixedCoordinate) other;
            return this.getX() == that.getX() && this.getY() == that.getY()
                    && this.getZ() == that.getZ();
        }
        return false;
    }

    public double getX()
    {
        return this.xValue;
    }

    public double getY()
    {
        return this.yValue;
    }

    public double getZ()
    {
        return this.zValue;
    }

    @Override
    public int hashCode()
    {
        return new HashCodeBuilder().append(this.getX()).append(this.getY()).append(this.getZ())
                .hashCode();
    }

    @Override
    public String toString()
    {
        return "(" + this.getX() + ", " + this.getY() + ", " + this.getZ() + ")";
    }
}
