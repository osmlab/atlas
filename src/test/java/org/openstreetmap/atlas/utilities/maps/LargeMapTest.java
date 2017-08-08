package org.openstreetmap.atlas.utilities.maps;

import java.io.ObjectOutputStream;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.Streams;
import org.openstreetmap.atlas.streaming.resource.File;

/**
 * @author matthieun
 */
public class LargeMapTest
{
    public static void main(final String[] args)
    {
        new LargeMapTest().largeSerialization();
    }

    public void largeSerialization()
    {
        final int size = 300_000_000;
        final LongToLongMap map = new LongToLongMap("testMap", size, size / 10, size, size, size,
                size);
        final File writableResource = new File(
                System.getProperty("user.home") + "/projects/data/unitTest/LongToLongMap.dat");
        final Random random = new Random();
        for (int i = 0; i < size; i++)
        {
            map.put((long) i, random.nextLong());
        }
        ObjectOutputStream out = null;
        try
        {
            out = new ObjectOutputStream(writableResource.write());
            out.writeObject(map);
            Streams.close(out);
        }
        catch (final Exception e)
        {
            Streams.close(out);
            throw new CoreException("Could not save to {}", e, writableResource);
        }
    }

    @Test
    public void testMap()
    {
        final LongToLongMap map = new LongToLongMap("TestMap", 100, 10, 2, 15, 2, 15);
        for (int i = 0; i < 100; i++)
        {
            map.put((long) i, i * 1000L);
        }
        try
        {
            map.put(101L, 0L);
            Assert.fail("Cannot go over array size");
        }
        catch (final CoreException e)
        {
            // OK
        }
        // This works as it is just replacing an item
        map.put(0L, 9999999999L);
        map.forEach(key -> System.out.print(key + "->" + map.get(key) + ", "));
        System.out.println();
    }
}
