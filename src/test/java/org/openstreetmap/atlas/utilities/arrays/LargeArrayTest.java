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

    /*
     * A NOTE ABOUT THIS TEST: Historically, LargeArray would mishandle trim() in the case that the
     * rightmost subarray was completely full. The trim() function has now been fixed, so this
     * should no longer happen. If this test ever fails, that means the bug has been re-introduced.
     */
    @Test
    public void trimArrayWithFullRightmostSubArray()
    {
        final int maxSize = 100;
        final int subArraySize = 10;
        final int blockSize = subArraySize;

        final LongArray array = new LongArray(maxSize, blockSize, subArraySize);

        // completely fill up three subarrays
        for (int i = 0; i < subArraySize * 3; i++)
        {
            array.add(2L);
        }

        // Now, we trim the array. This should only affect the rightmost* subarray
        // * Here, rightmost indicates it is the last subarray in LargeArray's list of subarrays
        array.trim();

        // this is fine, since the first subarray is not corrupted by trim()
        Assert.assertEquals(2L, array.get(2).longValue());

        // this is also fine, the second subarray is not corrupted
        Assert.assertEquals(2L, array.get(12).longValue());

        // this should be fine as well, now that the bug is fixed
        // NOTE that the bug would have caused this line to throw ArrayIndexOutOfBoundsException
        Assert.assertEquals(2L, array.get(23).longValue());
    }
}
