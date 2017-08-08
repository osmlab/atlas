package org.openstreetmap.atlas.utilities.runtime;

import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.threads.Pool;

/**
 * @author matthieun
 */
public class TimedRetry
{
    private static final String TIMEOUT_MESSAGE = "Timeout Error: ";

    private final int retries;

    public TimedRetry(final int retries)
    {
        this.retries = retries;
    }

    public <Value> Value run(final Supplier<Value> callable, final Duration timeBeforeRetry)
    {
        final Retry retry = new Retry(this.retries, Duration.ZERO);
        final Predicate<Throwable> exceptionsWhichShouldBreakDirectly = error -> !error.getMessage()
                .startsWith(TIMEOUT_MESSAGE);
        return retry.run(() ->
        {
            try (Pool pool = new Pool(1, Thread.currentThread().getName() + " # TimedRetry",
                    timeBeforeRetry.add(Duration.ONE_SECOND)))
            {
                return pool.queue(() -> callable.get()).get(timeBeforeRetry);
            }
            catch (final TimeoutException e)
            {
                throw new CoreException(TIMEOUT_MESSAGE + "Timeout in TimedRetry call", e);
            }
        }, exceptionsWhichShouldBreakDirectly);
    }
}
