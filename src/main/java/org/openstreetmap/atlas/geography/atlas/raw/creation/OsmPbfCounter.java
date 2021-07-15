package org.openstreetmap.atlas.geography.atlas.raw.creation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.raw.sectioning.TagMap;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.RouteTag;
import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Bound;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OsmPbfCounter} is responsible for identifying and counting the number of
 * {@link Point}s, {@link Line}s and {@link org.openstreetmap.atlas.geography.atlas.items.Relation}s
 * that will be brought in from the given OSM PBF file. This information will be used to populate
 * the {@link AtlasSize} field to efficiently construct a Raw {@link Atlas}. The logic for
 * determining whether a feature should be brought in is as follows: *
 * <ul>
 * <li>Look at each OSM Node, if it's inside the given bounding box, bring it in. Keep track of all
 * nodes that were not brought in.
 * <li>Look at each OSM Way, if it has a Node that was brought in, bring in the Way and all other
 * Nodes that are part of this Way (since they may not have been originally brought in).
 * <li>Look at each OSM Relation at a shallow level - if it has a member (Node, Way or Relation)
 * that was brought in, go ahead and bring this Relation in. At this stage, we don't look at nested
 * relations. If we can't find a shallow member, stage this relation.
 * <li>Look at all staged relations and try to bring any in. If we loop through all staged relations
 * without bringing anything in, we can conclude that all remaining relations are either fully
 * outside the given boundary or are looping over each other.
 * </ul>
 *
 * @author mgostintsev
 */
public class OsmPbfCounter implements Sink
{
    private static final Logger logger = LoggerFactory.getLogger(OsmPbfCounter.class);

    private static final int MAXIMUM_NETWORK_EXTENSION = 2;

    private final AtlasLoadingOption loadingOption;
    private final GeometricSurface boundingBox;

    // Identifiers to bring in to the raw atlas
    private final Set<Long> nodeIdentifiersToInclude = new HashSet<>();
    private final Set<Long> nodeIdentifiersBroughtInByWaysOrRelations = new HashSet<>();
    private final Set<Long> wayIdentifiersToInclude = new HashSet<>();
    private final Set<Long> relationIdentifiersToInclude = new HashSet<>();

    // Keep track of included nodes so that they can be used in calculating if a way intersects the
    // given shard
    private final Map<Long, Location> nodeIdentifierToLocation = new HashMap<>();

    // Keep track of excluded ways to see if we need to add them later
    private final Map<Long, Way> waysToExclude = new HashMap<>();

    // Keep track of non-shallow relations to see if we need to add them later
    private final List<Relation> stagedRelations = new ArrayList<>();

    /**
     * Default constructor.
     *
     * @param loadingOption
     *            The {@link AtlasLoadingOption} to use
     * @param boundingBox
     *            The bounding box to consider when including features in the raw atlas
     */
    public OsmPbfCounter(final AtlasLoadingOption loadingOption, final GeometricSurface boundingBox)
    {
        this.loadingOption = loadingOption;
        this.boundingBox = boundingBox;
    }

    @Override
    public void complete()
    {
        // No-Op
    }

    /**
     * @return the set of all OSM Node identifiers to include in the raw Atlas.
     */
    public Set<Long> getIncludedNodeIdentifiers()
    {
        return this.nodeIdentifiersToInclude;
    }

    /**
     * @return the set of all OSM Way identifiers to include in the raw Atlas.
     */
    public Set<Long> getIncludedWayIdentifiers()
    {
        return this.wayIdentifiersToInclude;
    }

    @Override
    public void initialize(final Map<String, Object> metaData)
    {
        logger.info("Initialized OSM PBF Counter successfully");
    }

    /**
     * @return the number of {@link Line} objects found
     */
    public long lineCount()
    {
        return this.wayIdentifiersToInclude.size();
    }

    /**
     * @return the number of {@link Point} objects found
     */
    public long pointCount()
    {
        return this.nodeIdentifiersToInclude.size();
    }

