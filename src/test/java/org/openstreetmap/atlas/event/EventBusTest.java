package org.openstreetmap.atlas.event;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.threads.Pool;

import com.google.common.eventbus.EventBus;

/**
 * Tests for {@link EventBus}.
 *
 * @author mkalender
 */
public class EventBusTest
{
    @Test
    public void testEventResult()
    {
        testEventResult(new TestEvent(null));
        testEventResult(new TestEvent(""));
        testEventResult(new TestEvent("This is a test!!!"));
    }

    @Test
    public void testOneComplete()
    {
        testCompleteCount(1);
    }

    @Test
    public void testOneThousandComplete()
    {
        testCompleteCount(1000);
    }

    @Test
    public void testSingleThreadOneEvent()
    {
        testProcessCount(1, 1);
    }

    @Test
    public void testSingleThreadOneMillionEvent()
    {
        testProcessCount(1, 1000000);
    }

    @Test
    public void testSingleThreadOneThousandEvent()
    {
        testProcessCount(1, 1000);
    }

    @Test
    public void testSingleThreadTenThousandEvent()
    {
        testProcessCount(1, 10000);
    }

    @Test
    public void testSingleThreadZeroEvent()
    {
        testProcessCount(1, 0);
    }

    @Test
    public void testTenThreadsOneEvent()
    {
        testProcessCount(10, 1);
    }

    @Test
    public void testTenThreadsOneThousandEvent()
    {
        testProcessCount(10, 1000);
    }

    @Test
    public void testTenThreadsZeroEvent()
    {
        testProcessCount(10, 0);
    }

    @Test
    public void testTwoComplete()
    {
        testCompleteCount(2);
    }

    @Test
    public void testTwoThreadsOneEvent()
    {
        testProcessCount(2, 1);
    }

    @Test
    public void testTwoThreadsOneThousandEvent()
    {
        testProcessCount(2, 1000);
    }

    @Test
    public void testTwoThreadsTenThousandEvent()
    {
        testProcessCount(2, 10000);
    }

    @Test
    public void testTwoThreadsZeroEvent()
    {
        testProcessCount(2, 0);
    }

    @Test
    public void testZeroComplete()
    {
        testCompleteCount(0);
    }

    private void testCompleteCount(final int completeCount)
    {
        final TestProcessor testProcessor = new TestProcessor();
        final EventBus eventBus = new EventBus();
        eventBus.register(testProcessor);

        // Straight complete
        for (int index = 0; index < completeCount; index++)
        {
            eventBus.post(new ShutdownEvent());
        }

        // Validate
        Assert.assertEquals(0, testProcessor.getProcessCount());
        Assert.assertEquals(completeCount, testProcessor.getCompleteCount());
    }

    private void testEventResult(final TestEvent event)
    {
        final TestProcessor testProcessor = new TestProcessor();
        final EventBus eventBus = new EventBus();
        eventBus.register(testProcessor);

        // Send event and complete
        eventBus.post(event);
        eventBus.post(new ShutdownEvent());

        // Validate
        Assert.assertEquals(event.getMessage(), testProcessor.getLastEventMessage());
        Assert.assertEquals(event != null ? 1 : 0, testProcessor.getProcessCount());
        Assert.assertEquals(1, testProcessor.getCompleteCount());
    }

    private void testProcessCount(final int threadCount, final int eventCount)
    {
        final TestProcessor testProcessor = new TestProcessor();
        final EventBus eventBus = new EventBus();
        eventBus.register(testProcessor);

        // Send events
        final Pool threadPool = new Pool(threadCount, "Test event pool", Duration.ONE_MINUTE);
        for (int threadIndex = 0; threadIndex < threadCount; threadIndex++)
        {
            threadPool.queue(() ->
            {
                for (int index = 0; index < eventCount; index++)
                {
                    eventBus.post(new TestEvent("test " + index));
                }
            });
        }
        threadPool.close();

        // Complete
        eventBus.post(new ShutdownEvent());

        // Validate
        Assert.assertEquals(eventCount * threadCount, testProcessor.getProcessCount());
        Assert.assertEquals(1, testProcessor.getCompleteCount());
    }
}
