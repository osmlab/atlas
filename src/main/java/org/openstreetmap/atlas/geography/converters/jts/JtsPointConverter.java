package org.openstreetmap.atlas.geography.converters.jts;

import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

/**
 * Convert a {@link Location} to a {@link Point} from the JTS library. The {@link Location}'s
 * {@link Latitude} dm7 value becomes the {@link Point}'s y value, and the {@link Location}'s
 * {@link Longitude} dm7 value becomes the the {@link Point}'s x value.
 *
 * @author mgostintsev
 */
public class JtsPointConverter implements TwoWayConverter<Location, Point>
{
    public static final GeometryFactory GEOMETRY_FACTORY = JtsPrecisionManager.getGeometryFactory();
    private static final JtsLocationConverter LOCATION_CONVERTER = new JtsLocationConverter();

    @Override
    public Location backwardConvert(final Point point)
    {
        return new Location(Latitude.degrees(point.getY()), Longitude.degrees(point.getX()));
    }

    @Override
    public Point convert(final Location location)
    {
        final Coordinate coordinate = LOCATION_CONVERTER.convert(location);
        final CoordinateArraySequence sequence = new CoordinateArraySequence(
                new Coordinate[] { coordinate });
        return new Point(sequence, GEOMETRY_FACTORY);
    }
}
