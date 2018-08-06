package org.openstreetmap.atlas.geography.atlas.raw.sectioning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasMetaData;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.dynamic.DynamicAtlas;
import org.openstreetmap.atlas.geography.atlas.dynamic.policy.DynamicAtlasPolicy;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.pbf.slicing.identifier.WaySectionIdentifierFactory;
import org.openstreetmap.atlas.geography.atlas.pbf.store.PbfOneWay;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryEdge;
import org.openstreetmap.atlas.geography.atlas.raw.slicing.temporary.TemporaryNode;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.tags.AtlasTag;
import org.openstreetmap.atlas.tags.LayerTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.time.Time;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Way-section processor that runs on raw atlases. Its main purpose is to split raw atlas points
 * into nodes, points or shape points, split lines into edges, lines or areas and update all
 * relation members to reflect these changes. This will work in two ways: 1. Section a given raw
 * atlas. 2. Given a shard, sharding and raw atlas fetcher policy to - leverage {@link DynamicAtlas}
 * to build an Atlas that contains all edges from the initial shard to their completion as well as
 * any edges that may intersect them. For the second case above, we are guaranteed to have
 * consistent identifiers across shards after way-sectioning, since we are relying on the line shape
 * point order creation and identifying all edge intersections that span shard boundaries.
 *
 * @author mgostintsev
 */
public class WaySectionProcessor
{
    private static final Logger logger = LoggerFactory.getLogger(WaySectionProcessor.class);

    private static final int MINIMUM_NUMBER_OF_SELF_INTERSECTIONS_FOR_A_NODE = 3;
    private static final int MINIMUM_SHAPE_POINTS_TO_QUALIFY_AS_AREA = 3;
    private static final int MINIMUM_POINTS_TO_QUALIFY_AS_A_LINE = 2;

    // Logging constants
    private static final String STARTED_TASK_MESSAGE = "Started {} for Shard {}";
    private static final String COMPLETED_TASK_MESSAGE = "Finished {} for Shard {} in {}";
    private static final String WAY_SECTIONING_TASK = "Way-Sectioning";
    private static final String ATLAS_FETCHING_TASK = "Atlas-Fetching";
    private static final String SUB_ATLAS_CUTTING_TASK = "Sub-Atlas Cutting";
    private static final String EDGE_SECTIONING_TASK = "Edge Sectioning";
    private static final String SHAPE_POINT_DETECTION_TASK = "Shape Point Detection";
    private static final String DYNAMIC_ATLAS_CREATION_TASK = "Dynamic Atlas Creation";
    private static final String ATLAS_FEATURE_DETECTION_TASK = "Atlas Feature Detection";
    private static final String SECTIONED_ATLAS_CREATION_TASK = "Sectioned Atlas Creation";

    // Expand the initial shard boundary to capture any edges that are crossing the shard boundary
    private static final Distance SHARD_EXPANSION_DISTANCE = Distance.meters(20);

    private final Atlas rawAtlas;
    private final AtlasLoadingOption loadingOption;
    private final List<Shard> loadedShards = new ArrayList<>();

    // Bring in all points that are part of any line that will become an edge
    private final Predicate<AtlasEntity> pointPredicate = entity -> entity instanceof Point
            && Iterables.stream(entity.getAtlas().linesContaining(((Point) entity).getLocation()))
                    .anyMatch(this::isAtlasEdge);

    // Bring in all lines that will become edges
    private final Predicate<AtlasEntity> linePredicate = entity -> entity instanceof Line
            && isAtlasEdge((Line) entity);

    // TODO - we are pulling in all edges and their contained points in the shard. We can optimize
    // this further by only considering the edges crossing the shard boundary and their intersecting
    // edges to reduce the memory overhead on each slave.

    // Dynamic expansion filter will be a combination of points and lines
    private final Predicate<AtlasEntity> dynamicAtlasExpansionFilter = entity -> this.pointPredicate
            .test(entity) || this.linePredicate.test(entity);

    /**
     * Default constructor. Will section given raw {@link Atlas} file.
     *
     * @param rawAtlas
     *            The raw {@link Atlas} to section
     * @param loadingOption
     *            The {@link AtlasLoadingOption} to use
     */
    public WaySectionProcessor(final Atlas rawAtlas, final AtlasLoadingOption loadingOption)
    {
        this.rawAtlas = rawAtlas;
        this.loadingOption = loadingOption;
    }

    /**
     * Takes in a starting {@link Shard} and uses the given sharding and atlas fetcher function to
     * build a {@link DynamicAtlas}, which is then sectioned. This guarantees consistent identifiers
     * across the constructed atlas. The sharding and raw atlas fetcher function must be provided
     * and the sharding must the same as the one used to generate the input shard. The overall logic
     * for atlas construction and sectioning is:
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
     * @param rawAtlasFetcher
     *            The fetching policy to use to obtain adjacent raw atlas files
     */
    public WaySectionProcessor(final Shard initialShard, final AtlasLoadingOption loadingOption,
            final Sharding sharding, final Function<Shard, Optional<Atlas>> rawAtlasFetcher)
    {
        this.loadingOption = loadingOption;
        if (sharding == null || rawAtlasFetcher == null)
        {
            throw new IllegalArgumentException(
                    "Must supply a valid sharding and fetcher function for sectioning!");
        }
        this.rawAtlas = buildExpandedAtlas(initialShard, sharding, rawAtlasFetcher);
    }

