package org.openstreetmap.atlas.geography.converters;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.utilities.maps.MultiMap;

/**
 * @author matthieun
 */
public class WktMultiPolygonConverterTest
{
    private static final WktMultiPolygonConverter CONVERTER = new WktMultiPolygonConverter();

    private static final Location LOCATION_1 = Location.forString("24.2889638, 68.763726");
    private static final Location LOCATION_2 = Location.forString("24.2847937, 68.7691521");
    private static final Location LOCATION_3 = Location.forString("24.2726051, 68.7695996");
    private static final Location LOCATION_4 = Location.forString("24.2558968, 68.7686899");

    @Test
    public void testConversionMultiPolygon()
    {
        final String wkt = "POLYGON ((68.763726 24.2889638, 68.7691521 24.2847937, 68.7695996 24.2726051, "
                + "68.7686899 24.2558968, 68.763726 24.2889638), "
                + "(68.763726 24.2889638, 68.7691521 24.2847937, 68.7695996 24.2726051, 68.763726 24.2889638))";
        final MultiPolygon multiPolygon = CONVERTER.backwardConvert(wkt);
        final Polygon polygon1 = new Polygon(LOCATION_1, LOCATION_2, LOCATION_3, LOCATION_4);
        final Polygon polygon2 = new Polygon(LOCATION_1, LOCATION_2, LOCATION_3);
        final MultiMap<Polygon, Polygon> outersToInners = new MultiMap<>();
        outersToInners.add(polygon1, polygon2);
        final MultiPolygon truth = new MultiPolygon(outersToInners);
        Assert.assertEquals(truth, multiPolygon);
        Assert.assertEquals(wkt, CONVERTER.convert(truth));
    }

    @Test
    public void testConversionMultiPolygonWithMultipleOuters()
    {
        final String wkt = "MULTIPOLYGON ("
                + "((68.763726 24.2889638, 68.7691521 24.2847937, 68.7695996 24.2726051, "
                + "68.7686899 24.2558968, 68.763726 24.2889638), "
                + "(68.763726 24.2889638, 68.7691521 24.2847937, 68.7695996 24.2726051, 68.763726 24.2889638)), "
                + "((68.763726 24.2889638, 68.7691521 24.2847937, 68.7695996 24.2726051, 68.763726 24.2889638), "
                + "(68.763726 24.2889638, 68.7691521 24.2847937, 68.7695996 24.2726051, 68.763726 24.2889638))"
                + ")";
        final MultiPolygon multiPolygon = CONVERTER.backwardConvert(wkt);
        final Polygon polygon1 = new Polygon(LOCATION_1, LOCATION_2, LOCATION_3, LOCATION_4);
        final Polygon polygon2 = new Polygon(LOCATION_1, LOCATION_2, LOCATION_3);
        final MultiMap<Polygon, Polygon> outersToInners = new MultiMap<>();
        outersToInners.add(polygon1, polygon2);
        outersToInners.add(polygon2, polygon2);
        final MultiPolygon truth = new MultiPolygon(outersToInners);
        Assert.assertEquals(truth, multiPolygon);
        Assert.assertEquals(wkt, CONVERTER.convert(truth));
    }

    @Test
    public void testConversionPolygon()
    {
        final String wkt = "POLYGON ((68.763726 24.2889638, 68.7691521 24.2847937, "
                + "68.7695996 24.2726051, 68.7686899 24.2558968, 68.763726 24.2889638))";
        final MultiPolygon multiPolygon = CONVERTER.backwardConvert(wkt);

        final MultiPolygon truth = MultiPolygon
                .forPolygon(new Polygon(LOCATION_1, LOCATION_2, LOCATION_3, LOCATION_4));
        Assert.assertEquals(truth, multiPolygon);
        Assert.assertEquals(wkt, CONVERTER.convert(truth));
    }
}
