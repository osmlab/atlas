package org.openstreetmap.atlas.geography.converters.jts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;
import org.openstreetmap.atlas.utilities.maps.MultiMap;

/**
 * Convert a {@link MultiPolygon} to a {@link Set} of {@link org.locationtech.jts.geom.Polygon} form
 * the JTS library. As a {@link MultiPolygon} can contain many outer bounds, each outer bound is
 * translated to a {@link org.locationtech.jts.geom.Polygon}. A JTS
 * {@link org.locationtech.jts.geom.Polygon} is one single outer bound and many inner bounds.
 *
 * @author matthieun
 */
public class JtsMultiPolygonConverter
        implements TwoWayConverter<MultiPolygon, Set<org.locationtech.jts.geom.Polygon>>
{
    private static final JtsLinearRingConverter LINEAR_RING_CONVERTER = new JtsLinearRingConverter();
    private static final JtsPolygonConverter POLYGON_CONVERTER = new JtsPolygonConverter();
    private static final GeometryFactory FACTORY = JtsPrecisionManager.getGeometryFactory();

    @Override
    public MultiPolygon backwardConvert(final Set<org.locationtech.jts.geom.Polygon> object)
    {
        final MultiMap<Polygon, Polygon> result = new MultiMap<>();
        for (final org.locationtech.jts.geom.Polygon polygon : object)
        {
            final Polygon outer = POLYGON_CONVERTER.backwardConvert(polygon);
            if (outer == null)
            {
                continue;
            }
            for (int n = 0; n < polygon.getNumInteriorRing(); n++)
            {
                final LinearRing ring = new LinearRing(
                        polygon.getInteriorRingN(n).getCoordinateSequence(), FACTORY);
                final Polygon inner = LINEAR_RING_CONVERTER.backwardConvert(ring);
                result.add(outer, inner);
            }
            if (polygon.getNumInteriorRing() == 0)
            {
                // Make sure the outer still exists if the inners are not there.
                result.put(outer, new ArrayList<>());
            }
        }
        return new MultiPolygon(result);
    }

    @Override
    public Set<org.locationtech.jts.geom.Polygon> convert(final MultiPolygon object)
    {
        final Set<org.locationtech.jts.geom.Polygon> result = new HashSet<>();
        for (final Polygon outer : object.outers())
        {
            final List<Polygon> inners = object.innersOf(outer);
            final LinearRing[] holes = new LinearRing[inners.size()];
            int index = 0;
            for (final Polygon inner : inners)
            {
                holes[index++] = LINEAR_RING_CONVERTER.convert(inner);
            }
            result.add(new org.locationtech.jts.geom.Polygon(LINEAR_RING_CONVERTER.convert(outer),
                    holes, FACTORY));
        }
        return result;
    }
}
