package org.openstreetmap.atlas.utilities.arrays;

import java.io.ObjectOutputStream;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.scalars.Ratio;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class LargeArrayTest
{
    private static final Logger logger = LoggerFactory.getLogger(LargeArrayTest.class);
    private LongArray array;

    public static void main(final String[] args)
    {
        new LargeArrayTest().largeSerialization();
    }

    @Before
    public void init()
    {
        this.array = (LongArray) new LongArray(100, 2, 15).withName("testArray");
        for (int i = 0; i < 100; i++)
        {
            this.array.add((long) i);
        }
    }

    public void largeSerialization()
    {
        final int size = 500_000_000;
        final IntegerArray array = new IntegerArray(size, size, size);
        final File writableResourceJava = new File(
                System.getProperty("user.home") + "/projects/data/unitTest/IntegerArrayJava.dat");
        for (int i = 0; i < size; i++)
        {
            array.add(new Random().nextInt());
        }
        final Time start = Time.now();
        try (ObjectOutputStream out = new ObjectOutputStream(writableResourceJava.write()))
        {
            out.writeObject(array);
        }
        catch (final Exception e)
        {
            throw new CoreException("Could not save to {}", e, writableResourceJava);
        }
        logger.info("Java Serialization: {}", start.elapsedSince());
    }

    @Test
    public void testArray()
    {
        try
        {
            this.array.add(0L);
            Assert.fail("Cannot go over array size");
        }
        catch (final CoreException e)
        {
            // OK
        }
        Assert.assertEquals(100, this.array.size());
        this.array.forEach(value -> System.out.print(value + " "));
        System.out.println();
        Assert.assertEquals(new Long(53L), this.array.get(53));
        Assert.assertEquals(new Long(97L), this.array.get(97));
    }

    @Test
    public void testTrimming()
    {
        Assert.assertEquals(15,
                this.array.getArrays().get(this.array.getArrays().size() - 1).size());
        this.array.trimIfLessFilledThan(Ratio.HALF);
        Assert.assertEquals(15,
                this.array.getArrays().get(this.array.getArrays().size() - 1).size());
        this.array.trim();
        Assert.assertEquals(10,
                this.array.getArrays().get(this.array.getArrays().size() - 1).size());
    }
}
