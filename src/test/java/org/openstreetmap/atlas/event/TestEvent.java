package org.openstreetmap.atlas.event;

/**
 * Sample {@link Event} to be used for testing.
 *
 * @author mkalender
 */
public class TestEvent extends Event
{
    private final String message;

    public TestEvent(final String message)
    {
        this.message = message;
    }

    public String getMessage()
    {
        return this.message;
    }
}
