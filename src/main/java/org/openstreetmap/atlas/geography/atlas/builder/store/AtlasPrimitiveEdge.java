package org.openstreetmap.atlas.geography.atlas.builder.store;

import java.util.Map;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.Edge;

/**
 * @author Sid
 */
public class AtlasPrimitiveEdge extends AtlasPrimitiveLineItem
{
    private static final long serialVersionUID = 4693146600590040648L;

    public static AtlasPrimitiveEdge from(final Edge edge)
    {
        return new AtlasPrimitiveEdge(edge.getIdentifier(), edge.asPolyLine(), edge.getTags());
    }

    public AtlasPrimitiveEdge(final long identifier, final PolyLine polyLine,
            final Map<String, String> tags)
    {
        super(identifier, polyLine, tags);
    }

    public Location end()
    {
        return this.getPolyLine().last();
    }

    public boolean isReversedEdge(final AtlasPrimitiveEdge reverseEdgeCandidate)
    {
        return this.getIdentifier() == -reverseEdgeCandidate.getIdentifier();
    }

    public Location start()
    {
        return this.getPolyLine().first();
    }

    @Override
    public String toString()
    {
        final StringBuilder builder = new StringBuilder();
        builder.append("[AtlasPrimitiveEdge : ");
        builder.append("id = " + getIdentifier());
        builder.append(" : polyLine = " + getPolyLine());
        builder.append(" ]");
        return builder.toString();
    }
}
