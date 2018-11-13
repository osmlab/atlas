package org.openstreetmap.atlas.geography.atlas.bloated;

import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Independent {@link Edge} that contains its own data. At scale, use at your own risk.
 *
 * @author matthieun
 */
public class BloatedEdge extends Edge
{
    private static final long serialVersionUID = 309534717673911086L;

    private final long identifier;
    private final PolyLine polyLine;
    private final Map<String, String> tags;
    final Long startNodeIdentifier;
    final Long endNodeIdentifier;
    final Set<Long> relationIdentifiers;

    /**
     * Constructor to be used only in BloatedNode and BloatedRelation. Used otherwise, and this
     * object will misbehave.
     * 
     * @param identifier
     *            The feature identifier
     */
    protected BloatedEdge(final long identifier)
    {
        super(new BloatedAtlas());
        this.identifier = identifier;
        this.polyLine = null;
        this.tags = null;
        this.startNodeIdentifier = null;
        this.endNodeIdentifier = null;
        this.relationIdentifiers = null;
    }

    protected BloatedEdge(final long identifier, final PolyLine polyLine,
            final Map<String, String> tags, final long startNodeIdentifier,
            final long endNodeIdentifier, final Set<Long> relationIdentifiers)
    {
        super(new BloatedAtlas());
        this.identifier = identifier;
        this.polyLine = polyLine;
        this.tags = tags;
        this.startNodeIdentifier = startNodeIdentifier;
        this.endNodeIdentifier = endNodeIdentifier;
        this.relationIdentifiers = relationIdentifiers;
    }

    @Override
    public PolyLine asPolyLine()
    {
        return this.polyLine;
    }

    @Override
    public Node end()
    {
        return new BloatedNode(this.endNodeIdentifier);
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
            // Here override the Atlas equality check in Edge.equals() as the BloatedAtlas is always
            // empty and unique.
            return this.getIdentifier() == that.getIdentifier();
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
        return new BloatedNode(this.startNodeIdentifier);
    }
}
