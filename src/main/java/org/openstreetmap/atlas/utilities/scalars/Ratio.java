package org.openstreetmap.atlas.utilities.scalars;

import java.io.Serializable;
import java.text.DecimalFormat;

import org.openstreetmap.atlas.exception.CoreException;

/**
 * A ratio between 0 and 1
 *
 * @author tony
 * @author matthieun
 */
public final class Ratio implements Serializable
{
    private static final long serialVersionUID = 8339825003035330715L;

    private static final double PERCENTAGE_PER_RATIO = 100.0;
    private static final double MAXIMUM_RATIO = 1.0;

    public static final Ratio MAXIMUM = ratio(MAXIMUM_RATIO);
    public static final Ratio MINIMUM = ratio(0.0);
    public static final Ratio HALF = ratio(MAXIMUM_RATIO / 2.0);

    private final double ratio;

    public static Ratio percentage(final double percentage)
    {
        if (percentage < 0 || percentage > PERCENTAGE_PER_RATIO)
        {
            throw new CoreException("percentage {} is not between 0.0 and {} inclusive.",
                    percentage, PERCENTAGE_PER_RATIO);
        }
        return new Ratio(percentage / PERCENTAGE_PER_RATIO);
    }

    public static Ratio ratio(final double ratio)
    {
        if (ratio < 0 || ratio > 1)
        {
            throw new CoreException("ratio {} is not between 0.0 and 1.0 inclusive.", ratio);
        }
        return new Ratio(ratio);
    }

    private Ratio(final double ratio)
    {
        this.ratio = ratio;
    }

    public double asPercentage()
    {
        return this.ratio * PERCENTAGE_PER_RATIO;
    }

    public double asRatio()
    {
        return this.ratio;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof Ratio)
        {
            final Ratio that = (Ratio) other;
            return this.asRatio() == that.asRatio();
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        return Double.valueOf(this.ratio).hashCode();
    }

    public boolean isGreaterThan(final Ratio that)
    {
        return this.asRatio() > that.asRatio();
    }

    public boolean isGreaterThanOrEqualTo(final Ratio that)
    {
        return this.asRatio() >= that.asRatio();
    }

    public boolean isLessThan(final Ratio that)
    {
        return this.asRatio() < that.asRatio();
    }

    public boolean isLessThanOrEqualTo(final Ratio that)
    {
        return this.asRatio() <= that.asRatio();
    }

    @Override
    public String toString()
    {
        return new DecimalFormat("#.##").format(this.asPercentage()) + " %";
    }

    public String toString(final int decimalPlaces)
    {
        if (decimalPlaces > 0)
        {
            final StringBuilder builder = new StringBuilder();
            builder.append("#.");
            for (int i = 0; i < decimalPlaces; i++)
            {
                builder.append("#");
            }
            return new DecimalFormat(builder.toString()).format(this.asPercentage()) + " %";
        }
        return String.valueOf(this.asPercentage());
    }
}
