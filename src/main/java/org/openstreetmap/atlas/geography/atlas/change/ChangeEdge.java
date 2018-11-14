package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * @author matthieun
 */
public class ChangeEdge extends Edge // NOSONAR
{
    private static final long serialVersionUID = -5658471275390043045L;

    private final Edge source;

    protected ChangeEdge(final ChangeAtlas atlas, final Edge source)
    {
        super(atlas);
        this.source = source;
    }

    @Override
    public PolyLine asPolyLine()
    {
        return this.source.asPolyLine();
    }

    @Override
    public Node end()
    {
        return getChangeAtlas().node(this.source.end().getIdentifier());
    }

    @Override
    public long getIdentifier()
    {
        return this.source.getIdentifier();
    }

    @Override
    public Map<String, String> getTags()
    {
        return this.source.getTags();
    }

    @Override
    public Set<Relation> relations()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node start()
    {
        return getChangeAtlas().node(this.source.start().getIdentifier());
    }

    private ChangeAtlas getChangeAtlas()
    {
        return (ChangeAtlas) getAtlas();
    }
}
