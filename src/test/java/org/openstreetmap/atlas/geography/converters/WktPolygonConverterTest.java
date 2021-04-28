package org.openstreetmap.atlas.geography.converters;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;

/**
 * @author matthieun
 */
public class WktPolygonConverterTest
{
    private static final WktPolygonConverter CONVERTER = new WktPolygonConverter();

    private static final Location LOCATION_1 = Location.forString("24.2889638, 68.763726");
    private static final Location LOCATION_2 = Location.forString("24.2847937, 68.7691521");
    private static final Location LOCATION_3 = Location.forString("24.2726051, 68.7695996");
    private static final Location LOCATION_4 = Location.forString("24.2558968, 68.7686899");

    @Test
    public void testConversionMultiPolygonSingleOuter()
    {
        final String wkt = "MULTIPOLYGON (((68.763726 24.2889638, 68.7691521 24.2847937, 68.7695996 24.2726051, "
                + "68.7686899 24.2558968, 68.763726 24.2889638)))";
        final Polygon polygon = CONVERTER.backwardConvert(wkt);
        final Polygon truth = new Polygon(LOCATION_1, LOCATION_2, LOCATION_3, LOCATION_4);
        Assert.assertEquals(truth, polygon);
    }

    @Test(expected = CoreException.class)
    public void testConversionMultiPolygonWithMultipleOuters()
    {
        final String wkt = "MULTIPOLYGON ("
                + "((68.763726 24.2889638, 68.7691521 24.2847937, 68.7695996 24.2726051, "
                + "68.7686899 24.2558968, 68.763726 24.2889638), "
                + "(68.763726 24.2889638, 68.7691521 24.2847937, 68.7695996 24.2726051, 68.763726 24.2889638)), "
                + "((68.763726 24.2889638, 68.7691521 24.2847937, 68.7695996 24.2726051, 68.763726 24.2889638), "
                + "(68.763726 24.2889638, 68.7691521 24.2847937, 68.7695996 24.2726051, 68.763726 24.2889638))"
                + ")";
        CONVERTER.backwardConvert(wkt);
    }
}