    /**
     * Slices the given raw {@link Atlas}.
     *
     * @return the way-sectioned {@link Atlas}
     */
    public Atlas run()
    {
        final Time time = logTaskStartedAsInfo(WAY_SECTIONING_TASK, getShardOrAtlasName());

        // Create a changeset to keep track of any intermediate state transitions (points becomes
        // nodes, lines becoming areas, etc.)
        final WaySectionChangeSet changeSet = new WaySectionChangeSet();

        // Go through all the lines and determine what will become an edge, area or stay a line.
        // This includes node detection through edge intersection and tagging configuration.
        identifyEdgesNodesAndAreasFromLines(changeSet);

        // Separate points and shape points
        distinguishPointsFromShapePoints(changeSet);

        // Go through the identified edges and split them at the identified nodes
        sectionEdges(changeSet);

        final Atlas atlas = buildSectionedAtlas(changeSet);
        logTaskCompletionAsInfo(WAY_SECTIONING_TASK, getShardOrAtlasName(), time.elapsedSince());

        return cutSubAtlasForOriginalShard(atlas);
    }

    /**
     * Adds a {@link TemporaryNode} for the given {@link Location} to the given
     * {@link NodeOccurrenceCounter}. Note: there should only be a single raw atlas {@link Point} at
     * the given {@link Location}.
     *
     * @param location
     *            The {@link Location} of the node to add
     * @param nodeCounter
     *            The {@link NodeOccurrenceCounter} to add to
     */
    private void addPointToNodeList(final Location location,
            final NodeOccurrenceCounter nodeCounter)
    {
        this.rawAtlas.pointsAt(location).forEach(point -> nodeCounter
                .addNode(new TemporaryNode(point.getIdentifier(), point.getLocation())));
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
     * @param rawAtlasFetcher
     *            The fetcher policy to retrieve an Atlas file for each shard
     * @return the expanded {@link Atlas}
     */
    private Atlas buildExpandedAtlas(final Shard initialShard, final Sharding sharding,
            final Function<Shard, Optional<Atlas>> rawAtlasFetcher)
    {
        final Time dynamicAtlasTime = logTaskStartedAsInfo(DYNAMIC_ATLAS_CREATION_TASK,
                initialShard.getName());

        // Keep track of all loaded shards. This will be used to cut the sub-atlas for the shard
        // we're processing after all sectioning is completed. Initial shard will always be first!
        this.loadedShards.add(initialShard);

        // Wraps the given fetcher, by always returning the entire atlas for the initial shard
        final Function<Shard, Optional<Atlas>> shardAwareFetcher = shard ->
        {
            if (shard.equals(initialShard))
            {
                final Time fetchTime = Time.now();
                final Optional<Atlas> fetchedAtlas = rawAtlasFetcher.apply(initialShard);
                logTaskCompletionAsTrace(ATLAS_FETCHING_TASK, getShardOrAtlasName(),
                        fetchTime.elapsedSince());
                return fetchedAtlas;
            }
            else
            {
                final Time fetchTime = Time.now();
                final Optional<Atlas> possibleAtlas = rawAtlasFetcher.apply(shard);
                logTaskCompletionAsTrace(ATLAS_FETCHING_TASK, getShardOrAtlasName(),
                        fetchTime.elapsedSince());

                if (possibleAtlas.isPresent())
                {
                    this.loadedShards.add(shard);
                    final Atlas atlas = possibleAtlas.get();
                    final Time subAtlasTime = Time.now();
                    final Optional<Atlas> subAtlas = atlas
                            .subAtlas(this.dynamicAtlasExpansionFilter);
                    logTaskCompletionAsTrace(SUB_ATLAS_CUTTING_TASK, getShardOrAtlasName(),
                            subAtlasTime.elapsedSince());
                    return subAtlas;
                }
                return Optional.empty();
            }
        };

        final DynamicAtlasPolicy policy = new DynamicAtlasPolicy(shardAwareFetcher, sharding,
                initialShard.bounds().expand(SHARD_EXPANSION_DISTANCE), Rectangle.MAXIMUM)
                        .withDeferredLoading(true).withExtendIndefinitely(false)
                        .withAtlasEntitiesToConsiderForExpansion(
                                this.dynamicAtlasExpansionFilter::test);

        final DynamicAtlas atlas = new DynamicAtlas(policy);
        atlas.preemptiveLoad();

        logTaskCompletionAsInfo(DYNAMIC_ATLAS_CREATION_TASK, getShardOrAtlasName(),
                dynamicAtlasTime.elapsedSince());
        return atlas;
    }

    // TODO add statistics

    /**
     * Final step of way-sectioning. Use the {@link WaySectionChangeSet} to build an {@link Atlas}
     * that has all entities.
     *
     * @param changeSet
     *            The {@link WaySectionChangeSet} that holds all intermediate information required
     *            to build the atlas
     * @return the built {@link Atlas}
     */
    private Atlas buildSectionedAtlas(final WaySectionChangeSet changeSet)
    {
        final Time buildTime = logTaskStartedAsInfo(SECTIONED_ATLAS_CREATION_TASK,
                getShardOrAtlasName());

        // Create builder and set properties
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        final AtlasSize sizeEstimate = createAtlasSizeEstimate(changeSet);
        builder.setSizeEstimates(sizeEstimate);
        builder.setMetaData(createAtlasMetadata());

        // Points
        changeSet.getPointsThatStayPoints().forEach(pointIdentifier ->
        {
            final Point point = this.rawAtlas.point(pointIdentifier);
            builder.addPoint(pointIdentifier, point.getLocation(), point.getTags());
        });

        // Nodes
        changeSet.getPointsThatBecomeNodes().forEach(nodeIdentifier ->
        {
            final Point point = this.rawAtlas.point(nodeIdentifier);
            builder.addNode(point.getIdentifier(), point.getLocation(), point.getTags());
        });

        // Lines
        Iterables.stream(this.rawAtlas.lines()).filter(line ->
        {
            final long lineIdentifier = line.getIdentifier();
            return !changeSet.getLinesThatBecomeAreas().contains(lineIdentifier)
                    && !changeSet.getLinesThatBecomeEdges().contains(lineIdentifier)
                    && !changeSet.getExcludedLines().contains(lineIdentifier);
        }).forEach(lineToKeep ->
        // Add any line that didn't become an edge or area
        builder.addLine(lineToKeep.getIdentifier(), lineToKeep.asPolyLine(), lineToKeep.getTags()));

        // Edges
        changeSet.getCreatedEdges().forEach(temporaryEdge ->
        {
            builder.addEdge(temporaryEdge.getIdentifier(), temporaryEdge.getPolyLine(),
                    temporaryEdge.getTags());

            // Add the reverse edge, if needed
            if (temporaryEdge.hasReverse())
            {
                builder.addEdge(temporaryEdge.getReverseEdgeIdentifier(),
                        temporaryEdge.getPolyLine().reversed(), temporaryEdge.getTags());
            }
        });

        // Areas
        changeSet.getLinesThatBecomeAreas().forEach(areaIdentifier ->
        {
            final Line line = this.rawAtlas.line(areaIdentifier);
            // The PolyLine class stores all points. Since these are closed lines, the first and
            // last point is identical. The Polygon class expects the last point to not be the first
            // point, that is implicit. So when forming the Polygon, we need to remove the last
            // point to avoid duplicating it.
            builder.addArea(areaIdentifier, new Polygon(line.asPolyLine().truncate(0, 1)),
                    line.getTags());
        });

        // Relations
        this.rawAtlas.relationsLowerOrderFirst().forEach(relation ->
        {
            final RelationBean bean = new RelationBean();
            relation.members().forEach(member ->
            {
                final AtlasEntity entity = member.getEntity();
                final long memberIdentifier = entity.getIdentifier();
                final String memberRole = member.getRole();
                switch (entity.getType())
                {
                    case POINT:
                        if (builder.peek().point(memberIdentifier) != null)
                        {
                            bean.addItem(memberIdentifier, memberRole, ItemType.POINT);
                        }
                        else if (builder.peek().node(memberIdentifier) != null)
                        {
                            bean.addItem(memberIdentifier, memberRole, ItemType.NODE);
                        }
                        else
                        {
                            logger.debug(
                                    "Excluding Point {} from Relation {} since it's no longer in the Atlas",
                                    memberIdentifier, relation.getIdentifier());
                        }
                        break;
                    case LINE:
                        if (changeSet.getLineToCreatedEdgesMapping().containsKey(memberIdentifier))
                        {
                            // Replace existing line with created edges
                            changeSet.getLineToCreatedEdgesMapping().get(memberIdentifier)
                                    .forEach(edge ->
                                    {
                                        bean.addItem(edge.getIdentifier(), memberRole,
                                                ItemType.EDGE);
                                        if (edge.hasReverse())
                                        {
                                            bean.addItem(edge.getReverseEdgeIdentifier(),
                                                    memberRole, ItemType.EDGE);
                                        }
                                    });
                        }
                        else if (builder.peek().area(memberIdentifier) != null)
                        {
                            bean.addItem(memberIdentifier, memberRole, ItemType.AREA);
                        }
                        else if (builder.peek().line(memberIdentifier) != null)
                        {
                            bean.addItem(memberIdentifier, memberRole, ItemType.LINE);
                        }
                        else
                        {
                            logger.debug(
                                    "Excluding Line {} from Relation {} since it's no longer in the Atlas",
                                    memberIdentifier, relation.getIdentifier());
                        }
                        break;
                    case RELATION:
                        bean.addItem(memberIdentifier, memberRole, ItemType.RELATION);
                        break;
                    default:
                        throw new CoreException("Unsupported relation member type in Raw Atlas, {}",
                                member.getEntity().getType());
                }
            });

            if (!bean.isEmpty())
            {
                builder.addRelation(relation.getIdentifier(), relation.getOsmIdentifier(), bean,
                        relation.getTags());
            }
            else
            {
                logger.debug("Relation {} bean is empty, dropping from Atlas",
                        relation.getIdentifier());
            }
        });

        logTaskCompletionAsInfo(SECTIONED_ATLAS_CREATION_TASK, getShardOrAtlasName(),
                buildTime.elapsedSince());
        return builder.get();
    }

    /**
     * Updates the {@link AtlasMetaData} with all Atlas Entity configurations.
     *
     * @return the final {@link AtlasMetaData}
     */
    private AtlasMetaData createAtlasMetadata()
    {
        final AtlasMetaData metadata = this.rawAtlas.metaData();
        metadata.getTags().put(AtlasMetaData.EDGE_CONFIGURATION,
                this.loadingOption.getEdgeFilter().toString());
        metadata.getTags().put(AtlasMetaData.AREA_CONFIGURATION,
                this.loadingOption.getAreaFilter().toString());
        metadata.getTags().put(AtlasMetaData.WAY_SECTIONING_CONFIGURATION,
                this.loadingOption.getWaySectionFilter().toString());
        return metadata;
    }

    /**
     * Creates an {@link AtlasSize} estimate for the final {@link Atlas}.
     *
     * @param changeSet
     *            The {@link WaySectionChangeSet} to use for calculations
     * @return the resulting {@link AtlasSize}
     */
    private AtlasSize createAtlasSizeEstimate(final WaySectionChangeSet changeSet)
    {
        final int numberOfAreas = changeSet.getLinesThatBecomeAreas().size();
        int numberOfEdges = 0;
        for (final TemporaryEdge edge : changeSet.getCreatedEdges())
        {
            if (edge.hasReverse())
            {
                numberOfEdges = numberOfEdges + 2;
            }
            else
            {
                numberOfEdges++;
            }
        }

        // We don't use numberOfEdges above here, since we don't want to factor in reverse edges. We
        // only care about the absolute number of lines that become edges
        final long numberOfLines = this.rawAtlas.numberOfLines()
                - (numberOfAreas + changeSet.getLineToCreatedEdgesMapping().keySet().size());

        return new AtlasSize(numberOfEdges, changeSet.getPointsThatBecomeNodes().size(),
                numberOfAreas, numberOfLines, changeSet.getPointsThatStayPoints().size(),
                this.rawAtlas.numberOfRelations());
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
            if (this.loadedShards.size() > 1)
            {
                // The first shard is always the initial one. Use its bounds to build the atlas.
                final Rectangle originalShardBounds = this.loadedShards.get(0).bounds();
                return atlas.subAtlas(originalShardBounds).get();
            }
            else
            {
                // We don't need to cut anything away, since no other shards were loaded
                return atlas;
            }
        }
        catch (final Exception e)
        {
            logger.error("Error creating sub-atlas for original shard bounds", e);
            return null;
        }
    }

