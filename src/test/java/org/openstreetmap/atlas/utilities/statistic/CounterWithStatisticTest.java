package org.openstreetmap.atlas.utilities.statistic;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.statistic.storeless.CounterWithStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tony
 */
public class CounterWithStatisticTest
{
    private static final Logger logger = LoggerFactory.getLogger(CounterWithStatisticTest.class);

    @Test
    public void testPause() throws InterruptedException
    {
        final CounterWithStatistic nonPaused = new CounterWithStatistic(logger);
        final CounterWithStatistic paused = new CounterWithStatistic(logger);
        final Duration sleepTime1 = Duration.milliseconds(100);
        Thread.sleep(sleepTime1.asMilliseconds());
        nonPaused.increment();
        paused.increment();
        paused.pause();

        final Duration sleepTime2 = Duration.milliseconds(200);
        Thread.sleep(sleepTime2.asMilliseconds());

        nonPaused.increment();
        paused.increment();

        Assert.assertEquals(2, nonPaused.count());
        Assert.assertEquals(2, paused.count());

        final Duration totalSleepTime = sleepTime1.add(sleepTime2);
        logger.info("sleep time 1 is {}, sleep time 2 is {}, total is {}", sleepTime1, sleepTime2,
                totalSleepTime);
        logger.info("non paused counter since start is {}", nonPaused.sinceStart());
        logger.info("paused counter since start is {}", paused.sinceStart());
        logger.info("paused counter accurate time spent is {}", paused.accurateTimeSpent());

        final Duration safe = Duration.milliseconds(100);
        Assert.assertTrue(nonPaused.sinceStart().isCloseTo(totalSleepTime, safe));
        Assert.assertTrue(nonPaused.sinceStart().isLessThan(totalSleepTime.add(safe)));
        Assert.assertTrue(paused.sinceStart().isCloseTo(totalSleepTime, safe));
        Assert.assertTrue(paused.sinceStart().isLessThan(totalSleepTime.add(safe)));

        Assert.assertTrue(paused.accurateTimeSpent().isCloseTo(sleepTime1, safe));
        Assert.assertTrue(paused.accurateTimeSpent().isLessThan(sleepTime1.add(safe)));
    }
}
