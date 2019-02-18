package org.openstreetmap.atlas.geography.atlas.complete;

import org.openstreetmap.atlas.geography.Location;

/**
 * Similar to a {@link org.openstreetmap.atlas.geography.atlas.items.LocationItem} but for {@link CompleteEntity}-ies.
 *
 * @author Yazad Khambata
 */
public interface CompleteLocationItem extends CompleteEntity {
    CompleteLocationItem withLocation(final Location location);
}
