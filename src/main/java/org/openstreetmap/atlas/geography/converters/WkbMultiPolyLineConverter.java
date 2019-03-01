package org.openstreetmap.atlas.geography.converters;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKBReader;
import org.locationtech.jts.io.WKBWriter;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolyLine;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolyLineConverter;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

/**
 * A class for converting between MultiLineStrings in WKB format and {@link MultiPolyLine}
 *
 * @author jklamer
 */
public class WkbMultiPolyLineConverter implements TwoWayConverter<MultiPolyLine, byte[]>
{
    private static final WKBReader WKB_READER = new WKBReader();

    @Override
    public MultiPolyLine backwardConvert(final byte[] wkb)
    {
        MultiLineString geometry = null;
        try
        {
            geometry = (MultiLineString) WKB_READER.read(wkb);
        }
        catch (final ParseException | ClassCastException e)
        {
            throw new CoreException("Cannot parse wkb : {}", WKBWriter.toHex(wkb));
        }
        return new JtsMultiPolyLineConverter().backwardConvert(geometry);
    }

    @Override
    public byte[] convert(final MultiPolyLine multiPolyLine)
    {
        final Geometry geometry = new JtsMultiPolyLineConverter().convert(multiPolyLine);
        return new WKBWriter().write(geometry);
    }
}
