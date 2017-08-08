package org.openstreetmap.atlas.utilities.maps;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.streaming.Streams;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;

/**
 * @author matthieun
 */
public class MultiMapTest
{
    public static final MultiMap<Polygon, Polygon> getMultiMap()
    {
        final MultiMap<Polygon, Polygon> map = new MultiMap<>();
        map.add(Polygon.SILICON_VALLEY, Rectangle.TEST_RECTANGLE_2);
        map.add(Polygon.SILICON_VALLEY, Rectangle.TEST_RECTANGLE);
        return map;
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException
    {
        final MultiMap<Polygon, Polygon> map = getMultiMap();
        final WritableResource out = new ByteArrayResource();
        final ObjectOutputStream outStream = new ObjectOutputStream(out.write());
        outStream.writeObject(map);
        Streams.close(outStream);

        try (ObjectInputStream inStream = new ObjectInputStream(out.read()))
        {
            @SuppressWarnings("unchecked")
            final MultiMap<Polygon, Polygon> result = (MultiMap<Polygon, Polygon>) inStream
                    .readObject();
            Assert.assertEquals(map, result);
        }
        catch (final Exception e)
        {
            throw e;
        }
    }
}
