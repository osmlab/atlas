package org.openstreetmap.atlas.geography.converters.jts;

import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;

/**
 * Convert a {@link Polygon} to a JTS {@link org.locationtech.jts.geom.Polygon}. Here the inner
 * bounds are left empty. When converting backwards, if there is an inner bound in the
 * {@link org.locationtech.jts.geom.Polygon}, it will be ignored.
 *
 * @author matthieun
 */
public class JtsPolygonConverter
        implements TwoWayConverter<Polygon, org.locationtech.jts.geom.Polygon>
{
    private static final JtsLinearRingConverter LINEAR_RING_CONVERTER = new JtsLinearRingConverter();
    private static final GeometryFactory FACTORY = JtsPrecisionManager.getGeometryFactory();

    @Override
    public Polygon backwardConvert(final org.locationtech.jts.geom.Polygon object)
    {
        return LINEAR_RING_CONVERTER.backwardConvert((LinearRing) object.getExteriorRing());
    }

    @Override
    public org.locationtech.jts.geom.Polygon convert(final Polygon object)
    {
        return new org.locationtech.jts.geom.Polygon(LINEAR_RING_CONVERTER.convert(object),
                new LinearRing[0], FACTORY);
    }
}
