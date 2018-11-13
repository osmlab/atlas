package org.openstreetmap.atlas.geography.atlas.bloated;

import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * @author matthieun
 */
public class BloatedEdge extends Edge
{
    private static final long serialVersionUID = 309534717673911086L;

    private final long identifier;
    private final PolyLine polyLine;
    private final Map<String, String> tags;

    protected BloatedEdge(final long identifier, final PolyLine polyLine,
            final Map<String, String> tags)
    {
        super(new BloatedAtlas());
        this.identifier = identifier;
        this.polyLine = polyLine;
        this.tags = tags;
    }

    @Override
    public PolyLine asPolyLine()
    {
        return this.polyLine;
    }

    @Override
    public Node end()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(final Object other) // NOSONAR
    {
        if (this == other)
        {
            return true;
        }
        if (other != null && this.getClass() == other.getClass())
        {
            final BloatedEdge that = (BloatedEdge) other;
            return this.getIdentifier() == that.getIdentifier()
                    && this.asPolyLine().equals(that.asPolyLine())
                    && this.getTags().equals(that.getTags());
        }
        return false;
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public Map<String, String> getTags()
    {
        return this.tags;
    }

    @Override
    public Set<Relation> relations()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node start()
    {
        throw new UnsupportedOperationException();
    }
}
