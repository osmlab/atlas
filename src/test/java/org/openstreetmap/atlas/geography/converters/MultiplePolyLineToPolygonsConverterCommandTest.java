package org.openstreetmap.atlas.geography.converters;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;

/**
 * @author matthieun
 */
public class MultiplePolyLineToPolygonsConverterCommandTest
{
    @Test
    public void testCommand()
    {
        final Resource input = new StringResource(
                "MULTILINESTRING ((10 10, 20 20, 10 40), (40 40, 30 30, 40 20, 30 10));"
                        + "LINESTRING (10 40, 40 40);LINESTRING (30 10, 10 10)");
        final WritableResource output = new StringResource();
        final String delimiter = ";";
        new MultiplePolyLineToPolygonsConverterCommand().translate(input, output, delimiter);
        final Polygon result = Polygon.wkt(output.all());
        final Polygon expected = Polygon
                .wkt("POLYGON ((30 10, 10 10, 20 20, 10 40, " + "40 40, 30 30, 40 20, 30 10))");
        Assert.assertTrue(expected.isSimilarTo(result));
    }
}
