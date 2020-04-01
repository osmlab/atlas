package org.openstreetmap.atlas.geography.converters;

import org.junit.Assert;
import org.junit.Test;
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
        final String result = output.all();
        final String expected = "POLYGON ((30 10, 10 10, 20 20, 10 40, "
                + "40 40, 30 30, 40 20, 30 10))";
        Assert.assertEquals(expected, result);
    }
}
