package org.openstreetmap.atlas.geography.atlas.complete;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.openstreetmap.atlas.geography.atlas.change.eventhandling.event.TagChangeEvent;
import org.openstreetmap.atlas.geography.atlas.change.eventhandling.listenable.TagChangeListenable;
import org.openstreetmap.atlas.geography.atlas.change.eventhandling.listener.TagChangeListener;

/**
 * Consolidates redundant state and behavior that would have otherwise be repeated across all
 * {@link CompleteEntity} implementations.
 *
 * @author Yazad Khambata
 */
class TagChangeDelegate implements TagChangeListenable, Serializable
{
    private static final long serialVersionUID = -7015756232511317683L;

    private final List<TagChangeListener> tagChangeListeners = new ArrayList<>();

    static TagChangeDelegate newTagChangeDelegate()
    {
        return new TagChangeDelegate();
    }

    protected TagChangeDelegate()
    {
        super();
    }

    @Override
    public void addTagChangeListener(final TagChangeListener tagChangeListener)
    {
        Validate.notNull(tagChangeListener, "tagChangeListener is NULL.");
        synchronized (this.tagChangeListeners)
        {
            this.tagChangeListeners.add(tagChangeListener);
        }
    }

    @Override
    public void fireTagChangeEvent(final TagChangeEvent tagChangeEvent)
    {
        Validate.notNull(tagChangeEvent, "tagChangeEvent is EMPTY!");
        if (!this.tagChangeListeners.isEmpty())
        {
            synchronized (this.tagChangeListeners)
            {
                this.tagChangeListeners.stream().forEach(
                        tagChangeListener -> tagChangeListener.entityChanged(tagChangeEvent));
            }
        }
    }

    @Override
    public void removeTagChangeListeners()
    {
        synchronized (this.tagChangeListeners)
        {
            this.tagChangeListeners.removeIf(tagChangeListener -> true);
        }
    }
}