    /**
     * This function distinguishes between raw atlas points that will become {@link Point}s in the
     * final {@link Atlas} and those that are simple shape points. If a raw atlas point doesn't
     * become a node or point in the final atlas, it's a shape point and will be tracked by the
     * underlying polyline of the area, line or edge that it's a part of.
     *
     * @param changeSet
     *            The {@link WaySectionChangeSet} to track any updates
     */
    private void distinguishPointsFromShapePoints(final WaySectionChangeSet changeSet)
    {
        final Time time = logTaskStartedAsInfo(SHAPE_POINT_DETECTION_TASK, getShardOrAtlasName());
        StreamSupport.stream(this.rawAtlas.points().spliterator(), true)
                .filter(point -> isAtlasPoint(changeSet, point)).forEach(changeSet::recordPoint);
        logTaskCompletionAsInfo(SHAPE_POINT_DETECTION_TASK, getShardOrAtlasName(),
                time.elapsedSince());
    }

    private String getShardOrAtlasName()
    {
        // Default to getting the Shard name, if available, otherwise fall back to atlas name
        if (!this.loadedShards.isEmpty())
        {
            return this.loadedShards.get(0).getName();
        }
        else
        {
            return this.rawAtlas.getName();
        }
    }

    /**
     * This function takes care of identifying any raw atlas {@link Line}s that will become
     * {@link Edge}s or {@link Area}s. If we happen to find an {@link Edge}, then we will loop
     * through its shape points and identify all the {@link Node}s. The {@link WaySectionChangeSet}
     * will be used to track of any entity updates. It's important to note that can avoid any future
     * spatial queries by doing them here and creating a mapping to use later for splitting
     * {@link Line}s into {@link Edge}s.
     *
     * @param changeSet
     *            The {@link WaySectionChangeSet} to track any updates
     */
    private void identifyEdgesNodesAndAreasFromLines(final WaySectionChangeSet changeSet)
    {
        final Time time = logTaskStartedAsInfo(ATLAS_FEATURE_DETECTION_TASK, getShardOrAtlasName());

        StreamSupport.stream(this.rawAtlas.lines().spliterator(), true).forEach(line ->
        {
            if (isAtlasEdge(line))
            {
                final NodeOccurrenceCounter nodesForEdge = new NodeOccurrenceCounter();
                final PolyLine polyLine = line.asPolyLine();
                final Set<Location> selfIntersections = polyLine.selfIntersections();

                // Ignoring repeated consecutive points, identify all rings - they will have a
                // self-intersection at the start/end - ignore it.
                if (line.isClosed()
                        && polyLine.withoutDuplicateConsecutiveShapePoints().occurrences(
                                polyLine.first()) < MINIMUM_NUMBER_OF_SELF_INTERSECTIONS_FOR_A_NODE)
                {
                    selfIntersections.remove(polyLine.first());
                }

                // Identify all nodes. We care about three cases: 1. self-intersections, if the way
                // contains a repeated location 2. sectioning based on tagging (ex. at a barrier) 3.
                // at an intersection with another edge
                for (int index = 0; index < polyLine.size(); index++)
                {
                    final Location shapePoint = polyLine.get(index);

                    // 1. We found a repeated location, mark it as a node
                    if (selfIntersections.contains(shapePoint))
                    {
                        addPointToNodeList(shapePoint, nodesForEdge);
                        continue;
                    }

                    // 2. Check if we need to section based on tagging
                    if (shouldSectionAtLocation(shapePoint))
                    {
                        addPointToNodeList(shapePoint, nodesForEdge);
                        continue;
                    }

                    // 3. Check if there is an edge intersection of the same layer at this location
                    if (locationIsPartOfAnIntersectingEdgeOfTheSameLayer(shapePoint, line))
                    {
                        addPointToNodeList(shapePoint, nodesForEdge);
                    }
                }

                if (line.isClosed() && nodesForEdge.size() == 0)
                {
                    // Handle isolated rings by creating a node at the start and the middle
                    // shape point, effectively splitting it in half.
                    addPointToNodeList(polyLine.first(), nodesForEdge);
                    addPointToNodeList(polyLine.get(polyLine.size() / 2), nodesForEdge);
                }
                else if (!line.isClosed())
                {
                    // For non-closed lines, the first and last point of the edge are always nodes.
                    // We don't care about first/last points for rings because these can be
                    // arbitrary and we only want to section at edge intersections.
                    addPointToNodeList(polyLine.first(), nodesForEdge);
                    addPointToNodeList(polyLine.last(), nodesForEdge);
                }

                // Record the edge and all its nodes
                changeSet.createEdgeToNodeMapping(line.getIdentifier(), nodesForEdge);
            }
            else if (isAtlasArea(line))
            {
                changeSet.recordArea(line);
            }
            else if (isAtlasLine(line))
            {
                // No-op. Unless a line becomes an area, edge or is excluded from the Atlas, it will
                // stay a line. It's easier to track of exclusions than lines that stay as lines
            }
            else
            {
                changeSet.recordExcludedLine(line);
                logger.debug(
                        "Excluding line {} from Atlas, it's not defined by an Atlas edge, area or line",
                        line.getIdentifier());
            }
        });

        logTaskCompletionAsInfo(ATLAS_FEATURE_DETECTION_TASK, getShardOrAtlasName(),
                time.elapsedSince());
    }

