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
        runWithTimer(Duration.seconds(5), () ->
        {
            try (Pool pool = new Pool(2, "testPoolQueue", Duration.ONE_SECOND))
            {
                pool.queue(() -> System.out.println("1"));
                pool.queue(() -> System.out.println("2"));
                pool.queue(() -> System.out.println("3"));
                pool.queue(() -> System.out.println("4"));
                pool.close();
            }
        });
    }

    @Test
    public void testTickerCallable()
    {
        runWithTimer(Duration.seconds(5), () ->
        {
            try (Pool pool = new Pool(2, "testTickerCallable", Duration.seconds(10)))
            {
                final List<Result<Boolean>> results = new ArrayList<>();
                for (int index = 0; index < 5; index++)
                {
                    final int idx = index;
                    final Callable<Boolean> callable = () ->
                    {
                        Duration.milliseconds(100).sleep();
                        System.out.println("Thread " + idx + " done.");
                        return true;
                    };
                    results.add(pool.queue(callable,
                            new LogTicker("Ticker " + idx, Duration.milliseconds(50))));
                }
                System.out.println("All submitted to pool!");
                for (final Result<Boolean> result : results)
                {
                    Assert.assertTrue(result.get());
                }
            }
            System.out.println("Pool Ended.");
        });
    }

    @Test(expected = CoreException.class)
    public void testTickerCallableWithErrors()
    {
        runWithTimer(Duration.seconds(500000), () ->
        {
            try (Pool pool = new Pool(2, "testTickerCallableWithErrors", Duration.seconds(10)))
            {
                final List<Result<Boolean>> results = new ArrayList<>();
                for (int index = 0; index < 5; index++)
                {
                    final int idx = index;
                    final Callable<Boolean> callable = () ->
                    {
                        Duration.milliseconds(100).sleep();
                        if (idx != 3)
                        {
                            System.out.println("Thread " + idx + " done.");
                        }
                        else
                        {
                            throw new CoreException("Failing task {} on purpose.", idx);
                        }
                        return true;
                    };
                    results.add(pool.queue(callable,
                            new LogTicker("Ticker " + idx, Duration.milliseconds(50))));
                }
                System.out.println("All submitted to pool!");
                for (final Result<Boolean> result : results)
                {
                    Assert.assertTrue(result.get());
                }
            }
            System.out.println("Pool Ended.");
        });
    }

    @Test
    public void testTickerFailureHandling()
    {
        runWithTimer(Duration.seconds(5), () ->
        {
            try (Pool pool = new Pool(2, "testTickerCallable", Duration.seconds(10)))
            {
                final List<Result<Boolean>> results = new ArrayList<>();
                for (int index = 0; index < 5; index++)
                {
                    final int idx = index;
                    final Callable<Boolean> callable = () ->
                    {
                        Duration.milliseconds(100).sleep();
                        System.out.println("Thread " + idx + " done.");
                        return true;
                    };
                    final Ticker rogueTicker = new Ticker("Ticker " + idx,
                            Duration.milliseconds(50))
                    {
                        @Override
                        protected void tickAction(final Duration sinceStart)
                        {
                            logger.info("{}: {}", getName(), sinceStart);
                            if ("Ticker 3".equals(getName()))
                            {
                                throw new CoreException("I am rogue Ticker 3");
                            }
                        }
                    };
                    results.add(pool.queue(callable, rogueTicker));
                }
                System.out.println("All submitted to pool!");
                for (final Result<Boolean> result : results)
                {
                    Assert.assertTrue(result.get());
                }
            }
            System.out.println("Pool Ended.");
        });
    }

    @Test
    public void testTickerRunnable()
    {
        runWithTimer(Duration.seconds(5), () ->
        {
            try (Pool pool = new Pool(2, "testTickerRunnable", Duration.seconds(10)))
            {
                for (int index = 0; index < 5; index++)
                {
                    final int idx = index;
                    final Runnable runnable = () ->
                    {
                        Duration.milliseconds(100).sleep();
                        System.out.println("Thread " + idx + " done.");
                    };
                    pool.queue(runnable, new LogTicker("Ticker " + idx, Duration.milliseconds(50)));
                }
                System.out.println("All submitted to pool!");
            }
            System.out.println("Pool Ended.");
        });
    }

    @Test(expected = CoreException.class)
    public void testTickerRunnableWithErrors()
    {
        runWithTimer(Duration.seconds(5), () ->
        {
            try (Pool pool = new Pool(2, "testTickerRunnableWithErrors", Duration.seconds(10)))
            {
                for (int index = 0; index < 5; index++)
                {
                    final int idx = index;
                    final Runnable runnable = () ->
                    {
                        Duration.milliseconds(100).sleep();
                        if (idx != 3)
                        {
                            System.out.println("Thread " + idx + " done.");
                        }
                        else
                        {
                            throw new CoreException("Failing task {} on purpose.", idx);
                        }
                    };
                    pool.queue(runnable, new LogTicker("Ticker " + idx, Duration.milliseconds(50)));
                }
                System.out.println("All submitted to pool!");
            }
            System.out.println("Pool Ended.");
        });
    }

    @Test(expected = TimeoutException.class)
    public void testTimeout() throws TimeoutException
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
            throw e;
        }
    }

    private void runWithTimer(final Duration maximum, final Runnable test)
    {
        try (Pool pool = new Pool(1, "RunWithTimer", maximum))
        {
            pool.queue(() ->
            {
                test.run();
                return true;
            }).get(maximum);
        }
        catch (final TimeoutException e)
        {
            throw new RuntimeException(
                    "Timeout while running unit test (Max allowed time was " + maximum + ").", e);
        }
    }
}
