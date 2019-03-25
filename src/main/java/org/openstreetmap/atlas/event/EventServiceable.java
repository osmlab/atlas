package org.openstreetmap.atlas.event;

import org.openstreetmap.atlas.utilities.threads.Pool;

/**
 * Contract for Event pub-sub implementations.
 *
 * @param <T>
 *            - event type that is going to be posted
 * @author Yazad Khambata
 */
public interface EventServiceable<T extends Event>
{

    /**
     * Stops event processing {@link Pool} and posts a {@link ShutdownEvent} event
     */
    void complete();

    /**
     * Publishes/posts a new event {@link Object}
     *
     * @param event
     *            {@link Object} to post
     */
    void post(T event);

    /**
     * Registers given {@link Processor} to subscribe for events
     *
     * @param processor
     *            {@link Processor} to register
     */
    void register(Processor<T> processor);

    /**
     * Unregisters given {@link Processor}
     *
     * @param processor
     *            {@link Processor} to unregister
     */
    void unregister(Processor<T> processor);
}
