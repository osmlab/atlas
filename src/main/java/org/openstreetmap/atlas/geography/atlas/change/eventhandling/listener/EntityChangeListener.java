package org.openstreetmap.atlas.geography.atlas.change.eventhandling.listener;

import org.openstreetmap.atlas.geography.atlas.change.eventhandling.event.EntityChangeEvent;

/**
 * The basic contract for tracking changes to a
 * {@link org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity}.
 *
 * @param <E>
 *            - the entity being observed.
 * @author Yazad Khambata
 */
@FunctionalInterface
public interface EntityChangeListener<E extends EntityChangeEvent>
{
    void entityChanged(E entityChangeEvent);
}
