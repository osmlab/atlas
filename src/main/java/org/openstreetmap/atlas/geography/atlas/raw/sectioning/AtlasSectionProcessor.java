package org.openstreetmap.atlas.geography.atlas.raw.sectioning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.change.ChangeAtlas;
import org.openstreetmap.atlas.geography.atlas.change.ChangeBuilder;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.dynamic.DynamicAtlas;
import org.openstreetmap.atlas.geography.atlas.dynamic.policy.DynamicAtlasPolicy;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.WaySectionIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.sub.AtlasCutType;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.tags.SyntheticInvalidWaySectionTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.time.Time;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Way-section processor that converts Lines to Edges. This will work in two ways: 1. Section a
 * given atlas. 2. Given a shard, sharding and an atlas fetcher policy to - leverage
 * {@link DynamicAtlas} to build an Atlas that contains all edges from the initial shard to their
 * completion as well as any edges that may intersect them. For the second case above, we are
 * guaranteed to have consistent identifiers across shards after way-sectioning, since we are
 * relying on the line shape point order creation and identifying all edge intersections that span
 * shard boundaries.
 *
 * @author mgostintsev
 * @author samg
 */
public class AtlasSectionProcessor
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasSectionProcessor.class);

    private static final int MINIMUM_NODES_TO_QUALIFY_AS_A_EDGE = 2;

    // Logging constants
    private static final String STARTED_SECTIONING = "Started way-sectioning for Atlas {}";
    private static final String FINISHED_SECTIONING = "Finished way-sectioning for Atlas {} in {}";
    private static final String STARTED_EDGE_CREATION = "Started creating Edges for Atlas {}";
    private static final String FINISHED_EDGE_CREATION = "Finished creating Edges for Atlas {} in {}}";
    private static final String STARTED_NODE_CREATION = "Started creating Nodes for Atlas {}";
    private static final String FINISHED_NODE_CREATION = "Finished creating Nodes for Atlas {} in {}}";
    private static final String STARTED_EXCESS_POINT_REMOVAL = "Started removing excess Points for Atlas {}";
    private static final String FINISHED_EXCESS_POINT_REMOVAL = "Finished removing excess Points for Atlas {} in {}}";
    private static final String STARTED_POINT_ADDITION = "Started adding additional Points for Atlas {}";
    private static final String FINISHED_POINT_ADDITION = "Finished adding additional Points for Atlas {} in {}";

    // Expand the initial shard boundary to capture any edges that are crossing the shard boundary
    private static final Distance SHARD_EXPANSION_DISTANCE = Distance.meters(20);

    private final Atlas inputAtlas;
    private final AtlasLoadingOption loadingOption;

    // Bring in all lines that will become edges
    private final List<Shard> loadedShards = new ArrayList<>();
    private final Predicate<AtlasEntity> dynamicAtlasExpansionFilter;
    private final Atlas edgeOnlySubAtlas;

    private final Set<FeatureChange> changes = Collections
            .newSetFromMap(new ConcurrentHashMap<FeatureChange, Boolean>());
    private final Map<Location, CompleteNode> nodeMap = new ConcurrentHashMap<>();

    /**
     * Default constructor. Will section given raw {@link Atlas} file.
     *
     * @param inputAtlas
     *            The {@link Atlas} to section
     * @param loadingOption
     *            The {@link AtlasLoadingOption} to use
     */
    public AtlasSectionProcessor(final Atlas inputAtlas, final AtlasLoadingOption loadingOption)
    {
        this.inputAtlas = inputAtlas;
        this.loadingOption = loadingOption;
        this.dynamicAtlasExpansionFilter = entity -> entity instanceof Line
                && this.loadingOption.getEdgeFilter().test(entity);
        final Optional<Atlas> edgeOnlySubAtlasOptional = inputAtlas
                .subAtlas(this.dynamicAtlasExpansionFilter, AtlasCutType.SILK_CUT);
        if (edgeOnlySubAtlasOptional.isPresent())
        {
            logger.info("Cut subatlas for edges-only");
            this.edgeOnlySubAtlas = edgeOnlySubAtlasOptional.get();
        }
        else
        {
            this.edgeOnlySubAtlas = inputAtlas;
        }
    }

    /**
     * Takes in a starting {@link Shard} and uses the given sharding and atlas fetcher function to
     * build a {@link DynamicAtlas}, which is then sectioned. This guarantees consistent identifiers
     * across the constructed atlas. The sharding and atlas fetcher function must be provided and
     * the sharding must the same as the one used to generate the input shard. The overall logic for
     * atlas construction and sectioning is:
     * <ul>
     * <li>Grab the atlas for the starting shard in its entirety, expand out if there are any edges
     * bleeding into neighboring shards.
     * <li>Once the full atlas is built, way-section it.
     * <li>After sectioning is completed and atlas is rebuild, cut a sub-atlas representing the
     * bounds of the original shard being processed. This is a soft cut, so any edges that start in
     * the shard and end in neighboring shards, will be captured.
     * </ul>
     *
     * @param initialShard
     *            The initial {@link Shard} to start at
     * @param loadingOption
     *            The {@link AtlasLoadingOption} to use
     * @param sharding
     *            The {@link Sharding} to use to know which neighboring atlas files to get
     * @param atlasFetcher
     *            The fetching policy to use to obtain adjacent atlas files
     */
    public AtlasSectionProcessor(final Shard initialShard, final AtlasLoadingOption loadingOption,
            final Sharding sharding, final Function<Shard, Optional<Atlas>> atlasFetcher)
    {
        this.loadingOption = loadingOption;
        this.dynamicAtlasExpansionFilter = entity -> entity instanceof Line
                && this.loadingOption.getEdgeFilter().test(entity);
        if (sharding == null || atlasFetcher == null)
        {
            throw new IllegalArgumentException(
                    "Must supply a valid sharding and fetcher function for sectioning!");
        }
        this.inputAtlas = buildExpandedAtlas(initialShard, sharding, atlasFetcher);
        final Optional<Atlas> edgeOnlySubAtlasOptional = this.inputAtlas
                .subAtlas(this.dynamicAtlasExpansionFilter, AtlasCutType.SILK_CUT);
        if (edgeOnlySubAtlasOptional.isPresent())
        {
            logger.info("Cut subatlas for edges-only");
            this.edgeOnlySubAtlas = edgeOnlySubAtlasOptional.get();
        }
        else
        {
            this.edgeOnlySubAtlas = this.inputAtlas;
        }
    }

    /**
     * Slices the given {@link Atlas}.
     *
     * @return the way-sectioned {@link Atlas}
     */
    public Atlas run()
    {
        final Time overallTime = Time.now();
        Time time = Time.now();

        logger.info(STARTED_SECTIONING, this.getShardOrAtlasName());

        logger.info(STARTED_EDGE_CREATION, this.getShardOrAtlasName());
        this.inputAtlas.lines(this.loadingOption.getEdgeFilter()::test).forEach(this::section);
        logger.info(FINISHED_EDGE_CREATION, this.getShardOrAtlasName(),
                time.elapsedSince().asMilliseconds());

        time = Time.now();
        logger.info(STARTED_NODE_CREATION, this.getShardOrAtlasName());
        this.nodeMap.values()
                .forEach(node -> this.changes.add(FeatureChange.add(node, this.inputAtlas)));
        logger.info(FINISHED_NODE_CREATION, this.getShardOrAtlasName(),
                time.elapsedSince().asMilliseconds());

        time = Time.now();
        // If this atlas is supposed to keep everything, add the points that are not also saved as a
        // node.
        if (this.loadingOption.isKeepAll())
        {
            logger.info(STARTED_POINT_ADDITION, this.getShardOrAtlasName());
            this.inputAtlas.points().forEach(point ->
            {
                final CompleteNode possibleDupe = this.nodeMap.get(point.getLocation());
                if (possibleDupe == null
                        || possibleDupe.getOsmIdentifier() != point.getOsmIdentifier())
                {
                    this.changes.add(FeatureChange.add(CompletePoint.from(point)));
                }
            });
            logger.info(FINISHED_POINT_ADDITION, this.getShardOrAtlasName(),
                    time.elapsedSince().asMilliseconds());
        }
        else
        {
            logger.info(STARTED_EXCESS_POINT_REMOVAL, this.getShardOrAtlasName());
            this.inputAtlas.points().forEach(point ->
            {
                // we care about a point if and only if it has pre-existing OSM tags OR it belongs
                // to a future edge OR we are doing QA
                if (!this.loadingOption.isKeepAll() && point.getOsmTags().isEmpty()
                        && point.relations().isEmpty())
                {
                    this.changes.add(FeatureChange.remove(CompletePoint.shallowFrom(point)));
                }
            });
            logger.info(FINISHED_EXCESS_POINT_REMOVAL, this.getShardOrAtlasName(),
                    time.elapsedSince().asMilliseconds());
        }

        logger.info(FINISHED_SECTIONING, this.getShardOrAtlasName(),
                overallTime.elapsedSince().asMilliseconds());

        // return either the unchanged original Atlas, or a cut-down version of the sectioned Atlas
        if (this.changes.isEmpty())
        {
            if (this.loadedShards.isEmpty() || this.loadedShards.size() == 1)
            {
                return this.inputAtlas.cloneToPackedAtlas();
            }
            return cutSubAtlasForOriginalShard(this.inputAtlas).cloneToPackedAtlas();
        }

        final String country = this.loadingOption.getCountryCode();
        final String shardOrAtlasName = this.getShardOrAtlasName();
        final ChangeAtlas sectionedAtlas = new ChangeAtlas(this.inputAtlas,
                new ChangeBuilder().addAll(this.changes).get())
        {
            private static final long serialVersionUID = -1379576156041355921L;

            @Override
            public synchronized AtlasMetaData metaData()
            {
                // Override meta-data here so the country code is properly included.
                final AtlasMetaData metaData = super.metaData();
                return new AtlasMetaData(metaData.getSize(), false,
                        metaData.getCodeVersion().orElse(null),
                        metaData.getDataVersion().orElse(null), country, shardOrAtlasName,
                        new HashMap<>());
            }
        };
        if (this.loadedShards.isEmpty())
        {
            return sectionedAtlas.cloneToPackedAtlas();
        }
        return cutSubAtlasForOriginalShard(sectionedAtlas).cloneToPackedAtlas();
    }

    /**
     * Grabs the atlas for the initial shard, in its entirety. Then proceeds to expand out to
     * surrounding shards if there are any edges bleeding over the shard bounds plus
     * {@link #SHARD_EXPANSION_DISTANCE}. Finally, will return the constructed Atlas.
     *
     * @param initialShard
     *            The initial {@link Shard} being processed
     * @param sharding
     *            The {@link Sharding} used to identify which shards to fetch
     * @param fullySlicedAtlasFetcher
     *            The fetcher policy to retrieve an Atlas file for each shard
     * @return the expanded {@link Atlas}
     */
    private Atlas buildExpandedAtlas(final Shard initialShard, final Sharding sharding,
            final Function<Shard, Optional<Atlas>> fullySlicedAtlasFetcher)
    {
        // Keep track of all loaded shards. This will be used to cut the sub-atlas for the shard
        // we're processing after all sectioning is completed. Initial shard will always be first!
        this.loadedShards.add(initialShard);

        final DynamicAtlasPolicy policy = new DynamicAtlasPolicy(fullySlicedAtlasFetcher, sharding,
                initialShard.bounds().expand(SHARD_EXPANSION_DISTANCE), Rectangle.MAXIMUM)
                        .withDeferredLoading(true).withExtendIndefinitely(false)
                        .withAtlasEntitiesToConsiderForExpansion(
                                this.dynamicAtlasExpansionFilter::test);

        final DynamicAtlas atlas = new DynamicAtlas(policy);
        atlas.preemptiveLoad();
        return atlas;
    }

    /**
     * Takes a polyline for a new Edge and adds the feature to the ChangeSet
     *
     * @param line
     *            The {@link Line} being converted to an {@link Edge}
     * @param edgePolyLine
     *            The polyline defining the geometry of the {@link Edge}
     * @param edgeIdentifier
     *            The identifier for the {@link Edge}
     * @param hasReverseEdge
     *            Boolean for if a reverse {@link Edge} should be made as well
     * @param tags
     *            The tags for the new {@link Edge}
     */
    private void createEdge(final Line line, final PolyLine edgePolyLine, final long edgeIdentifier,
            final boolean hasReverseEdge, final Map<String, String> tags)
    {
        // if a node already exists for the start/end locations, use theml otherwise make new ones
        CompleteNode startNode = this.nodeMap.get(edgePolyLine.first());
        if (startNode == null)
        {
            final SortedSet<Long> inEdges = new TreeSet<>();
            if (hasReverseEdge)
            {
                inEdges.add(-edgeIdentifier);
            }
            final SortedSet<Long> outEdges = new TreeSet<>();
            outEdges.add(edgeIdentifier);
            startNode = createNode(line, edgePolyLine.first(), inEdges, outEdges);
        }
        else
        {
            if (hasReverseEdge)
            {
                startNode.withAddedInEdgeIdentifier(-edgeIdentifier);
            }
            startNode.withAddedOutEdgeIdentifier(edgeIdentifier);
        }

        CompleteNode endNode = this.nodeMap.get(edgePolyLine.last());
        if (endNode == null)
        {
            final SortedSet<Long> inEdges = new TreeSet<>();
            inEdges.add(edgeIdentifier);
            final SortedSet<Long> outEdges = new TreeSet<>();
            if (hasReverseEdge)
            {
                outEdges.add(-edgeIdentifier);
            }
            endNode = createNode(line, edgePolyLine.last(), inEdges, outEdges);
        }
        else
        {
            if (hasReverseEdge)
            {
                endNode.withAddedOutEdgeIdentifier(-edgeIdentifier);
            }
            endNode.withAddedInEdgeIdentifier(edgeIdentifier);
        }
        final Set<Long> relations = new HashSet<>();
        line.relations().forEach(relation -> relations.add(relation.getIdentifier()));

        final CompleteEdge newEdge = new CompleteEdge(edgeIdentifier, edgePolyLine, tags,
                startNode.getIdentifier(), endNode.getIdentifier(), relations);
        final CompleteEdge newReverseEdge = new CompleteEdge(-edgeIdentifier,
                edgePolyLine.reversed(), tags, endNode.getIdentifier(), startNode.getIdentifier(),
                relations);
        updateRelations(line, newEdge, newReverseEdge, hasReverseEdge);

        this.changes.add(FeatureChange.add(newEdge, this.inputAtlas));
        if (hasReverseEdge)
        {
            this.changes.add(FeatureChange.add(newReverseEdge, this.inputAtlas));
        }
    }

    /**
     * Helper method to make a new Node for an Edge
     *
     * @param line
     *            The {@link Line} being converted to an {@link Edge}
     * @param nodeLocation
     *            The {@link Location} for the {@link Node} being made
     * @param inEdges
     *            The identifiers for the {@link Edge}s going into the {@link Node} being made
     * @param outEdges
     *            The identifiers for the {@link Edge}s going out of the {@link Node} being made
     * @return
     */
    private CompleteNode createNode(final Line line, final Location nodeLocation,
            final SortedSet<Long> inEdges, final SortedSet<Long> outEdges)
    {
        if (!this.inputAtlas.pointsAt(nodeLocation).iterator().hasNext())
        {
            throw new CoreException(
                    "Couldn't find node at {} while sectioning Line {} for Atlas {}",
                    nodeLocation.toString(), line.toString(), getShardOrAtlasName());
        }
        final Point pointForNode = this.inputAtlas.pointsAt(nodeLocation).iterator().next();
        // Drop nodes that don't have tags when we don't need them for other purposes (e.g., QA)
        if (!this.loadingOption.isKeepAll() && pointForNode.getOsmTags().isEmpty())
        {
            this.changes.add(FeatureChange.remove(CompletePoint.shallowFrom(pointForNode)));
        }
        final Set<Long> relationIds = new HashSet<>();
        pointForNode.relations().forEach(relation -> relationIds.add(relation.getIdentifier()));
        final CompleteNode node = new CompleteNode(pointForNode.getIdentifier(),
                pointForNode.getLocation(), pointForNode.getTags(), inEdges, outEdges, relationIds);
        this.nodeMap.put(node.getLocation(), node);

        pointForNode.relations().forEach(relation -> relation
                .membersMatching(member -> member.getEntity().getType().equals(ItemType.POINT)
                        && member.getEntity().getIdentifier() == pointForNode.getIdentifier())
                .forEach(member -> this.changes.add(FeatureChange.add(CompleteRelation
                        .shallowFrom(relation).withAddedMember(node, member.getRole())))));
        return node;
    }

    /**
     * Takes a given Line and its Nodes, and turns into Edges starting and ending at each Node.
     *
     * @param line
     *            The {@link Line} being converted to an {@link Edge}
     * @param nodes
     *            The identifiers for the {@link Point}s that will be converted into the
     *            {@link Node} for the {@link Edge}
     * @param isReversed
     *            Boolean for if the geometry of the {@link Edge} should reversed
     * @param hasReverseEdge
     *            Boolean for if a reverse {@link Edge} should be made as well
     * @param remainder
     *            Any remaining linear geometry at the end of the {@link Line} being converted to an
     *            {@link Edge}-- in some circumstances, this geometry will be converted to its own
     *            {@link Edge}, but in many cases it will be combined into the last {@link Edge} to
     *            reduce the number of {@link Edge}s made
     */
    private void createSections(final Line line, final List<Integer> nodes,
            final boolean isReversed, final boolean hasReverseEdge, final PolyLine remainder)
    {
        // Prepare the nodes identifiers, identifier factory and one way information
        final WaySectionIdentifierFactory identifierFactory = new WaySectionIdentifierFactory(
                line.getIdentifier());

        // if the edge geometry is going to be singular, make that directly and bypass the loops
        if (remainder != null && remainder.size() == line.asPolyLine().size())
        {
            createEdge(line, remainder, identifierFactory.nextIdentifier(), hasReverseEdge,
                    line.getTags());
            return;
        }
        else if (!line.isClosed() && nodes.size() == 2 && nodes.get(0) == 0
                && line.asPolyLine().size() - 1 == nodes.get(1))
        {
            createEdge(line, isReversed ? line.asPolyLine().reversed() : line.asPolyLine(),
                    line.getIdentifier(), hasReverseEdge, line.getTags());
            return;
        }

        final Iterator<Integer> nodesIterator = nodes.iterator();
        int startIndex = nodesIterator.next();

        // iterate over all node locations and make edges for each polyline section
        while (identifierFactory.hasMore() && nodesIterator.hasNext())
        {
            int endIndex = nodesIterator.next();
            final long edgeIdentifier = identifierFactory.nextIdentifier();
            final Map<String, String> tags = line.getTags();

            // if there are no more identifiers left, fast forward to the end of the line
            if (!identifierFactory.hasMore())
            {
                // Update the tags to indicate this edge wasn't way-sectioned
                tags.put(SyntheticInvalidWaySectionTag.KEY,
                        SyntheticInvalidWaySectionTag.YES.toString());
                endIndex = line.asPolyLine().size() - 1;
            }

            final PolyLine rawPolyLine = new PolyLine(line.asPolyLine().truncate(startIndex,
                    line.asPolyLine().size() - endIndex - 1));
            PolyLine edgePolyLine = isReversed ? rawPolyLine.reversed() : rawPolyLine;

            // if this is the last node, deal with the remainder. if the remainder
            if (!nodesIterator.hasNext() && remainder != null)
            {
                final Location potentialStitchLocation = isReversed ? edgePolyLine.first()
                        : edgePolyLine.last();

                // if we need a section at the last location, we'll make the remainder its own edge.
                // otherwise, we'll combine it with the last edge to reduce excess edges
                if (shouldSectionAtLocation(potentialStitchLocation, line))
                {
                    final long remainderIdentifier = identifierFactory.nextIdentifier();
                    createEdge(line, remainder, remainderIdentifier, hasReverseEdge, tags);
                }
                else
                {
                    edgePolyLine = isReversed ? remainder.append(edgePolyLine)
                            : edgePolyLine.append(remainder);
                }
            }

            createEdge(line, edgePolyLine, edgeIdentifier, hasReverseEdge, tags);
            startIndex = endIndex;
        }
    }

    /**
     * Up to this point, we've constructed the {@link DynamicAtlas} and way-sectioned it. Since
     * we're only responsible for returning an Atlas for the provided shard, we now need to cut a
     * sub-atlas for the initial shard boundary and return it. We can leverage the loaded shards
     * parameter, which will always contain the starting shard as the first shard and all other
     * loaded shards after. If no other shards were loaded, simply return the given Atlas.
     *
     * @param atlas
     *            The {@link Atlas} file we need to trim
     * @return the {@link Atlas} for the bounds of the input shard
     */
    private Atlas cutSubAtlasForOriginalShard(final Atlas atlas)
    {
        try
        {
            // The first shard is always the initial one. Use its bounds to build the atlas.
            final Rectangle originalShardBounds = this.loadedShards.get(0).bounds();
            return atlas.subAtlas(originalShardBounds, AtlasCutType.SOFT_CUT)
                    .orElseThrow(() -> new CoreException(
                            "Cannot have an empty atlas after way sectioning {}",
                            this.loadedShards.get(0).getName()));
        }
        catch (final Exception e)
        {
            throw new CoreException("Error creating sub-atlas for original shard bounds", e);
        }
    }

    /**
     * Iterate over a Line's locations to determine which ones qualify as a location to section at
     *
     * @param line
     *            The {@link Line} being converted to an {@link Edge}
     * @param linePolyLine
     *            The polyline for the {@link Line} being converted to an {@link Edge}
     * @return
     */
    private List<Integer> findNodesForEdge(final Line line, final PolyLine linePolyLine)
    {
        final List<Integer> nodesForEdge = new ArrayList<>();
        final Set<Location> selfIntersections = linePolyLine.selfIntersections();
        Location previousLocation = null;
        for (int i = 0; i < linePolyLine.size(); i++)
        {
            final Location location = linePolyLine.get(i);
            if (i == 0 || i == linePolyLine.size() - 1)
            {
                nodesForEdge.add(i);
            }
            else if (location.equals(previousLocation))
            {
                // NOOP
            }
            else if (selfIntersections.contains(location)
                    || shouldSectionAtLocation(location, line))
            {
                nodesForEdge.add(i);
            }
            previousLocation = location;
        }
        return nodesForEdge;
    }

    /**
     * Just a helper method to use a consistent naming scheme
     *
     * @return A String for either the Shard name or the Atlas name
     */
    private String getShardOrAtlasName()
    {
        // Default to getting the Shard name, if available, otherwise fall back to atlas name
        if (!this.loadedShards.isEmpty())
        {
            return this.loadedShards.get(0).getName();
        }
        else
        {
            return this.inputAtlas.getName();
        }
    }

    /**
     * Takes a Line, finds its Nodes, then makes Edges for each section.
     *
     * @param line
     *            The {@link Line} being converted to an {@link Edge}
     */
    private void section(final Line line)
    {
        final PolyLine polyLine = line.asPolyLine();

        // Determines if we need to reverse the polyline and if a reverse edge is needed
        final PbfOneWay oneWay = PbfOneWay.forTag(line);
        final boolean hasReverseEdge = oneWay == PbfOneWay.NO;
        final boolean isReversed = oneWay == PbfOneWay.REVERSED;

        final List<Integer> nodesForEdge = findNodesForEdge(line, polyLine);

        if (nodesForEdge.size() < MINIMUM_NODES_TO_QUALIFY_AS_A_EDGE)
        {
            logger.error("Edge {} hass less than {} nodes, cannot be sectioned!",
                    line.getIdentifier(), MINIMUM_NODES_TO_QUALIFY_AS_A_EDGE);
            this.changes
                    .add(FeatureChange.add(
                            CompleteLine.shallowFrom(line).withTags(line.getTags()).withAddedTag(
                                    SyntheticInvalidWaySectionTag.KEY,
                                    SyntheticInvalidWaySectionTag.YES.toString()),
                            this.inputAtlas));
            return;
        }

        // Initialize start location
        PolyLine remainder = null;

        // we'll preprocess rings a bit to make them consistently sectioned
        if (line.isClosed())
        {
            if (nodesForEdge.size() == 2)
            {
                // the node is just the beginning/end of the Edge, and connects to another Edge,
                // like a cul-de-sac
                if (nodesForEdge.get(0) == 0
                        && nodesForEdge.get(1) == line.numberOfShapePoints() - 1
                        && shouldSectionAtLocation(polyLine.get(0), line))
                {
                    // we just want a single Edge for the whole loop, connecting back to itself
                    // noop
                }
                else
                {
                    // corner case-- a ring with no other connected Edges that starts and ends at
                    // the same node. make an artificial node halfway through to make two Edges
                    nodesForEdge.remove(1);
                    nodesForEdge.add(polyLine.size() / 2);
                    nodesForEdge.add(polyLine.size() - 1);
                }
            }
            else if (polyLine.isSimple())
            {
                final int nextIndex = nodesForEdge.get(nodesForEdge.size() - 2);
                remainder = new PolyLine(polyLine.truncate(nextIndex, 0));
                nodesForEdge.remove(nodesForEdge.size() - 1);
                if (!shouldSectionAtLocation(polyLine.get(0), line))
                {
                    nodesForEdge.remove(0);
                    final int startIndex = nodesForEdge.get(0);
                    remainder = remainder.append(
                            new PolyLine(polyLine.truncate(0, polyLine.size() - 1 - startIndex)));
                }
                if (isReversed)
                {
                    remainder = remainder.reversed();
                }
            }
        }

        createSections(line, nodesForEdge, isReversed, hasReverseEdge, remainder);
        this.changes.add(FeatureChange.remove(CompleteLine.shallowFrom(line), this.inputAtlas));
    }

    /**
     * Determines if we should section at the given {@link Location}. Relies on the underlying
     * {@link AtlasLoadingOption} configuration to make the decision. If {@link true}, this implies
     * the point at this {@link Location} should be a {@link Node}. Sectioning is either based on
     * the tag values of the underlying points at the location, or the existence of an intersecting
     * Edge at that location
     *
     * @param location
     *            The {@link Location} to check
     * @param line
     *            The {@link Line} this {@link Location} belongs to
     * @return {@code true} if we should section at the given {@link Location}
     */
    private boolean shouldSectionAtLocation(final Location location, final Line line)
    {
        final long targetLayerValue = LayerTag.getTaggedOrImpliedValue(line, 0L);

        return Iterables.stream(this.inputAtlas.pointsAt(location))
                .anyMatch(point -> this.loadingOption.getWaySectionFilter().test(point))
                || Iterables
                        // Find all intersecting edges
                        .stream(this.edgeOnlySubAtlas.linesContaining(location,
                                target -> target.getIdentifier() != line.getIdentifier()
                                        && target.asPolyLine().contains(location)))
                        // Check whether that edge has a different layer value as the line we're
                        // looking at and that our point is its start or end node
                        .anyMatch(candidate ->
                        {
                            final long layerValue = LayerTag.getTaggedOrImpliedValue(candidate, 0L);
                            if (targetLayerValue == layerValue)
                            {
                                return true;
                            }
                            final boolean edgesOnDifferentLayers = targetLayerValue != layerValue;
                            final PolyLine candidatePolyline = candidate.asPolyLine();
                            final boolean intersectionIsAtEndPoint = candidatePolyline.first()
                                    .equals(location) || candidatePolyline.last().equals(location);

                            return edgesOnDifferentLayers && intersectionIsAtEndPoint;
                        });
    }

    /**
     * Iterate over Relations to make sure they're synchronized with the changes for new Edges
     *
     * @param line
     *            The {@link Line} being converted to an {@link Edge}
     * @param newEdge
     *            The new {@link Edge}
     * @param newReverseEdge
     *            The new reverse {@link Edge}
     * @param hasReverseEdge
     *            Boolean representing the existence of a reverse {@link Edge}
     */
    private void updateRelations(final Line line, final CompleteEdge newEdge,
            final CompleteEdge newReverseEdge, final boolean hasReverseEdge)
    {
        line.relations().forEach(relation -> relation
                .membersMatching(member -> member.getEntity().getType().equals(ItemType.LINE)
                        && member.getEntity().getIdentifier() == line.getIdentifier())
                .forEach(member ->
                {
                    this.changes.add(FeatureChange.add(CompleteRelation.shallowFrom(relation)
                            .withAddedMember(newEdge, member.getRole())));
                    if (hasReverseEdge)
                    {
                        this.changes.add(FeatureChange.add(CompleteRelation.shallowFrom(relation)
                                .withAddedMember(newReverseEdge, member.getRole())));
                    }
                }));
    }
}
