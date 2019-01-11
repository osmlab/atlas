package org.openstreetmap.atlas.geography.atlas.change;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedEdge;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedNode;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedRelation;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

/**
 * @author matthieun
 */
public class AtlasChangeGeneratorSplitRoundabout implements AtlasChangeGenerator
{
    @Override
    public Set<FeatureChange> generateWithoutValidation(final Atlas atlas)
    {
        final AtomicLong identifierGenerator = new AtomicLong();
        final Set<FeatureChange> result = new HashSet<>();
        for (final Edge edge : atlas.edges(JunctionTag::isRoundabout))
        {
            final PolyLine currentShape = edge.asPolyLine();
            if (currentShape.size() > 2 && !edge.hasReverseEdge())
            {
                // Prepare members to fill out: shapes, ids, etc.
                final Location cut = currentShape.get(currentShape.size() / 2);
                final PolyLine shape1 = currentShape.between(currentShape.first(), 0, cut, 0);
                final PolyLine shape2 = currentShape.between(cut, 0, currentShape.last(), 0);
                final long middleNodeIdentifier = identifierGenerator.incrementAndGet();
                final long oldEdgeIdentifier = edge.getIdentifier();
                final long newEdgeIdentifier1 = identifierGenerator.incrementAndGet();
                final long newEdgeIdentifier2 = identifierGenerator.incrementAndGet();

                // This is a new Edge, use "from" instead of "shallowFrom"
                final BloatedEdge firstEdge = BloatedEdge.from(edge)
                        .withIdentifier(newEdgeIdentifier1).withPolyLine(shape1)
                        .withEndNodeIdentifier(middleNodeIdentifier);
                // This is a new Edge, use "from" instead of "shallowFrom"
                final BloatedEdge secondEdge = BloatedEdge.from(edge)
                        .withIdentifier(newEdgeIdentifier2).withPolyLine(shape2)
                        .withStartNodeIdentifier(middleNodeIdentifier);

                // Update relations of edge to instead list first and second edge that have new IDs
                edge.relations().stream().map(relation ->
                {
                    // newMembers exclude the old edge explicitly
                    final RelationMemberList newMembers = new RelationMemberList(relation.members()
                            .stream().filter(member -> !member.getEntity().equals(edge))
                            .collect(Collectors.toList()));
                    return BloatedRelation.shallowFrom(relation)
                            .withMembersAndSource(newMembers, relation)
                            // With the new relation members
                            .withExtraMember(firstEdge, edge).withExtraMember(secondEdge, edge);
                }).map(FeatureChange::add).forEach(result::add);

                // Add the two new edges.
                result.add(FeatureChange.remove(BloatedEdge.shallowFrom(edge)));
                result.add(FeatureChange.add(firstEdge));
                result.add(FeatureChange.add(secondEdge));

                // Middle node is new. Create from scratch
                result.add(FeatureChange.add(new BloatedNode(middleNodeIdentifier, cut,
                        Maps.hashMap(), Sets.treeSet(newEdgeIdentifier1),
                        Sets.treeSet(newEdgeIdentifier2), Sets.hashSet())));

                // End node has a replaced start edge identifier
                result.add(FeatureChange.add(BloatedNode.shallowFrom(edge.end())
                        .withInEdges(edge.end().inEdges())
                        .withInEdgeIdentifierReplaced(oldEdgeIdentifier, newEdgeIdentifier2)));
                // Start node has a replaced end edge identifier
                result.add(FeatureChange.add(BloatedNode.shallowFrom(edge.start())
                        .withOutEdges(edge.start().outEdges())
                        .withOutEdgeIdentifierReplaced(oldEdgeIdentifier, newEdgeIdentifier1)));
            }
        }
        return result;
    }
}
