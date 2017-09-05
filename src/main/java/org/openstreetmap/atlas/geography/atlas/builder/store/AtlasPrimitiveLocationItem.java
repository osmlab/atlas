package org.openstreetmap.atlas.geography.atlas.builder.store;

import java.util.Map;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Node;

/**
 * A primitive object for {@link LocationItem}
 *
 * @author tony
 */
public class AtlasPrimitiveLocationItem extends AtlasPrimitiveEntity
{
    private static final long serialVersionUID = -6767702793907654973L;
    private final Location location;

    public static AtlasPrimitiveLocationItem from(final Node node)
    {
        final AtlasPrimitiveLocationItem locationItem = new AtlasPrimitiveLocationItem(
                node.getIdentifier(), node.getLocation(), node.getTags());
        return locationItem;
    }

    public AtlasPrimitiveLocationItem(final long identifier, final Location location,
            final Map<String, String> tags)
    {
        super(identifier, tags);
        this.location = location;
    }

    @Override
    public Rectangle bounds()
    {
        return this.location.bounds();
    }

    public Location getLocation()
    {
        return this.location;
    }

    @Override
    public String toString()
    {
        return "AtlasPrimitiveLocationItem [location=" + this.location + ", getIdentifier()="
                + getIdentifier() + ", getTags()=" + getTags() + "]";
    }
}
