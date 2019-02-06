package org.openstreetmap.atlas.geography.atlas.multi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.ReverseIdentifierFactory;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.atlas.utilities.maps.MultiMapWithSet;
import org.openstreetmap.atlas.utilities.scalars.Ratio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fix issues in a {@link MultiAtlas} that are related to way sectioning at {@link Atlas} border
 * stitching.
 *
 * @author matthieun
 * @author mkalender
 */
public class MultiAtlasBorderFixer implements Serializable
{
    private static final long serialVersionUID = -3774372864489402091L;
    private static final Logger logger = LoggerFactory.getLogger(MultiAtlasBorderFixer.class);

    private static final String MISSING_FIX_ATLAS = "Fix Atlas is not present.";

    // Keeps track of whether border fix process is completed or not
    private boolean isCompleted;

    // Below are all the maps and Atlas responsible for storing references to the edges (and
    // referencing nodes and relations) that were messed up with regards to way sectioning at
    // sub-atlas borders.
    private final MultiMapWithSet<Long, Long> nodeIdentifiersToRemovedInEdges;
    private final MultiMapWithSet<Long, Long> nodeIdentifiersToRemovedOutEdges;
    private final MultiMapWithSet<Long, Long> relationIdentifiersToRemovedEdgeMembers;

    // Set of fixed country OSM identifiers
    private final Set<Long> fixedCountryOsmIdentifiers;
    private transient Optional<Atlas> fixAtlas;
    private final List<Atlas> subAtlases;
    private final HashSet<Long> countryOsmIdentifierWithReverseEdges;
    private final MultiMap<Long, Long> countryOsmIdentifierToEdgeIdentifiers;

    protected MultiAtlasBorderFixer(final List<Atlas> subAtlases,
            final Iterable<Long> edgeIdentifiers)
    {
        // Set fix atlas and initialize maps
        this.fixAtlas = Optional.empty();
        this.nodeIdentifiersToRemovedInEdges = new MultiMapWithSet<>();
        this.nodeIdentifiersToRemovedOutEdges = new MultiMapWithSet<>();
        this.relationIdentifiersToRemovedEdgeMembers = new MultiMapWithSet<>();
        this.fixedCountryOsmIdentifiers = new HashSet<>();

        // Set helper indicator
        this.isCompleted = false;

        // Assign sub atlases
        this.subAtlases = subAtlases;

        // Build helpers country OSM idetifier to list of edge-identifier map
        // And also a set of country OSM identifiers with reverse edges
        this.countryOsmIdentifierToEdgeIdentifiers = new MultiMap<>();
        this.countryOsmIdentifierWithReverseEdges = new HashSet<>();
        edgeIdentifiers.forEach(edgeIdentifier ->
        {
            final long countryOsmIdentifier = getCountryOsmIdentifier(edgeIdentifier);
            if (Edge.isMasterEdgeIdentifier(edgeIdentifier))
            {
                this.countryOsmIdentifierToEdgeIdentifiers.add(countryOsmIdentifier,
                        edgeIdentifier);
            }
            else
            {
                this.countryOsmIdentifierWithReverseEdges.add(countryOsmIdentifier);
            }
        });
    }

