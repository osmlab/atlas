package org.openstreetmap.atlas.geography.atlas.change.eventhandling.listener;

import org.openstreetmap.atlas.geography.atlas.change.eventhandling.event.TagChangeEvent;

/**
 * Tracking changes to the tags of the
 * {@link org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity}.
 *
 * @author Yazad Khambata
 */
public interface TagChangeListener extends EntityChangeListener<TagChangeEvent>
{
}
