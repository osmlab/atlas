package org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary;

import java.util.Map;

import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.Edge;

/**
 * The {@link TemporaryEdge} object, keeps track of the bare minimum information required to create
 * an Atlas {@link Edge}. It is meant to be as light-weight as possible, keeping track of only the
 * positive identifier, polyLine, tags and whether there is an associated reverse Edge.
 *
 * @author mgostintsev
 */
public class TemporaryEdge extends TemporaryEntity
{
    private static final long serialVersionUID = 3867946360797866502L;

    private final PolyLine polyLine;
    private final boolean hasReverse;

    public TemporaryEdge(final long identifier, final PolyLine polyLine,
            final Map<String, String> tags, final boolean hasReverse)
    {
        super(identifier, tags);
        this.polyLine = polyLine;
        this.hasReverse = hasReverse;
    }

    public PolyLine getPolyLine()
    {
        return this.polyLine;
    }

    /**
     * The original edge identifier will always be positive. This method is used to return the
     * reverse edge identifier, which will always be negative.
     *
     * @return the reverse identifier for this {@link TemporaryEdge}
     */
    public long getReverseEdgeIdentifier()
    {
        return -1 * getIdentifier();
    }

    /**
     * @return {@code true} if this edge has a reverse edge
     */
    public boolean hasReverse()
    {
        return this.hasReverse;
    }

    @Override
    public String toString()
    {
        return "[Temporary Edge=" + this.getIdentifier() + ", polyLine=" + this.getPolyLine() + ", "
                + tagString() + ", hasReverse=" + hasReverse() + "]";
    }
}
