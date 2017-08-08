package org.openstreetmap.atlas.geography.atlas.multi;

import java.util.Map;

import org.openstreetmap.atlas.geography.PolyLine;

/**
 * @author matthieun
 */
public class TemporaryEdge
{
    private final long identifier;
    private final PolyLine polyLine;
    private final long startNodeIdentifier;
    private final long endNodeIdentifier;
    private final Map<String, String> tags;

    public TemporaryEdge(final long identifier, final PolyLine polyLine,
            final long startNodeIdentifier, final long endNodeIdentifier,
            final Map<String, String> tags)
    {
        this.identifier = identifier;
        this.polyLine = polyLine;
        this.startNodeIdentifier = startNodeIdentifier;
        this.endNodeIdentifier = endNodeIdentifier;
        this.tags = tags;
    }

    public long getEndNodeIdentifier()
    {
        return this.endNodeIdentifier;
    }

    public long getIdentifier()
    {
        return this.identifier;
    }

    public PolyLine getPolyLine()
    {
        return this.polyLine;
    }

    public long getReversedIdentifier()
    {
        return -1 * Math.abs(getIdentifier());
    }

    public long getStartNodeIdentifier()
    {
        return this.startNodeIdentifier;
    }

    public Map<String, String> getTags()
    {
        return this.tags;
    }

    @Override
    public String toString()
    {
        return "[TemporaryEdge: id = " + this.identifier + ", startNode = "
                + this.startNodeIdentifier + ", endNode = " + this.endNodeIdentifier + ", geom = "
                + this.polyLine + "]";
    }
}
