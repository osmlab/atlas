package org.openstreetmap.atlas.geography.atlas.multi;

import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.utilities.scalars.Ratio;

/**
 * @author matthieun
 */
public class TemporaryOrderedNode implements Comparable<TemporaryOrderedNode>
{
    private final long nodeIdentifier;
    private final Node node;
    private final Ratio offset;
    private final int occurrenceIndex;

    public TemporaryOrderedNode(final Node node, final Ratio offset, final int occurrenceIndex)
    {
        this.nodeIdentifier = node.getIdentifier();
        this.node = node;
        this.offset = offset;
        this.occurrenceIndex = occurrenceIndex;
    }

    @Override
    public int compareTo(final TemporaryOrderedNode other)
    {
        final double delta = this.getOffset().asRatio() - other.getOffset().asRatio();
        return delta > 0 ? 1 : delta < 0 ? -1 : 0;
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof TemporaryOrderedNode)
        {
            final TemporaryOrderedNode that = (TemporaryOrderedNode) other;
            return this.node.getLocation().equals(that.node.getLocation())
                    && this.offset.equals(that.offset)
                    && this.occurrenceIndex == that.getOccurrenceIndex();
        }
        return false;
    }

    public Node getNode()
    {
        return this.node;
    }

    public long getNodeIdentifier()
    {
        return this.nodeIdentifier;
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
        return this.node.hashCode() + this.occurrenceIndex;
    }

    @Override
    public String toString()
    {
        return "[TemporaryOrderedNode: id = " + this.nodeIdentifier + " (" + this.node.getLocation()
                + "), offset = " + this.offset + ", occurrence = " + this.occurrenceIndex + "]";
    }
}
