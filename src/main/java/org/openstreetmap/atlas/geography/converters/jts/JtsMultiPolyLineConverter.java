package org.openstreetmap.atlas.geography.converters.jts;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.geography.MultiPolyLine;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

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
            polyLineList.add(new PolyLine(COORDINATE_ARRAY_CONVERTER
                    .backwardConvert(lineString.getCoordinateSequence())));
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
