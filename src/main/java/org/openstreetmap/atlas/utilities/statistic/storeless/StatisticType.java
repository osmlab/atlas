package org.openstreetmap.atlas.utilities.statistic.storeless;

import org.apache.commons.math3.stat.descriptive.StorelessUnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.moment.GeometricMean;
import org.apache.commons.math3.stat.descriptive.moment.Kurtosis;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.SecondMoment;
import org.apache.commons.math3.stat.descriptive.moment.Skewness;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.apache.commons.math3.stat.descriptive.summary.SumOfLogs;
import org.apache.commons.math3.stat.descriptive.summary.SumOfSquares;

/**
 * A wrapper for all univariate storeless statistic from apache
 *
 * @author tony
 */
public enum StatisticType
{
    SecondMoment(0),
    GeometricMean(1),
    Kurtosis(2),
    Max(3),
    Mean(4),
    Min(5),
    Product(6),
    Skewness(7),
    StandardDeviation(8),
    Sum(9),
    SumOfLogs(10),
    SumOfSquares(11),
    Variance(12);

    private final int identifier;

    StatisticType(final int identifier)
    {
        this.identifier = identifier;
    }

    public int getIdentifier()
    {
        return this.identifier;
    }

    public StorelessUnivariateStatistic toStatistic()
    {
        switch (this)
        {
            case SecondMoment:
                return new SecondMoment();
            case GeometricMean:
                return new GeometricMean();
            case Kurtosis:
                return new Kurtosis();
            case Max:
                return new Max();
            case Mean:
                return new Mean();
            case Min:
                return new Min();
            case Product:
                return new Product();
            case Skewness:
                return new Skewness();
            case StandardDeviation:
                return new StandardDeviation();
            case Sum:
                return new Sum();
            case SumOfLogs:
                return new SumOfLogs();
            case SumOfSquares:
                return new SumOfSquares();
            case Variance:
                return new Variance();
            default:
                return null;
        }
    }
}