    /**
     * Make sure that the edges that are at the sharding borders are not different with regards to
     * way sectioning.
     */
    protected void fixBorderIssues()
    {
        // Start the process
        this.isCompleted = false;
        this.fixAtlas = Optional.empty();

        // Map to keep track of all the OSM identifiers that have been verified
        final HashSet<Long> processedCountryOsmIdentifiers = new HashSet<>();

        // A list to keep new nodes, new edges and a map to hold relations for new edges
        final Map<Long, Node> nodeIdentifiersToNewNodes = new HashMap<>();
        final List<TemporaryEdge> newEdgeList = new ArrayList<>();
        final Set<TemporaryRelation> relationsToUpdate = new HashSet<>();
        final MultiMapWithSet<Long, TemporaryRelationMember> relationMembersToUpdate = new MultiMapWithSet<>();

        // Loop through all the edges
        for (final Entry<Long, List<Long>> countryOsmIdentifierEdgeIdentifiersPair : this.countryOsmIdentifierToEdgeIdentifiers
                .entrySet())
        {
            // Get country and skip if already processed
            final long countryOsmIdentifier = countryOsmIdentifierEdgeIdentifiersPair.getKey();
            if (processedCountryOsmIdentifiers.contains(countryOsmIdentifier))
            {
                continue;
            }

            // Retrieve edges that are part of the same OSM identifier
            final List<Long> edgeIdentifiers = countryOsmIdentifierEdgeIdentifiersPair.getValue();
            final long osmIdentifier = getOsmIdentifier(edgeIdentifiers.get(0));

            // Create a map from edge identifiers to list of edges
            final MultiMap<Long, Edge> identifierToEdgeList = createIdentifierToEdgeMultiMap(
                    edgeIdentifiers);

            // If there are no inconsistent identifiers AND there are no inconsistent edges,
            // mark the edge identifier as "processed" and continue to the next one
            if (!hasInconsistentIdentifier(countryOsmIdentifier, identifierToEdgeList)
                    && !hasInconsistentEdges(identifierToEdgeList)
                    && !hasInconsistentRelations(identifierToEdgeList))
            {
                processedCountryOsmIdentifiers.add(countryOsmIdentifier);
                continue;
            }

            // We are going to fix this road
            // Find out if it has reverse edges
            final boolean hasReverseEdges = this.countryOsmIdentifierWithReverseEdges
                    .contains(countryOsmIdentifier);

            // Create a temporary road per sub atlas
            final List<Edge> edges = identifierToEdgeList.allValues();
            final List<TemporaryRoad> roads = createRoadsPerSubAtlas(countryOsmIdentifier, edges);

            try
            {
                // Check for road consistency
                if (!areRoadsConsistent(roads))
                {
                    logger.warn(
                            "Roads {} generated for OSM way {} were not consistent. Skipping fix!",
                            roads, countryOsmIdentifier);
                    continue;
                }

                // Collect tags and road polyline from roads
                final Map<String, String> tags = collectTags(roads);
                final PolyLine line = createPolyLineFromRoads(roads);

                // Create temporary relations and collect roles per edge
                final Set<TemporaryRelation> candidateRelations = collectRelations(edges);
                final MultiMapWithSet<Long, String> candidateRoles = collectRoles(
                        candidateRelations, osmIdentifier);
                final MultiMapWithSet<Long, TemporaryRelationMember> candidateRelationMembers = new MultiMapWithSet<>();

                // Create a list of nodes processing roads per sub atlas
                final SortedSet<TemporaryOrderedNode> nodes = createTemporaryOrderedNodeList(roads);

                // Temporary data structures to hold new nodes, edges and relations
                final Map<Long, Node> candidateNodeIdentifiersToNewNodes = new HashMap<>();
                final List<TemporaryEdge> candidateEdgeList = new ArrayList<>();

                // Add these nodes to the list of new nodes
                nodes.forEach(temporaryNode -> candidateNodeIdentifiersToNewNodes
                        .put(temporaryNode.getNodeIdentifier(), temporaryNode.getNode()));

                // Let's have an identifier reference for new edges
                long newEdgeIdentifier = getStartIdentifier(countryOsmIdentifier);

                // Process nodes one by one
                TemporaryOrderedNode previousNodePointer = null;
                for (final TemporaryOrderedNode currNodePointer : nodes)
                {
                    if (previousNodePointer != null)
                    {
                        // Previous node
                        final Node previousNode = previousNodePointer.getNode();
                        final Location previousLocation = previousNode.getLocation();
                        final long previousIdentifier = previousNode.getIdentifier();
                        final int previousOccurrenceIndex = previousNodePointer
                                .getOccurrenceIndex();

                        // Current node
                        final Node currentNode = currNodePointer.getNode();
                        final Location currentLocation = currentNode.getLocation();
                        final long currentIdentifier = currentNode.getIdentifier();
                        final int currentOccurrenceIndex = currNodePointer.getOccurrenceIndex();

                        // Create a line from previous node to current node
                        final PolyLine polyLine = line.between(previousLocation,
                                previousOccurrenceIndex, currentLocation, currentOccurrenceIndex);

                        // Increment new edge reference since we are creating a new edge
                        newEdgeIdentifier++;

                        // Create a new edge
                        final TemporaryEdge newEdge = new TemporaryEdge(newEdgeIdentifier, polyLine,
                                previousIdentifier, currentIdentifier, tags);
                        candidateEdgeList.add(newEdge);

                        // Create a reverse edge if needed
                        if (hasReverseEdges)
                        {
                            final TemporaryEdge newReverseEdge = new TemporaryEdge(
                                    newEdge.getReversedIdentifier(), polyLine.reversed(),
                                    currentIdentifier, previousIdentifier, tags);
                            candidateEdgeList.add(newReverseEdge);
                        }

                        // Add edges as relation members to the corresponding relation
                        candidateRoles.forEach((relationIdentifier, roles) ->
                        {
                            if (roles != null && !roles.isEmpty())
                            {
                                for (final String role : roles)
                                {
                                    candidateRelationMembers.add(relationIdentifier,
                                            new TemporaryRelationMember(newEdge.getIdentifier(),
                                                    role, ItemType.EDGE));

                                    if (hasReverseEdges)
                                    {
                                        candidateRelationMembers.add(relationIdentifier,
                                                new TemporaryRelationMember(
                                                        newEdge.getReversedIdentifier(), role,
                                                        ItemType.EDGE));
                                    }
                                }
                            }
                            else
                            {
                                logger.error("Edge {} is missing roles {} in relation {}.",
                                        osmIdentifier, roles, relationIdentifier);
                            }
                        });
                    }

                    previousNodePointer = currNodePointer;
                }

                // Persist candidate nodes, edges
                nodeIdentifiersToNewNodes.putAll(candidateNodeIdentifiersToNewNodes);
                newEdgeList.addAll(candidateEdgeList);

                // Persist relations
                relationsToUpdate.addAll(candidateRelations);

                // perform a set union instead of wiping out the set that is already mapped at the
                // current identifier
                candidateRelationMembers.forEach(
                        (identifier, temporaryRelationMember) -> temporaryRelationMember.forEach(
                                member -> relationMembersToUpdate.add(identifier, member)));

                // Mark old edge nodes/relations to be ignored
                markItemsToBeIgnored(roads, hasReverseEdges);

                // Mark this OSM way fixed
                this.fixedCountryOsmIdentifiers.add(countryOsmIdentifier);
            }
            catch (final Exception e)
            {
                logger.warn("OSM way {} fix for {} is failed.", osmIdentifier, roads, e);
            }
            finally
            {
                // Mark this OSM way processed
                processedCountryOsmIdentifiers.add(countryOsmIdentifier);
            }
        }

        try
        {
            // Fill the relations
            for (final TemporaryRelation relation : relationsToUpdate)
            {
                final Set<TemporaryRelationMember> members = relationMembersToUpdate
                        .get(relation.getIdentifier());
                if (members != null && !members.isEmpty())
                {
                    members.forEach(relation::addMember);
                }
                else
                {
                    logger.error(
                            "Relation {} was identified for an update. However, no fixed edge was added as a member.",
                            relation.getIdentifier());
                }
            }

            // Take all the fixes and apply them
            this.fixAtlas = applyFixesToAtlas(nodeIdentifiersToNewNodes, newEdgeList,
                    relationsToUpdate);

            // Complete the process
            this.isCompleted = true;
        }
        catch (final Exception e)
        {
            logger.error("Border fix process has failed.", e);
        }
    }