    /**
     * Determines if the given raw atlas {@link Line} qualifies to be an {@link Area} in the final
     * atlas. An Atlas {@link Area} is defined as being closed, with more than 3 shape points and
     * meeting some tag requirements. Relies on the underlying {@link AtlasLoadingOption}
     * configuration to make the decision regarding tags.
     *
     * @param line
     *            The {@link Line} to test
     * @return {@code true} if the given {@link Line} qualifies as an {@link Area}
     */
    private boolean isAtlasArea(final Line line)
    {
        return line.isClosed() && qualifiesAsArea(line)
                && this.loadingOption.getAreaFilter().test(line);
    }

    /**
     * Determines if the given raw atlas {@link Line} qualifies to be an {@link Edge} in the final
     * atlas. Relies on the underlying {@link AtlasLoadingOption} configuration to make the
     * decision.
     *
     * @param line
     *            The {@link Line} to check
     * @return {@code true} if the given raw atlas {@link Line} qualifies to be an {@link Edge} in
     *         the final atlas.
     */
    private boolean isAtlasEdge(final Line line)
    {
        return this.loadingOption.getEdgeFilter().test(line);
    }

    /**
     * Determines if the given raw atlas {@link Line} qualifies to be a {@link Line} in the final
     * atlas. An Atlas {@link Line} must not be an Edge, cannot be closed or be a single location.
     *
     * @param line
     *            The {@link Line} to check
     * @return {@code true} if the given raw atlas {@link Line} qualifies to be a {@link Line} in
     *         the final atlas.
     */
    private boolean isAtlasLine(final Line line)
    {
        return !isAtlasEdge(line) && (!line.isClosed() || line.numberOfShapePoints() == 1);
    }

