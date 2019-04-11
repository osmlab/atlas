package org.openstreetmap.atlas.geography.atlas.change.eventhandling.listener;

import org.openstreetmap.atlas.geography.atlas.change.eventhandling.event.TagChangeEvent;

/**
 * @author Yazad Khambata
 */
public class TestTagChangeListenerImpl implements TagChangeListener
{
    private static final long serialVersionUID = 6697728278083444095L;

    private TagChangeEvent lastEvent;
    private int callCount = 0;

    @Override
    public void entityChanged(final TagChangeEvent entityChangeEvent)
    {
        this.lastEvent = entityChangeEvent;
        callCount++;
    }

    public TagChangeEvent getLastEvent()
    {
        return lastEvent;
    }

    public int getCallCount()
    {
        return callCount;
    }
}
