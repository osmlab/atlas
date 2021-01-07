package org.openstreetmap.atlas.geography.converters.jts;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.openstreetmap.atlas.geography.MultiPolyLine;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

/**
 * Convert a {@link MultiPolyLine} to a JTS {@link MultiLineString}.
 *
 * @author yalimu
 */
public class JtsMultiPolyLineConverter implements TwoWayConverter<MultiPolyLine, MultiLineString>
{
    private static final JtsCoordinateArrayConverter COORDINATE_ARRAY_CONVERTER = new JtsCoordinateArrayConverter();
    private static final JtsPolyLineConverter POLYLINE_CONVERTER = new JtsPolyLineConverter();
    private static final GeometryFactory FACTORY = JtsPrecisionManager.getGeometryFactory();

    @Override
    public MultiPolyLine backwardConvert(final MultiLineString multiLineString)
    {
        final List<PolyLine> polyLineList = new ArrayList<>();
        for (int i = 0; i < multiLineString.getNumGeometries(); i++)
        {
            final LineString lineString = (LineString) multiLineString.getGeometryN(i);
            final PolyLine polyLine = new PolyLine(
                    COORDINATE_ARRAY_CONVERTER.backwardConvert(lineString.getCoordinateSequence()));
            // No duplicated polyline is allowed to add.
            if (!polyLineList.contains(polyLine))
            {
                polyLineList.add(new PolyLine(COORDINATE_ARRAY_CONVERTER
                        .backwardConvert(lineString.getCoordinateSequence())));
            }
        }
        return new MultiPolyLine(polyLineList);
    }

    @Override
    public MultiLineString convert(final MultiPolyLine multiPolyLine)
    {
        final List<LineString> lineStringList = Iterables.stream(multiPolyLine)
                .map(POLYLINE_CONVERTER::convert).collectToList();
        return new MultiLineString(lineStringList.toArray(new LineString[lineStringList.size()]),
                FACTORY);
    }
}