    /**
     * Determines if the given raw atlas {@link Point} qualifies to be a {@link Node} in the final
     * atlas. This is a simple lookup to check if the previously discovered node list contains the
     * given point's identifier.
     *
     * @param changeSet
     *            The {@link WaySectionChangeSet} to use
     * @param point
     *            The {@link Point} to check
     * @return {@code true} if the given raw atlas {@link Point} qualifies to be a {@link Node} in
     *         the final atlas.
     */
    private boolean isAtlasNode(final WaySectionChangeSet changeSet, final Point point)
    {
        return changeSet.getPointsThatBecomeNodes().contains(point.getIdentifier());
    }

    /**
     * Determines if the given raw atlas {@link Point} qualifies to be a {@link Point} in the final
     * atlas.
     *
     * @param changeSet
     *            The {@link WaySectionChangeSet} to use
     * @param point
     *            The {@link Point} to check
     * @return {@code true} if the given raw atlas {@link Point} qualifies to be a {@link Point} in
     *         the final atlas.
     */
    private boolean isAtlasPoint(final WaySectionChangeSet changeSet, final Point point)
    {
        final boolean hasExplicitOsmTags = pointHasExplicitOsmTags(point);
        final boolean isRelationMember = !point.relations().isEmpty();

        if (isRelationMember && !hasExplicitOsmTags && !isAtlasNode(changeSet, point))
        {
            // When the OSM node is part of a relation, doesn't have explicit OSM tagging and is not
            // at an intersection (not an atlas node), then we want to create an atlas point so we
            // don't lose this node as a member of our relation.
            return true;
        }

        final boolean isIsolatedNode = Iterables
                .isEmpty(this.rawAtlas.linesContaining(point.getLocation()));
        if (!isRelationMember && !hasExplicitOsmTags && isIsolatedNode)
        {
            // This is a special case - when an OSM node is not part of a relation, doesn't have
            // explicit OSM tagging and is not a part of an OSM way, then we want to bring it in as
            // a stand-alone Atlas point and differentiate this case from a simple shape point.
            return true;
        }

        // All other times, we use the presence of explicit OSM tagging to determine if it's a point
        return hasExplicitOsmTags;
    }

    /**
     * Determines whether the given {@link Line} has any intersecting {@link Line}s, with the same
     * layer tag value that meet the definition of an {@link Edge}, running through it at the given
     * {@link Location}.
     *
     * @param location
     *            The {@link Location} to use
     * @param line
     *            The {@link Line} to use
     * @return {@code true} if there is at least one {@link Edge} with the same layer tag value
     *         intersecting the given line at the given location
     */
    private boolean locationIsPartOfAnIntersectingEdgeOfTheSameLayer(final Location location,
            final Line line)
    {
        final long targetLayerValue = LayerTag.getTaggedOrImpliedValue(line, 0L);

        // TODO - Getting non-intersecting lines from the spatial query results.
        // So explicitly specifying "contains shapePoint". Need to resolve this!
        return Iterables
                // Find all intersecting edges
                .stream(this.rawAtlas.linesContaining(location,
                        target -> target.getIdentifier() != line.getIdentifier()
                                && isAtlasEdge(target) && target.asPolyLine().contains(location)))
                // Check whether that edge has the same layer value as the line we're looking at
                .anyMatch(candidate ->
                {
                    final long layerValue = LayerTag.getTaggedOrImpliedValue(candidate, 0L);
                    return targetLayerValue == layerValue;
                });
    }

    private void logTaskCompletionAsInfo(final String taskName, final String shardName,
            final Duration duration)
    {
        logger.info(COMPLETED_TASK_MESSAGE, taskName, shardName, duration);
    }

