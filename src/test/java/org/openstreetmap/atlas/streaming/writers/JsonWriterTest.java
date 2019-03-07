package org.openstreetmap.atlas.streaming.writers;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.streaming.resource.StringResource;

/**
 * @author matthieun
 */
public class JsonWriterTest
{
    @Test
    public void testWrite()
    {
        final StringResource resource = new StringResource();
        final JsonWriter writer = new JsonWriter(resource);
        final PolyLine polyLine = new PolyLine(Location.TEST_6, Location.TEST_2, Location.TEST_2);
        writer.write(polyLine.asGeoJson());
        writer.close();
        Assert.assertEquals(polyLine.asGeoJson().toString(), resource.writtenString());
    }
}
