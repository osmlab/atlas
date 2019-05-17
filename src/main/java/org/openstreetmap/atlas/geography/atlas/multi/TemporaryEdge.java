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
    private final Map<String, String> tags;

    public TemporaryEdge(final long identifier, final PolyLine polyLine,
            final Map<String, String> tags)
    {
        this.identifier = identifier;
        this.polyLine = polyLine;
        this.tags = tags;
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

    public Map<String, String> getTags()
    {
        return this.tags;
    }

    @Override
    public String toString()
    {
        return "[TemporaryEdge: id = " + this.identifier + ", geom = " + this.polyLine + "]";
    }
}
