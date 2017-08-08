package org.openstreetmap.atlas.geography.converters.jts;

import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author matthieun
 */
public class JtsMultiPolygonToMultiPolygonConverter
        implements TwoWayConverter<com.vividsolutions.jts.geom.MultiPolygon, MultiPolygon>
{
    private static final JtsMultiPolygonConverter JTS_MULTI_POLYGON_CONVERTER = new JtsMultiPolygonConverter();
    private static final GeometryFactory FACTORY = JtsPrecisionManager.getGeometryFactory();

    @Override
    public com.vividsolutions.jts.geom.MultiPolygon backwardConvert(final MultiPolygon object)
    {
        final Polygon[] polygons = JTS_MULTI_POLYGON_CONVERTER.convert(object)
                .toArray(new Polygon[0]);
        return new com.vividsolutions.jts.geom.MultiPolygon(polygons, FACTORY);
    }

    @Override
    public MultiPolygon convert(final com.vividsolutions.jts.geom.MultiPolygon object)
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
