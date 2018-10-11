package org.openstreetmap.atlas.utilities.threads;

import java.io.Closeable;

import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.time.Time;

/**
 * Ticker companion.
 *
 * @author matthieun
 */
public abstract class Ticker implements Runnable, Closeable
{
    private static final Duration CHECK_TIME = Duration.milliseconds(500);

    private final String name;
    private final Duration tickerTime;

    // This tells the ticker it is time to stop ticking.
    private volatile boolean stop;

    /**
     * @param name
     *            The name of the ticker companion
     * @param tickerTime
     *            The duration between each tick. It is indicative only.
     */
    public Ticker(final String name, final Duration tickerTime)
    {
        this.name = name;
        this.tickerTime = tickerTime;
        this.stop = false;
    }

    @Override
    public void close()
    {
        this.stop = true;
    }

    public String getName()
    {
        return this.name;
    }

    @Override
    public void run()
    {
        final Time start = Time.now();
        Time lastCheck = Time.now();
        while (!this.stop)
        {
            // Sleep small, to check regularly. If the thread is "closed" it will then die fairly
            // soon even if the ticker time is really long.
            CHECK_TIME.sleep();
            if (lastCheck.elapsedSince().isMoreThan(this.tickerTime))
            {
                tickAction(start.elapsedSince());
                lastCheck = Time.now();
            }
        }
    }

    @Override
    public String toString()
    {
        return "Ticker [name=" + this.name + ", tickerTime=" + this.tickerTime + "]";
    }

    /**
     * Act upon a tick event.
     *
     * @param sinceStart
     *            The duration elapsed since the start of the ticker.
     */
    protected abstract void tickAction(Duration sinceStart);
}
