package org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary;

import java.util.Collections;
import java.util.List;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;

import com.google.common.collect.Lists;

/**
 * Object that represents piece of polygon cut.
 *
 * @author Yiqing Jin
 */
public class PolygonPiece
{
    private final List<Location> locations;
    private final long identifier;

    public PolygonPiece(final LineItem line)
    {
        this.identifier = line.getIdentifier();
        this.locations = Lists.newArrayList(line);
    }

    @Override
    public boolean equals(final Object other)
    {
        return other instanceof PolygonPiece
                && this.identifier == ((PolygonPiece) other).identifier;
    }

    public Location getEndNode()
    {
        return this.locations.get(this.locations.size() - 1);
    }

    public long getIdentifier()
    {
        return this.identifier;
    }

    public List<Location> getLocations()
    {
        return this.locations;
    }

    public Location getStartNode()
    {
        return this.locations.get(0);
    }

    @Override
    public int hashCode()
    {
        return (int) this.identifier;
    }

    /**
     * @return {@code true} if coordinates represent a closed ring
     */
    public boolean isClosed()
    {
        return this.getStartNode().equals(this.getEndNode());
    }

    public void merge(final PolygonPiece other, final boolean isReverseSelf,
            final boolean isReverseOther)
    {
        if (isReverseSelf)
        {
            Collections.reverse(this.locations);
        }
        if (isReverseOther)
        {
            Collections.reverse(other.getLocations());
        }
        this.locations.addAll(other.getLocations());
    }
}
