package org.openstreetmap.atlas.geography.converters.jts;

import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;

/**
 * A simple utility for working with JTS objects.
 *
 * @author Yiqing Jin
 */
public final class JtsUtility
{
    public static final GeometryFactory GEOMETRY_FACTORY = JtsPrecisionManager.getGeometryFactory();
    private static final int MININMUM_NUMBER_OF_POLYGON_POINTS = 4;

    public static LinearRing buildLinearRing(final CoordinateSequence sequence)
    {
        return new LinearRing(sequence, GEOMETRY_FACTORY);
    }

    public static LinearRing buildLinearRing(final List<Coordinate> coordinates)
    {
        final Coordinate[] coordinateArray = coordinates
                .toArray(new Coordinate[coordinates.size()]);
        final CoordinateArraySequence sequence = new CoordinateArraySequence(coordinateArray);
        return new LinearRing(sequence, GEOMETRY_FACTORY);
    }

    public static LineString buildLineString(final Coordinate[] coordinates)
    {
        final CoordinateArraySequence sequence = new CoordinateArraySequence(coordinates);
        return new LineString(sequence, GEOMETRY_FACTORY);
    }

    public static Polygon toPolygon(final Coordinate[] coordinates)
    {
        if (coordinates.length < MININMUM_NUMBER_OF_POLYGON_POINTS && coordinates.length != 0)
        {
            // An invalid polygon. one example A->B->A
            return null;
        }

        final CoordinateArraySequence sequence = new CoordinateArraySequence(coordinates);

        final LinearRing shell = new LinearRing(sequence, GEOMETRY_FACTORY);
        return new Polygon(shell, new LinearRing[] {}, GEOMETRY_FACTORY);
    }

    public static Polygon toPolygon(final List<Coordinate> coordinates)
    {
        return toPolygon(coordinates.toArray(new Coordinate[coordinates.size()]));
    }

    private JtsUtility()
    {
    }

}
