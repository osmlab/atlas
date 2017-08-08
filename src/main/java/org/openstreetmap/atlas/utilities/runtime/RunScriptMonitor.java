package org.openstreetmap.atlas.utilities.runtime;

import java.io.InputStream;
import java.util.concurrent.TimeoutException;

import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.threads.Pool;
import org.openstreetmap.atlas.utilities.threads.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Monitor for the output of a {@link Process} started with {@link RunScript}.
 *
 * @author matthieun
 */
public abstract class RunScriptMonitor
{
    /**
     * Print the logs of a {@link Process} using a {@link Logger}
     *
     * @author matthieun
     */
    public static class PrinterMonitor extends RunScriptMonitor
    {
        private final Logger logger;

        public PrinterMonitor(final Logger logger)
        {
            this.logger = logger;
        }

        @Override
        protected void parseStandardError(final Iterable<String> lines)
        {
            parseStream(lines);
        }

        @Override
        protected void parseStandardOutput(final Iterable<String> lines)
        {
            parseStream(lines);
        }

        private void parseStream(final Iterable<String> lines)
        {
            lines.forEach(line -> this.logger.info(line));
        }
    }

    private static final Duration REFRESH_DURATION = Duration.milliseconds(100);

    private static final Logger logger = LoggerFactory.getLogger(RunScriptMonitor.class);

    private Thread out;
    private Thread err;

    /**
     * Parse the output logs of a {@link Process}.
     *
     * @param standardOut
     *            The output stream for standard output
     * @param standardErr
     *            The output stream for standard error
     */
    protected void parse(final InputStream standardOut, final InputStream standardErr)
    {
        this.out = new Thread(
                () -> parseStandardOutput(new InputStreamResource(standardOut).lines()));
        this.err = new Thread(
                () -> parseStandardError(new InputStreamResource(standardErr).lines()));
        this.out.setPriority(Thread.MAX_PRIORITY);
        this.err.setPriority(Thread.MAX_PRIORITY);
        this.out.start();
        this.err.start();
    }

    /**
     * Parse the standard error stream
     *
     * @param lines
     *            The stream
     */
    protected abstract void parseStandardError(Iterable<String> lines);

    /**
     * Parse the standard output stream
     *
     * @param lines
     *            The stream
     */
    protected abstract void parseStandardOutput(Iterable<String> lines);

    /**
     * Make sure that the logs are parsed before the end.
     *
     * @param maximum
     *            The maximum duration to wait for.
     */
    protected void waitForCompletion(final Duration maximum)
    {
        try (Pool waiter = new Pool(1, "waiter", Duration.ONE_SECOND))
        {
            final Result<Boolean> result = waiter.queue(() ->
            {
                while (this.out.isAlive() || this.err.isAlive())
                {
                    REFRESH_DURATION.sleep();
                }
                return true;
            });
            result.get(maximum);
        }
        catch (final TimeoutException e)
        {
            logger.warn("RunScript logs monitor did not finish in {}.", maximum);
        }
    }
}
