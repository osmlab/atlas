package org.openstreetmap.atlas.geography.converters.jts;

import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

/**
 * @author matthieun
 */
public class JtsMultiPolygonToMultiPolygonConverter
        implements TwoWayConverter<org.locationtech.jts.geom.MultiPolygon, MultiPolygon>
{
    private static final JtsMultiPolygonConverter JTS_MULTI_POLYGON_CONVERTER = new JtsMultiPolygonConverter();
    private static final GeometryFactory FACTORY = JtsPrecisionManager.getGeometryFactory();

    @Override
    public org.locationtech.jts.geom.MultiPolygon backwardConvert(final MultiPolygon object)
    {
        final Polygon[] polygons = JTS_MULTI_POLYGON_CONVERTER.convert(object)
                .toArray(new Polygon[0]);
        return new org.locationtech.jts.geom.MultiPolygon(polygons, FACTORY);
    }

    @Override
    public MultiPolygon convert(final org.locationtech.jts.geom.MultiPolygon object)
    {
        final int numberGeometries = object.getNumGeometries();
        final Set<Polygon> polygons = new HashSet<>();
        for (int index = 0; index < numberGeometries; index++)
        {
            final Polygon polygon = (Polygon) object.getGeometryN(index);
            polygons.add(polygon);
        }
        return JTS_MULTI_POLYGON_CONVERTER.backwardConvert(polygons);
    }

}
