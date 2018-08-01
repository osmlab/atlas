package org.openstreetmap.atlas.utilities.scalars;

/**
 * Counter wrapper.
 *
 * @author matthieun
 */
public class Counter
{
    private long value;

    public Counter()
    {
        this.value = 0;
    }

    public Counter(final long start)
    {
        this.value = start;
    }

    public void add(final long value)
    {
        this.value += value;
    }

    public long getValue()
    {
        return this.value;
    }

    public long getValueAndIncrement()
    {
        final long toReturn = this.value;
        this.increment();
        return toReturn;
    }

    public void increment()
    {
        this.add(1L);
    }

    public void reset()
    {
        this.value = 0;
    }

    @Override
    public String toString()
    {
        return String.valueOf(this.value);
    }
}
