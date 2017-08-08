package org.openstreetmap.atlas.geography.atlas.multi;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * {@link Edge} made from a {@link MultiAtlas}.
 *
 * @author matthieun
 */
public class MultiEdge extends Edge
{
    private static final long serialVersionUID = -3986525201031430336L;

    // Not index!
    private final long identifier;

    private Edge subEdge;

    protected MultiEdge(final MultiAtlas atlas, final long identifier)
    {
        super(atlas);
        this.identifier = identifier;
    }

    @Override
    public PolyLine asPolyLine()
    {
        return this.getSubEdge().asPolyLine();
    }

    @Override
    public Node end()
    {
        return new MultiNode(multiAtlas(),
                masteriseNodeIdentifier(this.getSubEdge().end().getIdentifier()));
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public Map<String, String> getTags()
    {
        return this.getSubEdge().getTags();
    }

    @Override
    public Set<Relation> relations()
    {
        return multiAtlas().multifyRelations(getSubEdge());
    }

    @Override
    public Node start()
    {
        return new MultiNode(multiAtlas(),
                masteriseNodeIdentifier(this.getSubEdge().start().getIdentifier()));
    }

    private Edge getSubEdge()
    {
        if (this.subEdge == null)
        {
            this.subEdge = this.multiAtlas().subEdge(this.identifier);
        }
        return this.subEdge;
    }

    /**
     * In case there is another node that overlaps this one, and the other one is the master, get
     * the other one
     *
     * @param identifier
     *            The node odentifier
     * @return The master node identifier if any, or identity
     */
    private Long masteriseNodeIdentifier(final long identifier)
    {
        final Optional<Long> masterNodeIdentifier = multiAtlas().masterNode(identifier);
        if (masterNodeIdentifier.isPresent())
        {
            return masterNodeIdentifier.get();
        }
        else
        {
            return identifier;
        }
    }

    private MultiAtlas multiAtlas()
    {
        return (MultiAtlas) this.getAtlas();
    }
}