    private void logTaskCompletionAsTrace(final String taskName, final String shardName,
            final Duration duration)
    {
        logger.trace(COMPLETED_TASK_MESSAGE, taskName, shardName, duration);
    }

    private Time logTaskStartedAsInfo(final String taskname, final String shardName)
    {
        final Time time = Time.now();
        logger.info(STARTED_TASK_MESSAGE, taskname, shardName);
        return time;
    }

    /**
     * Each Atlas entity will have a base set of tags added by Atlas generation (see
     * {@link AtlasTag#TAGS_FROM_OSM}). Each entity can also have additional synthetic tags (see
     * {@link AtlasTag#TAGS_FROM_ATLAS}). By default, all OSM {@link Node}s are ingested as Raw
     * Atlas Points, even if they are shape points. Our goal here is to only keep the points that
     * have explicit OSM tags as Atlas Points and drop any shape points. This method will calculate
     * the total number of base + synthetic tags for a given {@link Point} and return {@code true}
     * if the {@link Point} contains other, non-base and non-synthetic, tags. The logic is careful
     * not to count the synthetic tags as OSM tags, to avoid adding Points at extra locations.
     *
     * @param point
     *            The {@link Point} to look at
     * @return {@code true} if the {@link Point} contains explicit OSM tagging
     */
    private boolean pointHasExplicitOsmTags(final Point point)
    {
        final int pointTagSize = point.getTags().size();
        final int osmAndAtlasTagCount;

        if (pointTagSize > AtlasTag.TAGS_FROM_OSM.size())
        {
            int atlasTagCounter = 0;
            for (final String tag : point.getTags().keySet())
            {
                // Tags from atlas are the tags that only some points will have
                if (AtlasTag.TAGS_FROM_ATLAS.contains(tag))
                {
                    atlasTagCounter++;
                }
            }
            osmAndAtlasTagCount = AtlasTag.TAGS_FROM_OSM.size() + atlasTagCounter;
        }
        else if (pointTagSize < AtlasTag.TAGS_FROM_OSM.size())
        {
            osmAndAtlasTagCount = pointTagSize;
        }
        else
        {
            osmAndAtlasTagCount = AtlasTag.TAGS_FROM_OSM.size();
        }

        return pointTagSize > osmAndAtlasTagCount;
    }

    /**
     * Determines if the given {@link Line} contains more than
     * {@link #MINIMUM_SHAPE_POINTS_TO_QUALIFY_AS_AREA} shape points. The reason to use 3 is because
     * OSM forces closed polygons. If we have only 3 nodes (a -> b -> c), then it's not a polygon.
     * If we have (a -> b -> a), then we have a flat line. At minimum, we are looking for (a -> b ->
     * c -> a).
     *
     * @param line
     *            The {@link Line} to check
     * @return {@code true} if given {@link Line} contains more than
     *         {@link #MINIMUM_SHAPE_POINTS_TO_QUALIFY_AS_AREA} shape points
     */
    private boolean qualifiesAsArea(final Line line)
    {
        return line.numberOfShapePoints() > MINIMUM_SHAPE_POINTS_TO_QUALIFY_AS_AREA;
    }

    /**
     * Sections all lines that are set to become edges in the final atlas.
     *
     * @param changeSet
     *            The {@link WaySectionChangeSet} to rely on for keeping track of updates
     */
    private void sectionEdges(final WaySectionChangeSet changeSet)
    {
        final Time sectionTime = logTaskStartedAsInfo(EDGE_SECTIONING_TASK, getShardOrAtlasName());

        changeSet.getLinesThatBecomeEdges().forEach(lineIdentifier ->
        {
            final Line line = this.rawAtlas.line(lineIdentifier);
            final List<TemporaryEdge> edges;
            if (line.isClosed())
            {
                edges = splitRingLineIntoEdges(changeSet, line);
            }
            else
            {
                edges = splitNonRingLineIntoEdges(changeSet, line);
            }
            changeSet.createLineToEdgeMapping(line, edges);
        });

        logTaskCompletionAsInfo(EDGE_SECTIONING_TASK, getShardOrAtlasName(),
                sectionTime.elapsedSince());
    }

    /**
     * Determines if we should section at the given {@link Location}. Relies on the underlying
     * {@link AtlasLoadingOption} configuration to make the decision. If {@link true}, this implies
     * the point at this {@link Location} should be a {@link Node}.
     *
     * @param location
     *            The {@link Location} to check
     * @return {@code true} if we should section at the given {@link Location}
     */
    private boolean shouldSectionAtLocation(final Location location)
    {
        return Iterables.stream(this.rawAtlas.pointsAt(location))
                .anyMatch(point -> this.loadingOption.getWaySectionFilter().test(point));
    }

