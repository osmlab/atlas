package org.openstreetmap.atlas.geography.atlas.complete;

import java.util.ArrayList;
import java.util.List;

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
    @Override
    default Iterable<Location> getGeometry()
    {
        if (getLocation() != null)
        {
            final List<Location> geometry = new ArrayList<>();
            geometry.add(getLocation());
            return geometry;
        }
        return null;
    }

    Location getLocation();

    CompleteLocationItem withLocation(Location location);
}
