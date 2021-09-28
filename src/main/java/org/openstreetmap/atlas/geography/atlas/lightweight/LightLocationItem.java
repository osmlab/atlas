package org.openstreetmap.atlas.geography.atlas.lightweight;

import java.util.Collections;

import org.openstreetmap.atlas.geography.Location;

/**
 * A light location item
 *
 * @param <E>
 *            The type of location item
 * @author Taylor Smock
 */
public interface LightLocationItem<E extends LightLocationItem<E>>
        extends LightEntity<LightLocationItem<E>>
{
    @Override
    default Iterable<Location> getGeometry()
    {
        return Collections.singleton(this.getLocation());
    }

    Location getLocation();
}
