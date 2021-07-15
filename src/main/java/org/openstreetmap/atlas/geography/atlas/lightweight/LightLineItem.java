package org.openstreetmap.atlas.geography.atlas.lightweight;

import javax.annotation.Nullable;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;

/**
 * A common interface for line items
 *
 * @param <E>
 *            The line item type
 * @author Taylor Smock
 */
public interface LightLineItem<E extends LightLineItem<E>> extends LightEntity<E>
{
    PolyLine asPolyLine();

    @Nullable
    default Iterable<Location> getGeometry()
    {
        return this.asPolyLine();
    }
}
