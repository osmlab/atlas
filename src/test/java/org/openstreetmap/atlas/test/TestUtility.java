package org.openstreetmap.atlas.test;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

/**
 * @author Yiqing Jin
 */
public final class TestUtility
{
    private static WKTReader reader;

    static
    {
        reader = new WKTReader();
    }

    public static Geometry createJtsGeometryFromWKT(final String wktString)
    {
        Geometry geometry = null;
        try
        {
            geometry = reader.read(wktString);
        }
        catch (final ParseException e)
        {
            e.printStackTrace();
        }
        return geometry;
    };

    private TestUtility()
    {
    }
}
