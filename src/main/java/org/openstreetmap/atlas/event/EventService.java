package org.openstreetmap.atlas.event;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.openstreetmap.atlas.utilities.threads.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.EventBus;

/**
 * A simple in-memory publish-subscribe service built on top of {@link EventBus}
 *
 * @author mkalender
 * @author jklamer
 * @author Yazad Khambata
 */
public final class EventService<T extends Event> implements EventServiceable<T>
{
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    // A key-value mapping for multiple event services
    private static Map<String, EventService> serviceMap = new ConcurrentHashMap<>();

    // Event bus to dispatch events
    private final EventBus eventBus;

    // Container of processors on the EventService
    private final Collection<Processor<T>> processors = new HashSet<>();

    // Thread-safe complete indicator
    private final AtomicBoolean completed = new AtomicBoolean();

    /**
     * @param key
     *            key to retrieve {@link EventService}
     * @param <T>
     *            - event type that is going to be posted
     * @return {@link EventService} instance for given key
     */
    public static <T extends Event> EventService get(final String key)
    {
        serviceMap.putIfAbsent(key, new EventService<T>());
        return serviceMap.get(key);
    }

    private EventService()
    {
        this.eventBus = new EventBus((exception, context) -> logger
                .warn("An exception is thrown in EventBus.", exception));
    }

    /**
     * Stops event processing {@link Pool} and posts a {@link ShutdownEvent} event
     */
    @Override
    public void complete()
    {
        if (!this.completed.compareAndSet(false, true))
        {
            logger.warn("EventService is already completed. Skipping completion.");
            return;
        }

        this.eventBus.post(new ShutdownEvent());
        new HashSet<>(this.processors).forEach(this::unregister);
    }

    /**
     * Publishes/posts a new event {@link Object}
     *
     * @param event
     *            {@link Object} to post
     */
    @Override
    public void post(final T event)
    {
        if (event == null)
        {
            logger.warn("EventService received a null event. Skipping posting.");
            return;
        }

        if (this.completed.get())
        {
            logger.warn("EventService is already completed. Skipping posting.");
            return;
        }

        this.eventBus.post(event);
    }

    /**
     * Registers given {@link Processor} to subscribe for events
     *
     * @param processor
     *            {@link Processor} to register
     */
    @Override
    public void register(final Processor<T> processor)
    {
        this.eventBus.register(processor);
        this.processors.add(processor);
    }

    /**
     * Unregisters given {@link Processor}
     *
     * @param processor
     *            {@link Processor} to unregister
     */
    @Override
    public void unregister(final Processor<T> processor)
    {
        this.eventBus.unregister(processor);
        this.processors.remove(processor);
    }
}