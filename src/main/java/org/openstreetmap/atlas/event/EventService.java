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
 */
public final class EventService
{
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    // A key-value mapping for multiple event services
    private static Map<String, EventService> serviceMap = new ConcurrentHashMap<>();

    // Event bus to dispatch events
    private final EventBus eventBus;

    // Container of processors on the EventService
    private final Collection<Processor<?>> processors = new HashSet<>();

    // Thread-safe complete indicator
    private final AtomicBoolean completed = new AtomicBoolean();

    /**
     * @param key
     *            key to retrieve {@link EventService}
     * @return {@link EventService} instance for given key
     */
    public static EventService get(final String key)
    {
        serviceMap.putIfAbsent(key, new EventService());
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
     * @param <T>
     *            event type that is going to be posted
     * @param event
     *            {@link Object} to post
     */
    public <T extends Event> void post(final T event)
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
     * @param <T>
     *            processor event type that is going to be registered
     * @param processor
     *            {@link Processor} to register
     */
    public <T extends Event> void register(final Processor<T> processor)
    {
        this.eventBus.register(processor);
        this.processors.add(processor);
    }

    /**
     * Unregisters given {@link Processor}
     *
     * @param <T>
     *            processor event type that is going to be registered
     * @param processor
     *            {@link Processor} to unregister
     */
    public <T extends Event> void unregister(final Processor<T> processor)
    {
        this.eventBus.unregister(processor);
        this.processors.remove(processor);
    }
}
