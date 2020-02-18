package org.openstreetmap.atlas.utilities.runtime;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test for {@link Retry}
 *
 * @author matthieun
 */
public class RetryTest
{
    /**
     * @author matthieun
     */
    private static class NeedsAction implements Runnable
    {
        private boolean blocked = true;

        public boolean isBlocked()
        {
            return this.blocked;
        }

        @Override
        public void run()
        {
            if (this.blocked)
            {
                throw new RuntimeException("Blocked");
            }
            else
            {
                logger.info("Success, I have been unblocked!");
            }
        }

        public void unBlock()
        {
            this.blocked = false;
        }
    }

    /**
     * @author matthieun
     */
    private static class WithCounter implements Runnable
    {
        private int counter = 0;

        public int getCounter()
        {
            return this.counter;
        }

        @Override
        public void run()
        {
            if (this.counter++ < 4)
            {
                throw new RuntimeException();
            }
            else
            {
                logger.info("Success");
            }
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(RetryTest.class);

    private final NeedsAction needAction = new NeedsAction();
    private final WithCounter withCounter = new WithCounter();

    @Test
    public void testHandleException()
    {
        final Retry retry = new Retry(5, Duration.milliseconds(1)).withQuadratic(true);
        Assert.assertTrue(retry.isQuadratic());
        retry.run(this.withCounter);
        Assert.assertEquals(5, this.withCounter.getCounter());
    }

    @Test
    public void testRunBeforeRetry()
    {
        final Retry retry = new Retry(1, Duration.milliseconds(1)).withQuiet(true);
        Assert.assertTrue(retry.isQuiet());
        retry.run(this.needAction, this.needAction::unBlock);
        Assert.assertFalse(this.needAction.isBlocked());
    }

    @Test(expected = RuntimeException.class)
    public void testThrowException()
    {
        final Retry retry = new Retry(2, Duration.milliseconds(1));
        retry.run(this.withCounter);
    }
}
