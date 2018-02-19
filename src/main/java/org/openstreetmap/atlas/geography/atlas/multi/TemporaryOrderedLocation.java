package org.openstreetmap.atlas.geography.atlas.multi;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.utilities.scalars.Ratio;

/**
 * @author mkalender
 */
public class TemporaryOrderedLocation implements Comparable<TemporaryOrderedLocation>
{
    private final Location location;
    private final Ratio offset;
    private final int occurrenceIndex;

    public TemporaryOrderedLocation(final Location location, final Ratio offset,
            final int occurrenceIndex)
    {
        this.location = location;
        this.offset = offset;
        this.occurrenceIndex = occurrenceIndex;
    }

    @Override
    public int compareTo(final TemporaryOrderedLocation other)
    {
        final double delta = this.getOffset().asRatio() - other.getOffset().asRatio();
        return delta > 0 ? 1
                : delta < 0 ? -1
                        : this.occurrenceIndex < other.getOccurrenceIndex() ? 1
                                : this.occurrenceIndex > other.getOccurrenceIndex() ? -1 : 0;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof TemporaryOrderedLocation)
        {
            final TemporaryOrderedLocation that = (TemporaryOrderedLocation) other;
            return this.getLocation().equals(that.getLocation()) && this.offset.equals(that.offset)
                    && this.occurrenceIndex == that.getOccurrenceIndex();
        }
        return false;
    }

    public Location getLocation()
    {
        return this.location;
    }

    public int getOccurrenceIndex()
    {
        return this.occurrenceIndex;
    }

    public Ratio getOffset()
    {
        return this.offset;
    }

    @Override
    public int hashCode()
    {
        return this.location.hashCode() + this.occurrenceIndex;
    }

    @Override
    public String toString()
    {
        return "[TemporaryOrderedLocation: " + this.getLocation() + "), offset = " + this.offset
                + ", occurrence = " + this.occurrenceIndex + "]";
    }
}
