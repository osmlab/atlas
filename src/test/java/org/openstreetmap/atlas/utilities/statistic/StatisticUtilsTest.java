package org.openstreetmap.atlas.utilities.statistic;

import static org.junit.Assert.assertEquals;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.scalars.Ratio;

/**
 * @author tony
 */
public class StatisticUtilsTest
{
    private static final double DELTA = 0.1;
    private static double STRICT_DELTA = 0.00000001;
    private List<Double> randomBetween1And99;

    double[] toDoubleArray(final List<Double> list)
    {
        final double[] ret = new double[list.size()];
        for (int i = 0; i < ret.length; i++)
        {
            ret[i] = list.get(i);
        }
        return ret;
    }

    @Before
    public void loadData()
    {
        final DoubleStream doubleStream = new Random().doubles(1, 99);
        this.randomBetween1And99 = doubleStream.limit(1_000_000).boxed()
                .collect(Collectors.toList());
    }

    @Test
    public void testStatisticUtils()
    {
        // Java 8 SummaryStatistics
        final DoubleSummaryStatistics stats1 = StatisticUtils
                .summarizingDouble(this.randomBetween1And99);
        assertEquals(this.randomBetween1And99.size(), stats1.getCount());
        assertEquals(1, stats1.getMin(), DELTA);
        assertEquals(99, stats1.getMax(), DELTA);
        assertEquals(50, stats1.getAverage(), 0.1);

        // Customized operation
        final Optional<Double> min = StatisticUtils.summarizing(this.randomBetween1And99,
                (left, right) -> left > right ? right : left);
        assertEquals(stats1.getMin(), min.get().doubleValue(), STRICT_DELTA);

        // Apache percentile
        assertEquals(50, StatisticUtils.percentile(this.randomBetween1And99, Ratio.percentage(50)),
                1);
    }
}
