package org.openstreetmap.atlas.geography.atlas.change.eventhandling.listenable;

import org.openstreetmap.atlas.geography.atlas.change.eventhandling.event.TagChangeEvent;
import org.openstreetmap.atlas.geography.atlas.change.eventhandling.listener.TagChangeListener;

/**
 * A {@link org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity} whose tag changes can
 * be "listened" to.
 *
 * @author Yazad Khambata
 */
public interface TagChangeListenable extends EntityChangeListenable
{
    void addTagChangeListener(TagChangeListener tagChangeListener);

    void fireTagChangeEvent(TagChangeEvent tagChangeEvent);

    void removeTagChangeListeners();
}
