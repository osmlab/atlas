package org.openstreetmap.atlas.utilities.threads;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wrapper for a pool of threads.
 *
 * @author matthieun
 */
public class Pool implements Closeable
{
    /**
     * Executor that will save all unhandled exceptions thrown by failed tasks to be thrown out
     * later in a call to close.
     *
     * @author cstaylor
     */
    private class FixedThreadPoolExecutor extends ThreadPoolExecutor
    {
        FixedThreadPoolExecutor()
        {
            super(Pool.this.numberOfThreads, Pool.this.numberOfThreads, 0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(),
                    new CustomNamesThreadPoolFactory(Pool.this.name));
        }

        @Override
        protected void afterExecute(final Runnable runnable, final Throwable oops)
        {
            if (oops != null)
            {
                Pool.this.errors.add(oops);
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(Pool.class);
    private final ExecutorService pool;
    private final String name;
    private final int numberOfThreads;
    private final Duration endTimeout;
    private final Vector<Throwable> errors;

    public Pool(final int numberOfThreads, final String name)
    {
        this(numberOfThreads, name, Duration.ONE_DAY);
    }

    public Pool(final int numberOfThreads, final String name, final Duration endTimeout)
    {
        this.numberOfThreads = numberOfThreads;
        this.name = name;
        this.endTimeout = endTimeout;
        this.errors = new Vector<>();
        this.pool = new FixedThreadPoolExecutor();
    }

    @Override
    public void close()
    {
        this.end(this.endTimeout);
    }

    /**
     * This method checks that a thread pool is really finished, i.e. all the tasks have been
     * completed.
     *
     * @param maxDuration
     *            The maximum duration to wait before returning.
     */
    public void end(final Duration maxDuration)
    {
        if (!stop(maxDuration))
        {
            logger.warn("Thread pool {} has ended before it was terminated (maxDuration = {}).",
                    this.getName(), maxDuration);
        }
        if (!this.errors.isEmpty())
        {
            this.errors
                    .forEach((error) -> logger.error("Unhandled error in {}!", this.name, error));
            throw new CoreException("{} tasks in {} had uncaught errors!", this.errors.size(),
                    this.name);
        }
    }

    public String getName()
    {
        return this.name;
    }

    public boolean isDead()
    {
        return this.pool.isTerminated() || this.pool.isShutdown();
    }

    public <T> Result<T> queue(final Callable<T> task)
    {
        return new Result<>(this.pool.submit(task), this);
    }

    public <T, V> V queue(final Callable<T> task, final Function<T, V> doWithTheOutput,
            final Duration timeout) throws TimeoutException
    {
        final Result<T> result = queue(task);
        final T item = result.get(timeout);
        return doWithTheOutput.apply(item);
    }

    public <T> Result<T> queue(final Callable<T> task, final Ticker ticker)
    {
        final Callable<T> taskWrapper = () ->
        {
            try
            {
                return task.call();
            }
            finally
            {
                ticker.close();
            }
        };
        this.queue(ticker);
        return new Result<>(this.pool.submit(taskWrapper), this, ticker);
    }

    public void queue(final Runnable command)
    {
        this.pool.execute(command);
    }

    public void queue(final Runnable command, final Ticker ticker)
    {
        final Runnable commandWrapper = () ->
        {
            try
            {
                command.run();
            }
            finally
            {
                ticker.close();
            }
        };
        this.pool.execute(ticker);
        this.pool.execute(commandWrapper);
    }

    public <T> List<Result<T>> queueAll(final Iterable<Callable<T>> tasks)
    {
        try
        {
            final List<Future<T>> results = this.pool.invokeAll(Iterables.asList(tasks));
            return results.stream().flatMap(future -> Stream.of(new Result<>(future, this)))
                    .collect(Collectors.toList());
        }
        catch (final InterruptedException e)
        {
            throw new CoreException("Could not submit multiple Callables to {}", e, this.name);
        }
    }

    public <T, V> List<V> queueAll(final Iterable<Callable<T>> tasks,
            final Function<T, V> doWithTheOutput, final Duration timeoutForEach)
    {
        final List<V> result = new ArrayList<>();
        final List<Result<T>> output = queueAll(tasks);
        output.forEach(futureResult ->
        {
            try
            {
                final T input = futureResult.get(timeoutForEach);
                final V out = doWithTheOutput.apply(input);
                result.add(out);
            }
            catch (final TimeoutException e)
            {
                logger.warn("Timed out on {}", futureResult);
            }
        });
        return result;
    }

    public void queueCommands(final Iterable<Runnable> commands)
    {
        commands.forEach(command -> queue(command));
    }

    public boolean stop(final Duration waitBeforeKill)
    {
        this.pool.shutdown();
        try
        {
            return this.pool.awaitTermination(waitBeforeKill.asMilliseconds(),
                    TimeUnit.MILLISECONDS);
        }
        catch (final InterruptedException e)
        {
            logger.warn("Was interrupted. Could not stop {} within {}.", this.name, waitBeforeKill,
                    e);
            return false;
        }
    }

    @Override
    public String toString()
    {
        return "[Pool: " + getName() + ", " + this.numberOfThreads + " threads]";
    }
}
