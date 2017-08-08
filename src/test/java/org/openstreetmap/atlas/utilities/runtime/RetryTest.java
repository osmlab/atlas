package org.openstreetmap.atlas.utilities.runtime;

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

    private static final Logger logger = LoggerFactory.getLogger(RetryTest.class);

    private final Runnable runnable = new Runnable()
    {
        private int counter = 0;

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
    };

    private final NeedsAction needAction = new NeedsAction();

    @Test
    public void testHandleException()
    {
        final Retry retry = new Retry(5, Duration.milliseconds(1));
        retry.run(this.runnable);
    }

    @Test
    public void testRunBeforeRetry()
    {
        final Retry retry = new Retry(1, Duration.milliseconds(1));
        retry.run(this.needAction, () -> this.needAction.unBlock());
    }

    @Test
    public void testThrowException()
    {
        final Retry retry = new Retry(2, Duration.milliseconds(1));
        try
        {
            retry.run(this.runnable);
        }
        catch (final Exception e)
        {
            logger.info("Threw the exception! Success");
            return;
        }
        throw new RuntimeException("The Retry did not throw the exception in time");
    }
}
