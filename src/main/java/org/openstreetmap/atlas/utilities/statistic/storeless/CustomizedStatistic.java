package org.openstreetmap.atlas.utilities.statistic.storeless;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.statistic.AbstractStatistic;
import org.slf4j.Logger;

/**
 * This class provide arbitrary combination statistic of {@link StatisticType}, while no need to
 * store any value inside
 *
 * @author tony
 */
public class CustomizedStatistic extends AbstractStatistic
{
    private final Map<StatisticType, StorelessUnivariateStatistic> statistics = new HashMap<>();

    public CustomizedStatistic(final Logger logger, final long logFrequency,
            final StatisticType... types)
    {
        super(logger, logFrequency);
        configure(types);
    }

    public CustomizedStatistic(final Logger logger, final StatisticType... types)
    {
        super(logger);
        configure(types);
    }

    public double getGeometricMean()
    {
        if (this.statistics.containsKey(StatisticType.GeometricMean))
        {
            return this.statistics.get(StatisticType.GeometricMean).getResult();
        }
        throw new CoreException("You didn't choose the statistic type GeometricMean");
    }

    public double getKurtosis()
    {
        if (this.statistics.containsKey(StatisticType.Kurtosis))
        {
            return this.statistics.get(StatisticType.Kurtosis).getResult();
        }
        throw new CoreException("You didn't choose the statistic type Kurtosis");
    }

    public double getMax()
    {
        if (this.statistics.containsKey(StatisticType.Max))
        {
            return this.statistics.get(StatisticType.Max).getResult();
        }
        throw new CoreException("You didn't choose the statistic type Max");
    }

    public double getMean()
    {
        if (this.statistics.containsKey(StatisticType.Mean))
        {
            return this.statistics.get(StatisticType.Mean).getResult();
        }
        throw new CoreException("You didn't choose the statistic type Mean");
    }

    public double getMin()
    {
        if (this.statistics.containsKey(StatisticType.Min))
        {
            return this.statistics.get(StatisticType.Min).getResult();
        }
        throw new CoreException("You didn't choose the statistic type Min");
    }

    public double getProduct()
    {
        if (this.statistics.containsKey(StatisticType.Product))
        {
            return this.statistics.get(StatisticType.Product).getResult();
        }
        throw new CoreException("You didn't choose the statistic type Product");
    }

    public double getSecondMoment()
    {
        if (this.statistics.containsKey(StatisticType.SecondMoment))
        {
            return this.statistics.get(StatisticType.SecondMoment).getResult();
        }
        throw new CoreException("You didn't choose the statistic type SecondMoment");
    }

    public double getSkewness()
    {
        if (this.statistics.containsKey(StatisticType.Skewness))
        {
            return this.statistics.get(StatisticType.Skewness).getResult();
        }
        throw new CoreException("You didn't choose the statistic type Skewness");
    }

    public double getStandardDeviation()
    {
        if (this.statistics.containsKey(StatisticType.StandardDeviation))
        {
            return this.statistics.get(StatisticType.StandardDeviation).getResult();
        }
        throw new CoreException("You didn't choose the statistic type StandardDeviation");
    }

    public double getSum()
    {
        if (this.statistics.containsKey(StatisticType.Sum))
        {
            return this.statistics.get(StatisticType.Sum).getResult();
        }
        throw new CoreException("You didn't choose the statistic type Sum");
    }

    public double getSumOfLogs()
    {
        if (this.statistics.containsKey(StatisticType.SumOfLogs))
        {
            return this.statistics.get(StatisticType.SumOfLogs).getResult();
        }
        throw new CoreException("You didn't choose the statistic type SumOfLogs");
    }

    public double getSumOfSquares()
    {
        if (this.statistics.containsKey(StatisticType.SumOfSquares))
        {
            return this.statistics.get(StatisticType.SumOfSquares).getResult();
        }
        throw new CoreException("You didn't choose the statistic type SumOfSquares");
    }

    /**
     * Each statistic will have the same amount of total values associated with it so we really just
     * need to find the first one and get the N value from it.
     *
     * @return The number of times increment was called, or 0 if never called.
     */
    public long getTotal()
    {
        if (this.statistics.size() > 0)
        {
            return this.statistics.entrySet().iterator().next().getValue().getN();
        }
        return 0;
    }

    public double getVariance()
    {
        if (this.statistics.containsKey(StatisticType.Variance))
        {
            return this.statistics.get(StatisticType.Variance).getResult();
        }
        throw new CoreException("You didn't choose the statistic type Variance");
    }

    @Override
    public void onIncrement(final double value)
    {
        for (final StorelessUnivariateStatistic stat : this.statistics.values())
        {
            stat.increment(value);
        }
    }

    @Override
    public void summary()
    {
        getLog().accept(toString());
        this.statistics.forEach(
                (type, statistic) -> getLog().accept(type.name() + ": " + statistic.getResult()));
    }

    @Override
    protected void onIncrement()
    {
    }

    private void configure(final StatisticType... types)
    {
        for (final StatisticType type : types)
        {
            this.statistics.put(type, type.toStatistic());
        }
    }
}
