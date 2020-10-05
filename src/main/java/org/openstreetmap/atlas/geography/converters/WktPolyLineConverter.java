package org.openstreetmap.atlas.geography.converters;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolyLineConverter;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

/**
 * Given an WKT string generate a {@link PolyLine} and vice-versa
 *
 * @author matthieun
 */
public class WktPolyLineConverter implements TwoWayConverter<PolyLine, String>
{
    @Override
    public PolyLine backwardConvert(final String wkt)
    {
        LineString geometry = null;
        final WKTReader myReader = new WKTReader();
        try
        {
            geometry = (LineString) myReader.read(wkt);
        }
        catch (final ParseException | ClassCastException e)
        {
            throw new CoreException("Cannot parse wkt : {}", wkt, e);
        }
        return new JtsPolyLineConverter().backwardConvert(geometry);
    }

    @Override
    public String convert(final PolyLine polyLine)
    {
        final Geometry geometry = new JtsPolyLineConverter().convert(polyLine);
        return new WKTWriter().write(geometry);
    }
}
