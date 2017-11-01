package org.openstreetmap.atlas.geography.atlas.items.complex.bignode;

import static org.openstreetmap.atlas.tags.names.NameFinder.STANDARD_TAGS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Route;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.BigNode.Type;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.LocationIterableProperties;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.JunctionTag;
import org.openstreetmap.atlas.tags.names.NameFinder;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.direction.EdgeDirectionComparator;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Sets;

/**
 * This finds all the {@link BigNode}s in an {@link Atlas} (including dual carriage way junctions
 * and simple intersections).
 *
 * @author sid
 * @author matthieun
 */
public class BigNodeFinder implements Finder<BigNode>
{
    /**
     * An intermediate {@link BigNode} candidate that is easily merged with other {@link BigNode}s
     *
     * @author Sid
     */
    public static final class BigNodeCandidate implements Comparable<BigNodeCandidate>, Serializable
    {
        private static final long serialVersionUID = 7225634482602225746L;

        private final Set<Long> nodeIdentifiers;

        public static BigNodeCandidate from(final Set<Node> nodes)
        {
            return new BigNodeCandidate(
                    nodes.stream().map(Node::getIdentifier).collect(Collectors.toSet()));
        }

        public BigNodeCandidate()
        {
            this.nodeIdentifiers = new TreeSet<>();
        }

        public BigNodeCandidate(final Set<Long> nodeIds)
        {
            this.nodeIdentifiers = new TreeSet<>(nodeIds);
        }

        @Override
        public int compareTo(final BigNodeCandidate bigNodeCandidate)
        {
            return new CompareToBuilder()
                    .append(this.nodeIdentifiers, bigNodeCandidate.nodeIdentifiers).toComparison();
        }

        @Override
        public boolean equals(final Object other)
        {
            if (other instanceof BigNodeCandidate)
            {
                final BigNodeCandidate that = (BigNodeCandidate) other;
                if (this.nodeIdentifiers.size() == that.nodeIdentifiers.size())
                {
                    return new EqualsBuilder().append(this.nodeIdentifiers, that.nodeIdentifiers)
                            .isEquals();
                }
            }
            return false;
        }

        public Set<Long> getNodeIdentifiers()
        {
            return this.nodeIdentifiers;
        }

        public long getSourceNodeIdentifier()
        {
            if (!this.nodeIdentifiers.isEmpty())
            {
                return this.nodeIdentifiers.iterator().next();
            }
            throw new IllegalArgumentException(
                    "Could not get source node identifier since nodesIdentifiers are empty");
        }

        @Override
        public int hashCode()
        {
            return new HashCodeBuilder().append(this.nodeIdentifiers).hashCode();
        }

        public void merge(final BigNodeCandidate mergeBigNodeCandidate)
        {
            this.nodeIdentifiers.addAll(mergeBigNodeCandidate.nodeIdentifiers);
        }
    }

    /**
     * @author Sid
     */
    public class BigNodeIterable implements Iterable<BigNode>
    {
        private final BigNodeIterator iterator;

        public BigNodeIterable(final BigNodeIterator iterator)
        {
            this.iterator = iterator;
        }

        @Override
        public Iterator<BigNode> iterator()
        {
            return this.iterator;
        }
    }

    /**
     * The iterator avoids pre-computation of all the {@link BigNode}s. We maintain only the
     * dualCarriageWay {@link BigNode}s to reduce memory.
     *
     * @author Sid
     */
    public class BigNodeIterator extends AbstractIterator<BigNode>
    {
        private final Iterator<BigNodeCandidate> bigNodeCandidateIterator;
        private final Atlas atlas;
        private final Iterator<Node> nodeIterator;

        // We maintain a set of processed nodeIds to avoid returning duplicates
        private final Set<Long> bigNodeIdentifiers;

        public BigNodeIterator(final Atlas atlas, final Set<BigNodeCandidate> bigNodeCandidates)
        {
            this.atlas = atlas;
            this.bigNodeCandidateIterator = bigNodeCandidates.iterator();
            this.nodeIterator = this.atlas.nodes().iterator();
            this.bigNodeIdentifiers = new HashSet<>();
        }