    @Override
    public void process(final EntityContainer entityContainer)
    {
        final Entity rawEntity = entityContainer.getEntity();

        if (OsmPbfReader.shouldProcessEntity(this.loadingOption, rawEntity))
        {
            // store all node locations for calculating way geometry
            if (rawEntity instanceof Node)
            {
                final Node node = (Node) rawEntity;
                final Location nodeLocation = new Location(Latitude.degrees(node.getLatitude()),
                        Longitude.degrees(node.getLongitude()));
                this.nodeIdentifierToLocation.put(rawEntity.getId(), nodeLocation);
            }
            if (shouldLoadOsmNode(rawEntity))
            {
                // This node is within the boundary or we are using the generated atlas file for QA
                // purposes, bring it in
                this.nodeIdentifiersToInclude.add(rawEntity.getId());
            }
            else if (shouldLoadOsmWay(rawEntity))
            {
                final Way way = (Way) rawEntity;

                if (wayIntersectsBoundary(way))
                {
                    // This way contains at least one shape-point within the given bounding box.
                    // Bring it and all of its nodes in to the atlas.
                    addWayNodes(this.nodeIdentifiersBroughtInByWaysOrRelations, way);
                    this.wayIdentifiersToInclude.add(way.getId());
                }
                else
                {
                    // This way doesn't have any shape-points within the given boundary. Mark it as
                    // a way to exclude so it can be revisited during relation processing
                    this.waysToExclude.put(way.getId(), way);
                }
            }
            else if (shouldLoadOsmRelation(rawEntity))
            {
                final Relation relation = (Relation) rawEntity;
                if (relationContainsMemberWithinBoundary(relation))
                {
                    // Shallow check showed that this relation has a member that is inside our
                    // boundary, mark all members and relation as inside
                    markRelationAndMembersInsideBoundary(relation);
                }
                else
                {
                    // Stage the relation - it might be added later
                    this.stagedRelations.add(relation);
                }
            }
            else if (rawEntity instanceof Bound)
            {
                logger.trace("Encountered PBF Bound {}, skipping over it.", rawEntity.getId());
            }
        }
    }

    /**
     * @return the number of {@link org.openstreetmap.atlas.geography.atlas.items.Relation} objects
     *         found.
     */
    public long relationCount()
    {
        return this.relationIdentifiersToInclude.size();
    }

    @Override
    public void release()
    {
        // Process all staged Relations
        processStagedRelations();

        // Grab any bridges, ferries or other ways that may be outside the immediate boundary
        bringInConnectedOutsideWays();

        // Combine all included nodes into a single collection
        this.nodeIdentifiersToInclude.addAll(this.nodeIdentifiersBroughtInByWaysOrRelations);
        logger.info("Released OSM PBF Counter");
    }

    private void addWayNodes(final Set<Long> set, final Way way)
    {
        way.getWayNodes().forEach(wayNode -> set.add(wayNode.getNodeId()));
    }

    /**
     * Sometimes there are OSM ways (bridges, ferry routes, etc.) that are connected to the road
     * network, but are outside the working country boundary. This method will expand up to
     * {@link #MAXIMUM_NETWORK_EXTENSION} connections past our existing boundary nodes and pull in
     * any ways that meet the above criteria. The while loop terminates if we've exceeded the
     * maximum number of extensions or if we haven't added anything during the previous iteration.
     */
    private void bringInConnectedOutsideWays()
    {
        if (this.loadingOption.isLoadWaysSpanningCountryBoundaries())
        {
            int extensionCounter = 0;
            final Set<Long> alreadyAddedWays = new HashSet<>();
            final AtomicBoolean addedNewEdge = new AtomicBoolean(true);
            while (extensionCounter < MAXIMUM_NETWORK_EXTENSION && addedNewEdge.get())
            {
                logger.trace("Adding connected ways outside boundary pass {}", extensionCounter);
                addedNewEdge.set(false);
                this.waysToExclude.values().stream().filter(this::isHighwayOrFerry)
                        .filter(way -> !alreadyAddedWays.contains(way.getId())).forEach(way ->
                        {
                            final List<WayNode> wayNodes = way.getWayNodes();
                            for (final WayNode wayNode : wayNodes)
                            {
                                final long identifier = wayNode.getNodeId();
                                if (this.nodeIdentifiersBroughtInByWaysOrRelations
                                        .contains(identifier))
                                {
                                    // Add way and its members
                                    logger.trace("Adding connected way with identifier {}",
                                            way.getId());
                                    this.wayIdentifiersToInclude.add(way.getId());
                                    addWayNodes(this.nodeIdentifiersBroughtInByWaysOrRelations,
                                            way);
                                    addedNewEdge.set(true);
                                    alreadyAddedWays.add(way.getId());
                                    break;
                                }
                            }
                        });
                extensionCounter++;
            }
        }
    }

    private boolean isHighwayOrFerry(final Way way)
    {
        final TagMap taggableWay = new TagMap(way.getTags());
        return HighwayTag.isCoreWay(taggableWay) || RouteTag.isFerry(taggableWay);
    }

    private void markRelationAndMembersInsideBoundary(final Relation relation)
    {
        // Add all the members
        for (final RelationMember member : relation.getMembers())
        {
            final EntityType memberType = member.getMemberType();
            final Long memberIdentifier = member.getMemberId();

            if (memberType == EntityType.Node)
            {
                this.nodeIdentifiersToInclude.add(memberIdentifier);
            }
            else if (memberType == EntityType.Way)
            {
                this.wayIdentifiersToInclude.add(memberIdentifier);

                // If this line was originally excluded, bring it in now
                final Way toAdd = this.waysToExclude.get(memberIdentifier);
                if (toAdd != null)
                {
                    addWayNodes(this.nodeIdentifiersBroughtInByWaysOrRelations, toAdd);
                    this.waysToExclude.remove(memberIdentifier, toAdd);
                }
            }
            else if (memberType == EntityType.Relation)
            {
                this.relationIdentifiersToInclude.add(member.getMemberId());
            }
        }

        // Add the relation itself
        this.relationIdentifiersToInclude.add(relation.getId());
    }