    /**
     * Splits a non-ring {@link Line} into a list of {@link TemporaryEdge}s. The logic consists of
     * iterating through all the line shape points, trying to match each one to a
     * {@link TemporaryNode} for this {@link Line}. If we find a match, then we create a
     * corresponding {@link TemporaryEdge}, making sure to reverse the polyline if the original line
     * was reversed and to note whether we need to create a corresponding reverse edge.
     *
     * @param changeSet
     *            The {@link WaySectionChangeSet} to use
     * @param line
     *            The {@link Line} to split
     * @return a list of resulting {@link TemporaryEdge}s
     */
    private List<TemporaryEdge> splitNonRingLineIntoEdges(final WaySectionChangeSet changeSet,
            final Line line)
    {
        final List<TemporaryEdge> newEdgesForLine = new ArrayList<>();
        final PolyLine polyline = line.asPolyLine();
        if (polyline.size() < MINIMUM_POINTS_TO_QUALIFY_AS_A_LINE)
        {
            logger.error("Line {} hass less than {} points, cannot be sectioned!",
                    line.getIdentifier(), MINIMUM_POINTS_TO_QUALIFY_AS_A_LINE);
            return newEdgesForLine;
        }

        // Prepare the nodes identifiers, identifier factory and one way information
        final NodeOccurrenceCounter nodesToSectionAt = changeSet.getNodesForEdge(line);
        final WaySectionIdentifierFactory identifierFactory = new WaySectionIdentifierFactory(
                line.getIdentifier());

        // Determines if we need to reverse the polyline and if a reverse edge is needed
        final PbfOneWay oneWay = PbfOneWay.forTag(line);
        final boolean hasReverseEdge = oneWay == PbfOneWay.NO;
        final boolean isReversed = oneWay == PbfOneWay.REVERSED;

        // Keep track of the start index, start/end nodes
        int startIndex;
        Optional<TemporaryNode> startNode = Optional.empty();
        Optional<TemporaryNode> endNode = Optional.empty();

        try
        {
            // Find the first node
            startIndex = 0;
            startNode = nodesToSectionAt.getNode(polyline.first());
            if (!startNode.isPresent())
            {
                // Weren't able to find the starting node, abort slicing
                logger.error(
                        "Can't find starting Node for Line {} during way-sectioning. Aborting!",
                        line.getIdentifier());
                return newEdgesForLine;
            }

            // Keep track of duplicate polyline locations to avoid single node edges
            final Map<Long, Integer> duplicateLocations = new HashMap<>();

            // We've already processed the starting node, so start with the first index
            for (int index = 1; index < polyline.size(); index++)
            {
                // Check to see if this location is a node
                endNode = nodesToSectionAt.getNode(polyline.get(index));
                if (endNode.isPresent())
                {
                    final TemporaryNode end = endNode.get();

                    // Avoid sectioning at consecutive repeated points
                    if (end.equals(startNode.get())
                            && polyline.get(index).equals(polyline.get(index - 1)))
                    {
                        // Found a duplicate point, update the map and skip over it
                        final long startIdentifier = startNode.get().getIdentifier();
                        final int duplicateCount = duplicateLocations.containsKey(startIdentifier)
                                ? duplicateLocations.get(startIdentifier) : 0;
                        duplicateLocations.put(startIdentifier, duplicateCount + 1);
                        continue;
                    }

                    // Update end point occurrence to factor in duplicates
                    final Integer duplicates = duplicateLocations.get(end.getIdentifier());
                    if (duplicates != null)
                    {
                        for (int duplicate = 0; duplicate < duplicates; duplicate++)
                        {
                            nodesToSectionAt.incrementOccurrence(end);
                        }
                    }

                    // We found the end node, create the edge. Note: using occurrence minus one
                    // since PolyLine uses zero-based numbering. We are incrementing only the
                    // start node occurrence, since the end node will either be used as a future
                    // start node or be the end of the way, in which case we don't care.
                    final int startOccurrence = nodesToSectionAt.getOccurrence(startNode.get()) - 1;
                    nodesToSectionAt.incrementOccurrence(startNode.get());
                    final int endOccurrence = nodesToSectionAt.getOccurrence(end) - 1;

                    // Build the underlying polyline and reverse it, if necessary
                    final PolyLine rawPolyline = polyline.between(polyline.get(startIndex),
                            startOccurrence, polyline.get(index), endOccurrence);
                    final PolyLine edgePolyline = isReversed ? rawPolyline.reversed() : rawPolyline;

                    // If the line (OSM way) was split, start the identifier at 001, otherwise
                    // identifier will start at 000.
                    final long edgeIdentifier;
                    if (!line.isClosed() && nodesToSectionAt.size() == 2
                            && polyline.size() - 1 == index && newEdgesForLine.isEmpty())
                    {
                        // The only time we want to do this is if there are two nodes, the line
                        // isn't a ring, the last node is the end of the polyline and if we haven't
                        // split this line previously
                        edgeIdentifier = line.getIdentifier();
                    }
                    else
                    {
                        edgeIdentifier = identifierFactory.nextIdentifier();
                    }

                    newEdgesForLine.add(new TemporaryEdge(edgeIdentifier, edgePolyline,
                            line.getTags(), hasReverseEdge));

                    // Increment starting pointers
                    startIndex = index;
                    startNode = endNode;
                }
            }
        }
        catch (final Exception e)
        {
            throw new CoreException("Failed to way-section line {}", line.getIdentifier(), e);
        }
        return newEdgesForLine;
    }