        @Override
        protected BigNode computeNext()
        {
            while (this.bigNodeCandidateIterator.hasNext())
            {
                final BigNodeCandidate bigNodeCandidate = this.bigNodeCandidateIterator.next();

                // Sorting to ensure deterministic id
                final Set<Node> nodes = new TreeSet<>(
                        (final Node node1, final Node node2) -> new CompareToBuilder()
                                .append(node1.getIdentifier(), node2.getIdentifier())
                                .toComparison());
                bigNodeCandidate.nodeIdentifiers
                        .forEach(nodeIdentifier -> nodes.add(this.atlas.node(nodeIdentifier)));
                if (!nodes.isEmpty())
                {
                    final Node sourceNode = nodes.iterator().next();
                    final BigNode bigNode = new BigNode(sourceNode, nodes, Type.DUAL_CARRIAGEWAY);
                    nodes.stream()
                            .forEach(node -> this.bigNodeIdentifiers.add(node.getIdentifier()));
                    return bigNode;
                }
            }
            while (this.nodeIterator.hasNext())
            {
                // Next, look for simple intersections
                final Node candidateNode = this.nodeIterator.next();
                if (!this.bigNodeIdentifiers.contains(candidateNode.getIdentifier()))
                {
                    if (candidateNode.connectedEdges().stream()
                            .anyMatch(HighwayTag::isCarNavigableHighway))
                    {
                        this.bigNodeIdentifiers.add(candidateNode.getIdentifier());
                        return new BigNode(candidateNode);
                    }
                }
            }
            // We reached the end of the list
            return endOfData();
        }
    }

    /**
     * Minimum number of master edges involved in dual carriage way intersection
     */
    private static final int MIN_MASTER_EDGE_DUAL_CARRIAGEWAY_INTERSECTION = 2;
    /**
     * LEVENSHTEIN limit is used for fuzzy name match
     */
    private static final int LEVENSHTEIN_DISTANCE_THRESHOLD = 1;
    private static final Logger logger = LoggerFactory.getLogger(BigNodeFinder.class);

    /**
     * The limits below are used as upper bounds for length of the junction edge. The limits for the
     * junction edge are based not on road classification of the junction edge but the highest road
     * class of the connected edges of the junction edge. These limits might need some tuning. In
     * some cases like TestCase 6, the junction edge is not straight at an angle (hence longer
     * length). TODO : Experiment with max length of junction Route?
     */
    private static final Distance SEARCH_RADIUS_MOTORWAY = Distance.meters(70);
    private static final Distance SEARCH_RADIUS_TRUNK = Distance.meters(60);
    private static final Distance SEARCH_RADIUS_PRIMARY = Distance.meters(50);
    private static final Distance SEARCH_RADIUS_SECONDARY = Distance.meters(40);
    private static final Distance SEARCH_RADIUS_TERTIARY = Distance.meters(35);
    private static final Distance SEARCH_RADIUS_RESIDENTIAL = Distance.meters(25);
    /**
     * This is maximum number of edges in dual carriage way route and is used as safety threshold to
     * prevent bad edge cases while constructing the big node
     */
    private static final int MAXIMUM_DUAL_CARRIAGEWAY_ROUTE_SIZE = 10;

    private final EdgeDirectionComparator edgeDirectionComparator = new EdgeDirectionComparator();

    private final NameFinder nameFinder = new NameFinder().withTags(STANDARD_TAGS);