    protected Edge fixEdge(final long identifier)
    {
        return this.fixAtlas.orElseThrow(() -> new CoreException(MISSING_FIX_ATLAS))
                .edge(identifier);
    }

    protected Node fixNode(final long identifier)
    {
        return this.fixAtlas.orElseThrow(() -> new CoreException(MISSING_FIX_ATLAS))
                .node(identifier);
    }

    protected Relation fixRelation(final Long identifier)
    {
        return this.fixAtlas.orElseThrow(() -> new CoreException(MISSING_FIX_ATLAS))
                .relation(identifier);
    }

    protected Atlas getFixAtlas()
    {
        return this.fixAtlas.orElseThrow(() -> new CoreException(MISSING_FIX_ATLAS));
    }

    protected MultiMapWithSet<Long, Long> getNodeIdentifiersToRemovedInEdges()
    {
        if (this.isCompleted)
        {
            return this.nodeIdentifiersToRemovedInEdges;
        }

        return new MultiMapWithSet<>();
    }

    protected MultiMapWithSet<Long, Long> getNodeIdentifiersToRemovedOutEdges()
    {
        if (this.isCompleted)
        {
            return this.nodeIdentifiersToRemovedOutEdges;
        }

        return new MultiMapWithSet<>();
    }

    protected MultiMapWithSet<Long, Long> getRelationIdentifiersToRemovedEdgeMembers()
    {
        if (this.isCompleted)
        {
            return this.relationIdentifiersToRemovedEdgeMembers;
        }

        return new MultiMapWithSet<>();
    }

