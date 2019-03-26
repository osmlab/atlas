package org.openstreetmap.atlas.event;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.threads.Pool;

/**
 * Tests for {@link EventService}.
 *
 * @author mkalender
 * @author Yazad Khambata
 */
public class EventServiceTest
{
    @Test
    public void testEventResult()
    {
        testEventResult(new TestEvent(null));
        testEventResult(new TestEvent(""));
        testEventResult(new TestEvent("This is a test!!!"));
    }

    @Test
    public void testNullEvent()
    {
        final TestProcessor testProcessor = new TestProcessor();
        final EventServiceable eventService = EventService.get("Test service for null event");
        eventService.register(testProcessor);

        // Send event and complete
        eventService.post(null);
        eventService.complete();

        // Validate
        Assert.assertEquals(0, testProcessor.getProcessCount());
        Assert.assertEquals(1, testProcessor.getCompleteCount());
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
    public void testPostAfterComplete()
    {
        final TestProcessor testProcessor = new TestProcessor();
        final EventServiceable eventService = EventService
                .get("Test service for posting after completion");
        eventService.register(testProcessor);

        // Send event and complete
        eventService.complete();
        eventService.post(new TestEvent("a message"));

        // Validate
        Assert.assertEquals(null, testProcessor.getLastEventMessage());
        Assert.assertEquals(0, testProcessor.getProcessCount());
        Assert.assertEquals(1, testProcessor.getCompleteCount());
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
    public void testUnregisterProcessor()
    {
        final TestProcessor testProcessor = new TestProcessor();
        final EventServiceable eventService = EventService
                .get("Event service for unregistering processor");

        eventService.register(testProcessor);
        eventService.unregister(testProcessor);
        eventService.post(new TestEvent("TESTING"));

        eventService.complete();

        Assert.assertEquals(0, testProcessor.getCompleteCount());
        Assert.assertEquals(0, testProcessor.getProcessCount());
    }

    @Test
    public void testZeroComplete()
    {
        testCompleteCount(0);
    }

    private void testCompleteCount(final int completeCount)
    {
        final TestProcessor testProcessor = new TestProcessor();
        final EventServiceable eventService = EventService
                .get("Test service complete " + completeCount);
        eventService.register(testProcessor);

        // Straight complete
        for (int index = 0; index < completeCount; index++)
        {
            eventService.complete();
        }

        // Validate
        Assert.assertEquals(0, testProcessor.getProcessCount());
        Assert.assertEquals(Math.min(completeCount, 1), testProcessor.getCompleteCount());
    }

    private void testEventResult(final TestEvent event)
    {
        final TestProcessor testProcessor = new TestProcessor();
        final EventServiceable eventService = EventService
                .get("Test service for " + event.getMessage());
        eventService.register(testProcessor);

        // Send event and complete
        eventService.post(event);
        eventService.complete();

        // Validate
        Assert.assertEquals(event.getMessage(), testProcessor.getLastEventMessage());
        Assert.assertEquals(1, testProcessor.getProcessCount());
        Assert.assertEquals(1, testProcessor.getCompleteCount());
    }

    private void testProcessCount(final int threadCount, final int eventCount)
    {
        final TestProcessor testProcessor = new TestProcessor();
        final TestProcessor otherProcessor = new TestProcessor();
        final EventServiceable eventService = EventService
                .get("Test service for " + threadCount + "-" + eventCount);
        eventService.register(testProcessor);
        eventService.register(otherProcessor);

        // Send events
        final Pool threadPool = new Pool(threadCount,
                "Test pool for " + threadCount + "-" + eventCount, Duration.ONE_MINUTE);
        for (int threadIndex = 0; threadIndex < threadCount; threadIndex++)
        {
            threadPool.queue(() ->
            {
                for (int index = 0; index < eventCount; index++)
                {
                    eventService.post(new TestEvent("test " + index));
                }
            });
        }
        threadPool.close();

        // Complete
        eventService.complete();

        // Validate
        Assert.assertEquals(eventCount * threadCount, testProcessor.getProcessCount());
        Assert.assertEquals(1, testProcessor.getCompleteCount());
    }
}