    @Override
    public Iterable<BigNode> find(final Atlas atlas)
    {
        /*
         * Maintain a set of junction edges that are already part of a big Node. Sort the
         * junctionEdge ids for maintaining order consistency
         */
        final Set<Long> junctionEdgeIds = new TreeSet<>();

        /*
         * For junctionRouteEdges (sequence of edges that for a junctionRoute), maintain the first
         * edge
         */
        final Set<Long> junctionRouteEdgeIds = new TreeSet<>();

        // First pass through edges
        for (final Edge candidateEdge : atlas.edges(this::isCandidateJunctionEdge))
        {
            // Check if the candidate edge is already part of another big Node
            if (!junctionEdgeIds.contains(candidateEdge.getIdentifier()))
            {
                if (isDualCarriageWayJunctionEdge(candidateEdge))
                {
                    junctionEdgeIds.add(candidateEdge.getIdentifier());
                }
                else
                {
                    // Expand Junction Edge to Junction Route before checking for Dual Carriage way
                    // intersection
                    final Optional<Route> junctionRoute = isDualCarriageWayJunctionRoute(
                            Route.forEdge(candidateEdge));

                    junctionRoute.ifPresent(route ->
                    {
                        final Edge startEdge = route.start();
                        junctionRouteEdgeIds.add(startEdge.getIdentifier());
                        route.forEach(edge -> junctionEdgeIds.add(edge.getIdentifier()));
                    });
                }
            }
        }

        final Map<Long, BigNodeCandidate> nodeIdToBigNodeCandidateMap = new HashMap<>();
        /*
         * There are some cases where a junction edge is bidirectional. We do not explicitly remove
         * the negative Edge in those cases. This may have to be revisited
         */
        while (!junctionEdgeIds.isEmpty())
        {
            final Long candidateEdgeId = junctionRouteEdgeIds.isEmpty()
                    ? junctionEdgeIds.iterator().next() : junctionRouteEdgeIds.iterator().next();
            final Edge candidate = atlas.edge(candidateEdgeId);
            final Route mergedCandidate = mergeJunctionEdges(Route.forEdge(candidate),
                    junctionEdgeIds);
            logger.debug("Merged bigNode Route : {}. Number of Edges : {}", mergedCandidate,
                    mergedCandidate.size());

            /*
             * mergedRoutes are formed after strict connectivity checks. But mergedRoutes can share
             * same node. Each node must have 1-1 mapping with a big node. So we merge routes into
             * same big node if they share a node
             */
            final Set<Node> nodes = new HashSet<>();
            mergedCandidate.forEach(edge -> nodes.addAll(edge.connectedNodes()));
            final Set<BigNodeCandidate> bigNodesMergeCandidates = new HashSet<>();
            for (final Node node : nodes)
            {
                if (nodeIdToBigNodeCandidateMap.containsKey(node.getIdentifier()))
                {
                    bigNodesMergeCandidates
                            .add(nodeIdToBigNodeCandidateMap.get(node.getIdentifier()));
                }
            }
            final BigNodeCandidate bigNodeCandidate = BigNodeCandidate.from(nodes);
            for (final BigNodeCandidate mergeBigNode : bigNodesMergeCandidates)
            {
                bigNodeCandidate.merge(mergeBigNode);
            }
            for (final Long nodeIdentifier : bigNodeCandidate.getNodeIdentifiers())
            {
                nodeIdToBigNodeCandidateMap.put(nodeIdentifier, bigNodeCandidate);
            }
            // Successfully added the bigNodeCandidate, can safely remove the junction edge
            mergedCandidate.forEach(edge -> junctionEdgeIds.remove(edge.getIdentifier()));
            mergedCandidate.forEach(edge -> junctionRouteEdgeIds.remove(edge.getIdentifier()));
        }

        final Set<BigNodeCandidate> bigNodeCandidateSet = new HashSet<>(
                nodeIdToBigNodeCandidateMap.values());
        logger.info(
                "Atlas has {} DualCarriageWay bigNodes with {} subNodes. Total Number of Big Nodes (including Simple Intersections) : {}",
                bigNodeCandidateSet.size(), nodeIdToBigNodeCandidateMap.keySet().size(),
                atlas.numberOfNodes() - nodeIdToBigNodeCandidateMap.keySet().size()
                        + bigNodeCandidateSet.size());
        final BigNodeIterator bigNodeIterator = new BigNodeIterator(atlas, bigNodeCandidateSet);
        return new BigNodeIterable(bigNodeIterator);
    }

    /**
     * Find the big nodes and save them as geojson in a resource
     *
     * @param atlas
     *            The atlas to look at
     * @param writableResource
     *            Where to save the geojson
     */
    public void findAndSaveBigNodesAsGeoJson(final Atlas atlas,
            final WritableResource writableResource)
    {
        final List<LocationIterableProperties> features = new ArrayList<>();
        Iterables.stream(this.find(atlas)).map(BigNode::asGeoJsonBigNode).forEach(features::add);
        new GeoJsonBuilder().create(features).save(writableResource);
    }

    /**
     * Find restricted paths of all bignodes and save them as geojson in a resource
     *
     * @param atlas
     *            The atlas to look at
     * @param writableResource
     *            Where to save the geojson
     */
    public void findAndSaveRestrictedPathsAsGeoJson(final Atlas atlas,
            final WritableResource writableResource)
    {
        final List<LocationIterableProperties> features = new ArrayList<>();
        Iterables.stream(this.find(atlas)).flatMap(BigNode::asGeoJsonRestrictedPath)
                .forEach(features::add);
        new GeoJsonBuilder().create(features).save(writableResource);
    }

