package org.openstreetmap.atlas.utilities.collections;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class SubIterableTest
{
    private static final Logger logger = LoggerFactory.getLogger(SubIterableTest.class);

    @Test
    public void testTruncation()
    {
        final List<Integer> values = new ArrayList<>();
        for (int index = 0; index < 10; index++)
        {
            values.add(index);
        }

        // Middle
        final List<Integer> truncated1 = Iterables.stream(values).truncate(2, 4).collectToList();
        logger.info("{}", truncated1);
        Assert.assertEquals(4, truncated1.size());
        Assert.assertEquals(Iterables.asList(Iterables.from(2, 3, 4, 5)), truncated1);

        // 0, 0
        final List<Integer> truncated2 = Iterables.stream(values).truncate(0, 0).collectToList();
        logger.info("{}", truncated2);
        Assert.assertEquals(10, truncated2.size());
        Assert.assertEquals(Iterables.asList(Iterables.from(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)),
                truncated2);

        // 0, middle
        final List<Integer> truncated3 = Iterables.stream(values).truncate(0, 7).collectToList();
        logger.info("{}", truncated3);
        Assert.assertEquals(3, truncated3.size());
        Assert.assertEquals(Iterables.asList(Iterables.from(0, 1, 2)), truncated3);

        // middle, 0
        final List<Integer> truncated4 = Iterables.stream(values).truncate(7, 0).collectToList();
        logger.info("{}", truncated4);
        Assert.assertEquals(3, truncated4.size());
        Assert.assertEquals(Iterables.asList(Iterables.from(7, 8, 9)), truncated4);

        // negative
        final List<Integer> truncated5 = Iterables.stream(values).truncate(-5, -1).collectToList();
        logger.info("{}", truncated5);
        Assert.assertEquals(10, truncated5.size());
        Assert.assertEquals(Iterables.asList(Iterables.from(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)),
                truncated5);

        // crossing
        final List<Integer> truncated6 = Iterables.stream(values).truncate(7, 7).collectToList();
        logger.info("{}", truncated6);
        Assert.assertEquals(0, truncated6.size());
        Assert.assertEquals(Iterables.asList(Iterables.from()), truncated6);

        // Only one left
        final List<Integer> truncated7 = Iterables.stream(values).truncate(5, 4).collectToList();
        logger.info("{}", truncated7);
        Assert.assertEquals(1, truncated7.size());
        Assert.assertEquals(Iterables.asList(Iterables.from(5)), truncated7);

        // Bigger than size
        final List<Integer> truncated8 = Iterables.stream(values).truncate(20, 15).collectToList();
        logger.info("{}", truncated8);
        Assert.assertEquals(0, truncated8.size());
    }
}
