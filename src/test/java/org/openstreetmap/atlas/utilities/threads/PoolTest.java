package org.openstreetmap.atlas.utilities.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class PoolTest
{
    private static final Logger logger = LoggerFactory.getLogger(PoolTest.class);

    public static void main(final String[] args)
    {
        new PoolTest().mainTest();
    }

    public void mainTest()
    {
        final Pool pool = new Pool(10, "test", Duration.milliseconds(100));
        for (int index = 0; index < 1000; index++)
        {
            final int idx = index;
            final Callable<Boolean> runnable = () ->
            {
                Duration.milliseconds(100).sleep();
                System.out.println("Thread " + idx + " done.");
                return true;
            };
            pool.queue(runnable);
        }
        System.out.println("All submitted to pool!");
        pool.close();
        System.out.println("Pool Ended.");
    }

    @Test
    public void testCallable()
    {
        final int size = 1_000_000;
        final Callable<Integer> callable = () -> new Random().nextInt();
        final List<Callable<Integer>> callables = new ArrayList<>();
        IntStream.range(0, size).forEach(value -> callables.add(callable));
        final Pool pool = new Pool(10, "testPoolCallable", Duration.milliseconds(100));
        final List<Integer> values = pool.queueAll(callables, value -> value, Duration.ONE_SECOND);
        Assert.assertEquals(size, values.size());
        pool.close();
    }

    @Test(expected = CoreException.class)
    public void testFailureCaught()
    {
        try (Pool pool = new Pool(2, "Failure", Duration.ONE_DAY))
        {
            // Cast to Runnable otherwise it assumes it is a Callable.
            pool.queue((Runnable) () ->
            {
                Duration.milliseconds(100).sleep();
                logger.info("Thread 1");
                throw new CoreException("Fail thread 1");
            });
            pool.queue((Runnable) () ->
            {
                Duration.milliseconds(100).sleep();
                logger.info("Thread 2");
                throw new CoreException("Fail thread 2");
            });
        }
    }

    @Test
    public void testQueue()
    {
        final Pool pool = new Pool(2, "testPoolQueue", Duration.ONE_SECOND);
        pool.queue(() -> System.out.println("1"));
        pool.queue(() -> System.out.println("2"));
        pool.queue(() -> System.out.println("3"));
        pool.queue(() -> System.out.println("4"));
        pool.close();
    }

    @Test
    public void testTimeout()
    {
        try (Pool pool = new Pool(1, "testPoolTimeout", Duration.ONE_SECOND))
        {
            final Result<Integer> result = pool.queue(() ->
            {
                Duration.seconds(10).sleep();
                return 10;
            });
            result.get(Duration.ONE_SECOND);
            Assert.fail("Did not throw TimeoutException");
        }
        catch (final TimeoutException e)
        {
            // Normal
        }
    }
}
