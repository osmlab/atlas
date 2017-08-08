package org.openstreetmap.atlas.utilities.statistic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.statistic.storeless.CounterWithStatistic;
import org.openstreetmap.atlas.utilities.statistic.storeless.CustomizedStatistic;
import org.openstreetmap.atlas.utilities.statistic.storeless.StatisticType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tony
 */
public class CustomizedStatisticTest
{
    private static final double DELTA = 0.00001;
    private final List<Double> testData = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(CustomizedStatisticTest.class);

    @Before
    public void loadData()
    {
        this.testData.add(-5d);
        this.testData.add(-1d);
        this.testData.add(0d);
        this.testData.add(5d);
        this.testData.add(5d);
        this.testData.add(5.1);
        this.testData.add(100d);
    }

    @Test
    public void testCounter()
    {
        final CounterWithStatistic counter = new CounterWithStatistic(this.logger);
        assertEquals(0, counter.count());
        this.testData.forEach(value -> counter.increment());
        assertEquals(7, counter.count());
        counter.summary();
    }

    @Test
    public void testCustomizedStorelessStatistic()
    {
        final CustomizedStatistic stat = new CustomizedStatistic(this.logger, StatisticType.Max,
                StatisticType.Min, StatisticType.Sum, StatisticType.SumOfSquares);
        this.testData.forEach(value -> stat.increment(value));
        assertEquals(7, stat.count());
        assertEquals(100, stat.getMax(), DELTA);
        assertEquals(-5, stat.getMin(), DELTA);
        assertEquals(109.1, stat.getSum(), DELTA);
        assertEquals(10102.01, stat.getSumOfSquares(), DELTA);
        try
        {
            stat.getGeometricMean();
            fail();
        }
        catch (final CoreException e)
        {
            // Expected
        }
        stat.summary();
    }
}
