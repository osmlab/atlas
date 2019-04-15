package org.openstreetmap.atlas.geography.atlas.complete;

import org.openstreetmap.atlas.geography.Location;

/**
 * Similar to a {@link org.openstreetmap.atlas.geography.atlas.items.LocationItem} but for
 * {@link CompleteEntity}-ies.
 *
 * @param <E>
 *            - the {@link CompleteEntity} bveing worked on.
 * @author Yazad Khambata
 */
public interface CompleteLocationItem<E extends CompleteLocationItem<E>> extends CompleteEntity<E>
{
    CompleteLocationItem withLocation(Location location);
}