    /**
     * Splits a ring {@link Line}s into a list of {@link TemporaryEdge}s. The logic consists of
     * iterating through all the line shape points, trying to match each one to a
     * {@link TemporaryNode} for this {@link Line}. If we find a match, then we create a
     * corresponding {@link TemporaryEdge}, making sure to reverse the polyline if the original line
     * was reversed and to note whether we need to create a corresponding reverse edge. This main
     * difference between this and the non-ring split method is that this one looks specifically for
     * rings and avoid splitting at the first polyline location, since it is not guaranteed to be a
     * node.
     *
     * @param changeSet
     *            The {@link WaySectionChangeSet} to use
     * @param line
     *            The {@link Line} to split
     * @return a list of resulting {@link TemporaryEdge}s
     */
    private List<TemporaryEdge> splitRingLineIntoEdges(final WaySectionChangeSet changeSet,
            final Line line)
    {
        final List<TemporaryEdge> newEdgesForLine = new ArrayList<>();
        final PolyLine polyline = line.asPolyLine();
        if (polyline.size() < MINIMUM_POINTS_TO_QUALIFY_AS_A_LINE)
        {
            logger.error("Line {} hass less than {} points, cannot be sectioned!",
                    line.getIdentifier(), MINIMUM_POINTS_TO_QUALIFY_AS_A_LINE);
            return newEdgesForLine;
        }

        // Prepare the nodes identifiers, identifier factory and one way information
        final NodeOccurrenceCounter nodesToSectionAt = changeSet.getNodesForEdge(line);
        final WaySectionIdentifierFactory identifierFactory = new WaySectionIdentifierFactory(
                line.getIdentifier());

        // Determines if we need to reverse the polyline and if a reverse edge is needed
        final PbfOneWay oneWay = PbfOneWay.forTag(line);
        final boolean hasReverseEdge = oneWay == PbfOneWay.NO;
        final boolean isReversed = oneWay == PbfOneWay.REVERSED;

        // Keep track of the starting index, start/end nodes and relevant flags
        int startIndex = 0;
        PolyLine polyLineUpToFirstNode = null;
        boolean isFirstNode = true;
        Optional<TemporaryNode> startNode = Optional.empty();
        Optional<TemporaryNode> endNode = Optional.empty();

        try
        {
            startNode = nodesToSectionAt.getNode(polyline.first());

            if (startNode.isPresent())
            {
                // The first node is the start of the ring, treat it as a flat line
                return splitNonRingLineIntoEdges(changeSet, line);
            }
            else
            {
                // Keep track of duplicate polyline locations to avoid single node edges
                final Map<Location, Integer> duplicateLocations = new HashMap<>();
                duplicateLocations.put(polyline.first(), 1);

                for (int index = 1; index < polyline.size(); index++)
                {
                    final Location currentLocation = polyline.get(index);
                    endNode = nodesToSectionAt.getNode(currentLocation);

                    // Keep track of duplicate start locations
                    if (!endNode.isPresent() && !startNode.isPresent())
                    {
                        final int duplicateCount = duplicateLocations.containsKey(currentLocation)
                                ? duplicateLocations.get(currentLocation) : 0;
                        duplicateLocations.put(currentLocation, duplicateCount + 1);
                    }

                    // Check to see if this location is a node
                    if (endNode.isPresent())
                    {
                        final TemporaryNode end = endNode.get();

                        if (!isFirstNode)
                        {
                            // Avoid sectioning at consecutive repeated points
                            if (end.equals(startNode.get())
                                    && polyline.get(index).equals(polyline.get(index - 1)))
                            {
                                // Found a duplicate point, update the map and skip over it
                                final int duplicateCount = duplicateLocations
                                        .containsKey(currentLocation)
                                                ? duplicateLocations.get(currentLocation) : 0;
                                duplicateLocations.put(currentLocation, duplicateCount + 1);
                                continue;
                            }

                            // Update end point occurrence to factor in duplicates
                            final Integer duplicates = duplicateLocations.get(currentLocation);
                            if (duplicates != null)
                            {
                                for (int duplicate = 0; duplicate < duplicates; duplicate++)
                                {
                                    nodesToSectionAt.incrementOccurrence(end);
                                }
                            }

                            // We only want to create an edge if we've started from a node. If we've
                            // started from a shape point, we've just encountered our first node.
                            final PolyLine rawPolyline = polyline.between(polyline.get(startIndex),
                                    nodesToSectionAt.getOccurrence(startNode.get()) - 1,
                                    polyline.get(index), nodesToSectionAt.getOccurrence(end) - 1);
                            final PolyLine edgePolyline = isReversed ? rawPolyline.reversed()
                                    : rawPolyline;

                            newEdgesForLine
                                    .add(new TemporaryEdge(identifierFactory.nextIdentifier(),
                                            edgePolyline, line.getTags(), hasReverseEdge));

                            // Increment start node occurrence
                            nodesToSectionAt.incrementOccurrence(startNode.get());
                        }

                        // Update starting points
                        startIndex = index;
                        startNode = endNode;

                        // Found the first node, save the polyline from the first location to the
                        // first node so we can append it later
                        if (isFirstNode)
                        {
                            final PolyLine rawPolyline = polyline.between(polyline.first(), 0,
                                    polyline.get(index), 0);
                            polyLineUpToFirstNode = isReversed ? rawPolyline.reversed()
                                    : rawPolyline;
                            isFirstNode = false;
                        }
                    }

                    // If we're at the last index, we've come full circle, back to the starting
                    // point. We need to append the polyline we built leading up to the first node
                    // to the polyline we've built so far.
                    if (index == polyline.size() - 1)
                    {
                        if (polyLineUpToFirstNode == null)
                        {
                            throw new CoreException(
                                    "Cannot section ring {} - reached end of ring without valid end node",
                                    line.getIdentifier());
                        }

                        // Get the raw polyline from the last node to the last(first) location
                        final int endOccurence = duplicateLocations.containsKey(currentLocation)
                                ? duplicateLocations.get(currentLocation) : 1;
                        final PolyLine rawPolylineFromLastNodeToLastLocation = polyline.between(
                                polyline.get(startIndex),
                                nodesToSectionAt.getOccurrence(startNode.get()) - 1,
                                currentLocation, endOccurence);

                        final PolyLine edgePolyLine;
                        if (isReversed)
                        {
                            // If the line is reversed - reverse the raw polyline we just found and
                            // append it to the polyline we had saved
                            edgePolyLine = polyLineUpToFirstNode
                                    .append(rawPolylineFromLastNodeToLastLocation.reversed());
                        }
                        else
                        {
                            // Append the polyline we had saved to the polyline we just found
                            edgePolyLine = rawPolylineFromLastNodeToLastLocation
                                    .append(polyLineUpToFirstNode);
                        }

                        final TemporaryEdge edge = new TemporaryEdge(
                                identifierFactory.nextIdentifier(), edgePolyLine, line.getTags(),
                                hasReverseEdge);
                        newEdgesForLine.add(edge);
                    }
                }
            }
        }
        catch (final Exception e)
        {
            throw new CoreException("Failed to way-section line {}", line.getIdentifier(), e);
        }
        return newEdgesForLine;
    }
}