    private boolean nodeWithinTargetBoundary(final Node node)
    {
        return this.boundingBox.fullyGeometricallyEncloses(new Location(
                Latitude.degrees(node.getLatitude()), Longitude.degrees(node.getLongitude())));
    }

    private void processStagedRelations()
    {
        List<Relation> stagedRelations = this.stagedRelations;
        int currentStagedRelationSize = this.stagedRelations.size();
        int previousStagedRelationSize = 0;

        // Loop through all staged relations and see if adding any of the staged relations trigger a
        // new relation to be added. Once we have a full cycle where no new relation has been marked
        // as inside the boundary, we can safely conclude that all remaining relations have members
        // outside of the boundary
        while (!this.stagedRelations.isEmpty()
                && currentStagedRelationSize != previousStagedRelationSize)
        {
            final List<Relation> updatedStagedRelations = new ArrayList<>();
            for (final Relation relation : stagedRelations)
            {
                if (relationContainsMemberWithinBoundary(relation))
                {
                    markRelationAndMembersInsideBoundary(relation);
                }
                else
                {
                    updatedStagedRelations.add(relation);
                }
            }
            stagedRelations = updatedStagedRelations;
            previousStagedRelationSize = currentStagedRelationSize;
            currentStagedRelationSize = stagedRelations.size();
        }
    }

    private boolean relationContainsMemberWithinBoundary(final Relation relation)
    {
        // This relation has already been marked as inside
        if (this.relationIdentifiersToInclude.contains(relation.getId()))
        {
            return true;
        }

        // Do a shallow check to see if any members hit
        for (final RelationMember member : relation.getMembers())
        {
            final EntityType memberType = member.getMemberType();

            if (memberType == EntityType.Node
                    && this.nodeIdentifiersToInclude.contains(member.getMemberId()))
            {
                return true;
            }
            else if (memberType == EntityType.Way
                    && this.wayIdentifiersToInclude.contains(member.getMemberId()))
            {
                return true;
            }
            else if (memberType == EntityType.Relation
                    && this.relationIdentifiersToInclude.contains(member.getMemberId()))
            {
                return true;
            }
        }

        // We've looped through every member (excluding nested relations) and found no match
        return false;
    }

    private boolean shouldLoadOsmNode(final Entity entity)
    {
        // For QA purposes, it is necessary to keep nodes that are outside the target boundary.
        // For example, atlas-checks needs to know all the node ids in order to reverse a way and
        // then create an
        // osmChange file for MapRoulette.
        return this.loadingOption.isLoadOsmNode() && entity instanceof Node
                && (nodeWithinTargetBoundary((Node) entity) || this.loadingOption.isKeepAll());
    }

    private boolean shouldLoadOsmRelation(final Entity entity)
    {
        return this.loadingOption.isLoadOsmRelation() && entity instanceof Relation;
    }

    private boolean shouldLoadOsmWay(final Entity entity)
    {
        return this.loadingOption.isLoadOsmWay() && entity instanceof Way;
    }

    private boolean wayIntersectsBoundary(final Way way)
    {
        // CASE 1: Line crosses (or is enclosed by) the shard bounds and has at least one shapepoint
        // within the shard bounds
        ArrayList<Location> wayNodesLocations = new ArrayList<>();
        for (final WayNode node : way.getWayNodes())
        {
            // nodes are processed first so allNodes will contain all node locations
            wayNodesLocations.add(this.nodeIdentifierToLocation.get(node.getNodeId()));
            if (this.nodeIdentifiersToInclude.contains(node.getNodeId()))
            {
                this.wayIdentifiersToInclude.add(way.getId());
                return true;
            }
        }

        // CASE 2: Line crossed the shard but has no shapepoints within it, so we must check for
        // intersections
        wayNodesLocations = wayNodesLocations.stream().filter(node -> node != null)
                .collect(Collectors.toCollection(ArrayList::new));
        if (wayNodesLocations.isEmpty())
        {
            return false;
        }
        final PolyLine wayGeometry = new PolyLine(wayNodesLocations);
        if (wayGeometry.isPoint() || wayGeometry.isEmpty() || wayGeometry.bounds() == null)
        {
            return false;
        }
        // Checking the bounds of the polyline instead of the actual geometry may include some
        // extraneous lines, but is much more performant. The extra lines will be filtered out after
        // the slicing process
        if (this.boundingBox.bounds().overlaps(wayGeometry.bounds()))
        {
            this.wayIdentifiersToInclude.add(way.getId());
            return true;
        }

        // If we reach here, the way doesn't have a node anywhere inside (or on) the given boundary
        return false;
    }
}
