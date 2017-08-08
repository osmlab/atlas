package org.openstreetmap.atlas.utilities.runtime;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple retry of a job
 *
 * @author matthieun
 */
public class Retry
{
    private static final Logger logger = LoggerFactory.getLogger(Retry.class);
    private static final Predicate<Throwable> FILTER_NONE = throwable -> false;

    private final int retries;
    private final Duration waitBeforeRetry;
    private boolean quiet = false;

    /**
     * @param retries
     *            The number of allowed retries
     * @param waitBeforeRetry
     *            The {@link Duration} to wait for before each retry
     */
    public Retry(final int retries, final Duration waitBeforeRetry)
    {
        this.retries = retries >= 0 ? retries : 0;
        this.waitBeforeRetry = waitBeforeRetry;
    }

    /**
     * Run a {@link Runnable} task with some retries
     *
     * @param runnable
     *            The task
     */
    public void run(final Runnable runnable)
    {
        run(runnable, null, FILTER_NONE);
    }

    /**
     * @param runnable
     *            The task
     * @param exceptionsWhichShouldBreakDirectly
     *            A filter that chooses what exceptions are to break directly, without triggering a
     *            retry
     */
    public void run(final Runnable runnable,
            final Predicate<Throwable> exceptionsWhichShouldBreakDirectly)
    {
        run(runnable, null, exceptionsWhichShouldBreakDirectly);
    }

    /**
     * Run a {@link Runnable} task with some retries
     *
     * @param runnable
     *            The task
     * @param runBeforeRetry
     *            What to do before retrying
     */
    public void run(final Runnable runnable, final Runnable runBeforeRetry)
    {
        run(runnable, runBeforeRetry, FILTER_NONE);
    }

    /**
     * @param runnable
     *            The task
     * @param runBeforeRetry
     *            What to do before retrying
     * @param exceptionsWhichShouldBreakDirectly
     *            A filter that chooses what exceptions are to break directly, without triggering a
     *            retry
     */
    public void run(final Runnable runnable, final Runnable runBeforeRetry,
            final Predicate<Throwable> exceptionsWhichShouldBreakDirectly)
    {
        // Use the Callable implementation
        run(() ->
        {
            runnable.run();
            return "";
        }, runBeforeRetry, exceptionsWhichShouldBreakDirectly);
    }

    /**
     * Run a {@link Supplier} task with some retries
     *
     * @param callable
     *            The task
     * @param <Value>
     *            The return type of the callable
     * @return The result of the first successful call
     */
    public <Value> Value run(final Supplier<Value> callable)
    {
        return run(callable, null, FILTER_NONE);
    }

    /**
     * Run a {@link Supplier} task with some retries
     *
     * @param callable
     *            The task
     * @param <Value>
     *            The return type of the callable
     * @param exceptionsWhichShouldBreakDirectly
     *            A filter that chooses what exceptions are to break directly, without triggering a
     *            retry
     * @return The result of the first successful call
     */
    public <Value> Value run(final Supplier<Value> callable,
            final Predicate<Throwable> exceptionsWhichShouldBreakDirectly)
    {
        return run(callable, null, exceptionsWhichShouldBreakDirectly);
    }

    /**
     * Run a {@link Supplier} task with some retries
     *
     * @param callable
     *            The task
     * @param <Value>
     *            The return type of the callable
     * @param runBeforeRetry
     *            What to do before retrying
     * @return The result of the first successful call
     */
    public <Value> Value run(final Supplier<Value> callable, final Runnable runBeforeRetry)
    {
        return run(callable, runBeforeRetry, FILTER_NONE);
    }

    /**
     * Run a {@link Supplier} task with some retries
     *
     * @param callable
     *            The task
     * @param <Value>
     *            The return type of the callable
     * @param runBeforeRetry
     *            What to do before retrying
     * @param exceptionsWhichShouldBreakDirectly
     *            A filter that chooses what exceptions are to break directly, without triggering a
     *            retry
     * @return The result of the first successful call
     */
    public <Value> Value run(final Supplier<Value> callable, final Runnable runBeforeRetry,
            final Predicate<Throwable> exceptionsWhichShouldBreakDirectly)
    {
        int retry = 0;
        Value result = null;
        boolean success = false;
        Throwable lastError = null;
        while (!success && retry <= this.retries)
        {
            try
            {
                if (retry > 0 && runBeforeRetry != null)
                {
                    runBeforeRetry.run();
                }
                result = callable.get();
                success = true;
            }
            catch (final Throwable throwable)
            {
                if (exceptionsWhichShouldBreakDirectly.test(throwable))
                {
                    throw throwable;
                }
                if (!this.quiet)
                {
                    logger.error("Failed retry number " + retry, throwable);
                }
                lastError = throwable;
                retry++;
                this.waitBeforeRetry.sleep();
            }
        }
        if (success)
        {
            return result;
        }
        else
        {
            throw new RuntimeException("Failed execution after " + retry + " retries.", lastError);
        }
    }

    /**
     * @param quiet
     *            True to suppress logging of errors when a retry is happening.
     */
    public void setQuiet(final boolean quiet)
    {
        this.quiet = quiet;
    }
}
