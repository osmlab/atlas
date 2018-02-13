package org.openstreetmap.atlas.geography.atlas.raw.sectioning;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
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
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.ManMadeTag;
import org.openstreetmap.atlas.tags.RailwayTag;
import org.openstreetmap.atlas.tags.RouteTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.time.Time;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 *
 * @author mgostintsev
 */
public class WaySectionProcessor
{
    private static final Logger logger = LoggerFactory.getLogger(WaySectionProcessor.class);

    private static final int MINIMUM_SHAPE_POINTS_TO_QUALIFY_AS_AREA = 3;
    private static final int MINIMUM_POINTS_TO_QUALIFY_AS_A_LINE = 2;

    private final Atlas rawAtlas;
    private final Sharding shardingTree;
    private final Function<Shard, Optional<Atlas>> atlasFetcher;
    private final AtlasLoadingOption loadingOption;

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
        this(rawAtlas, loadingOption, null, null);
    }

    /**
     * Sections the given raw {@link Atlas} and guarantees consistent identifiers for all Atlas
     * files obtained by using the given fetcher policy. If the sharding tree and atlas fetcher
     * function is not provided, then no expansion will be done and only the given atlas will be
     * way-sectioned. Note: the sharding tree must the same as the one used to generate the input
     * raw Atlas.
     *
     * @param rawAtlas
     *            The raw {@link Atlas} to section
     * @param loadingOption
     *            The {@link AtlasLoadingOption} to use
     * @param shardingTree
     *            The {@link Sharding} tree to use to know which neighboring atlas files to get
     * @param atlasFetcher
     *            The fetching policy to use to obtain adjacent atlas files
     */
    public WaySectionProcessor(final Atlas rawAtlas, final AtlasLoadingOption loadingOption,
            final Sharding shardingTree, final Function<Shard, Optional<Atlas>> atlasFetcher)
    {
        this.rawAtlas = rawAtlas;
        this.shardingTree = shardingTree;
        this.loadingOption = loadingOption;
        this.atlasFetcher = atlasFetcher;
    }

    /**
     * Slices the given raw {@link Atlas}.
     *
     * @return the way-sectioned {@link Atlas}
     */
    public Atlas run()
    {
        final Time time = Time.now();

        // Create a changeset to keep track of any intermediate state transitions (points becomes
        // nodes, lines becoming areas, etc.)
        final WaySectionChangeSet changeSet = new WaySectionChangeSet();

        // Go through all the Lines and determine what will become an edge, area or stay a line.
        // This includes node detection through edge intersection and tagging configuration.
        identifyEdgesNodesAndAreasFromLines(changeSet);

        // Separate points and shape points
        distinguishPointsFromShapePoints(changeSet);

        // Go through the identified edges and split them at the identified nodes
        sectionEdges(changeSet);

        final Atlas atlas = buildSectionedAtlas(changeSet);
        logger.info("Finished way-sectioning atlas {} in {}", atlas.getName(), time.untilNow());

        return atlas;
    }

    // TODO add statistics

    /**
     * Adds a {@link TemporaryNode} for the given {@link Location} to the given
     * {@link NodeOccurenceCounter} . Note: there should only be a single raw atlas {@link Point} at
     * the given {@link Location}.
     *
     * @param location
     *            The {@link Location} of the node to add
     * @param nodeCounter
     *            The {@link NodeOccurenceCounter} to add to
     */
    private void addPointToNodeList(final Location location, final NodeOccurenceCounter nodeCounter)
    {
        this.rawAtlas.pointsAt(location).forEach(point -> nodeCounter
                .addNode(new TemporaryNode(point.getIdentifier(), point.getLocation())));
    }

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
        // Create builder and set properties
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        final AtlasSize sizeEstimate = createAtlasSizeEstimate(changeSet);
        builder.setSizeEstimates(sizeEstimate);
        builder.setMetaData(this.rawAtlas.metaData());

        // Points
        changeSet.getPointsThatStayPoints().forEach(pointIdentifier ->
        {
            final Point point = this.rawAtlas.point(pointIdentifier);
            builder.addPoint(pointIdentifier, point.getLocation(), point.getTags());
        });

        // Nodes
        changeSet.getPointsThatBecomeNodes().forEach(temporaryNode ->
        {
            final Point point = this.rawAtlas.point(temporaryNode.getIdentifier());
            builder.addNode(point.getIdentifier(), point.getLocation(), point.getTags());
        });

        // Lines
        this.rawAtlas
                .lines(line -> !changeSet.getLinesThatBecomeAreas().contains(line.getIdentifier())
                        && !changeSet.getLinesThatBecomeEdges().contains(line.getIdentifier()))
                .forEach(lineToKeep ->
                {
                    // Add any line that didn't become an edge or area
                    builder.addLine(lineToKeep.getIdentifier(), lineToKeep.asPolyLine(),
                            lineToKeep.getTags());
                });

        // Edges
        changeSet.getCreatedEdges().forEach(temporaryEdge ->
        {
            builder.addEdge(temporaryEdge.getIdentifier(), temporaryEdge.getPolyLine(),
                    temporaryEdge.getTags());

            // Add the reverse edge, if needed
            if (temporaryEdge.hasReverse())
            {
                builder.addEdge(temporaryEdge.getReversedIdentifier(),
                        temporaryEdge.getPolyLine().reversed(), temporaryEdge.getTags());
            }
        });

        // Areas
        changeSet.getLinesThatBecomeAreas().forEach(areaIdentifier ->
        {
            final Line line = this.rawAtlas.line(areaIdentifier);
            builder.addArea(areaIdentifier, new Polygon(line.asPolyLine()), line.getTags());
            // TODO fix the second to last point repeated issue. Remove last point!
        });

        // Relations
        this.rawAtlas.relationsLowerOrderFirst().forEach(relation ->
        {
            final RelationBean bean = new RelationBean();
            relation.members().forEach(member ->
            {
                final long memberIdentifier = member.getEntity().getIdentifier();
                switch (member.getEntity().getType())
                {
                    case POINT:
                        if (builder.peek().point(memberIdentifier) != null)
                        {
                            bean.addItem(memberIdentifier, member.getRole(), ItemType.POINT);
                        }
                        else if (builder.peek().node(memberIdentifier) != null)
                        {
                            bean.addItem(memberIdentifier, member.getRole(), ItemType.NODE);
                        }
                        else
                        {
                            throw new CoreException(
                                    "Could not find corresponding Atlas entity for Point {} in Relation {}",
                                    memberIdentifier, relation.getIdentifier());
                        }
                        break;
                    case LINE:
                        if (changeSet.getLineToCreatedEdgesMapping().containsKey(memberIdentifier))
                        {
                            // Replace existing line with created Edges
                            changeSet.getLineToCreatedEdgesMapping().allValues().forEach(edge ->
                            {
                                bean.addItem(edge.getIdentifier(), member.getRole(), ItemType.EDGE);
                                if (edge.hasReverse())
                                {
                                    bean.addItem(edge.getReversedIdentifier(), member.getRole(),
                                            ItemType.EDGE);
                                }
                            });
                        }
                        else if (builder.peek().area(memberIdentifier) != null)
                        {
                            bean.addItem(memberIdentifier, member.getRole(), ItemType.AREA);
                        }
                        else if (builder.peek().line(memberIdentifier) != null)
                        {
                            bean.addItem(memberIdentifier, member.getRole(), ItemType.LINE);
                        }
                        else
                        {
                            throw new CoreException(
                                    "Could not find corresponding Atlas entity for Line {} in Relation {}",
                                    memberIdentifier, relation.getIdentifier());
                        }
                        break;
                    case RELATION:
                        bean.addItem(memberIdentifier, member.getRole(), ItemType.RELATION);
                        break;
                    default:
                        throw new CoreException("Unsupported relation member type in Raw Atlas, {}",
                                member.getEntity().getType());
                }
            });
            builder.addRelation(relation.getIdentifier(), relation.getOsmIdentifier(), bean,
                    relation.getTags());
        });

        return builder.get();
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
    private boolean containsMoreThanThreeShapePoints(final Line line)
    {
        return line.size() > MINIMUM_SHAPE_POINTS_TO_QUALIFY_AS_AREA;
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
        final int numberOfAreas = changeSet.getLinesThatBecomeAreas().size();
        final long numberOfLines = this.rawAtlas.numberOfLines()
                - (numberOfAreas + changeSet.getCreatedEdges().size());

        return new AtlasSize(numberOfEdges, changeSet.getPointsThatBecomeNodes().size(),
                numberOfAreas, numberOfLines, changeSet.getPointsThatStayPoints().size(),
                this.rawAtlas.numberOfRelations());
    }

    /**
     * This function distinguishes between raw atlas points that will become {@link Point}s in the
     * final {@link Atlas} or be simple shape points. We also explicitly call out the case where a
     * raw atlas point may qualify to be an atlas {@link Node} by its tagging, but is not part of an
     * {@link Edge}. In order to avoid creating floating {@link Node}s, we leave them as
     * {@link Point}s in the final atlas and log the occurrence.
     *
     * @param changeSet
     *            The {@link WaySectionChangeSet} to track any updates
     */
    private void distinguishPointsFromShapePoints(final WaySectionChangeSet changeSet)
    {
        this.rawAtlas.points().forEach(point ->
        {
            if (shouldSectionAtPoint(point)
                    && !changeSet.getPointsThatBecomeNodes().contains(point.getIdentifier()))
            {
                logger.error("Point {} qualifies to be a Node, but isn't part of an Edge",
                        point.getIdentifier());
                changeSet.recordPoint(point);
            }
            else if (isAtlasPoint(changeSet, point))
            {
                changeSet.recordPoint(point);
            }
            else
            {
                // If a raw atlas point doesn't become a node or point in the way-sectioned atlas,
                // that point is a shape point and will be tracked by the underlying polyline of the
                // Area, Line or Edge that the shape point is a part of. We can safely ignore
                // handling them here.
            }
        });
    }

    /**
     * This function takes care of identifying any raw atlas {@link Line}s that will becomes
     * {@link Edge}s or {@link Area}s. If we happen to find an {@link Edge}, then we will loop
     * through its shape points and identify all the {@link Node}s as well. The
     * {@link WaySectionChangeSet} will be used to track of any entity updates. It's important to
     * note that can avoid any future spatial queries by doing them here and creating a mapping to
     * use later for splitting {@link Line}s into {@link Edge}s.
     *
     * @param changeSet
     *            The {@link WaySectionChangeSet} to track any updates
     */
    private void identifyEdgesNodesAndAreasFromLines(final WaySectionChangeSet changeSet)
    {
        this.rawAtlas.lines().forEach(line ->
        {
            if (isAtlasEdge(line))
            {
                final NodeOccurenceCounter nodesForEdge = new NodeOccurenceCounter();
                final PolyLine polyLine = line.asPolyLine();

                // Find any intersections with other edges
                for (int index = 0; index < polyLine.size(); index++)
                {
                    final Location shapePoint = polyLine.get(index);

                    // Based on a configurable tag filter, a shape point that doesn't intersect
                    // an edge may also become a node
                    if (shouldSectionAtLocation(shapePoint))
                    {
                        addPointToNodeList(shapePoint, nodesForEdge);
                    }

                    // TODO - Getting non-intersecting lines from the spatial query results.
                    // So purposefully specifying "contains shapePoint". Need to resolve this!

                    // If there are other edges that intersect our shape point, it becomes a
                    // node
                    if (locationHasIntersectingLinesMatchingPredicate(shapePoint,
                            target -> target.getIdentifier() != line.getIdentifier()
                                    && isAtlasEdge(target)
                                    && target.asPolyLine().contains(shapePoint)))
                    {
                        final Iterable<Point> pointsAtLocation = this.rawAtlas.pointsAt(shapePoint);
                        if (Iterables.size(pointsAtLocation) > 1)
                        {
                            // This shouldn't happen, so let's log this case and section at all
                            // points. By definition, all stacked (same location) OSM nodes will
                            // be collapsed to a single atlas point during raw atlas creation.
                            logger.error("Detected more than one point at {}", shapePoint);
                        }
                        addPointToNodeList(shapePoint, nodesForEdge);
                    }
                }

                if (line.isClosed() && nodesForEdge.size() == 0)
                {
                    // Handle isolated rings by creating a node at the start and the middle
                    // shape point, effectively splitting it in half
                    addPointToNodeList(polyLine.first(), nodesForEdge);
                    addPointToNodeList(polyLine.get(polyLine.size() / 2), nodesForEdge);
                }
                else if (!line.isClosed())
                {
                    // For non-closed loops, the first and last point of the edge are always nodes.
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
                // No-op. If a line doesn't quality to be an edge or area, it will stay a line in
                // the resulting atlas.
            }
        });
    }

    /**
     * Determines if the given raw atlas {@link Line} qualifies to be an {@link Area} in the final
     * atlas. An Atlas {@link Area} is defined as being closed, with more than 3 shape points and
     * must not contain highway tags, be a ferry, pier or railway.
     *
     * @param line
     *            The {@link Line} to test
     * @return {@code true} if the given {@link Line} qualifies as an {@link Area}
     */
    private boolean isAtlasArea(final Line line)
    {
        return line.isClosed() && containsMoreThanThreeShapePoints(line)
                && !Validators.hasValuesFor(line, HighwayTag.class) && !RouteTag.isFerry(line)
                && !ManMadeTag.isPier(line) && !RailwayTag.isRailway(line);
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
        return !isAtlasEdge(line) && (!line.isClosed() || line.size() == 1);
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

        if (!point.relations().isEmpty() && !hasExplicitOsmTags && !isAtlasNode(changeSet, point))
        {
            // When the OSM node is part of a relation, doesn't have any explicit OSM tagging and is
            // not at an intersection (not an atlas node), then we want to create an atlas point so
            // we don't lose this node as a member of our relation.
            return true;
        }

        // All other times, we defer to the presence of explicit OSM tagging to determine whether
        // it's a point
        return hasExplicitOsmTags;
    }

    /**
     * Determines whether the given {@link Location} has any {@link Line}s that run through it and
     * match the given {@link Predicate}.
     *
     * @param location
     *            The {@link Location} to use
     * @param matcher
     *            The {@link Predicate} to use when looking for intersecting lines
     * @return {@code true} if the given {@link Location} has any {@link Line}s, that match the
     *         given {@link Predicate}, running through it
     */
    private boolean locationHasIntersectingLinesMatchingPredicate(final Location location,
            final Predicate<Line> matcher)
    {
        return Iterables.size(this.rawAtlas.linesContaining(location, matcher)) > 0;
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
            int counter = 0;
            for (final String tag : point.getTags().keySet())
            {
                // Tags from atlas are the tags that only some points will have
                if (AtlasTag.TAGS_FROM_ATLAS.contains(tag))
                {
                    counter++;
                }
            }
            osmAndAtlasTagCount = AtlasTag.TAGS_FROM_OSM.size() + counter;
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
     * TODO
     *
     * @param line
     */
    private void sectionEdges(final WaySectionChangeSet changeSet)
    {
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
     * Determines if we should section at the given {@link Point}. Relies on the underlying
     * {@link AtlasLoadingOption} configuration to make the decision. If {@link true}, this implies
     * the {@link Point} should be a {@link Node}.
     *
     * @param point
     *            The {@link Point} to check
     * @return {@code true} if we should section at the given {@link Point}
     */
    private boolean shouldSectionAtPoint(final Point point)
    {
        return this.loadingOption.getWaySectionFilter().test(point);
    }

    /**
     * TODO
     *
     * @param line
     * @param nodes
     * @return
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
        final NodeOccurenceCounter nodesToSectionAt = changeSet.getNodesForEdge(line);
        final WaySectionIdentifierFactory identifierFactory = new WaySectionIdentifierFactory(
                line.getIdentifier());
        final PbfOneWay oneWay = PbfOneWay.forTag(line);
        final boolean hasReverse = oneWay == PbfOneWay.NO;

        // Keep track of the starting index, start/end nodes
        int startingIndex;
        Optional<TemporaryNode> startNode = Optional.empty();
        Optional<TemporaryNode> endNode = Optional.empty();

        try
        {
            if (oneWay != PbfOneWay.REVERSED)
            {
                // Find the first node
                startingIndex = 0;
                startNode = nodesToSectionAt.getNode(polyline.first());
                if (!startNode.isPresent())
                {
                    // Weren't able to find the starting node, abort slicing
                    logger.error(
                            "Could not find starting Node for Line {} during way-sectioning. Aborting!",
                            line.getIdentifier());
                    return newEdgesForLine;
                }

                // We've already processed the starting node, so we start with the first index
                for (int index = 1; index < polyline.size(); index++)
                {
                    // Check to see if this location is a node
                    endNode = nodesToSectionAt.getNode(polyline.get(index));
                    if (endNode.isPresent())
                    {
                        // We found the end node, create the edge. Note: using occurrence minus one
                        // since PolyLine uses zero-based numbering
                        final PolyLine edgePolyline = polyline.between(polyline.get(startingIndex),
                                nodesToSectionAt.getOccurence(startNode.get()) - 1,
                                polyline.get(index),
                                nodesToSectionAt.getOccurence(endNode.get()) - 1);
                        newEdgesForLine.add(new TemporaryEdge(identifierFactory.nextIdentifier(),
                                edgePolyline, line.getTags(), hasReverse));

                        // Increment start node occurrence and starting identifiers
                        nodesToSectionAt.incrementOccurence(startNode.get());
                        startingIndex = index;
                        startNode = endNode;
                    }
                }
            }
            else
            {
                // Find the first node (start from the back)
                startingIndex = polyline.size() - 1;
                startNode = nodesToSectionAt.getNode(polyline.last());
                if (!startNode.isPresent())
                {
                    // Weren't able to find the starting node, abort slicing
                    logger.error(
                            "Could not find starting Node for Line {} during way-sectioning. Aborting!",
                            line.getIdentifier());
                    return newEdgesForLine;
                }

                // The line is reversed, we need to reverse the direction of the sectioning, and
                // we've already processed the last node, so we start with next to last
                for (int index = polyline.size() - 2; index > 0; index--)
                {
                    // Check to see if this location is a node
                    endNode = nodesToSectionAt.getNode(polyline.get(index));
                    if (endNode.isPresent())
                    {
                        // We found the end node, create the edge. Because we're going from polyline
                        // end, we need to specify indices in occurrence order and then reverse it.
                        // Note: using occurrence minus one since PolyLine uses zero-based numbering
                        final PolyLine edgePolyline = polyline
                                .between(polyline.get(index),
                                        nodesToSectionAt.getOccurence(endNode.get()) - 1,
                                        polyline.get(startingIndex),
                                        nodesToSectionAt.getOccurence(startNode.get()) - 1)
                                .reversed();
                        newEdgesForLine.add(new TemporaryEdge(identifierFactory.nextIdentifier(),
                                edgePolyline, line.getTags(), hasReverse));

                        // Increment start node occurrence and starting identifiers
                        nodesToSectionAt.incrementOccurence(startNode.get());
                        startingIndex = index;
                        startNode = endNode;
                    }
                }
            }
        }
        catch (final Exception e)
        {
            logger.error("Failed to way-section line {}", line.getIdentifier(), e);
        }

        return newEdgesForLine;
    }

    /**
     * TODO
     *
     * @param changeSet
     * @param line
     * @return
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
        final NodeOccurenceCounter nodesToSectionAt = changeSet.getNodesForEdge(line);
        final WaySectionIdentifierFactory identifierFactory = new WaySectionIdentifierFactory(
                line.getIdentifier());
        final PbfOneWay oneWay = PbfOneWay.forTag(line);
        final boolean hasReverse = oneWay == PbfOneWay.NO;

        // Keep track of the starting index, start/end nodes and relevant flags
        int startingIndex = 0;
        PolyLine polyLineUpToFirstNode = null;
        boolean firstLocationNotNode = true;
        Optional<TemporaryNode> startNode = Optional.empty();
        Optional<TemporaryNode> endNode = Optional.empty();

        try
        {
            startNode = nodesToSectionAt.getNode(polyline.first());

            if (startNode.isPresent())
            {
                // We got lucky, the first node is at the start of the polyline. Use existing logic
                // to treat it as a flat line.
                return splitNonRingLineIntoEdges(changeSet, line);
            }
            else
            {
                for (int index = 1; index < polyline.size(); index++)
                {
                    // Check to see if this location is a node
                    endNode = nodesToSectionAt.getNode(polyline.get(index));
                    if (endNode.isPresent())
                    {
                        if (!firstLocationNotNode)
                        {
                            // We only want to create an edge if we've started from a node. If we've
                            // started from a shape point, we've just encountered our first node.
                            final PolyLine edgePolyline = polyline.between(
                                    polyline.get(startingIndex),
                                    nodesToSectionAt.getOccurence(startNode.get()) - 1,
                                    polyline.get(index),
                                    nodesToSectionAt.getOccurence(endNode.get()) - 1);
                            newEdgesForLine
                                    .add(new TemporaryEdge(identifierFactory.nextIdentifier(),
                                            edgePolyline, line.getTags(), hasReverse));

                            // Increment start node occurrence
                            nodesToSectionAt.incrementOccurence(startNode.get());
                        }

                        // Update starting points
                        startingIndex = index;
                        startNode = endNode;

                        // We've found the first node, save the polyline from the first location to
                        // the first node so we can append it later
                        if (firstLocationNotNode)
                        {
                            polyLineUpToFirstNode = polyline.between(polyline.first(), 0,
                                    polyline.get(index), 0);
                            firstLocationNotNode = false;
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
                                    "Cannot section line {} - reached end of line without valid end node",
                                    line.getIdentifier());
                        }
                        final PolyLine polylineFromLastNodeToLastLocation = polyline.between(
                                polyline.get(startingIndex),
                                nodesToSectionAt.getOccurence(startNode.get()) - 1,
                                polyline.get(index), 1);
                        final PolyLine edgePolyLine = polylineFromLastNodeToLastLocation
                                .append(polyLineUpToFirstNode);
                        final TemporaryEdge edge = new TemporaryEdge(
                                identifierFactory.nextIdentifier(), edgePolyLine, line.getTags(),
                                hasReverse);
                        newEdgesForLine.add(edge);
                    }
                }
            }
        }
        catch (final Exception e)
        {
            logger.error("Failed to way-section line {}", line.getIdentifier(), e);
        }

        return newEdgesForLine;
    }
}
