package org.openstreetmap.atlas.geography.converters;

import org.openstreetmap.atlas.geography.Altitude;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.constants.WorldGeodeticStandardConstants;
import org.openstreetmap.atlas.geography.coordinates.EarthCenteredEarthFixedCoordinate;
import org.openstreetmap.atlas.geography.coordinates.GeodeticCoordinate;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

/**
 * Conversion from a {@link GeodeticCoordinate} to an {@link EarthCenteredEarthFixedCoordinate}. For
 * reference, images and formulas used are found
 * <a href= "https://microem.ru/files/2012/08/GPS.G1-X-00006.pdf"> here</a>.
 *
 * @author mgostintsev
 */
public class GeodeticEarthCenteredEarthFixedConverter
        implements TwoWayConverter<GeodeticCoordinate, EarthCenteredEarthFixedCoordinate>
{
    @Override
    public GeodeticCoordinate backwardConvert(final EarthCenteredEarthFixedCoordinate coordinate)
    {
        final double semiMinor = Math.sqrt(WorldGeodeticStandardConstants.SEMI_MAJOR_AXIS_SQUARED
                * (1 - WorldGeodeticStandardConstants.ECCENTRICITY_SQUARED));
        final double semiMinorSquared = Math.pow(semiMinor, 2);

        final double secondEccentricity = Math
                .sqrt((WorldGeodeticStandardConstants.SEMI_MAJOR_AXIS_SQUARED - semiMinorSquared)
                        / semiMinorSquared);
        final double auxiliaryP = Math
                .sqrt(Math.pow(coordinate.getX(), 2) + Math.pow(coordinate.getY(), 2));
        final double theta = Math.atan2(
                coordinate.getZ() * WorldGeodeticStandardConstants.SEMI_MAJOR_AXIS.asMeters(),
                auxiliaryP * semiMinor);

        final double longitude = Math.atan2(coordinate.getY(), coordinate.getX());
        final double latitude = Math.atan2(
                coordinate.getZ() + Math.pow(secondEccentricity, 2) * semiMinor
                        * Math.pow(Math.sin(theta), 3),
                auxiliaryP - WorldGeodeticStandardConstants.ECCENTRICITY_SQUARED
                        * WorldGeodeticStandardConstants.SEMI_MAJOR_AXIS.asMeters()
                        * Math.pow(Math.cos(theta), 3));

        final double radiusOfCurviture = WorldGeodeticStandardConstants.SEMI_MAJOR_AXIS.asMeters()
                / Math.sqrt(1 - WorldGeodeticStandardConstants.ECCENTRICITY_SQUARED
                        * Math.pow(Math.sin(latitude), 2));

        final double altitude = auxiliaryP / Math.cos(latitude) - radiusOfCurviture;

        return new GeodeticCoordinate(Latitude.radians(latitude), Longitude.radians(longitude),
                Altitude.meters(altitude));
    }

    @Override
    public EarthCenteredEarthFixedCoordinate convert(final GeodeticCoordinate coordinate)
    {
        final double radiusOfCurviture = WorldGeodeticStandardConstants.SEMI_MAJOR_AXIS.asMeters()
                / Math.sqrt(1 - WorldGeodeticStandardConstants.ECCENTRICITY_SQUARED
                        * Math.pow(Math.sin(coordinate.getLatitude().asPositiveRadians()), 2));

        final double height = coordinate.getAltitude().asMeters();

        final double xValue = (radiusOfCurviture + height)
                * Math.cos(coordinate.getLatitude().asPositiveRadians())
                * Math.cos(coordinate.getLongitude().asPositiveRadians());

        final double yValue = (radiusOfCurviture + height)
                * Math.cos(coordinate.getLatitude().asPositiveRadians())
                * Math.sin(coordinate.getLongitude().asPositiveRadians());

        final double zValue = ((1 - WorldGeodeticStandardConstants.ECCENTRICITY_SQUARED)
                * radiusOfCurviture + height)
                * Math.sin(coordinate.getLatitude().asPositiveRadians());

        return new EarthCenteredEarthFixedCoordinate(xValue, yValue, zValue);
    }
}
