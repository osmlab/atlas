package org.openstreetmap.atlas.geography.converters.jts;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LinearRing;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;
import org.openstreetmap.atlas.utilities.maps.MultiMap;

/**
 * @author matthieun
 */
public class JtsPolygonToMultiPolygonConverter
        implements TwoWayConverter<org.locationtech.jts.geom.Polygon, MultiPolygon>
{
    private static final JtsLinearRingConverter LINEAR_RING_CONVERTER = new JtsLinearRingConverter();
    private static final GeometryFactory FACTORY = JtsPrecisionManager.getGeometryFactory();

    @Override
    public org.locationtech.jts.geom.Polygon backwardConvert(final MultiPolygon object)
    {
        if (object.getOuterToInners().keySet().size() != 1)
        {
            throw new CoreException(
                    "A MultiPolygon can be converted to JTS Polygon only if it has no more than one outer ring.");
        }
        final Polygon outer = object.outers().iterator().next();
        final LinearRing linearRingOuter = LINEAR_RING_CONVERTER.convert(outer);
        final List<Polygon> inners = object.getOuterToInners().get(outer);
        final LinearRing[] linearRingInners = new LinearRing[inners.size()];
        for (int index = 0; index < inners.size(); index++)
        {
            linearRingInners[index] = LINEAR_RING_CONVERTER.convert(inners.get(index));
        }
        return new org.locationtech.jts.geom.Polygon(linearRingOuter, linearRingInners, FACTORY);
    }

    @Override
    public MultiPolygon convert(final org.locationtech.jts.geom.Polygon object)
    {
        final MultiMap<Polygon, Polygon> outersToInners = new MultiMap<>();
        // Here we cannot use the reverse JtsLinearRingConverter because
        // jts.Polygon.getExteriorRing() returns a LineString instead of a LinearRing :(
        final List<Location> locationsOuter = (List<Location>) new JtsCoordinateArrayConverter()
                .backwardConvert(object.getExteriorRing().getCoordinateSequence());
        final Polygon outer = new Polygon(locationsOuter.subList(0, locationsOuter.size() - 1));
        outersToInners.put(outer, new ArrayList<>());
        for (int index = 0; index < object.getNumInteriorRing(); index++)
        {
            final List<Location> locationsInner = (List<Location>) new JtsCoordinateArrayConverter()
                    .backwardConvert(object.getInteriorRingN(index).getCoordinateSequence());
            final Polygon inner = new Polygon(locationsInner.subList(0, locationsInner.size() - 1));
            outersToInners.add(outer, inner);
        }
        return new MultiPolygon(outersToInners);
    }
}
