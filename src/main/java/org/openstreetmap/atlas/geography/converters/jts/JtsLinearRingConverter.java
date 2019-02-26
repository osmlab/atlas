package org.openstreetmap.atlas.geography.converters.jts;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateSequence;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

/**
 * Convert a {@link Polygon} to a {@link LinearRing} from the JTS library. It is worth noting that a
 * {@link Polygon} assumes that the last point is connected to the first point, and hence does not
 * supply the first point twice. However the JTS {@link LinearRing} implementation assumes that the
 * ring has to be closed, i.e. that the last point and the first point have to be the same. If this
 * is not the case, creating the {@link LinearRing} will fail. This converter accounts for that both
 * ways.
 *
 * @author matthieun
 */
public class JtsLinearRingConverter implements TwoWayConverter<Polygon, LinearRing>
{
    private static final JtsCoordinateArrayConverter COORDINATE_ARRAY_CONVERTER = new JtsCoordinateArrayConverter();
    private static final GeometryFactory FACTORY = JtsPrecisionManager.getGeometryFactory();
    // Protect from: java.lang.IllegalArgumentException: Invalid number of points in LinearRing
    // (found x - must be 0 or >= 4)
    private static final int MINIMUM_LINEAR_RING_SIZE = 4;

    @Override
    public Polygon backwardConvert(final LinearRing object)
    {
        final CoordinateSequence sequence = object.getCoordinateSequence();
        if (sequence.size() <= 0)
        {
            // Cannot have an empty polygon.
            return null;
        }
        final Coordinate[] newArray = new Coordinate[sequence.size() - 1];
        for (int i = 0; i < newArray.length; i++)
        {
            newArray[i] = sequence.getCoordinate(i);
        }
        return new Polygon(
                COORDINATE_ARRAY_CONVERTER.backwardConvert(new CoordinateArraySequence(newArray)));
    }

    @Override
    public LinearRing convert(final Polygon object)
    {
        final List<Location> locations = new ArrayList<>(object);
        // Hack to close the loop, as JTS expects it...
        locations.add(locations.get(0));
        while (locations.size() < MINIMUM_LINEAR_RING_SIZE)
        {
            locations.add(locations.get(0));
        }
        return new LinearRing(COORDINATE_ARRAY_CONVERTER.convert(locations), FACTORY);
    }
}
