package org.openstreetmap.atlas.geography.constants;

import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * World Geodetic Standard 1984 ellipsoid constants.
 *
 * @author mgostintsev
 */
public final class WorldGeodeticStandardConstants
{
    /**
     * Equatorial radius, typically denoted as 'a' in standard convention.
     */
    public static final Distance SEMI_MAJOR_AXIS = Distance.meters(6378137.0);

    /**
     * {@link #SEMI_MAJOR_AXIS} squared, for convenience.
     */
    public static final double SEMI_MAJOR_AXIS_SQUARED = Math.pow(SEMI_MAJOR_AXIS.asMeters(), 2);

    /**
     * Flattening is the measure of compression of a sphere to form a spheroid. Typically denoted as
     * 'f' in standard convention.
     */
    public static final double FLATTENING = 298.257223563;

    /**
     * {@link #FLATTENING} inverse, for convenience.
     */
    public static final double INVERSE_FLATTENING = 1 / FLATTENING;

    /**
     * The measure of how much a conic section deviates from being circular. Typically denoted as
     * 'e' in standard convention. Eccentricity does not have units. Ex: the eccentricity of a
     * circle is 0 and that of a line is infinite.
     */
    public static final double ECCENTRICITY = Math.sqrt(1 - Math.pow(1 - INVERSE_FLATTENING, 2));

    /**
     * {@link #ECCENTRICITY} squared, for convenience.
     */
    public static final double ECCENTRICITY_SQUARED = Math.pow(ECCENTRICITY, 2);

    private WorldGeodeticStandardConstants()
    {
    }
}
