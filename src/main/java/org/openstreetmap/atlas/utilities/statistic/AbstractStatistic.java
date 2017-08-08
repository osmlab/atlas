package org.openstreetmap.atlas.utilities.statistic;

import java.text.NumberFormat;
import java.util.function.Consumer;

import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;

/**
 * This class holds a counter and a timer, and will print out information with logPrintFrequency.
 *
 * @author tony
 */
public abstract class AbstractStatistic implements Statistic
{
    private static final long DEFAULT_LOG_PRINT_FREQUENCY = 1_000_000;
    private static final String DEFAULT_NAME = "";

    private final NumberFormat numberFormat;
    private Time startTime;
    private long logPrintFrequency;
    private String name;
    private long count;
    private Consumer<String> log;

    protected AbstractStatistic(final Logger logger)
    {
        this(logger, DEFAULT_LOG_PRINT_FREQUENCY, DEFAULT_NAME);
    }

    protected AbstractStatistic(final Logger logger, final long logPrintFrequency)
    {
        this(logger, logPrintFrequency, DEFAULT_NAME);
    }

    protected AbstractStatistic(final Logger logger, final long logPrintFrequency,
            final String name)
    {
        this.log = logger::info;
        this.logPrintFrequency = logPrintFrequency;
        this.name = name;
        this.startTime = Time.now();
        this.numberFormat = NumberFormat.getInstance();
        this.numberFormat.setGroupingUsed(true);
    }

    protected AbstractStatistic(final Logger logger, final String name)
    {
        this(logger, DEFAULT_LOG_PRINT_FREQUENCY, name);
    }

    public void clear()
    {
        this.count = 0;
        this.startTime = Time.now();
    }

    public void clearCounter()
    {
        this.count = 0;
    }

    public long count()
    {
        return this.count;
    }

    public Consumer<String> getLog()
    {
        return this.log;
    }

    public long getLogPrintFrequency()
    {
        return this.logPrintFrequency;
    }

    public String getName()
    {
        return this.name;
    }

    public Time getStartTime()
    {
        return this.startTime;
    }

    public void increment()
    {
        this.count++;
        if (this.count % this.logPrintFrequency == 0)
        {
            this.log.accept(toString());
        }
        onIncrement();
    }

    @Override
    public void increment(final double value)
    {
        this.increment();
        onIncrement(value);
    }

    public void logUsingLevel(final Consumer<String> log)
    {
        this.log = log;
    }

    public void setLogPrintFrequency(final long logPrintFrequency)
    {
        this.logPrintFrequency = logPrintFrequency;
    }

    public void setName(final String name)
    {
        this.name = name;
    }

    public Duration sinceStart()
    {
        return this.startTime.untilNow();
    }

    @Override
    public String toString()
    {
        return this.name + " Count: " + this.numberFormat.format(this.count) + ", Time spent: "
                + sinceStart();
    }

    public String toStringWithoutTimer()
    {
        return this.name + " Count: " + this.numberFormat.format(this.count);
    }

    protected NumberFormat getNumberFormat()
    {
        return this.numberFormat;
    }

    protected abstract void onIncrement();

    protected abstract void onIncrement(double value);

    protected void setCount(final long count)
    {
        this.count = count;
    }

}
