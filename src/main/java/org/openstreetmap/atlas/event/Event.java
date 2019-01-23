package org.openstreetmap.atlas.event;

import java.util.Date;

/**
 * Useful base class to hold common information for {@link Event} implementations
 *
 * @author mkalender
 */
public abstract class Event
{
    private final Date timestamp;

    /**
     * Default constructor
     */
    protected Event()
    {
        this.timestamp = new Date();
    }

    protected Date getTimestamp()
    {
        return this.timestamp;
    }
}
