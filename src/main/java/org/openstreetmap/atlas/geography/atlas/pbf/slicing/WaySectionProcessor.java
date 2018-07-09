package org.openstreetmap.atlas.geography.atlas.pbf.slicing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.AbstractIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.WaySectionIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.pbf.store.PbfMemoryStore;
import org.openstreetmap.atlas.geography.atlas.pbf.store.PbfOneWay;
import org.openstreetmap.atlas.geography.atlas.pbf.store.SectionCounter;
import org.openstreetmap.atlas.geography.atlas.pbf.store.TagMap;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;

/**
 * Split ways at intersections. E.g. if there are two ways sharing the same shape point, then both
 * ways should be split at the shared point.
 *
 * @author tony
 */
public class WaySectionProcessor
{
    private final SectionCounter countForNode;
    private final PbfMemoryStore store;

    // Both splitWay and splitRing will share this identifier factory
    private AbstractIdentifierFactory identifierFactory;

    public WaySectionProcessor(final PbfMemoryStore store)
    {
        this.countForNode = new SectionCounter();
        this.store = store;
        this.identifierFactory = null;
    }

    public void run()
    {
        // Go through each Atlas edge and increment counter for way nodes
        this.store.forEachAtlasEdge(way -> way.getWayNodes().forEach(this.countForNode::increment));

        // Increment for nodes at barriers
        this.store.forEachNode((identifier, node) ->
        {
            if (shouldSectionNode(node))
            {
                this.countForNode.increment(identifier);
            }
        });

        // Split at intersections, store mapping from original way to sectioned ways
        final Map<Long, List<Way>> waysNeedingUpdate = new HashMap<>(this.store.wayCount(), 1);
        this.store.forEachAtlasEdge(way ->
        {
            if (numberOfSlicingPoints(way) + 1 < AbstractIdentifierFactory.IDENTIFIER_SCALE)
            {
                // Split the ways.
                final List<Way> sectionedWays = splitWay(way);

                // If way get splits
                if (sectionedWays.size() != 0)
                {
                    waysNeedingUpdate.put(way.getId(), sectionedWays);
                }
            }
        });

        // Add new Nodes
        this.countForNode.sections()
                .forEach(nodeIdentifier -> this.store.addNodeAtEndOfEdge(nodeIdentifier));

        // Update Ways
        waysNeedingUpdate.forEach((oldIdentifier, newWays) ->
        {
            this.store.removeWay(oldIdentifier);
            newWays.forEach(this.store::addWay);
        });

        // Update Relations
        final Map<Long, Relation> modified = new HashMap<>();
        this.store.forEachRelation((identifier, relation) ->
        {
            final Set<Long> toUpdate = checkRelation(waysNeedingUpdate, relation);
            if (toUpdate.size() != 0)
            {
                modified.put(identifier, createNew(waysNeedingUpdate, relation, toUpdate));
            }
        });

        modified.forEach((oldIdentifier, newRelation) ->
        {
            this.store.removeRelation(oldIdentifier);
            this.store.addRelation(newRelation);
        });
    }

    private void addSplitWay(final List<Way> splitWays, final Way splitWay)
    {
        splitWays.add(splitWay);
    }

    private Set<Long> checkRelation(final Map<Long, List<Way>> splitWays, final Relation relation)
    {
        final Set<Long> results = new HashSet<>();
        for (final RelationMember member : relation.getMembers())
        {
            if (splitWays.containsKey(member.getMemberId()))
            {
                results.add(member.getMemberId());
            }
        }
        return results;
    }

    private Relation createNew(final Map<Long, List<Way>> splitWays, final Relation reference,
            final Set<Long> needUpdate)
    {
        final List<RelationMember> oldMembers = reference.getMembers();
        final List<RelationMember> newMembers = new ArrayList<>(oldMembers.size());
        for (final RelationMember member : oldMembers)
        {
            final long identifier = member.getMemberId();
            if (needUpdate.contains(identifier))
            {
                splitWays.get(identifier).forEach(way -> newMembers.add(
                        new RelationMember(way.getId(), EntityType.Way, member.getMemberRole())));
            }
            else
            {
                newMembers.add(member);
            }
        }
        return this.store.createRelation(reference, reference.getId(), newMembers);
    }

    private int numberOfSlicingPoints(final Way way)
    {
        int count = 0;
        final List<WayNode> wayNodes = way.getWayNodes();
        for (int index = 1; index < wayNodes.size() - 1; index++)
        {
            if (this.countForNode.isSection(wayNodes.get(index).getNodeId()))
            {
                count++;
            }
        }
        return count;
    }

    /**
     * @return {@code true} for all the nodes that have tags that warrant sectioning the way, even
     *         if those nodes are not at a navigable way to navigable way intersection.
     */
    private boolean shouldSectionNode(final Node node)
    {
        final Taggable taggableNode = Taggable.with(node.getTags());
        return this.store.getAtlasLoadingOption().getWaySectionFilter().test(taggableNode);
    }

    /**
     * Split {@link Way}s based on this.countForNode, if the way is not splitable, add 000 at the
     * end of identifier, otherwise the identifier will start from 001
     *
     * @param way
     *            The {@link Way} to split
     * @return A list of more than one split {@link Way}, if the way needs to be split, otherwise
     *         return an empty list
     */
    private List<Way> splitWay(final Way way)
    {
        final List<Way> splitWays = new ArrayList<>();
        this.identifierFactory = new WaySectionIdentifierFactory(way.getId());
        final List<WayNode> wayNodes = way.getWayNodes();
        boolean split = false;

        final PbfOneWay oneWay = new TagMap(way.getTags()).getOneWay();
        if (oneWay != PbfOneWay.REVERSED)
        {
            int lastIndex = 0;
            for (int currentIndex = 1; currentIndex < wayNodes.size() - 1; currentIndex++)
            {
                if (this.countForNode.isSection(wayNodes.get(currentIndex).getNodeId()))
                {
                    split = true;
                    final Way splitWay = this.store.createWay(way,
                            this.identifierFactory.nextIdentifier(),
                            wayNodes.subList(lastIndex, currentIndex + 1));
                    addSplitWay(splitWays, splitWay);
                    lastIndex = currentIndex;
                }
            }
            if (split)
            {
                // Add last segment
                final Way splitWay = this.store.createWay(way,
                        this.identifierFactory.nextIdentifier(),
                        wayNodes.subList(lastIndex, wayNodes.size()));
                addSplitWay(splitWays, splitWay);
            }
        }
        else
        {
            int lastIndex = wayNodes.size();
            for (int currentIndex = wayNodes.size() - 2; currentIndex > 0; currentIndex--)
            {
                if (this.countForNode.isSection(wayNodes.get(currentIndex).getNodeId()))
                {
                    split = true;
                    final Way splitWay = this.store.createWay(way,
                            this.identifierFactory.nextIdentifier(),
                            wayNodes.subList(currentIndex, lastIndex));
                    addSplitWay(splitWays, splitWay);
                    lastIndex = currentIndex + 1;
                }
            }
            if (split)
            {
                // Add last segment
                final Way splitWay = this.store.createWay(way,
                        this.identifierFactory.nextIdentifier(), wayNodes.subList(0, lastIndex));
                addSplitWay(splitWays, splitWay);
            }
        }
        return splitWays;
    }
}
