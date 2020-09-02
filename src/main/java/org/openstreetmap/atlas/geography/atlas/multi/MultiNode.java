package org.openstreetmap.atlas.geography.atlas.multi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * {@link Node} made from a {@link MultiAtlas}
 *
 * @author matthieun
 */
public class MultiNode extends Node
{
    private static final long serialVersionUID = 4280290265432052817L;
    private static final Logger logger = LoggerFactory.getLogger(MultiNode.class);

    private final long identifier;
    private SubNodeList subNodes;

    MultiNode(final MultiAtlas atlas, final long identifier)
    {
        super(atlas);
        this.identifier = identifier;
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public Location getLocation()
    {
        return getRepresentativeSubNode().getLocation();
    }

    @Override
    public Map<String, String> getTags()
    {
        return getRepresentativeSubNode().getTags();
    }

    @Override
    public SortedSet<Edge> inEdges()
    {
        return attachedEdgesFromOverlappingNodes(Node::inEdges);
    }

    @Override
    public SortedSet<Edge> outEdges()
    {
        return attachedEdgesFromOverlappingNodes(Node::outEdges);
    }

    @Override
    public Set<Relation> relations()
    {
        Set<Relation> unionOfAllParentRelations = new HashSet<>();
        for (final Node subNode : getSubNodes().getSubNodes())
        {
            final Set<Relation> currentSubNodeParentRelations = multiAtlas()
                    .multifyRelations(subNode);
            unionOfAllParentRelations = Sets.union(unionOfAllParentRelations,
                    currentSubNodeParentRelations);
        }
        return unionOfAllParentRelations;
    }

    /**
     * Get all the attached edges from one single node identifier, directly coming from the
     * sub-Atlases...
     *
     * @param getConnectedEdges
     *            The function that decides what side the edges are to be taken (in or out)
     * @return All the attached edges from one single node identifier
     */
    private SortedSet<Edge> attachedEdges(final Function<Node, Set<Edge>> getConnectedEdges)
    {
        final Set<Long> subResult = new HashSet<>();
        if (getSubNodes().size() == 1)
        {
            // The node is in one single sub-atlas only, hence not fixed.
            getConnectedEdges.apply(getRepresentativeSubNode())
                    .forEach(edge -> subResult.add(edge.getIdentifier()));
        }
        else
        {
            // The node is in many sub-atlases. Collect all the edges from the different Atlases.
            final SubNodeList subNodes = getSubNodes();
            for (final Node node : subNodes.getSubNodes())
            {
                for (final Edge connectedEdge : getConnectedEdges.apply(node))
                {
                    subResult.add(connectedEdge.getIdentifier());
                }
            }
        }

        // Return MultiEdges so they still have a reference to the MultiAtlas
        final SortedSet<Edge> result = new TreeSet<>();
        for (final Long subEdgeIdentifier : subResult)
        {
            final Edge multiEdge = multiAtlas().edge(subEdgeIdentifier);
            if (multiEdge == null)
            {
                // This can happen sometimes when a node has another overlapping node in a sub-atlas
                // which gets all the connected edges, and that edge has been corrected for way
                // sectioning issues at the border, but the edge has been marked as "removed" from
                // the other node and not this one. Log it, and do not include the null.
                final List<String> missingEdgeAtlasNames = new ArrayList<>();
                final List<Atlas> atlases = ((MultiAtlas) getAtlas()).getAtlases();
                for (int index = 0; index < atlases.size(); index++)
                {
                    final Atlas subAtlas = atlases.get(index);
                    final Edge subEdge = subAtlas.edge(subEdgeIdentifier);
                    if (subEdge != null)
                    {
                        missingEdgeAtlasNames.add(subAtlas.getName());
                    }
                }
                logger.warn("Some edge got lost in translation, and is not in the MultiAtlas. "
                        + "The node below probably has another node at the exact same location!\n\t"
                        + "Node: {}\n\t" + "Edge connected: {}\n\t" + "From SubAtlas: {}",
                        this.identifier, subEdgeIdentifier, missingEdgeAtlasNames);
            }
            else
            {
                result.add(multiEdge);
            }
        }
        return result;
    }

    /**
     * Get all the attached edges from one node, including those coming from overlapping nodes.
     *
     * @param getConnectedEdges
     *            The function that decides what side the edges are to be taken (in or out)
     * @return All the attached edges from one node, including those coming from overlapping edges
     */
    private SortedSet<Edge> attachedEdgesFromOverlappingNodes(
            final Function<Node, Set<Edge>> getConnectedEdges)
    {
        final Set<Long> alternateNodes = multiAtlas().overlappingNodes(getIdentifier());
        final Optional<Long> mainNode = multiAtlas().mainNode(this.identifier);
        if (!alternateNodes.isEmpty())
        {
            // This Multi-Node is an overlapping main node. Return all the in/out edges of this
            // node, plus those of the other alternate nodes.
            final SortedSet<Edge> result = attachedEdges(getConnectedEdges);
            alternateNodes.forEach(alternateIdentifier -> result
                    .addAll(((MultiNode) multiAtlas().node(alternateIdentifier))
                            .attachedEdges(getConnectedEdges)));
            return result;
        }
        else if (mainNode.isPresent())
        {
            // This Multi-Node is an overlapping alternate node. Return no edges. All its edges will
            // be directed to the main node. Alternate nodes are then rendered useless (even though
            // they are still present) to mimic the behavior of the PackedAtlas.
            return new TreeSet<>();
        }
        else
        {
            // This Multi-Node is not an overlapping node. Return the standard set of in/out edges.
            return attachedEdges(getConnectedEdges);
        }
    }

    private Node getRepresentativeSubNode()
    {
        return getSubNodes().getSubNodes().get(0);
    }

    private SubNodeList getSubNodes()
    {
        if (this.subNodes == null)
        {
            this.subNodes = multiAtlas().subNodes(this.identifier);
        }
        return this.subNodes;
    }

    private MultiAtlas multiAtlas()
    {
        return (MultiAtlas) this.getAtlas();
    }
}
