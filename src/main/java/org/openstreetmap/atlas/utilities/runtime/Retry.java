package org.openstreetmap.atlas.utilities.runtime;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.openstreetmap.atlas.exception.CoreException;
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
    private boolean quadratic = false;

    /**
     * @param retries
     *            The number of allowed retries
     * @param waitBeforeRetry
     *            The {@link Duration} to wait for before each retry
     */
    public Retry(final int retries, final Duration waitBeforeRetry)
    {
        this.retries = Math.max(retries, 0);
        this.waitBeforeRetry = waitBeforeRetry;
    }

    /**
     * @return True if the Retry is quadratic
     */
    public boolean isQuadratic()
    {
        return this.quadratic;
    }

    /**
     * @return True if the Retry is quiet
     */
    public boolean isQuiet()
    {
        return this.quiet;
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
     * @param <V>
     *            The return type of the callable
     * @return The result of the first successful call
     */
    public <V> V run(final Supplier<V> callable)
    {
        return run(callable, null, FILTER_NONE);
    }

    /**
     * Run a {@link Supplier} task with some retries
     *
     * @param callable
     *            The task
     * @param <V>
     *            The return type of the callable
     * @param exceptionsWhichShouldBreakDirectly
     *            A filter that chooses what exceptions are to break directly, without triggering a
     *            retry
     * @return The result of the first successful call
     */
    public <V> V run(final Supplier<V> callable,
            final Predicate<Throwable> exceptionsWhichShouldBreakDirectly)
    {
        return run(callable, null, exceptionsWhichShouldBreakDirectly);
    }

    /**
     * Run a {@link Supplier} task with some retries
     *
     * @param callable
     *            The task
     * @param <V>
     *            The return type of the callable
     * @param runBeforeRetry
     *            What to do before retrying
     * @return The result of the first successful call
     */
    public <V> V run(final Supplier<V> callable, final Runnable runBeforeRetry)
    {
        return run(callable, runBeforeRetry, FILTER_NONE);
    }

    /**
     * Run a {@link Supplier} task with some retries
     *
     * @param callable
     *            The task
     * @param <V>
     *            The return type of the callable
     * @param runBeforeRetry
     *            What to do before retrying
     * @param exceptionsWhichShouldBreakDirectly
     *            A filter that chooses what exceptions are to break directly, without triggering a
     *            retry
     * @return The result of the first successful call
     */
    public <V> V run(final Supplier<V> callable, final Runnable runBeforeRetry, // NOSONAR
            final Predicate<Throwable> exceptionsWhichShouldBreakDirectly)
    {
        int retry = 0;
        V result = null;
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
            catch (final Throwable throwable) // NOSONAR
            {
                if (exceptionsWhichShouldBreakDirectly.test(throwable))
                {
                    throw throwable;
                }
                if (!this.quiet && logger.isErrorEnabled())
                {
                    logger.error("Failed retry number {}", retry, throwable);
                }
                lastError = throwable;
                retry++;
                wait(retry);
            }
        }
        if (success)
        {
            return result;
        }
        else
        {
            throw new CoreException("Failed execution after {} retries.", retry, lastError);
        }
    }

    /**
     * @param quadratic
     *            True to wait twice as long as the previous retry
     */
    public void setQuadratic(final boolean quadratic)
    {
        this.quadratic = quadratic;
    }

    /**
     * @param quiet
     *            True to suppress logging of errors when a retry is happening.
     */
    public void setQuiet(final boolean quiet)
    {
        this.quiet = quiet;
    }

    /**
     * @param quadratic
     *            True to wait twice as long as the previous retry
     * @return This
     */
    public Retry withQuadratic(final boolean quadratic)
    {
        this.setQuadratic(quadratic);
        return this;
    }

    /**
     * @param quiet
     *            True to suppress logging of errors when a retry is happening.
     * @return This
     */
    public Retry withQuiet(final boolean quiet)
    {
        this.setQuiet(quiet);
        return this;
    }

    private void wait(final int retry)
    {
        if (retry > 1 && this.quadratic)
        {
            final Duration quadraticWait = this.waitBeforeRetry.times(Math.pow(2.0, retry - 1.0));
            if (logger.isInfoEnabled())
            {
                logger.info("Wait is quadratic. Waiting {}", quadraticWait);
            }
            quadraticWait.sleep();
        }
        else
        {
            this.waitBeforeRetry.sleep();
        }
    }
}
