package org.openstreetmap.atlas.geography.atlas.change;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.change.feature.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

import com.google.common.collect.Lists;

/**
 * @author matthieun
 */
public class AtlasChangeGeneratorAddTurnRestrictions implements AtlasChangeGenerator
{
    @Override
    public Set<FeatureChange> generateWithoutValidation(final Atlas atlas)
    {
        final AtomicLong identifierGenerator = new AtomicLong();
        final Set<FeatureChange> result = new HashSet<>();
        final Long parentRelationIdentifier = identifierGenerator.incrementAndGet();
        final RelationBean parentMembers = new RelationBean();
        Rectangle parentBounds = null;
        for (final Node node : atlas.nodes(node -> node.valence() > 3))
        {
            final SortedSet<Edge> inEdges = node.inEdges();
            final SortedSet<Edge> outEdges = node.outEdges();
            for (final Edge inEdge : inEdges)
            {
                for (final Edge outEdge : outEdges)
                {
                    final RelationBean members = new RelationBean();
                    members.addItem(inEdge.getIdentifier(), "from", ItemType.EDGE);
                    inEdge.reversed().ifPresent(reversed -> members
                            .addItem(reversed.getIdentifier(), "from", ItemType.EDGE));
                    members.addItem(node.getIdentifier(), "via", ItemType.NODE);
                    members.addItem(outEdge.getIdentifier(), "to", ItemType.EDGE);
                    outEdge.reversed().ifPresent(reversed -> members
                            .addItem(reversed.getIdentifier(), "to", ItemType.EDGE));
                    final Long relationIdentifier = identifierGenerator.incrementAndGet();
                    final Rectangle bounds = Rectangle.forLocated(inEdge, outEdge);
                    if (parentBounds == null)
                    {
                        parentBounds = bounds;
                    }
                    else
                    {
                        parentBounds = Rectangle.forLocated(parentBounds, bounds);
                    }
                    parentMembers.addItem(relationIdentifier, "addition", ItemType.RELATION);
                    result.add(FeatureChange.add(new CompleteRelation(relationIdentifier,
                            Maps.hashMap("type", "restriction", "restriction", "no_left_turn"),
                            bounds, members, Lists.newArrayList(relationIdentifier), members,
                            relationIdentifier, Sets.hashSet(parentRelationIdentifier))));
                    result.add(FeatureChange
                            .add(CompleteEdge.shallowFrom(inEdge).withRelationIdentifiers(
                                    mergeRelationMembers(inEdge.relations(), relationIdentifier))));
                    if (inEdge.hasReverseEdge())
                    {
                        result.add(
                                FeatureChange.add(CompleteEdge.shallowFrom(inEdge.reversed().get())
                                        .withRelationIdentifiers(mergeRelationMembers(
                                                inEdge.relations(), relationIdentifier))));
                    }
                    result.add(FeatureChange
                            .add(CompleteNode.shallowFrom(node).withRelationIdentifiers(
                                    mergeRelationMembers(node.relations(), relationIdentifier))));
                    result.add(FeatureChange.add(CompleteEdge.shallowFrom(outEdge)
                            .withRelationIdentifiers(mergeRelationMembers(outEdge.relations(),
                                    relationIdentifier))));
                    if (outEdge.hasReverseEdge())
                    {
                        result.add(
                                FeatureChange.add(CompleteEdge.shallowFrom(outEdge.reversed().get())
                                        .withRelationIdentifiers(mergeRelationMembers(
                                                outEdge.relations(), relationIdentifier))));
                    }
                    // Break here to avoid too many Relation FeatureChanges and make validation
                    // super slow for unit tests.
                    break;
                }
                // Break here to avoid too many Relation FeatureChanges and make validation super
                // slow for unit tests.
                break;
            }
        }
        if (!result.isEmpty())
        {
            result.add(FeatureChange.add(new CompleteRelation(parentRelationIdentifier,
                    Maps.hashMap("name", "parent_of_new_restrictions"), parentBounds, parentMembers,
                    Lists.newArrayList(parentRelationIdentifier), parentMembers,
                    parentRelationIdentifier, Sets.hashSet())));
        }
        return result;
    }

    private Set<Long> mergeRelationMembers(final Set<Relation> relations, final Long newIdentifier)
    {
        return Sets.withSets(
                relations.stream().map(Relation::getIdentifier).collect(Collectors.toSet()),
                Sets.hashSet(newIdentifier));
    }
}