    protected boolean hasFixes()
    {
        return this.fixAtlas.isPresent();
    }

    /**
     * Once an Atlas is fixed, returning the list of edges needs to be filtered out to not return
     * the offending and not fixed edge identifiers.
     *
     * @param identifier
     *            The identifier
     * @return True if it is fixed
     */
    protected boolean isFixEdgeIdentifier(final long identifier)
    {
        final long osmIdentifier = getCountryOsmIdentifier(identifier);
        return this.fixedCountryOsmIdentifiers.contains(osmIdentifier);
    }

    /**
     * @param nodeIdentifier
     *            The node identifier to test for
     * @return True when the node is directly connected to at least one fixed Edge.
     */
    protected boolean nodeIsFixed(final long nodeIdentifier)
    {
        return this.nodeIdentifiersToRemovedInEdges.get(nodeIdentifier) != null
                || this.nodeIdentifiersToRemovedOutEdges.get(nodeIdentifier) != null;
    }

    /**
     * @param relationIdentifier
     *            The relation identifier to query
     * @return True when the relation has members that are fixed.
     */
    protected boolean relationIsFixed(final long relationIdentifier)
    {
        return this.relationIdentifiersToRemovedEdgeMembers.get(relationIdentifier) != null;
    }

    /**
     * Applies fixes to atlas
     *
     * @param newNodes
     *            nodes to be created
     * @param newEdges
     *            edges to be created
     * @param newRelations
     *            relation identifiers to edges to be used for new relation creation
     */
    private Optional<Atlas> applyFixesToAtlas(final Map<Long, Node> newNodes,
            final List<TemporaryEdge> newEdges, final Collection<TemporaryRelation> newRelations)
    {
        // Terminate early if there is nothing to fix
        if (newNodes.isEmpty() && newEdges.isEmpty() && newRelations.isEmpty())
        {
            logger.debug("No fix is applied.");
            return Optional.empty();
        }

        // Create Atlas estimater and builder for fixes
        final AtlasSize fixSizeEstimates = new AtlasSize(newEdges.size(), newNodes.size(), 0L, 0L,
                0L, newRelations.size());
        final PackedAtlasBuilder fixBuilder = new PackedAtlasBuilder()
                .withSizeEstimates(fixSizeEstimates);

        // Populate the fixAtlas with the fixed ways, nodes and relations
        // Node fixes
        for (final Entry<Long, Node> nodeEntry : newNodes.entrySet())
        {
            final Node node = nodeEntry.getValue();
            fixBuilder.addNode(nodeEntry.getKey(), node.getLocation(), node.getTags());
        }

        // Edges
        for (final TemporaryEdge newEdge : newEdges)
        {
            final long newEdgeIdentifier = newEdge.getIdentifier();
            fixBuilder.addEdge(newEdgeIdentifier, newEdge.getPolyLine(), newEdge.getTags());
        }

        // Relations
        for (final TemporaryRelation newRelation : newRelations)
        {
            fixBuilder.addRelation(newRelation.getIdentifier(), newRelation.getOsmIdentifier(),
                    newRelation.getRelationBean(), newRelation.getTags());
        }

        // Get fixed atlas
        final Atlas fixedAtlas = fixBuilder.get();
        logger.debug("Fix atlas meta data: {}", fixedAtlas.metaData());

        return Optional.of(fixedAtlas);
    }

