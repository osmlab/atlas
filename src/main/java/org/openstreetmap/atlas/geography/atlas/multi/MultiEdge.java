package org.openstreetmap.atlas.geography.atlas.multi;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

import com.google.common.collect.Sets;

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

    private SubEdgeList subEdgeList;

    protected MultiEdge(final MultiAtlas atlas, final long identifier)
    {
        super(atlas);
        this.identifier = identifier;
    }

    @Override
    public PolyLine asPolyLine()
    {
        return this.getRepresentativeSubEdge().asPolyLine();
    }

    @Override
    public Node end()
    {
        return new MultiNode(multiAtlas(),
                masteriseNodeIdentifier(this.getRepresentativeSubEdge().end().getIdentifier()));
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public Map<String, String> getTags()
    {
        return this.getRepresentativeSubEdge().getTags();
    }

    @Override
    public Set<Relation> relations()
    {
        Set<Relation> unionOfAllParentRelations = new HashSet<>();
        for (final Edge subEdge : getSubEdges().getSubEdges())
        {
            final Set<Relation> currentSubEdgeParentRelations = multiAtlas()
                    .multifyRelations(subEdge);
            unionOfAllParentRelations = Sets.union(unionOfAllParentRelations,
                    currentSubEdgeParentRelations);
        }
        return unionOfAllParentRelations;
    }

    @Override
    public Node start()
    {
        return new MultiNode(multiAtlas(),
                masteriseNodeIdentifier(this.getRepresentativeSubEdge().start().getIdentifier()));
    }

    private Edge getRepresentativeSubEdge()
    {
        return getSubEdges().getSubEdges().get(0);
    }

    private SubEdgeList getSubEdges()
    {
        if (this.subEdgeList == null)
        {
            this.subEdgeList = this.multiAtlas().subEdge(this.identifier);
        }
        return this.subEdgeList;
    }

    /**
     * In case there is another node that overlaps this one, and the other one is the master, get
     * the other one
     *
     * @param identifier
     *            The node identifier
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