    /**
     * Identify {@link Edge} name matches when both {@link Edge}s have same names. When strict mode
     * parameter is set to {@code true}, both edge names must be non-empty and must match. Instead
     * of exact match, we allow for fuzzy match. TODO : Improve normalization (remove cardinal
     * Directions before match?)
     */
    private boolean edgeNameFuzzyMatch(final Edge edgeA, final Edge edgeB, final boolean strictMode)
    {
        final Optional<String> edgeAName = this.nameFinder.best(edgeA);
        final Optional<String> edgeBName = this.nameFinder.best(edgeB);
        if (edgeAName.isPresent() && edgeBName.isPresent())
        {
            return nameFuzzyMatch(edgeAName.get(), edgeBName.get());
        }
        if (!strictMode && (!edgeAName.isPresent() || !edgeBName.isPresent()))
        {
            return true;
        }
        return false;
    }

    /**
     * Test if an {@link Edge} is a candidate for expanding a {@link BigNode}, potentially becoming
     * a Junction Edge in the process. This filters off all the {@link Edge}s that are less
     * important than {@link HighwayTag#RESIDENTIAL}. It also filters off all the edges that are
     * likely roundabouts and link Roads.
     *
     * @param edge
     *            The candidate {@link Edge}
     * @return {@code true} if the {@link Edge} is a candidate for expanding a {@link BigNode}
     */
    private boolean isCandidateJunctionEdge(final Edge edge)
    {
        final HighwayTag highwayTag = edge.highwayTag();
        return isShortEnough(edge)
                && highwayTag.isMoreImportantThanOrEqualTo(HighwayTag.RESIDENTIAL)
                && !JunctionTag.isRoundabout(edge);
    }

    private boolean isDualCarriageWayJunctionEdge(final Edge candidateEdge)
    {
        return isDualCarriageWayRoute(Route.forEdge(candidateEdge));
    }

    private Optional<Route> isDualCarriageWayJunctionRoute(final Route candidateJunctionRoute)
    {
        final Set<Route> candidateJunctionRoutes = new LinkedHashSet<>();
        candidateJunctionRoutes.add(candidateJunctionRoute);
        return isDualCarriageWayJunctionRoute(candidateJunctionRoutes);
    }

    /**
     * Look through all the edges around this node and expand if edge is connected to another short
     * Edge in same direction.
     */
    private Optional<Route> isDualCarriageWayJunctionRoute(final Set<Route> candidateJunctionRoutes)
    {
        if (candidateJunctionRoutes.isEmpty())
        {
            return Optional.empty();
        }
        // Maintain a set of expandable routes.
        final Set<Route> expandableJunctionRoutes = new LinkedHashSet<>();
        for (final Route candidateJunctionRoute : candidateJunctionRoutes)
        {
            final Set<Edge> expandableEdges = new LinkedHashSet<>();
            candidateJunctionRoute.end().outEdges().stream().filter(this::isCandidateJunctionEdge)
                    .filter(edge -> edge.getMasterEdgeIdentifier() != candidateJunctionRoute.end()
                            .getMasterEdgeIdentifier())
                    .filter(edge -> this.edgeDirectionComparator
                            .isSameDirection(candidateJunctionRoute.end(), edge, false))
                    .forEach(expandableEdges::add);

            for (final Edge expandableEdge : expandableEdges)
            {
                Route route = null;
                try
                {
                    route = candidateJunctionRoute.append(expandableEdge);
                }
                catch (final CoreException e)
                {
                    logger.warn("Could not append dual carriageway route {} with with {}",
                            candidateJunctionRoute, expandableEdge.getIdentifier(), e);
                }
                // If the routes are DualCarriageWayRoutes, then return
                if (isDualCarriageWayRoute(route))
                {
                    logger.debug("Adding Dual Carriageway Junction Route : {}", route);
                    return Optional.of(route);
                }
                // Upper safety threshold to prevent bad edge cases
                if (route.size() <= MAXIMUM_DUAL_CARRIAGEWAY_ROUTE_SIZE)
                {
                    expandableJunctionRoutes.add(route);
                }
                else
                {
                    logger.trace(
                            "Maximum number of edges in dual carriageway route  ({}) reached. Skipping route : {}",
                            MAXIMUM_DUAL_CARRIAGEWAY_ROUTE_SIZE, route);
                }
            }
        }
        return isDualCarriageWayJunctionRoute(expandableJunctionRoutes);
    }

