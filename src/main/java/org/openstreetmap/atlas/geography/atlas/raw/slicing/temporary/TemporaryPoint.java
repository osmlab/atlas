package org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary;

import java.util.Map;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.Point;

/**
 * The {@link TemporaryPoint} object, keeps track of the bare minimum information required to create
 * an Atlas {@link Point}.
 *
 * @author mgostintsev
 */
public class TemporaryPoint extends TemporaryEntity
{
    private static final long serialVersionUID = -9088251405959839239L;

    private final Location location;

    public TemporaryPoint(final long identifier, final Location location,
            final Map<String, String> tags)
    {
        super(identifier, tags);
        this.location = location;
    }

    public Location getLocation()
    {
        return this.location;
    }

    @Override
    public String toString()
    {
        return "[Temporary Point=" + this.getIdentifier() + ", location=" + this.getLocation()
                + ", " + tagString() + "]";
    }
}
