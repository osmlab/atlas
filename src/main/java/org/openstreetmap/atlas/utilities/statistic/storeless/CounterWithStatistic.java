package org.openstreetmap.atlas.utilities.statistic.storeless;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.statistic.AbstractStatistic;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;

/**
 * A simple class with a counter and a timer which is able to pause in the middle, and can print log
 * every a period of time.
 *
 * @author tony
 */
public class CounterWithStatistic extends AbstractStatistic
{
    private boolean paused = false;
    private Duration duration = Duration.ZERO;
    private Time temporaryTime;

    public CounterWithStatistic(final Logger logger)
    {
        super(logger);
        this.temporaryTime = getStartTime();
    }

    public CounterWithStatistic(final Logger logger, final long logPrintFrequency,
            final String name)
    {
        super(logger, logPrintFrequency, name);
        this.temporaryTime = getStartTime();
    }

    public CounterWithStatistic(final Logger logger, final String name)
    {
        super(logger, name);
        this.temporaryTime = getStartTime();
    }

    /**
     * @return Time duration without pause
     */
    public Duration accurateTimeSpent()
    {
        if (!this.paused)
        {
            this.duration = this.duration.add(this.temporaryTime.untilNow());
            resetTemporaryTime();
        }
        return this.duration;
    }

    public void incrementCount(final long count)
    {
        unPause();
        for (long i = 0; i < count; i++)
        {
            long countLocal = count();
            countLocal++;
            this.setCount(countLocal);
            if (countLocal % getLogPrintFrequency() == 0)
            {
                getLog().accept(toString());
            }
        }
    }

    public boolean isPaused()
    {
        return this.paused;
    }

    @Override
    public void onIncrement(final double value)
    {
        throw new CoreException("Counter doesn't support increment double value {}", value);
    }

    public void pause()
    {
        if (!this.paused)
        {
            this.paused = true;
            this.duration = this.duration.add(this.temporaryTime.untilNow());
        }
    }

    @Override
    public void summary()
    {
        getLog().accept(toString());
    }

    public void summaryWithAccurateTimeSpent()
    {
        getLog().accept(toStringWithAccurateTimeSpent());
    }

    public void summaryWithoutTimer()
    {
        getLog().accept(toStringWithoutTimer());
    }

    public String toStringWithAccurateTimeSpent()
    {
        return getName() + " Count: " + getNumberFormat().format(count()) + ", Time spent: "
                + accurateTimeSpent();
    }

    public void unPause()
    {
        if (this.paused)
        {
            this.paused = false;
            resetTemporaryTime();
        }
    }

    @Override
    protected void onIncrement()
    {
        unPause();
    }

    private void resetTemporaryTime()
    {
        this.temporaryTime = Time.now();
    }
}
