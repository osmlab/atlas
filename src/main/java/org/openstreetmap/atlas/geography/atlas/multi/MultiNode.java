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
import org.openstreetmap.atlas.utilities.maps.MultiMapWithSet;
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

    protected MultiNode(final MultiAtlas atlas, final long identifier)
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

    public SubNodeList getSubNodes()
    {
        if (this.subNodes == null)
        {
            this.subNodes = multiAtlas().subNodes(this.identifier);
        }
        return this.subNodes;
    }

    @Override
    public Map<String, String> getTags()
    {
        return getRepresentativeSubNode().getTags();
    }

    @Override
    public SortedSet<Edge> inEdges()
    {
        return attachedEdgesFromOverlappingNodes(node -> node.inEdges(),
                multiAtlas().getNodeIdentifiersToRemovedInEdges());
    }

    @Override
    public SortedSet<Edge> outEdges()
    {
        return attachedEdgesFromOverlappingNodes(node -> node.outEdges(),
                multiAtlas().getNodeIdentifiersToRemovedOutEdges());
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
     * @param removedEdges
     *            The map of node identifier to edges that have been removed by the MultiAtlas fix
     *            for way sectioning at Atlas borders
     * @return All the attached edges from one single node identifier
     */
    protected SortedSet<Edge> attachedEdges(final Function<Node, Set<Edge>> getConnectedEdges,
            final MultiMapWithSet<Long, Long> removedEdges)
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
            final boolean isFixedNode = subNodes.hasFixNode();
            final Set<Long> removedEdgesForNode = removedEdges.get(getIdentifier());
            final boolean hasRemovedEdges = removedEdgesForNode != null;
            for (final Node node : subNodes.getSubNodes())
            {
                for (final Edge connectedEdge : getConnectedEdges.apply(node))
                {
                    if (!isFixedNode || !hasRemovedEdges
                            || !removedEdgesForNode.contains(connectedEdge.getIdentifier()))
                    {
                        // Add this in/out edge only if the node is not fixed or edge is fixed, but
                        // if the specific edge is not one of the fixed edges of this node not
                        // removed from this node's connections
                        subResult.add(connectedEdge.getIdentifier());
                    }
                }
            }

            // Add the edges left out if they have been fixed, and take the list from the fix node.
            if (isFixedNode)
            {
                final Node fixNode = subNodes.getFixNode();
                for (final Edge connectedEdge : getConnectedEdges.apply(fixNode))
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
                logger.warn(
                        "Some edge got lost in translation, and is not in the MultiAtlas. "
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
     * @param removedEdges
     *            The map of edges that have been removed by the MultiAtlas fix for way sectioning
     *            at Atlas borders
     * @return All the attached edges from one node, including those coming from overlapping edges
     */
    protected SortedSet<Edge> attachedEdgesFromOverlappingNodes(
            final Function<Node, Set<Edge>> getConnectedEdges,
            final MultiMapWithSet<Long, Long> removedEdges)
    {
        final Set<Long> slaveNodes = multiAtlas().overlappingNodes(getIdentifier());
        final Optional<Long> masterNode = multiAtlas().masterNode(this.identifier);
        if (!slaveNodes.isEmpty())
        {
            // This Multi-Node is an overlapping master node. Return all the in/out edges of this
            // node, plus those of the other slave nodes.
            final SortedSet<Edge> result = attachedEdges(getConnectedEdges, removedEdges);
            slaveNodes.forEach(slaveIdentifier -> result
                    .addAll(((MultiNode) multiAtlas().node(slaveIdentifier))
                            .attachedEdges(getConnectedEdges, removedEdges)));
            return result;
        }
        else if (masterNode.isPresent())
        {
            // This Multi-Node is an overlapping slave node. Return no edges. All its edges will be
            // directed to the master node. Slaves nodes are then rendered useless (even though they
            // are still present) to mimic the behavior of the PackedAtlas.
            return new TreeSet<>();
        }
        else
        {
            // This Multi-Node is not an overlapping node. Return the standard set of in/out edges.
            return attachedEdges(getConnectedEdges, removedEdges);
        }
    }

    private Node getRepresentativeSubNode()
    {
        final SubNodeList subNodes = getSubNodes();

        // If we have a fixed node, then give that node higher priority
        if (subNodes.hasFixNode())
        {
            return subNodes.getFixNode();
        }

        return getSubNodes().getSubNodes().get(0);
    }

    private MultiAtlas multiAtlas()
    {
        return (MultiAtlas) this.getAtlas();
    }
}
