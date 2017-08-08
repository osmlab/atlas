package org.openstreetmap.atlas.geography.converters;

import java.util.ArrayList;

import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.conversion.TwoWayStringConverter;
import org.openstreetmap.atlas.utilities.maps.MultiMap;

/**
 * Convert a {@link MultiPolygon} back and forth to a {@link String}
 *
 * @author matthieun
 */
public class MultiPolygonStringConverter implements TwoWayStringConverter<MultiPolygon>
{
    // For some reason, splitting the String fails when OUTER_SEPARATOR is "|"
    public static final String OUTER_SEPARATOR = "&";
    public static final String OUTER_INNERS_SEPARATOR = "#";
    public static final String INNER_SEPARATOR = "+";

    private static final PolygonStringConverter POLYGON_STRING_CONVERTER = new PolygonStringConverter();

    @Override
    public String backwardConvert(final MultiPolygon object)
    {
        final StringList outers = new StringList();
        for (final Polygon outer : object.outers())
        {
            final StringList inners = new StringList();
            for (final Polygon inner : object.innersOf(outer))
            {
                inners.add(inner.toCompactString());
            }
            outers.add(outer.toCompactString() + OUTER_INNERS_SEPARATOR
                    + inners.join(INNER_SEPARATOR));
        }
        return outers.join(OUTER_SEPARATOR);
    }

    @Override
    public MultiPolygon convert(final String object)
    {
        final MultiMap<Polygon, Polygon> result = new MultiMap<>();
        final StringList outers = StringList.split(object, OUTER_SEPARATOR);
        for (final String outerString : outers)
        {
            final StringList outerInners = StringList.split(outerString, OUTER_INNERS_SEPARATOR);
            final Polygon outer = POLYGON_STRING_CONVERTER.convert(outerInners.get(0));
            if (outerInners.size() > 1)
            {
                final StringList inners = StringList.split(outerInners.get(1), INNER_SEPARATOR);

                for (final String innerString : inners)
                {
                    result.add(outer, POLYGON_STRING_CONVERTER.convert(innerString));
                }
            }
            else
            {
                result.put(outer, new ArrayList<>());
            }
        }
        return new MultiPolygon(result);
    }
}
