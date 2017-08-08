package org.openstreetmap.atlas.utilities.threads;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.scalars.Duration;

/**
 * Wrapper for a {@link Future}
 *
 * @param <T>
 *            The type returned in the result
 * @author matthieun
 */
public class Result<T>
{
    private final Future<T> future;
    private final Pool pool;

    public Result(final Future<T> future, final Pool pool)
    {
        this.pool = pool;
        this.future = future;
    }

    public boolean cancel(final boolean mayInterruptIfRunning)
    {
        return this.future.cancel(mayInterruptIfRunning);
    }

    public T get()
    {
        try
        {
            return this.future.get();
        }
        catch (final Exception e)
        {
            throw new CoreException("Could not get value from Future in {}", this.pool, e);
        }
    }

    public T get(final Duration timeout) throws TimeoutException
    {
        try
        {
            return this.future.get(timeout.asMilliseconds(), TimeUnit.MILLISECONDS);
        }
        catch (final InterruptedException | ExecutionException e)
        {
            throw new CoreException(
                    "Interrupted before {} elapsed. Could not get value from Future in {}", timeout,
                    this.pool, e);
        }
    }

    public boolean isCancelled()
    {
        return this.future.isCancelled();
    }

    public boolean isDone()
    {
        return this.future.isDone();
    }
}
