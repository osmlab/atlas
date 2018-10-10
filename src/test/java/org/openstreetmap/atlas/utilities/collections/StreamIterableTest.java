package org.openstreetmap.atlas.utilities.collections;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Sagar Rohankar
 * @author mgostintsev
 */
public class StreamIterableTest
{
    private static final Logger logger = LoggerFactory.getLogger(StreamIterableTest.class);

    @Test
    public void testAllMatch() throws Exception
    {
        final StreamIterable<Integer> streamIterable = new StreamIterable<>(asList(1, 2, 3, 4));

        // true -> all numbers are less than 5
        Assert.assertTrue(streamIterable.allMatch(n -> n < 5));
        // false -> all numbers are even
        Assert.assertFalse(streamIterable.allMatch(n -> n % 2 == 0));
    }

    @Test
    public void testAllMatchParallel() throws Exception
    {
        final StreamIterable<Integer> streamIterable = new StreamIterable<>(asList(1, 2, 3, 4),
                true);

        // true -> all numbers are less than 5
        Assert.assertTrue(streamIterable.allMatch(n -> n < 5));
        // false -> all numbers are even
        Assert.assertFalse(streamIterable.allMatch(n -> n % 2 == 0));
    }

    @Test
    public void testAnyMatch()
    {
        final StreamIterable<Integer> streamIterable = new StreamIterable<>(asList(6, 12, 18));

        // true -> any number that is divisible by 9
        Assert.assertTrue(streamIterable.anyMatch(n -> n % 9 == 0));
        // false -> any number that is divisible by 5
        Assert.assertFalse(streamIterable.allMatch(n -> n % 5 == 0));
    }

    @Test
    public void testAnyMatchParallel()
    {
        final StreamIterable<Integer> streamIterable = new StreamIterable<>(asList(6, 12, 18),
                true);

        // true -> any number that is divisible by 9
        Assert.assertTrue(streamIterable.anyMatch(n -> n % 9 == 0));
        // false -> any number that is divisible by 5
        Assert.assertFalse(streamIterable.allMatch(n -> n % 5 == 0));
    }

    @Test
    public void testParallelPerformance()
    {
        final List<Integer> numbers = new ArrayList<>();
        IntStream.range(0, 10000000).forEach(number ->
        {
            numbers.add(number);
        });
        final StreamIterable<Integer> streamIterable = new StreamIterable<>(numbers)
                .disableParallelization();
        Time currentTime = Time.now();
        streamIterable.anyMatch(n -> n > 100000);
        final Duration sequentialDuration = currentTime.elapsedSince();
        logger.debug("Sequential duration was {} ms", sequentialDuration.asMilliseconds());

        currentTime = Time.now();
        streamIterable.enableParallelization();
        streamIterable.anyMatch(n -> n > 100000);
        final Duration parallelDuration = currentTime.elapsedSince();
        logger.debug("Parallel duration was {} ms", parallelDuration.asMilliseconds());
        Assert.assertTrue(parallelDuration.isLessThan(sequentialDuration));
    }

}
