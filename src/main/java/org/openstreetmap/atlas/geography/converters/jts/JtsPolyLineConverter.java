package org.openstreetmap.atlas.geography.converters.jts;

import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

/**
 * Convert a {@link PolyLine} to a JTS {@link LineString}.
 *
 * @author matthieun
 */
public class JtsPolyLineConverter implements TwoWayConverter<PolyLine, LineString>
{
    private static final JtsCoordinateArrayConverter COORDINATE_ARRAY_CONVERTER = new JtsCoordinateArrayConverter();
    private static final GeometryFactory FACTORY = JtsPrecisionManager.getGeometryFactory();

    @Override
    public PolyLine backwardConvert(final LineString lineString)
    {
        return new PolyLine(
                COORDINATE_ARRAY_CONVERTER.backwardConvert(lineString.getCoordinateSequence()));
    }

    @Override
    public LineString convert(final PolyLine polyLine)
    {
        return new LineString(COORDINATE_ARRAY_CONVERTER.convert(polyLine), FACTORY);
    }
}
