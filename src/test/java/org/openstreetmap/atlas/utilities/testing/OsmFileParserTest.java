package org.openstreetmap.atlas.utilities.testing;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.StringResource;

/**
 * @author matthieun
 */
public class OsmFileParserTest
{
    @Test
    public void testReplacement()
    {
        final Resource josmOsmFile = new InputStreamResource(
                () -> OsmFileParserTest.class.getResourceAsStream("josmOsmFile.osm"));
        final Resource osmFile = new InputStreamResource(
                () -> OsmFileParserTest.class.getResourceAsStream("osmFile.osm"));
        final StringResource result = new StringResource();
        new OsmFileParser().update(josmOsmFile, result);
        Assert.assertEquals(osmFile.all(), result.all());
    }
}
