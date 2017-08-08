package org.openstreetmap.atlas.utilities.scalars;

/**
 * Double counter Wrapper
 *
 * @author jklamer
 */
public class DoubleCounter
{
    private double value;

    public DoubleCounter()
    {
        this.value = 0.0;
    }

    public DoubleCounter(final double start)
    {
        this.value = start;

    }

    public void add(final double delta)
    {
        this.value += delta;
    }

    public double getValue()
    {
        return this.value;
    }

    public void reset()
    {
        this.value = 0.0;
    }

    @Override
    public String toString()
    {
        return String.valueOf(this.value);
    }
}
