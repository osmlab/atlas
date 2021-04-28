package org.openstreetmap.atlas.geography.atlas.items;

import java.util.Iterator;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;

/**
 * A flyweight item in an Atlas.
 *
 * @author matthieun
 */
public abstract class AtlasItem extends AtlasEntity implements Iterable<Location>
{
    private static final long serialVersionUID = -2907538413224236973L;

    protected AtlasItem(final Atlas atlas)
    {
        super(atlas);
    }

    /**
     * @return The raw geometry, as {@link Location}, {@link PolyLine} or {@link Polygon}.
     */
    public abstract Iterable<Location> getRawGeometry();

    @Override
    public Iterator<Location> iterator()
    {
        return getRawGeometry().iterator();
    }
}
