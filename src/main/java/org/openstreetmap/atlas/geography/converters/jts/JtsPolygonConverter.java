package org.openstreetmap.atlas.geography.converters.jts;

import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * Convert a {@link Polygon} to a JTS {@link com.vividsolutions.jts.geom.Polygon}. Here the inner
 * bounds are left empty. When converting backwards, if there is an inner bound in the
 * {@link com.vividsolutions.jts.geom.Polygon}, it will be ignored.
 *
 * @author matthieun
 */
public class JtsPolygonConverter
        implements TwoWayConverter<Polygon, com.vividsolutions.jts.geom.Polygon>
{
    private static final JtsLinearRingConverter LINEAR_RING_CONVERTER = new JtsLinearRingConverter();
    private static final GeometryFactory FACTORY = JtsPrecisionManager.getGeometryFactory();

    @Override
    public Polygon backwardConvert(final com.vividsolutions.jts.geom.Polygon object)
    {
        return LINEAR_RING_CONVERTER.backwardConvert((LinearRing) object.getExteriorRing());
    }

    @Override
    public com.vividsolutions.jts.geom.Polygon convert(final Polygon object)
    {
        return new com.vividsolutions.jts.geom.Polygon(LINEAR_RING_CONVERTER.convert(object),
                new LinearRing[0], FACTORY);
    }
}
