package org.openstreetmap.atlas.geography.boundary;

import java.util.List;

import org.openstreetmap.atlas.geography.converters.jts.JtsUtility;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * This builder builds a spatial index using R-tree based on JTS.
 *
 * @author Yiqing Jin
 */
public abstract class AbstractGridIndexBuilder
{
    /**
     * Constructs a JTS {@link Polygon} representation of a box given x and y coordinates.
     *
     * @param minX
     *            Minimum X
     * @param maxX
     *            Maximum X
     * @param minY
     *            Minimum Y
     * @param maxY
     *            Maximum Y
     * @return the constructed {@link Polygon}
     */
    public static Polygon buildGeoBox(final double minX, final double maxX, final double minY,
            final double maxY)
    {
        final Coordinate lowerLeft = new Coordinate(minX, maxY);
        final Coordinate lowerRight = new Coordinate(maxX, maxY);
        final Coordinate upperRight = new Coordinate(maxX, minY);
        final Coordinate upperLeft = new Coordinate(minX, minY);
        final LinearRing shell = new LinearRing(new CoordinateArraySequence(
                new Coordinate[] { lowerLeft, lowerRight, upperRight, upperLeft, lowerLeft }),
                JtsUtility.GEOMETRY_FACTORY);
        final Polygon geoBox = new Polygon(shell, null, JtsUtility.GEOMETRY_FACTORY);
        return geoBox;
    }

    public abstract List<Polygon> getBoundaries();

    public abstract Envelope getEnvelope();

    public abstract STRtree getIndex();
}
