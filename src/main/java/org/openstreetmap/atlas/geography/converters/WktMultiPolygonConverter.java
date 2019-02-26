package org.openstreetmap.atlas.geography.converters;

import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

/**
 * Given an WKT string generate a {@link MultiPolygon} and vice-versa
 *
 * @author matthieun
 */
public class WktMultiPolygonConverter implements TwoWayConverter<MultiPolygon, String>
{
    @Override
    public MultiPolygon backwardConvert(final String wkt)
    {
        org.locationtech.jts.geom.MultiPolygon geometry = null;
        final WKTReader myReader = new WKTReader();
        try
        {
            geometry = (org.locationtech.jts.geom.MultiPolygon) myReader.read(wkt);
        }
        catch (final ParseException | ClassCastException e)
        {
            throw new CoreException("Cannot parse wkt : {}", wkt);
        }
        return new JtsMultiPolygonToMultiPolygonConverter().convert(geometry);
    }

    @Override
    public String convert(final MultiPolygon multiPolygon)
    {
        final org.locationtech.jts.geom.MultiPolygon geometry = new JtsMultiPolygonToMultiPolygonConverter()
                .backwardConvert(multiPolygon);
        return new WKTWriter().write(geometry);
    }
}
