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
import java.util.UUID;

import org.openstreetmap.atlas.exception.CoreException;
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
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.atlas.utilities.maps.MultiMapWithSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Fix {@link Relation} inconsistencies in a {@link MultiAtlas}. The inconsistencies are a result of
 * each underlying {@link Atlas} only having a portion of the entire {@link Relation}. All other
 * inconsistencies - geometry or identifier are treated as errors in the way-sectioning process.
 *
 * @author matthieun
 * @author mkalender
 * @author mgostintsev
 */
public class MultiAtlasRelationAggregator implements Serializable
{
    private static final long serialVersionUID = -3774372864489402091L;
    private static final Logger logger = LoggerFactory
            .getLogger(MultiAtlasRelationAggregator.class);

    private static final String MISSING_FIX_ATLAS = "Fix Atlas is not present.";

    // Keeps track of whether border fix process is completed or not
    private boolean isCompleted;

    // Below are all the maps and Atlas responsible for storing references to the edges (and
    // referencing nodes and relations) that were affected by the relation aggregation
    private final MultiMapWithSet<Long, Long> nodeIdentifiersToRemovedInEdges;
    private final MultiMapWithSet<Long, Long> nodeIdentifiersToRemovedOutEdges;
    private final MultiMapWithSet<Long, Long> relationIdentifiersToRemovedEdgeMembers;

    // Set of fixed country OSM identifiers
    private final Set<Long> fixedCountryOsmIdentifiers;
    private transient Optional<Atlas> fixAtlas;
    private final List<Atlas> subAtlases;
    private final MultiMap<Long, Long> countryOsmIdentifierToEdgeIdentifiers;

    protected MultiAtlasRelationAggregator(final List<Atlas> subAtlases,
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

        // Build helpers country OSM identifier to list of edge-identifier map
        this.countryOsmIdentifierToEdgeIdentifiers = new MultiMap<>();
        edgeIdentifiers.forEach(edgeIdentifier ->
        {
            final long countryOsmIdentifier = getCountryOsmIdentifier(edgeIdentifier);
            if (Edge.isMasterEdgeIdentifier(edgeIdentifier))
            {
                this.countryOsmIdentifierToEdgeIdentifiers.add(countryOsmIdentifier,
                        edgeIdentifier);
            }
        });
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

    /**
     * Make sure that relations spanning {@link Shard} boundaries are returning a consistent
     * representation of their members and vice versa.
     */
    protected void fixRelationInconsistencies()
    {
        // Start the process
        this.isCompleted = false;
        this.fixAtlas = Optional.empty();

        // Map to keep track of all the OSM identifiers that have been verified
        final HashSet<Long> processedCountryOsmIdentifiers = new HashSet<>();

        // A list to keep new nodes, new edges and a map to hold relations for new edges
        final Map<Long, Node> nodeIdentifiersToNewNodes = new HashMap<>();
        final List<TemporaryEdge> edgesToUpdate = new ArrayList<>();
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

            // We're operating under the assumption that way-sectioning produces consistent
            // identifiers and geometry. If this isn't the case, the multiAtlas won't build until
            // the way-sectioning is fixed.
            if (hasInconsistentIdentifier(countryOsmIdentifier, identifierToEdgeList)
                    || hasInconsistentEdges(identifierToEdgeList))
            {
                throw new CoreException(
                        "Inconsistent edges for {} - way-sectioning issue detected! Edges: {}",
                        countryOsmIdentifier, identifierToEdgeList);
            }

            // If there are no inconsistent relations, mark the edge identifier as "processed" and
            // continue to the next one
            if (!hasInconsistentRelations(identifierToEdgeList))
            {
                processedCountryOsmIdentifiers.add(countryOsmIdentifier);
                continue;
            }

            // Create a temporary road per sub atlas
            final List<Edge> edges = identifierToEdgeList.allValues();
            final List<TemporaryRoad> roads = createRoadsPerSubAtlas(countryOsmIdentifier, edges);

            try
            {
                // Create temporary relations and collect roles per edge
                final Set<TemporaryRelation> candidateRelations = collectRelations(edges);
                final MultiMapWithSet<Long, String> candidateRoles = collectRoles(
                        candidateRelations, osmIdentifier);
                final MultiMapWithSet<Long, TemporaryRelationMember> candidateRelationMembers = new MultiMapWithSet<>();

                // Add edges as relation members to the corresponding relation
                candidateRoles.forEach((relationIdentifier, roles) ->
                {
                    if (roles != null && !roles.isEmpty())
                    {
                        for (final String role : roles)
                        {
                            for (final Edge edge : edges)
                            {
                                candidateRelationMembers.add(relationIdentifier,
                                        new TemporaryRelationMember(edge.getIdentifier(), role,
                                                ItemType.EDGE));

                                if (edge.hasReverseEdge())
                                {
                                    candidateRelationMembers.add(relationIdentifier,
                                            new TemporaryRelationMember(
                                                    edge.reversed().get().getIdentifier(), role,
                                                    ItemType.EDGE));
                                }
                            }
                        }
                    }
                    else
                    {
                        logger.error("Edge {} is missing roles {} in relation {}.", osmIdentifier,
                                roles, relationIdentifier);
                    }
                });

                // Because relations were updated, we need to create an "updated" version of the
                // affected Edge and Nodes.
                if (!candidateRelationMembers.isEmpty())
                {
                    // Create a temporary edge that's a single unified representation of all the
                    // consistent edges from the underlying sub atlases - we can do this because
                    // it's already been verified that all edges with this identifier are
                    // consistent.
                    final Edge firstEdge = edges.get(0);
                    final TemporaryEdge unifiedEdge = new TemporaryEdge(firstEdge.getIdentifier(),
                            firstEdge.asPolyLine(), firstEdge.getTags());
                    edgesToUpdate.add(unifiedEdge);

                    if (firstEdge.reversed().isPresent())
                    {
                        final TemporaryEdge reversedUnifiedEdge = new TemporaryEdge(
                                firstEdge.reversed().get().getIdentifier(),
                                firstEdge.asPolyLine().reversed(), firstEdge.getTags());
                        edgesToUpdate.add(reversedUnifiedEdge);
                    }

                    // Update the start and end nodes for this edge
                    final Node startNode = firstEdge.start();
                    final Node endNode = firstEdge.end();
                    nodeIdentifiersToNewNodes.put(startNode.getIdentifier(), startNode);
                    nodeIdentifiersToNewNodes.put(endNode.getIdentifier(), endNode);
                }

                // Persist relations
                relationsToUpdate.addAll(candidateRelations);

                // Perform a set union instead of wiping out the set that is already mapped at the
                // current identifier
                candidateRelationMembers.forEach(
                        (identifier, temporaryRelationMember) -> temporaryRelationMember.forEach(
                                member -> relationMembersToUpdate.add(identifier, member)));

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
            this.fixAtlas = applyFixesToAtlas(nodeIdentifiersToNewNodes, edgesToUpdate,
                    relationsToUpdate);

            // Complete the process
            this.isCompleted = true;
        }
        catch (final Exception e)
        {
            logger.error("Border fix process has failed.", e);
        }
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

}
