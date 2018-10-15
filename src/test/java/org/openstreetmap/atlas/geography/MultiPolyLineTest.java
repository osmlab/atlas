package org.openstreetmap.atlas.geography;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolyLineConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolyLineConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;

/**
 * {@link MultiPolyLine} tests.
 *
 * @author yalimu
 * @author mgostintsev
 */
public class MultiPolyLineTest
{
    private static final Logger logger = LoggerFactory.getLogger(MultiPolyLineTest.class);

    @Test
    public void testBounds()
    {
        final String wkt = "MULTILINESTRING ((113.9980787038803 7.3216002915048872, 113.99803847074506 7.3215225281339456), "
                + "(113.99799555540086 7.3218335816030984, 113.99808806341453 7.3217805876994444))";
        final MultiPolyLine multiPolyLine = MultiPolyLine.wkt(wkt);
        final Rectangle bound = multiPolyLine.bounds();

        final List<Location> locations = Lists.newArrayList();
        locations.add(Location.forString("7.3216002915048872, 113.9980787038803"));
        locations.add(Location.forString("7.3215225281339456, 113.99803847074506"));
        locations.add(Location.forString("7.3218335816030984, 113.99799555540086"));
        locations.add(Location.forString("7.3217805876994444, 113.99808806341453"));
        final Rectangle rectangle = Rectangle.forLocations(locations);
        Assert.assertTrue(rectangle.equals(bound));
    }

    @Test
    public void testConvertMultiPloyLineToJson()
    {
        final String wkt = "MULTILINESTRING ((113.9980787038803 7.3216002915048872, "
                + "113.99803847074506 7.3215225281339456))";
        final MultiPolyLine multiPolyLine = MultiPolyLine.wkt(wkt);
        final Set<Location> locations = Sets.newHashSet();
        locations.add(Location.forString("7.3216002915048872, 113.9980787038803"));
        locations.add(Location.forString("7.3215225281339456, 113.99803847074506"));
        multiPolyLine.asLocationIterableProperties().forEach(property -> property.getLocations()
                .forEach(location -> Assert.assertTrue(locations.contains(location))));
    }

    @Test
    public void testCreateMultiLineStringFromMultiPolyLine()
    {
        final String wkt = "MULTILINESTRING ((107.68471354246141 2.2346191319821231, 107.68471354246141 2.2345360028045156), "
                + "(107.68454724550249 2.2345601370821555, 107.68453115224835 2.2345601370821555, "
                + "107.68449419872607 2.2344243539043736))";

        final MultiPolyLine multiPolyLine = MultiPolyLine.wkt(wkt);
        final PolyLine polyLine1 = PolyLine.wkt(
                "LINESTRING (107.68471354246141 2.2346191319821231, 107.68471354246141 2.2345360028045156)");
        final PolyLine polyLine2 = PolyLine
                .wkt("LINESTRING (107.68454724550249 2.2345601370821555, "
                        + "107.68453115224835 2.2345601370821555, 107.68449419872607 2.2344243539043736)");

        final LineString lineString1 = new JtsPolyLineConverter().convert(polyLine1);
        final LineString lineString2 = new JtsPolyLineConverter().convert(polyLine2);

        final MultiLineString multiLineString = new JtsMultiPolyLineConverter()
                .convert(multiPolyLine);
        Assert.assertTrue("First line is contained", multiLineString.contains(lineString1));
        Assert.assertTrue("Second line is contained", multiLineString.contains(lineString2));
    }

    @Test
    public void testCreateMultiPolyLineFromPolyLine()
    {
        final PolyLine polyLine1 = PolyLine
                .wkt("LINESTRING (10.5553105 48.3419094, 10.5552096 48.3417501, 10.5551312 48.3416583, "
                        + "10.5551027 48.341611, 10.5550183 48.3415143, 10.5549357 48.3414668, "
                        + "10.5548325 48.3414164, 10.5548105 48.3415201, 10.5548015 48.3415686, "
                        + "10.5548925 48.3416166, 10.5550334 48.3416375, 10.5551312 48.3416583)");
        final PolyLine polyLine2 = PolyLine
                .wkt("LINESTRING (10.5551312 48.3416583, 10.5551027 48.341611, 10.5550183 48.3415143, "
                        + "10.5549357 48.3414668, 10.5548325 48.3414164, 10.5548105 48.3415201, "
                        + "10.5548015 48.3415686, 10.5548925 48.3416166, 10.5550334 48.3416375)");
        final MultiPolyLine multiPolyLine = new MultiPolyLine(Arrays.asList(polyLine1, polyLine2));
        logger.info("Create MultiPolyLine from a list of PolyLine {}", multiPolyLine.toString());
        final Set<PolyLine> polyLines = Sets.newHashSet();
        multiPolyLine.iterator().forEachRemaining(polyLines::add);
        Assert.assertEquals(2, polyLines.size());
        Assert.assertTrue(polyLines.contains(polyLine1));
        Assert.assertTrue(polyLines.contains(polyLine2));
    }

    @Test
    public void testCreateMultiPolyLineFromWKT()
    {
        final String wkt = "MULTILINESTRING ((107.68471354246141 2.2346191319821231, 107.68471354246141 2.2345360028045156), "
                + "(107.68454724550249 2.2345601370821555, 107.68453115224835 2.2345601370821555, "
                + "107.68449419872607 2.2344243539043736))";

        final MultiPolyLine multiPolyLine = MultiPolyLine.wkt(wkt);
        logger.info("Create MultiPolyLine from wkt {}", multiPolyLine.toString());
        final String polyLineWkt1 = "LINESTRING (107.68471354246141 2.2346191319821231, 107.68471354246141 2.2345360028045156)";
        final String polyLineWkt2 = "LINESTRING (107.68454724550249 2.2345601370821555, "
                + "107.68453115224835 2.2345601370821555, 107.68449419872607 2.2344243539043736))";
        final PolyLine polyLine1 = PolyLine.wkt(polyLineWkt1);
        final PolyLine polyLine2 = PolyLine.wkt(polyLineWkt2);

        final Set<PolyLine> polyLines = Sets.newHashSet();
        multiPolyLine.iterator().forEachRemaining(polyLines::add);
        Assert.assertTrue(polyLines.contains(polyLine1));
        Assert.assertTrue(polyLines.contains(polyLine2));
    }
}
