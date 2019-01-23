package org.openstreetmap.atlas.event;

/**
 * The {@link Processor} interface provides simple hooks for implementations to handle events.
 *
 * @author mkalender
 * @param <T>
 *            Type that is going to be processed
 */
public interface Processor<T extends Event>
{
    /**
     * Method to process {@link ShutdownEvent}. This method will be called only once.<br>
     * <strong>Please make sure to add {@code @Subscribe} annotation to the method that is
     * implementing this method.</strong>
     *
     * @param event
     *            {@link ShutdownEvent} to process
     */
    void process(ShutdownEvent event);

    /**
     * Method to process {@link Event}. If your method can process multiple events simultaneously,
     * then mark your method with {@code @AllowConcurrentEvents} annotation.<strong>Please make sure
     * to add {@code @Subscribe} annotation to the method that is implementing this method.</strong>
     *
     * @param event
     *            {@link Event} to process
     */
    void process(T event);
}
