package org.openstreetmap.atlas.geography.converters;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolyLine;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolyLineConverter;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * Given an WKT string generate a {@link MultiLineString} and vice-versa
 *
 * @author yalimu
 */
public class WktMultiPolyLineConverter implements TwoWayConverter<MultiPolyLine, String>
{
    @Override
    public MultiPolyLine backwardConvert(final String wkt)
    {
        MultiLineString geometry = null;
        final WKTReader myReader = new WKTReader();
        try
        {
            geometry = (MultiLineString) myReader.read(wkt);
        }
        catch (final ParseException | ClassCastException e)
        {
            throw new CoreException("Cannot parse wkt : {}", wkt);
        }
        return new JtsMultiPolyLineConverter().backwardConvert(geometry);
    }

    @Override
    public String convert(final MultiPolyLine multiPolyLine)
    {
        final MultiLineString geometry = new JtsMultiPolyLineConverter().convert(multiPolyLine);
        return new WKTWriter().write(geometry);
    }
}