    private boolean isDualCarriageWayRoute(final Route candidateRoute)
    {
        if (candidateRoute == null)
        {
            return false;
        }

        /*
         * A restriction with at least 4 master edges in a dual carriage way intersection, used to
         * filter out false positives, missed Test case 5 in BigNodeFinderTest. To increase
         * coverage, this is set to 2.
         */
        if (candidateRoute.connectedEdges().stream().filter(Edge::isMasterEdge)
                .count() >= MIN_MASTER_EDGE_DUAL_CARRIAGEWAY_INTERSECTION)
        {
            for (final Edge inEdge : candidateRoute.start().inEdges())
            {
                // If inEdge is less important than RESIDENTIAL or has the same name as
                // candidateEdge or has the same Heading as candidateEdge, then skip
                if (inEdge.highwayTag().isLessImportantThan(HighwayTag.RESIDENTIAL)
                        || this.edgeDirectionComparator.isSameDirection(candidateRoute.start(),
                                inEdge, false)
                        || edgeNameFuzzyMatch(candidateRoute.start(), inEdge, true))
                {
                    continue;
                }
                // If the candidateRoute has inEdge and outEdge that are in opposite direction
                for (final Edge outEdge : candidateRoute.end().outEdges())
                {
                    /*
                     * Usually Dual Carriage Way roads are one way. OutEdge and inEdge cannot have
                     * name mismatch. Including other Car Navigable roads like Service roads
                     * increases false positive cases.
                     */
                    if (outEdge.highwayTag().isMoreImportantThanOrEqualTo(HighwayTag.RESIDENTIAL)
                            && this.edgeDirectionComparator.isOppositeDirection(inEdge, outEdge,
                                    false)
                            && !outEdge.hasReverseEdge() && !inEdge.hasReverseEdge()
                            && edgeNameFuzzyMatch(outEdge, inEdge, false))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if a candidate {@link Edge} can join the candidate {@link Route} to junction edges
     * that can be merged to candidate {@link Route} to form a {@link BigNode}
     */
    private boolean isMergeCandidateEdge(final Edge candidateEdge, final Route candidateRoute,
            final Set<Long> junctionEdgeIds)
    {
        if (isCandidateJunctionEdge(candidateEdge))
        {
            final Set<Edge> candidateRouteEdges = Sets.newHashSet(candidateRoute);
            // Remove the first edge of a multi-edge route to ensure bigger routes have a cycle
            if (candidateRoute.size() > 1)
            {
                candidateRouteEdges.remove(candidateRoute.start());
            }
            final Set<Long> candidateRouteEdgeIds = candidateRouteEdges.stream()
                    .map(edge -> edge.getIdentifier()).collect(Collectors.toSet());

            final Set<Long> filteredJunctionEdgeIds = Sets.difference(junctionEdgeIds,
                    candidateRouteEdgeIds);
            for (final Edge edge : candidateEdge.outEdges())
            {
                // Check if edge is connected to a junctionEdge with same name. Unless
                // there is a name mismatch we merge them.
                if (filteredJunctionEdgeIds.contains(edge.getIdentifier())
                        && edgeNameFuzzyMatch(candidateRoute.end(), edge, false))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isShortEnough(final Edge edge)
    {
        final Distance length = edge.length();
        if (SEARCH_RADIUS_MOTORWAY.isLessThanOrEqualTo(length))
        {
            return false;
        }
        final HighwayTag highwayTag = mostSignificantConnectedHighwayType(edge);
        return searchRadius(highwayTag).isGreaterThan(length);
    }

    /**
     * Merging/coalescing connected junctionEdges together
     */
    private Route mergeJunctionEdges(final Route candidateRoute, final Set<Long> junctionEdgeIds)
    {
        // If the start Node and end Node are equal, we have a complete Route
        if (candidateRoute.start().start().equals(candidateRoute.end().end()))
        {
            return candidateRoute;
        }
        final Set<Edge> connectedEdges = candidateRoute.end().outEdges();
        connectedEdges.addAll(candidateRoute.start().inEdges());

        /*
         * The perfect case is when we have 4 junction edges that constitute a big node. We check if
         * there are outgoing or incoming Edges that are connected to other junction edges. Use of a
         * TreeSet is to consistently order the edge candidates so that we have a stable identifier
         * for the bigNode
         */
        final Set<Edge> mergeCandidates = new TreeSet<>((edge1, edge2) -> ComparisonChain.start()
                .compare(edge1.getIdentifier(), edge2.getIdentifier()).result());

        final Set<Long> masterEdgeIdentifiers = new HashSet<>();
        candidateRoute.forEach(edge -> masterEdgeIdentifiers.add(edge.getMasterEdgeIdentifier()));

        connectedEdges.stream().filter(edge -> junctionEdgeIds.contains(edge.getIdentifier()))
                .filter(edge -> !candidateRoute.includes(edge))
                .filter(edge -> !masterEdgeIdentifiers.contains(edge.getMasterEdgeIdentifier()))
                .forEach(mergeCandidates::add);
        /*
         * There are some cases where we have a couple of junction edges (instead of all four) that
         * are part of big node
         */
        if (mergeCandidates.isEmpty())
        {
            connectedEdges.stream().filter(edge -> !junctionEdgeIds.contains(edge.getIdentifier()))
                    .filter(edge -> !masterEdgeIdentifiers.contains(edge.getMasterEdgeIdentifier()))
                    .filter(edge -> isMergeCandidateEdge(edge, candidateRoute, junctionEdgeIds))
                    .filter(edge -> !candidateRoute.includes(edge)).forEach(mergeCandidates::add);
        }
        if (!mergeCandidates.isEmpty())
        {
            for (final Edge mergeCandidate : mergeCandidates)
            {
                final Set<Edge> connectedEdge = Collections.singleton(mergeCandidate);
                if (candidateRoute.end().isConnectedAtEndTo(connectedEdge))
                {
                    return mergeJunctionEdges(candidateRoute.append(mergeCandidate),
                            junctionEdgeIds);
                }
                else if (candidateRoute.start().isConnectedAtStartTo(connectedEdge))
                {
                    return mergeJunctionEdges(Route.forEdge(mergeCandidate).append(candidateRoute),
                            junctionEdgeIds);
                }
            }
        }
        return candidateRoute;
    }

    /**
     * @param edge
     *            The {@link Edge} to look at
     * @return The most significant {@link HighwayTag} directly connected to this {@link Edge}
     *         (including itself)
     */
    private HighwayTag mostSignificantConnectedHighwayType(final Edge edge)
    {
        HighwayTag edgeTag = edge.highwayTag();
        for (final Edge connected : edge.connectedEdges())
        {
            final HighwayTag connectedTag = connected.highwayTag();
            if (connectedTag.isMoreImportantThan(edgeTag))
            {
                edgeTag = connectedTag;
            }
        }
        return edgeTag;
    }

    private boolean nameFuzzyMatch(final String nameA, final String nameB)
    {
        return nameA.equalsIgnoreCase(nameB) || StringUtils.getLevenshteinDistance(nameA, nameB,
                LEVENSHTEIN_DISTANCE_THRESHOLD) != -1;
    }

    private Distance searchRadius(final HighwayTag highwayTag)
    {
        if (highwayTag == HighwayTag.MOTORWAY || highwayTag == HighwayTag.MOTORWAY_LINK)
        {
            return SEARCH_RADIUS_MOTORWAY;
        }
        if (highwayTag == HighwayTag.TRUNK || highwayTag == HighwayTag.TRUNK_LINK)
        {
            return SEARCH_RADIUS_TRUNK;
        }
        if (highwayTag == HighwayTag.PRIMARY || highwayTag == HighwayTag.PRIMARY_LINK)
        {
            return SEARCH_RADIUS_PRIMARY;
        }
        if (highwayTag == HighwayTag.SECONDARY || highwayTag == HighwayTag.SECONDARY_LINK)
        {
            return SEARCH_RADIUS_SECONDARY;
        }
        if (highwayTag == HighwayTag.TERTIARY || highwayTag == HighwayTag.TERTIARY_LINK)
        {
            return SEARCH_RADIUS_TERTIARY;
        }
        return SEARCH_RADIUS_RESIDENTIAL;
    }
}