    /**
     * Finds out whether there is a road inconsistency or not. These roads correspond to same OSM
     * way, but they come from different sub atlases. If their shape is different in any way (start
     * location, end location, length etc), that implies a inconsistency.
     *
     * @param roads
     *            Roads to check for consistency
     * @return Indicator whether given roads are consistent or not
     */
    private boolean areRoadsConsistent(final List<TemporaryRoad> roads)
    {
        // Use first road as reference
        final PolyLine referencePolyLine = createPolyLineFromRoad(roads.get(0));

        // Go through the roads and make sure their shape is same
        // Polyline equality check will go through location by location
        for (int i = 1; i < roads.size(); i++)
        {
            final PolyLine otherPolyLine = createPolyLineFromRoad(roads.get(i));
            if (!referencePolyLine.equalsShape(otherPolyLine))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Collects relations from given edges
     *
     * @param edges
     *            Edges to use for relation extraction
     * @return Relations per relation identifier collected from given edges
     */
    private Set<TemporaryRelation> collectRelations(final List<Edge> edges)
    {
        final Set<TemporaryRelation> relations = new HashSet<>();
        edges.forEach(edge -> edge.relations()
                .forEach(relation -> relations.add(new TemporaryRelation(relation))));
        return relations;
    }

    /**
     * Collects roles from given candidate relations for given OSM identifier
     *
     * @param candidateRelations
     *            Candidate relations for role extraction
     * @param osmIdentifier
     *            OSM identifier to check for role extraction
     * @return Roles collected from given edges
     */
    private MultiMapWithSet<Long, String> collectRoles(
            final Set<TemporaryRelation> candidateRelations, final long osmIdentifier)
    {
        final MultiMapWithSet<Long, String> roles = new MultiMapWithSet<>();
        for (final TemporaryRelation relation : candidateRelations)
        {
            for (final RelationMember member : relation.getOldMembers())
            {
                final long memberIdentifier = member.getEntity().getOsmIdentifier();
                if (memberIdentifier == osmIdentifier)
                {
                    try
                    {
                        final long relationIdentifier = relation.getIdentifier();
                        final String role = member.getRole();
                        roles.add(relationIdentifier, role);
                    }
                    catch (final Exception error)
                    {
                        throw new CoreException("Error adding in roles: {}", relation, error);
                    }
                }
            }
        }
        return roles;
    }

    /**
     * Collects tags from given roads' edges. Theoretically edges correspond to same OSM way, so
     * they should have the same tags.
     *
     * @param roads
     *            Roads to use for tag extraction
     * @return Tags
     */
    private Map<String, String> collectTags(final List<TemporaryRoad> roads)
    {
        final Map<String, String> tags = new HashMap<>();
        roads.forEach(road -> road.getMembers().forEach(edge -> tags.putAll(edge.getTags())));
        return tags;
    }

    /**
     * Creates a mapping from edge identifiers to edges themselves. This map will be useful to check
     * if two edges with the same identifier are geometrically equal or not.
     *
     * @param edgeIdentifiers
     *            List of edge identifiers
     * @return Map from edge identifiers to list of edges
     */
    private MultiMap<Long, Edge> createIdentifierToEdgeMultiMap(final List<Long> edgeIdentifiers)
    {
        final MultiMap<Long, Edge> edgeIdToEdges = new MultiMap<>();

        // Go through edge identifiers one by one
        for (final Long candidateIdentifier : edgeIdentifiers)
        {
            for (final Atlas subAtlas : this.subAtlases)
            {
                // Try to retrieve given edge from sub atlas
                // If it does not exist in sub atlas, skip adding
                final Edge candidate = subAtlas.edge(candidateIdentifier);
                if (candidate != null)
                {
                    edgeIdToEdges.add(candidate.getIdentifier(), candidate);
                }
            }
        }

        return edgeIdToEdges;
    }

    private PolyLine createPolyLineFromRoad(final TemporaryRoad road)
    {
        // A set of locations to collect from the road
        final SortedSet<TemporaryOrderedLocation> temporaryNodeSet = createTemporaryOrderedLocations(
                road);

        return new PolyLine(temporaryNodeSet.stream().map(TemporaryOrderedLocation::getLocation)
                .collect(Collectors.toList()));
    }

    private PolyLine createPolyLineFromRoads(final List<TemporaryRoad> roads)
    {
        // A set of locations to collect from roads
        final SortedSet<TemporaryOrderedLocation> temporaryNodeSet = new TreeSet<>();

        // Go through the roads one by one
        for (final TemporaryRoad road : roads)
        {
            temporaryNodeSet.addAll(createTemporaryOrderedLocations(road));
        }

        // return a polyline from collected locations
        return new PolyLine(temporaryNodeSet.stream().map(TemporaryOrderedLocation::getLocation)
                .collect(Collectors.toList()));
    }

    /**
     * Create a road for given OSM identifier per sub atlas given edge list. Ignore empty roads,
     * because not all sub atlases will have a road for an OSM way.
     *
     * @param osmIdentifier
     *            OSM identifier to used as reference
     * @param edges
     *            Edge list
     * @return List of {@link TemporaryRoad}s
     */
    private List<TemporaryRoad> createRoadsPerSubAtlas(final long osmIdentifier,
            final List<Edge> edges)
    {
        final Map<UUID, TemporaryRoad> edgesPerSubAtlas = new HashMap<>();
        for (final Atlas subAtlas : this.subAtlases)
        {
            edgesPerSubAtlas.put(subAtlas.getIdentifier(),
                    new TemporaryRoad(subAtlas, osmIdentifier));
        }

        for (final Edge edge : edges)
        {
            edgesPerSubAtlas.get(edge.getAtlas().getIdentifier()).add(edge);
        }

        // Filter out empty roads
        edgesPerSubAtlas.values().removeIf(road -> road.getMembers().isEmpty());

        // Return the rest
        return new ArrayList<>(edgesPerSubAtlas.values());
    }

    private SortedSet<TemporaryOrderedLocation> createTemporaryOrderedLocations(
            final TemporaryRoad road)
    {
        // A set of locations to collect from roads
        final SortedSet<TemporaryOrderedLocation> temporaryNodeSet = new TreeSet<>();

        // Map containing the number of times a location has been seen, to know what occurence index
        // it is at. A location that is present twice will be seen 0, then one, then two times.
        final Map<String, Integer> locationToTimesSeenSoFar = new HashMap<>();
        final PolyLine line = new PolyLine(road.locations());

        // Loop through shape points (locations) and calculate number of occurrences
        for (final Location location : road.locations())
        {
            final String locationIdentifier = location.toString();
            // Initialize the number of times a location has been seen to 0
            locationToTimesSeenSoFar.putIfAbsent(locationIdentifier, 0);
        }

        // Loop through shape points (locations), and calculate how many times each has been seen
        // and the offset from the start of the polyLine.
        for (final Location location : road.locations())
        {
            final String locationIdentifier = location.toString();
            // Increment the number of times this location has been seen
            locationToTimesSeenSoFar.put(locationIdentifier,
                    locationToTimesSeenSoFar.get(locationIdentifier) + 1);
            // The number of times it has been seen. The first time, it will be 1.
            final int locationTimesSeenSoFar = locationToTimesSeenSoFar.get(locationIdentifier);
            // The index at which this location occured.
            final int occurrenceIndex = locationTimesSeenSoFar - 1;
            final Ratio offset = line.offsetFromStart(location, occurrenceIndex);

            // Create new ordered location and add it to the set
            final TemporaryOrderedLocation newTemporaryNode = new TemporaryOrderedLocation(location,
                    offset, occurrenceIndex);
            if (!temporaryNodeSet.contains(newTemporaryNode))
            {
                temporaryNodeSet.add(newTemporaryNode);
            }
        }

        return temporaryNodeSet;
    }

    /**
     * Creates a sorted set of {@link TemporaryOrderedNode}s from road list. These roads are same
     * roads coming from different sub atlases. However, they have different segments. Here we are
     * trying to merge roads and come up with an ordered node list.
     *
     * @param roads
     *            Roads to extract nodes
     * @return Sorted set of {@link TemporaryOrderedNode}s
     */
    private SortedSet<TemporaryOrderedNode> createTemporaryOrderedNodeList(
            final List<TemporaryRoad> roads)
    {
        // A set of nodes to collect from roads
        // A node id could have more than one nodes, if that is the case, that means road is self
        // intersecting
        final SortedSet<TemporaryOrderedNode> temporaryNodeSet = new TreeSet<>();

        // Go through the roads one by one
        for (final TemporaryRoad road : roads)
        {
            // Turn road into a polyline through it's route
            final PolyLine line = road.getRoute().asPolyLine();

            // Create node identifier to an occurrence/time seen so far mappings
            // These two maps will be used to calculate occurrence index
            final Map<Long, Integer> nodeIdentifierToOccurenceMap = new HashMap<>();
            final Map<Long, Integer> nodeIdentifierToTimesSeenSoFar = new HashMap<>();

            // Process nodes and calculate number of occurrences
            for (final Node node : road.getRoute().nodes())
            {
                final long nodeIdentifier = node.getIdentifier();
                final int count = nodeIdentifierToOccurenceMap.containsKey(nodeIdentifier)
                        ? nodeIdentifierToOccurenceMap.get(nodeIdentifier) + 1 : 1;
                nodeIdentifierToOccurenceMap.put(nodeIdentifier, count);
                nodeIdentifierToTimesSeenSoFar.putIfAbsent(nodeIdentifier, 0);
            }

            // Process nodes, calculate occurrence index, their offset from start
            for (final Node node : road.getRoute().nodes())
            {
                final long nodeIdentifier = node.getIdentifier();
                nodeIdentifierToTimesSeenSoFar.put(nodeIdentifier,
                        nodeIdentifierToTimesSeenSoFar.get(nodeIdentifier) + 1);
                final int occurrenceIndex = nodeIdentifierToOccurenceMap.get(nodeIdentifier)
                        - nodeIdentifierToTimesSeenSoFar.get(nodeIdentifier);
                final Ratio offset = line.offsetFromStart(node.getLocation(), occurrenceIndex);

                // Create new node and add it to the set
                final TemporaryOrderedNode newTemporaryNode = new TemporaryOrderedNode(node, offset,
                        occurrenceIndex);
                if (!temporaryNodeSet.contains(newTemporaryNode))
                {
                    temporaryNodeSet.add(newTemporaryNode);
                }
                else if (nodeIdentifier != newTemporaryNode.getNodeIdentifier())
                {
                    logger.warn(
                            "Node {} (vs a node with id {}) is appearing in different subatlases"
                                    + " at the same location.",
                            newTemporaryNode, nodeIdentifier);
                }
            }
        }

        return temporaryNodeSet;
    }

    private long getCountryOsmIdentifier(final long edgeIdentifier)
    {
        return new ReverseIdentifierFactory().getCountryOsmIdentifier(edgeIdentifier);
    }

    private long getOsmIdentifier(final long edgeIdentifier)
    {
        return new ReverseIdentifierFactory().getOsmIdentifier(edgeIdentifier);
    }

    private long getStartIdentifier(final long osmIdentifier)
    {
        return new ReverseIdentifierFactory().getStartIdentifier(osmIdentifier);
    }

    /**
     * Compare edges that have the same identifier. If two edges have the same identifier, but do
     * not share the same geometry, that means we have inconsistent edges
     *
     * @param edgesPerEdgeIdentifier
     *            A mapping from edge identifiers to list of edges for that identifier
     * @return Indicator whether we have inconsistent edges or not
     */
    private boolean hasInconsistentEdges(final MultiMap<Long, Edge> edgesPerEdgeIdentifier)
    {
        for (final List<Edge> edgeListForSameIdentifier : edgesPerEdgeIdentifier.values())
        {
            // Use first edge as reference
            final Edge referenceEdge = edgeListForSameIdentifier.get(0);
            final PolyLine referenceGeometry = referenceEdge.asPolyLine();

            // Go through edges
            // These edges have the same id, but they come from different sub atlases
            // If they are equal, that implies consistency issues
            for (int i = 1; i < edgeListForSameIdentifier.size(); i++)
            {
                final PolyLine similarGeometry = edgeListForSameIdentifier.get(i).asPolyLine();
                if (!referenceGeometry.equals(similarGeometry))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Use given OSM identifier as reference and check if there is an edge id same as OSM identifier
     * (which implies that a way-sectioning did not happen). Still if given map has more than one
     * edge identifier, that means there is an identifier inconsistency
     *
     * @param osmIdentifier
     *            OSM identifier to use as reference
     * @param edgesPerEdgeIdentifier
     *            A mapping from edge identifiers to list of edges for that identifier
     * @return Indicator whether we have inconsistent identifier or not
     */
    private boolean hasInconsistentIdentifier(final long osmIdentifier,
            final MultiMap<Long, Edge> edgesPerEdgeIdentifier)
    {
        // Convert OSM identifier to edge identifier
        final long startEdgeIdentifier = getStartIdentifier(osmIdentifier);

        // Go though edge identifiers and compare them
        for (final Long referenceIdentifier : edgesPerEdgeIdentifier.keySet())
        {
            // We found an inconsistency
            // First condition implies way-sectioning did not happen
            // Second implies otherwise
            if (referenceIdentifier == startEdgeIdentifier && edgesPerEdgeIdentifier.size() > 1)
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if identical edges in the new {@link MultiAtlas} have consistent parent relations. It
     * is possible the the same {@link Edge} may have a different set of parent relations depending
     * on which subatlas is was pulled from. In this case, we want to detect this so we can unify
     * the sets.
     *
     * @param identifierToEdgeList
     *            A mapping from edge identifiers to list of edges with that identifier
     * @return Indicator whether relations are consistent across identical edges
     */
    private boolean hasInconsistentRelations(final MultiMap<Long, Edge> identifierToEdgeList)
    {
        for (final Long edgeListForSameIdentifier : identifierToEdgeList.keySet())
        {
            final List<Edge> valuesForCurrentKey = identifierToEdgeList
                    .get(edgeListForSameIdentifier);
            final Edge firstEdge = valuesForCurrentKey.get(0);
            final Set<Relation> masterRelations = firstEdge.relations();

            for (int index = 1; index < valuesForCurrentKey.size(); index++)
            {
                final Edge comparisonEdge = valuesForCurrentKey.get(index);
                final Set<Relation> candidateRelations = comparisonEdge.relations();
                if (!Objects.equals(masterRelations, candidateRelations))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Marks node-to-edge connections, relations (that edges in given roads are part of) to be
     * ignored after the fix. MultiAtlas will act like these relations never existed.
     *
     * @param roads
     *            Roads to get edges
     * @param hasReverseEdges
     *            Indicator whether given roads has reverse edges as well
     */
    private void markItemsToBeIgnored(final List<TemporaryRoad> roads,
            final boolean hasReverseEdges)
    {
        for (final TemporaryRoad road : roads)
        {
            for (final Edge edge : road.getRoute())
            {
                final Long edgeIdentifier = edge.getIdentifier();

                // Populate the identifiers to remove
                this.nodeIdentifiersToRemovedInEdges.add(edge.end().getIdentifier(),
                        edgeIdentifier);
                this.nodeIdentifiersToRemovedOutEdges.add(edge.start().getIdentifier(),
                        edgeIdentifier);
                edge.relations().forEach(relation -> this.relationIdentifiersToRemovedEdgeMembers
                        .add(relation.getIdentifier(), edgeIdentifier));

                // Do again for reversed edges
                if (hasReverseEdges)
                {
                    final Long reversedEdgeIdentifier = -edge.getIdentifier();
                    this.nodeIdentifiersToRemovedInEdges.add(edge.start().getIdentifier(),
                            reversedEdgeIdentifier);
                    this.nodeIdentifiersToRemovedOutEdges.add(edge.end().getIdentifier(),
                            reversedEdgeIdentifier);
                    edge.relations()
                            .forEach(relation -> this.relationIdentifiersToRemovedEdgeMembers
                                    .add(relation.getIdentifier(), reversedEdgeIdentifier));
                }
            }
        }
    }
}
