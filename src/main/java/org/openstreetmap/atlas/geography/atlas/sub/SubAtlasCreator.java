package org.openstreetmap.atlas.geography.atlas.sub;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.BareAtlas;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.MultiIterable;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for creating sub-atlases given a cut type and Atlas.
 *
 * @author mgostintsev
 */
public class SubAtlasCreator implements SubAtlas
{
    private static final Logger logger = LoggerFactory.getLogger(SubAtlasCreator.class);

    private static final String CUT_START_MESSAGE = "Starting {} of Atlas {} with meta-data {}";
    private static final String CUT_STOP_MESSAGE = "Finished {} of Atlas {} in {}";

    private final Atlas atlas;

    public SubAtlasCreator(final Atlas atlas)
    {
        this.atlas = atlas;
    }

    @Override
    public Optional<Atlas> hardCutAllEntities(final GeometricSurface boundary)
    {
        logger.debug(CUT_START_MESSAGE, AtlasCutType.HARD_CUT_ALL, this.atlas.getName(),
                this.atlas.metaData());
        final Time begin = Time.now();

        final Supplier<Iterable<Node>> nodesWithin = getContainmentCachingSupplier(
                this.atlas.nodesWithin(boundary), ItemType.NODE);
        final Supplier<Iterable<Edge>> edgesWithin = getContainmentCachingSupplier(
                this.atlas.edgesWithin(boundary), ItemType.EDGE);
        final Supplier<Iterable<Area>> areasWithin = getContainmentCachingSupplier(
                this.atlas.areasWithin(boundary), ItemType.AREA);
        final Supplier<Iterable<Line>> linesWithin = getContainmentCachingSupplier(
                this.atlas.linesWithin(boundary), ItemType.LINE);
        final Supplier<Iterable<Point>> pointsWithin = getContainmentCachingSupplier(
                this.atlas.pointsWithin(boundary), ItemType.POINT);

        // Generate the size estimates and the builder. There is an edge case we need to consider
        // for node size estimating. Because of our underlying dependency on awt insideness
        // definition - we may be excluding some nodes that are exactly on the boundary of the given
        // polygon. To account for this, instead of doing a count to have an exact number, we choose
        // here to have an arbitrary 5% buffer on top of the nodes inside the polygon. This mostly
        // avoids resizing. No other entity needs the buffer since the resulting within calls give
        // us exact features we want to include.
        final double ratioBuffer = 0.5;
        final long nodeNumber = Math.round(Iterables.size(nodesWithin.get()) * ratioBuffer);
        final long edgeNumber = Iterables.size(edgesWithin.get());
        final long areaNumber = Iterables.size(areasWithin.get());
        final long lineNumber = Iterables.size(linesWithin.get());
        final long pointNumber = Iterables.size(pointsWithin.get());

        // Use intersecting call here instead of within, since we want to consider all possible
        // relations that have a member intersecting the bounds and then then pare that set of
        // relations down.
        final long relationNumber = Iterables
                .size(this.atlas.relationsWithEntitiesIntersecting(boundary));
        final AtlasSize size = new AtlasSize(edgeNumber, nodeNumber, areaNumber, lineNumber,
                pointNumber, relationNumber);
        final PackedAtlasBuilder builder = new PackedAtlasBuilder().withSizeEstimates(size)
                .withMetaData(this.atlas.metaData());

        // Predicates to test if some items have already been added.
        final Predicate<Node> hasNode = item -> builder.peek().node(item.getIdentifier()) != null;
        final Predicate<Edge> hasEdge = item -> builder.peek().edge(item.getIdentifier()) != null;
        final Predicate<Area> hasArea = item -> builder.peek().area(item.getIdentifier()) != null;
        final Predicate<Line> hasLine = item -> builder.peek().line(item.getIdentifier()) != null;
        final Predicate<Point> hasPoint = item -> builder.peek()
                .point(item.getIdentifier()) != null;
        final Predicate<Relation> hasRelation = item -> builder.peek()
                .relation(item.getIdentifier()) != null;

        // Add the nodes needed for Edges. We need to check the missing nodes in case of a corner
        // case where a Node right on the boundary may have gotten left out of the "within"
        // predicate.
        edgesWithin.get().forEach(edge ->
        {
            final Node start = edge.start();
            final Node end = edge.end();
            if (!hasNode.test(start))
            {
                builder.addNode(start.getIdentifier(), start.getLocation(), start.getTags());
            }
            if (!hasNode.test(end))
            {
                builder.addNode(end.getIdentifier(), end.getLocation(), end.getTags());
            }
        });

        // Add the remaining nodes, if any.
        Iterables.stream(nodesWithin.get()).filter(hasNode.negate()).forEach(
                node -> builder.addNode(node.getIdentifier(), node.getLocation(), node.getTags()));

        // Add the edges. Use a consumer that makes sure master edges are always added first.
        final Consumer<Edge> edgeAdder = edge ->
        {
            if (builder.peek().edge(edge.getIdentifier()) == null)
            {
                // Here, making sure that edge identifiers are not 0 to work around an issue in unit
                // tests: https://github.com/osmlab/atlas/issues/252
                if (edge.getIdentifier() != 0 && edge.hasReverseEdge())
                {
                    final Edge reverse = edge.reversed().get();
                    if (builder.peek().edge(reverse.getIdentifier()) == null)
                    {
                        builder.addEdge(reverse.getIdentifier(), reverse.asPolyLine(),
                                reverse.getTags());
                    }
                }
                builder.addEdge(edge.getIdentifier(), edge.asPolyLine(), edge.getTags());
            }
        };
        edgesWithin.get().forEach(edgeAdder::accept);

        // Add the Areas
        areasWithin.get().forEach(
                area -> builder.addArea(area.getIdentifier(), area.asPolygon(), area.getTags()));

        // Add the Lines
        linesWithin.get().forEach(
                line -> builder.addLine(line.getIdentifier(), line.asPolyLine(), line.getTags()));

        // Add the Points
        pointsWithin.get().forEach(point -> builder.addPoint(point.getIdentifier(),
                point.getLocation(), point.getTags()));

        // Add all the relations that are not in the sub atlas, in lower order first
        Iterables.stream(this.atlas.relationsLowerOrderFirst()).filter(hasRelation.negate())
                .forEach(relation ->
                {
                    final RelationMemberList members = relation.members();
                    final List<RelationMember> validMembers = new ArrayList<>();
                    // And consider them only if they have members that have already been added to
                    // the sub atlas.
                    members.forEach(member ->
                    {
                        final AtlasEntity entity = member.getEntity();
                        // Non-Relation members
                        if (entity instanceof Node && hasNode.test((Node) entity)
                                || entity instanceof Edge && hasEdge.test((Edge) entity)
                                || entity instanceof Area && hasArea.test((Area) entity)
                                || entity instanceof Line && hasLine.test((Line) entity)
                                || entity instanceof Point && hasPoint.test((Point) entity))
                        {
                            validMembers.add(member);
                        }
                        // Relation members
                        if (entity instanceof Relation && hasRelation.test((Relation) entity))
                        {
                            validMembers.add(member);
                        }
                    });
                    if (!validMembers.isEmpty())
                    {
                        // If there are legitimate members, we need to add the relation to the sub
                        // atlas
                        final RelationBean structure = new RelationBean();
                        validMembers.forEach(validMember -> structure.addItem(
                                validMember.getEntity().getIdentifier(), validMember.getRole(),
                                ItemType.forEntity(validMember.getEntity())));
                        builder.addRelation(relation.getIdentifier(), relation.getOsmIdentifier(),
                                structure, relation.getTags());
                    }
                    else
                    {
                        logger.trace(
                                "Excluding relation {} from sub-atlas since none of its members pass the {} cut.",
                                relation.getIdentifier(), AtlasCutType.HARD_CUT_ALL);
                    }
                });
        final PackedAtlas result = (PackedAtlas) builder.get();
        if (result != null)
        {
            result.trim();
        }

        logger.info(CUT_STOP_MESSAGE, AtlasCutType.HARD_CUT_ALL, this.atlas.getName(),
                begin.elapsedSince());
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Atlas> hardCutAllEntities(final Predicate<AtlasEntity> matcher)
    {
        logger.debug(CUT_START_MESSAGE, AtlasCutType.HARD_CUT_ALL, this.atlas.getName(),
                this.atlas.metaData());
        final Time begin = Time.now();

        // Using a predicate here can create wild changes in entity counts. For example a predicate
        // would include only edges, but all the nodes would have to be pulled in. In that case, we
        // use the same size as the source Atlas, but we trim it at the end.
        final PackedAtlasBuilder builder = new PackedAtlasBuilder()
                .withSizeEstimates(this.atlas.size()).withMetaData(this.atlas.metaData())
                .withName(this.atlas.getName() + "_sub");

        // Identify all nodes and edges that match the given predicate
        final Set<Node> matchedNodes = Iterables.stream(this.atlas.nodes(matcher::test))
                .map(item -> item).collectToSet();
        final Set<Long> matchedEdgeIdentifiers = Iterables.stream(this.atlas.edges(matcher::test))
                .map(Edge::getIdentifier).collectToSet();

        // Avoid floating nodes, the matched node has to be connected to at least one matched edge
        for (final Node node : matchedNodes)
        {
            if (node.connectedEdges().stream().anyMatch(connectedEdge -> matchedEdgeIdentifiers
                    .contains(connectedEdge.getIdentifier())))
            {
                // Safely add this node
                final Long nodeIdentifier = node.getIdentifier();
                if (builder.peek().node(nodeIdentifier) == null)
                {
                    builder.addNode(nodeIdentifier, node.getLocation(), node.getTags());
                }
            }
            else
            {
                logger.trace("Dropping node {} due to lack of connectivity after {}",
                        node.getIdentifier(), AtlasCutType.HARD_CUT_ALL);
            }
        }

        // Before adding an edge, check that its start and end nodes matched the predicate
        for (final Long matchedEdgeIdentifier : matchedEdgeIdentifiers)
        {
            final Edge matchedEdge = this.atlas.edge(matchedEdgeIdentifier);
            if (builder.peek().node(matchedEdge.start().getIdentifier()) != null
                    && builder.peek().node(matchedEdge.end().getIdentifier()) != null)
            {
                // Safely add this edge
                if (builder.peek().edge(matchedEdge.getIdentifier()) == null)
                {
                    builder.addEdge(matchedEdge.getIdentifier(), matchedEdge.asPolyLine(),
                            matchedEdge.getTags());
                }
            }
            else
            {
                logger.trace("Dropping edge {} due to missing start/end node after {}",
                        matchedEdge.getIdentifier(), AtlasCutType.HARD_CUT_ALL);
            }
        }

        // Points, Lines and Areas require no additional checks - just apply the predicate
        indexPointsAreasLines(matcher, builder);

        // Process relations - members could have been filtered out, avoid adding an empty relation.
        // Because we're processing relations in the lowest order first, we're guaranteed to not
        // encounter a relation that contains an un-processed relation.
        final Iterable<Relation> matchedRelations = Iterables
                .filter(this.atlas.relationsLowerOrderFirst(), matcher::test);
        for (final Relation relation : matchedRelations)
        {
            // Gather all members that made it through the predicate
            final List<RelationMember> validMembers = relation.members().stream()
                    .filter(member -> builder.peek().entity(member.getEntity().getIdentifier(),
                            member.getEntity().getType()) != null)
                    .collect(Collectors.toList());

            if (!validMembers.isEmpty())
            {
                // Safely add this relation
                if (builder.peek().relation(relation.getIdentifier()) == null)
                {
                    final RelationBean structure = new RelationBean();
                    validMembers.forEach(validMember -> structure.addItem(
                            validMember.getEntity().getIdentifier(), validMember.getRole(),
                            ItemType.forEntity(validMember.getEntity())));
                    builder.addRelation(relation.getIdentifier(), relation.getOsmIdentifier(),
                            structure, relation.getTags());
                }
            }
            else
            {
                logger.trace("Dropping relation {} due to empty member list after {}",
                        relation.getIdentifier(), AtlasCutType.HARD_CUT_ALL);
            }
        }

        final PackedAtlas result = (PackedAtlas) builder.get();
        if (result != null)
        {
            result.trim();
        }

        logger.info(CUT_STOP_MESSAGE, AtlasCutType.HARD_CUT_ALL, this.atlas.getName(),
                begin.elapsedSince());
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Atlas> hardCutRelationsOnly(final Predicate<AtlasEntity> matcher)
    {
        logger.debug(CUT_START_MESSAGE, AtlasCutType.HARD_CUT_RELATIONS_ONLY, this.atlas.getName(),
                this.atlas.metaData());
        final Time begin = Time.now();

        // Using a predicate here can create wild changes in entity counts. For example a predicate
        // would include only edges, but all the nodes would have to be pulled in. In that case, we
        // use the same size as the source Atlas, but we trim it at the end.
        final PackedAtlasBuilder builder = new PackedAtlasBuilder()
                .withSizeEstimates(this.atlas.size()).withMetaData(this.atlas.metaData())
                .withName(this.atlas.getName() + "_sub");

        // Identify all nodes that match the predicate. This includes pulling in nodes that did not
        // match the predicate, but are required by matched edges
        final Iterable<AtlasEntity> nodes = Iterables.stream(this.atlas.nodes(matcher::test))
                .map(item -> (AtlasEntity) item).collectToSet();
        final Iterable<AtlasEntity> edges = Iterables.stream(this.atlas.edges(matcher::test))
                .map(item -> (AtlasEntity) item);
        for (final AtlasEntity entity : new MultiIterable<>(nodes, edges))
        {
            addNodesToAtlas(entity, builder);
        }

        // Next, add the edges, areas, lines and points. We can safely add the edges since we've
        // made sure to pull in all appropriate nodes in the step above.
        for (final Edge edge : this.atlas.edges(matcher::test))
        {
            indexEdge(edge, builder);
        }
        indexPointsAreasLines(matcher, builder);

        // Process relations - because they are being hard-cut, we can't blindly add all
        // members. We must verify that each member matches the predicate and the relation being
        // added isn't empty. Because we're processing relations in the lowest order first, we're
        // guaranteed to not encounter a relation that contains an un-processed relation.
        final Iterable<Relation> relations = Iterables.filter(this.atlas.relationsLowerOrderFirst(),
                matcher::test);
        for (final Relation relation : relations)
        {
            // Gather all members that made it through the predicate
            final List<RelationMember> matchingMembers = relation.members().stream()
                    .filter(member -> matcher.test(member.getEntity()))
                    .collect(Collectors.toList());

            if (!matchingMembers.isEmpty())
            {
                // Safely add this relation
                if (builder.peek().relation(relation.getIdentifier()) == null)
                {
                    final RelationBean structure = new RelationBean();
                    matchingMembers.forEach(validMember -> structure.addItem(
                            validMember.getEntity().getIdentifier(), validMember.getRole(),
                            ItemType.forEntity(validMember.getEntity())));
                    builder.addRelation(relation.getIdentifier(), relation.getOsmIdentifier(),
                            structure, relation.getTags());
                }
            }
            else
            {
                logger.trace("Dropping relation {} due to empty member list after {}",
                        relation.getIdentifier(), AtlasCutType.HARD_CUT_RELATIONS_ONLY);
            }
        }

        final PackedAtlas result = (PackedAtlas) builder.get();
        if (result != null)
        {
            result.trim();
        }

        logger.info(CUT_STOP_MESSAGE, AtlasCutType.HARD_CUT_RELATIONS_ONLY, this.atlas.getName(),
                begin.elapsedSince());
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Atlas> silkCut(final Polygon boundary)
    {
        logger.debug(CUT_START_MESSAGE, AtlasCutType.SILK_CUT, this.atlas.getName(),
                this.atlas.metaData());
        final Time begin = Time.now();

        final Supplier<Iterable<Node>> nodesWithin = getIntersectingCachingSupplier(
                this.atlas.nodesWithin(boundary), ItemType.NODE);
        final Supplier<Iterable<Edge>> edgesIntersecting = getIntersectingCachingSupplier(
                this.atlas.edgesIntersecting(boundary), ItemType.EDGE);
        final Supplier<Iterable<Area>> areasIntersecting = getIntersectingCachingSupplier(
                this.atlas.areasIntersecting(boundary), ItemType.AREA);
        final Supplier<Iterable<Line>> linesIntersecting = getIntersectingCachingSupplier(
                this.atlas.linesIntersecting(boundary), ItemType.LINE);
        final Supplier<Iterable<Point>> pointsWithin = getIntersectingCachingSupplier(
                this.atlas.pointsWithin(boundary), ItemType.POINT);

        // Generate the size estimates, then the builder.
        // Nodes estimating is a bit tricky. We want to include all the nodes within the polygon,
        // but we also want to include those attached to edges that span outside the polygon.
        // Instead of doing a count to have an exact number, we choose here to have an arbitrary 20%
        // buffer on top of the nodes inside the polygon. This mostly avoids resizing.
        final double ratioBuffer = 1.2;
        final long nodeNumber = Math.round(Iterables.size(nodesWithin.get()) * ratioBuffer);
        final long edgeNumber = Math.round(Iterables.size(edgesIntersecting.get()) * ratioBuffer);
        final long areaNumber = Math.round(Iterables.size(areasIntersecting.get()) * ratioBuffer);
        final long lineNumber = Math.round(Iterables.size(linesIntersecting.get()) * ratioBuffer);
        final long pointNumber = Math.round(Iterables.size(pointsWithin.get()) * ratioBuffer);
        final long relationNumber = Math
                .round(Iterables.size(this.atlas.relationsWithEntitiesIntersecting(boundary))
                        * ratioBuffer);
        final AtlasSize size = new AtlasSize(edgeNumber, nodeNumber, areaNumber, lineNumber,
                pointNumber, relationNumber);
        final PackedAtlasBuilder builder = new PackedAtlasBuilder().withSizeEstimates(size)
                .withMetaData(this.atlas.metaData());

        // Predicates to test if some items have already been added.
        final Predicate<Node> hasNode = item -> builder.peek().node(item.getIdentifier()) != null;
        final Predicate<Edge> hasEdge = item -> builder.peek().edge(item.getIdentifier()) != null;
        final Predicate<Area> hasArea = item -> builder.peek().area(item.getIdentifier()) != null;
        final Predicate<Line> hasLine = item -> builder.peek().line(item.getIdentifier()) != null;
        final Predicate<Point> hasPoint = item -> builder.peek()
                .point(item.getIdentifier()) != null;
        final Predicate<Relation> hasRelation = item -> builder.peek()
                .relation(item.getIdentifier()) != null;

        // Add the nodes needed for Edges.
        edgesIntersecting.get().forEach(edge ->
        {
            final Node start = edge.start();
            final Node end = edge.end();
            if (!hasNode.test(start))
            {
                builder.addNode(start.getIdentifier(), start.getLocation(), start.getTags());
            }
            if (!hasNode.test(end))
            {
                builder.addNode(end.getIdentifier(), end.getLocation(), end.getTags());
            }
        });

        // Add the remaining Nodes if any.
        Iterables.stream(nodesWithin.get()).filter(hasNode.negate()).forEach(
                node -> builder.addNode(node.getIdentifier(), node.getLocation(), node.getTags()));

        // Add the edges. Use a consumer that makes sure master edges are always added first.
        final Consumer<Edge> edgeAdder = edge ->
        {
            if (builder.peek().edge(edge.getIdentifier()) == null)
            {
                // Here, making sure that edge identifiers are not 0 to work around an issue in unit
                // tests: https://github.com/osmlab/atlas/issues/252
                if (edge.getIdentifier() != 0 && edge.hasReverseEdge())
                {
                    final Edge reverse = edge.reversed().get();
                    if (builder.peek().edge(reverse.getIdentifier()) == null)
                    {
                        builder.addEdge(reverse.getIdentifier(), reverse.asPolyLine(),
                                reverse.getTags());
                    }
                }
                builder.addEdge(edge.getIdentifier(), edge.asPolyLine(), edge.getTags());
            }
        };
        edgesIntersecting.get().forEach(edgeAdder::accept);

        // Add the Areas
        areasIntersecting.get().forEach(
                area -> builder.addArea(area.getIdentifier(), area.asPolygon(), area.getTags()));

        // Add the Lines
        linesIntersecting.get().forEach(
                line -> builder.addLine(line.getIdentifier(), line.asPolyLine(), line.getTags()));

        // Add the Points
        pointsWithin.get().forEach(point -> builder.addPoint(point.getIdentifier(),
                point.getLocation(), point.getTags()));

        // Add the Points for all included Lines
        linesIntersecting.get().forEach(line ->
        {
            line.getRawGeometry().forEach(location ->
            {
                this.atlas.pointsAt(location).forEach(point ->
                {
                    if (!hasPoint.test(point))
                    {
                        builder.addPoint(point.getIdentifier(), point.getLocation(),
                                point.getTags());
                    }
                });
            });
        });

        Iterables.stream(this.atlas.relationsLowerOrderFirst()).filter(hasRelation.negate())
                .forEach(relation ->
                {
                    final RelationMemberList members = relation.members();
                    final List<RelationMember> validMembers = new ArrayList<>();
                    // And consider them only if they have members that have already been added
                    // to the sub atlas.
                    members.forEach(member ->
                    {
                        final AtlasEntity entity = member.getEntity();
                        // Non-Relation members
                        if (entity instanceof Node && hasNode.test((Node) entity)
                                || entity instanceof Edge && hasEdge.test((Edge) entity)
                                || entity instanceof Area && hasArea.test((Area) entity)
                                || entity instanceof Line && hasLine.test((Line) entity)
                                || entity instanceof Point && hasPoint.test((Point) entity))
                        {
                            validMembers.add(member);
                        }
                        // Relation members
                        if (entity instanceof Relation && hasRelation.test((Relation) entity))
                        {
                            validMembers.add(member);
                        }
                    });
                    if (!validMembers.isEmpty())
                    {
                        // If there are legitimate members, we need to add the relation to the
                        // sub atlas
                        final RelationBean structure = new RelationBean();
                        validMembers.forEach(validMember -> structure.addItem(
                                validMember.getEntity().getIdentifier(), validMember.getRole(),
                                ItemType.forEntity(validMember.getEntity())));
                        builder.addRelation(relation.getIdentifier(), relation.getOsmIdentifier(),
                                structure, relation.getTags());
                    }
                });

        final PackedAtlas result = (PackedAtlas) builder.get();
        if (result != null)
        {
            result.trim();
        }

        logger.info(CUT_STOP_MESSAGE, AtlasCutType.SILK_CUT, this.atlas.getName(),
                begin.elapsedSince());
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Atlas> softCut(final GeometricSurface boundary, final boolean hardCutRelations)
    {
        logger.debug(CUT_START_MESSAGE,
                !hardCutRelations ? AtlasCutType.SOFT_CUT : AtlasCutType.HARD_CUT_RELATIONS_ONLY,
                this.atlas.getName(), this.atlas.metaData());
        final Time begin = Time.now();

        final Supplier<Iterable<Node>> nodesWithin = getIntersectingCachingSupplier(
                this.atlas.nodesWithin(boundary), ItemType.NODE);
        final Supplier<Iterable<Edge>> edgesIntersecting = getIntersectingCachingSupplier(
                this.atlas.edgesIntersecting(boundary), ItemType.EDGE);
        final Supplier<Iterable<Area>> areasIntersecting = getIntersectingCachingSupplier(
                this.atlas.areasIntersecting(boundary), ItemType.AREA);
        final Supplier<Iterable<Line>> linesIntersecting = getIntersectingCachingSupplier(
                this.atlas.linesIntersecting(boundary), ItemType.LINE);
        final Supplier<Iterable<Point>> pointsWithin = getIntersectingCachingSupplier(
                this.atlas.pointsWithin(boundary), ItemType.POINT);

        // Generate the size estimates, then the builder.
        // Nodes estimating is a bit tricky. We want to include all the nodes within the polygon,
        // but we also want to include those attached to edges that span outside the polygon.
        // Instead of doing a count to have an exact number, we choose here to have an arbitrary 20%
        // buffer on top of the nodes inside the polygon. This mostly avoids resizing.
        final double ratioBuffer = 1.2;
        final long nodeNumber = Math.round(Iterables.size(nodesWithin.get()) * ratioBuffer);
        final long edgeNumber = Math.round(Iterables.size(edgesIntersecting.get()) * ratioBuffer);
        final long areaNumber = Math.round(Iterables.size(areasIntersecting.get()) * ratioBuffer);
        final long lineNumber = Math.round(Iterables.size(linesIntersecting.get()) * ratioBuffer);
        final long pointNumber = Math.round(Iterables.size(pointsWithin.get()) * ratioBuffer);
        final long relationNumber = Math
                .round(Iterables.size(this.atlas.relationsWithEntitiesIntersecting(boundary))
                        * ratioBuffer);
        final AtlasSize size = new AtlasSize(edgeNumber, nodeNumber, areaNumber, lineNumber,
                pointNumber, relationNumber);
        final PackedAtlasBuilder builder = new PackedAtlasBuilder().withSizeEstimates(size)
                .withMetaData(this.atlas.metaData());

        // Predicates to test if some items have already been added.
        final Predicate<Node> hasNode = item -> builder.peek().node(item.getIdentifier()) != null;
        final Predicate<Edge> hasEdge = item -> builder.peek().edge(item.getIdentifier()) != null;
        final Predicate<Area> hasArea = item -> builder.peek().area(item.getIdentifier()) != null;
        final Predicate<Line> hasLine = item -> builder.peek().line(item.getIdentifier()) != null;
        final Predicate<Point> hasPoint = item -> builder.peek()
                .point(item.getIdentifier()) != null;
        final Predicate<Relation> hasRelation = item -> builder.peek()
                .relation(item.getIdentifier()) != null;

        // Add the nodes needed for Edges.
        edgesIntersecting.get().forEach(edge ->
        {
            final Node start = edge.start();
            final Node end = edge.end();
            if (!hasNode.test(start))
            {
                builder.addNode(start.getIdentifier(), start.getLocation(), start.getTags());
            }
            if (!hasNode.test(end))
            {
                builder.addNode(end.getIdentifier(), end.getLocation(), end.getTags());
            }
        });

        // Add the remaining Nodes if any.
        Iterables.stream(nodesWithin.get()).filter(hasNode.negate()).forEach(
                node -> builder.addNode(node.getIdentifier(), node.getLocation(), node.getTags()));

        // Add the edges. Use a consumer that makes sure master edges are always added first.
        final Consumer<Edge> edgeAdder = edge ->
        {
            if (builder.peek().edge(edge.getIdentifier()) == null)
            {
                // Here, making sure that edge identifiers are not 0 to work around an issue in unit
                // tests: https://github.com/osmlab/atlas/issues/252
                if (edge.getIdentifier() != 0 && edge.hasReverseEdge())
                {
                    final Edge reverse = edge.reversed().get();
                    if (builder.peek().edge(reverse.getIdentifier()) == null)
                    {
                        builder.addEdge(reverse.getIdentifier(), reverse.asPolyLine(),
                                reverse.getTags());
                    }
                }
                builder.addEdge(edge.getIdentifier(), edge.asPolyLine(), edge.getTags());
            }
        };
        edgesIntersecting.get().forEach(edgeAdder::accept);

        // Add the Areas
        areasIntersecting.get().forEach(
                area -> builder.addArea(area.getIdentifier(), area.asPolygon(), area.getTags()));

        // Add the Lines
        linesIntersecting.get().forEach(
                line -> builder.addLine(line.getIdentifier(), line.asPolyLine(), line.getTags()));

        // Add the Points
        pointsWithin.get().forEach(point -> builder.addPoint(point.getIdentifier(),
                point.getLocation(), point.getTags()));

        // Add all the relations that are not in the sub atlas, in lower order first
        if (hardCutRelations)
        {
            Iterables.stream(this.atlas.relationsLowerOrderFirst()).filter(hasRelation.negate())
                    .forEach(relation ->
                    {
                        final RelationMemberList members = relation.members();
                        final List<RelationMember> validMembers = new ArrayList<>();
                        // And consider them only if they have members that are fully within the
                        // polygon
                        members.forEach(member ->
                        {
                            final AtlasEntity entity = member.getEntity();
                            if (entity instanceof LocationItem
                                    && boundary.fullyGeometricallyEncloses(
                                            ((LocationItem) entity).getLocation()))
                            {
                                validMembers.add(member);
                            }
                            else if (entity instanceof LineItem && boundary
                                    .fullyGeometricallyEncloses(((LineItem) entity).asPolyLine()))
                            {
                                validMembers.add(member);
                            }
                            else if (entity instanceof Area && boundary
                                    .fullyGeometricallyEncloses(((Area) entity).asPolygon()))
                            {
                                validMembers.add(member);
                            }
                            else if (entity instanceof Relation
                                    && hasRelation.test((Relation) entity))
                            {
                                validMembers.add(member);
                            }
                        });
                        if (!validMembers.isEmpty())
                        {
                            // If there are legitimate members, we need to add the relation to the
                            // sub atlas
                            final RelationBean structure = new RelationBean();
                            validMembers.forEach(validMember -> structure.addItem(
                                    validMember.getEntity().getIdentifier(), validMember.getRole(),
                                    ItemType.forEntity(validMember.getEntity())));
                            builder.addRelation(relation.getIdentifier(),
                                    relation.getOsmIdentifier(), structure, relation.getTags());
                        }
                    });
        }
        else
        {
            Iterables.stream(this.atlas.relationsLowerOrderFirst()).filter(hasRelation.negate())
                    .forEach(relation ->
                    {
                        final RelationMemberList members = relation.members();
                        final List<RelationMember> validMembers = new ArrayList<>();
                        // And consider them only if they have members that have already been added
                        // to the sub atlas.
                        members.forEach(member ->
                        {
                            final AtlasEntity entity = member.getEntity();
                            // Non-Relation members
                            if (entity instanceof Node && hasNode.test((Node) entity)
                                    || entity instanceof Edge && hasEdge.test((Edge) entity)
                                    || entity instanceof Area && hasArea.test((Area) entity)
                                    || entity instanceof Line && hasLine.test((Line) entity)
                                    || entity instanceof Point && hasPoint.test((Point) entity))
                            {
                                validMembers.add(member);
                            }
                            // Relation members
                            if (entity instanceof Relation && hasRelation.test((Relation) entity))
                            {
                                validMembers.add(member);
                            }
                        });
                        if (!validMembers.isEmpty())
                        {
                            // If there are legitimate members, we need to add the relation to the
                            // sub atlas
                            final RelationBean structure = new RelationBean();
                            validMembers.forEach(validMember -> structure.addItem(
                                    validMember.getEntity().getIdentifier(), validMember.getRole(),
                                    ItemType.forEntity(validMember.getEntity())));
                            builder.addRelation(relation.getIdentifier(),
                                    relation.getOsmIdentifier(), structure, relation.getTags());
                        }
                    });
        }

        final PackedAtlas result = (PackedAtlas) builder.get();
        if (result != null)
        {
            result.trim();
        }

        logger.info(CUT_STOP_MESSAGE,
                !hardCutRelations ? AtlasCutType.SOFT_CUT : AtlasCutType.HARD_CUT_RELATIONS_ONLY,
                this.atlas.getName(), begin.elapsedSince());
        return Optional.ofNullable(result);
    }

    /**
     * @param matcher
     *            The matcher to consider
     * @return a sub-atlas from this Atlas.
     */
    @Override
    public Optional<Atlas> softCut(final Predicate<AtlasEntity> matcher)
    {
        logger.debug(CUT_START_MESSAGE, AtlasCutType.SOFT_CUT, this.atlas.getName(),
                this.atlas.metaData());
        final Time begin = Time.now();

        // Using a predicate here can create wild changes in entity counts. For example a predicate
        // would include only edges, but all the nodes would have to be pulled in. In that case, we
        // use the same size as the source Atlas, but we trim it at the end.
        final PackedAtlasBuilder builder = new PackedAtlasBuilder()
                .withSizeEstimates(this.atlas.size()).withMetaData(this.atlas.metaData())
                .withName(this.atlas.getName() + "_sub");

        // First, index all the nodes contained by relations and all start/stop nodes from edges
        // contained by relations
        for (final AtlasEntity entity : this.atlas.relations(matcher::test))
        {
            indexAllNodesFromRelation((Relation) entity, builder, 0);
        }

        // Next, index all the individual nodes and edge start/stop nodes coming from the predicate
        final Iterable<AtlasEntity> nodes = Iterables.stream(this.atlas.nodes(matcher::test))
                .map(item -> (AtlasEntity) item).collectToSet();
        final Iterable<AtlasEntity> edges = Iterables.stream(this.atlas.edges(matcher::test))
                .map(item -> (AtlasEntity) item);
        for (final AtlasEntity entity : new MultiIterable<>(nodes, edges))
        {
            addNodesToAtlas(entity, builder);
        }

        // Next, add the Lines, Points, Areas and Edges. Edges are a little trickier - 1) They rely
        // on their start/end nodes to have already been added 2) They can potentially pull in nodes
        // that weren't matched by the given predicate. These two cases are handled above.
        // Similarly, Relations depend on all other entities to have been added, since they make up
        // the member list. For this pass, add all entities, except Relations, that match the given
        // Predicate to the builder.
        for (final Edge edge : this.atlas.edges(matcher::test))
        {
            indexEdge(edge, builder);
        }
        indexPointsAreasLines(matcher, builder);

        // It's now safe to add Relations. There are two caveats: 1. A relation member may not
        // have been pulled in by the given predicate. In order to maintain relation validity, we
        // need to pull in the un-indexed members. 2. The member may be a relation that hasn't been
        // indexed yet. We check if any of the members are un-indexed relations and if so, we add
        // the parent relation to a staged set to be processed later (after the child member
        // relation has been processed). This guarantees that anything we add to the index has all
        // of its members indexed already.
        Set<Long> stagedRelationIdentifiers = new HashSet<>();
        final Iterable<Relation> relations = Iterables.filter(this.atlas.relationsLowerOrderFirst(),
                matcher::test);
        for (final Relation relation : relations)
        {
            checkRelationMembersAndIndexRelation(relation, matcher, stagedRelationIdentifiers,
                    builder);
        }

        // Process all staged relations
        int iterations = 0;
        while (++iterations < BareAtlas.MAXIMUM_RELATION_DEPTH
                && !stagedRelationIdentifiers.isEmpty())
        {
            logger.trace("Copying relations level {} deep.", iterations);
            final Set<Long> stagedRelationIdentifiersCopy = new HashSet<>();
            for (final Long relationIdentifier : stagedRelationIdentifiers)
            {
                // Apply the same logic to all staged relations - if any member is an
                // un-indexed relations, re-stage the parent relation.
                final Relation relation = this.atlas.relation(relationIdentifier);
                checkRelationMembersAndIndexRelation(relation, matcher,
                        stagedRelationIdentifiersCopy, builder);
            }
            stagedRelationIdentifiers = stagedRelationIdentifiersCopy;
        }

        if (iterations >= BareAtlas.MAXIMUM_RELATION_DEPTH)
        {
            throw new CoreException(
                    "There might be a loop in relations! It took more than {} loops to update the relation from atlas {}.",
                    BareAtlas.MAXIMUM_RELATION_DEPTH, this.atlas.getName());
        }

        final PackedAtlas result = (PackedAtlas) builder.get();
        if (result != null)
        {
            result.trim();
        }

        logger.info(CUT_STOP_MESSAGE, AtlasCutType.SOFT_CUT, this.atlas.getName(),
                begin.elapsedSince());
        return Optional.ofNullable(result);
    }

    /**
     * This is used by the {@link #subAtlas(Predicate)} method to add all {@link Node}s to the
     * sub-atlas. If the given {@link AtlasEntity} is a {@link Node}, it will be added. If it's an
     * {@link Edge}, its start and end {@link Node}s will be added. Note: the {@link Edge} itself
     * will NOT be added here due to the constraint that all {@link Node}s must be indexed before
     * any {@link Edge}s can be indexed.
     *
     * @param entity
     *            The {@link AtlasEntity} whose {@link Node}s we intend to add to the sub-atlas
     * @param builder
     *            The {@link PackedAtlasBuilder} used to build the sub-atlas
     */
    private void addNodesToAtlas(final AtlasEntity entity, final PackedAtlasBuilder builder)
    {
        final long identifier = entity.getIdentifier();
        if (entity instanceof Node)
        {
            final Node node = (Node) entity;
            if (builder.peek().node(identifier) == null)
            {
                builder.addNode(identifier, node.getLocation(), node.getTags());
            }
        }
        else if (entity instanceof Edge)
        {
            final Edge edge = (Edge) entity;
            final Node start = edge.start();
            final Node end = edge.end();
            final long startIdentifier = start.getIdentifier();
            final long endIdentifier = end.getIdentifier();

            if (builder.peek().node(startIdentifier) == null)
            {
                builder.addNode(startIdentifier, start.getLocation(), start.getTags());
            }

            if (builder.peek().node(endIdentifier) == null)
            {
                builder.addNode(endIdentifier, end.getLocation(), end.getTags());
            }

            // Note: Don't add the Edge here, only care about Nodes.
        }
    }

    /**
     * This is used by the {@link #subAtlas(Predicate)} method to add a {@link Relation} to the
     * sub-atlas. We loop through each {@link RelationMemmber} of the given {@link Relation}, and
     * add any member that hasn't been added. If the given {@link Relation} contains an un-added
     * {@link Relation} member, then we can't process this {@link Relation} and we store its
     * identifier in the given {@link Set} of staged identifiers. If we've looked at all members and
     * didn't find any un-added {@link Relation}s, then we safely add the given {@link Relation} to
     * the sub-atlas.
     *
     * @param relation
     *            The {@link Relation} we intend to add
     * @param stagedRelationIdentifiers
     *            The {@link Set} of {@link Relation} identifiers used to stage {@link Relation}s
     *            that contain an un-added {@link Relation} member.
     * @param builder
     *            The {@link PackedAtlasBuilder} used to build the sub-atlas
     */
    private void checkRelationMembersAndIndexRelation(final Relation relation,
            final Predicate<AtlasEntity> matcher, final Set<Long> stagedRelationIdentifiers,
            final PackedAtlasBuilder builder)
    {
        boolean allRelationsMembersAreIndexed = true;
        final long relationIdentifier = relation.getIdentifier();
        for (final RelationMember member : relation.members())
        {
            final ItemType memberType = member.getEntity().getType();

            // If the relation member wasn't pulled in by the given predicate, we still want
            // to add it to maintain relation integrity
            final long memberIdentifier = member.getEntity().getIdentifier();
            switch (memberType)
            {
                case EDGE:
                    final Edge edgeMember = (Edge) member.getEntity();
                    indexEdge(edgeMember, builder);
                    break;
                case AREA:
                    final Area areaMember = (Area) member.getEntity();
                    if (builder.peek().area(memberIdentifier) == null)
                    {
                        builder.addArea(memberIdentifier, areaMember.asPolygon(),
                                areaMember.getTags());
                    }
                    break;
                case LINE:
                    final Line lineMember = (Line) member.getEntity();
                    if (builder.peek().line(memberIdentifier) == null)
                    {
                        builder.addLine(memberIdentifier, lineMember.asPolyLine(),
                                lineMember.getTags());
                    }
                    break;
                case POINT:
                    final Point pointMember = (Point) member.getEntity();
                    if (builder.peek().point(memberIdentifier) == null)
                    {
                        builder.addPoint(memberIdentifier, pointMember.getLocation(),
                                pointMember.getTags());
                    }
                    break;
                case RELATION:
                    final Relation relationMember = (Relation) member.getEntity();
                    // If the built Atlas does not have that member relation yet
                    if (builder.peek().relation(memberIdentifier) == null)
                    {
                        if (!matcher.test(relationMember))
                        {
                            // If the relation member is not there because the matcher excluded it,
                            // then add it back to make sure the relation structure is not broken
                            stagedRelationIdentifiers.add(memberIdentifier);
                        }
                        stagedRelationIdentifiers.add(relationIdentifier);
                        allRelationsMembersAreIndexed = false;
                    }
                    break;
                default:
                    // We already handled all Relation member Nodes
                    break;
            }
        }

        // All members of this relation have been indexed, it's safe to index it.
        if (allRelationsMembersAreIndexed && builder.peek().relation(relationIdentifier) == null)
        {
            final RelationBean bean = new RelationBean();
            relation.members().forEach(member -> bean.addItem(member.getEntity().getIdentifier(),
                    member.getRole(), member.getEntity().getType()));
            builder.addRelation(relationIdentifier, relation.getOsmIdentifier(), bean,
                    relation.getTags());
        }
    }

    private <M extends AtlasEntity> Supplier<Iterable<M>> getContainmentCachingSupplier(
            final Iterable<M> source, final ItemType type)
    {
        final Set<Long> memberIdentifiersWithin = new HashSet<>();
        // A supplier that will effectively cache all the members contained by that polygon. This is
        // thread safe because the cache is internal to the supplier's scope.
        @SuppressWarnings("unchecked")
        final Supplier<Iterable<M>> result = () ->
        {
            if (memberIdentifiersWithin.isEmpty())
            {
                // Here using a map instead of forEach to pipe the edge identifiers to the cache
                // list without having to re-open the iterable.
                return Iterables.stream(source).map(member ->
                {
                    memberIdentifiersWithin.add(member.getIdentifier());
                    return member;
                }).collect();
            }
            else
            {
                return (Iterable<M>) Iterables.stream(memberIdentifiersWithin)
                        .map(entityIdentifier -> this.atlas.entity(entityIdentifier, type))
                        .collect();
            }
        };
        return result;
    }

    private <M extends AtlasEntity> Supplier<Iterable<M>> getIntersectingCachingSupplier(
            final Iterable<M> source, final ItemType type)
    {
        final Set<Long> memberIdentifiersIntersecting = new HashSet<>();
        // A supplier that will effectively cache all the members intersecting that polygon. This is
        // thread safe because the cache is internal to the supplier's scope.
        @SuppressWarnings("unchecked")
        final Supplier<Iterable<M>> result = () ->
        {
            if (memberIdentifiersIntersecting.isEmpty())
            {
                // Here using a map instead of forEach to pipe the edge identifiers to the cache
                // list without having to re-open the iterable.
                return Iterables.stream(source).map(member ->
                {
                    memberIdentifiersIntersecting.add(member.getIdentifier());
                    return member;
                }).collect();
            }
            else
            {
                return (Iterable<M>) Iterables.stream(memberIdentifiersIntersecting)
                        .map(entityIdentifier -> this.atlas.entity(entityIdentifier, type))
                        .collect();
            }
        };
        return result;
    }

    /**
     * This is used by the {@link #subAtlas(Predicate)} method to index all {@link Node}s contained
     * by the given {@link Relation} and the start/end {@link Node}s for all {@link Edge}s contained
     * by the given {@link Relation} and recursively do this for all sub-{@link Relation}s.
     *
     * @param relation
     *            The {@link Relation} which we're exploring
     * @param builder
     *            The {@link PackedAtlasBuilder} used to build the sub-atlas
     * @param relationDepth
     *            The depth of the {@link Relation} to guard against loops and extremely large
     *            {@link Relation}s
     */
    private void indexAllNodesFromRelation(final Relation relation,
            final PackedAtlasBuilder builder, final int relationDepth)
    {
        if (relationDepth > BareAtlas.MAXIMUM_RELATION_DEPTH)
        {
            throw new CoreException(
                    "Relation depth is greater than {} for the relation from atlas {}.",
                    BareAtlas.MAXIMUM_RELATION_DEPTH, this.atlas.getName());
        }

        for (final RelationMember member : relation.members())
        {
            final ItemType type = member.getEntity().getType();
            if (ItemType.NODE == type || ItemType.EDGE == type)
            {
                // Add all individual nodes and nodes from edges for this relation
                addNodesToAtlas(member.getEntity(), builder);
            }
            else if (ItemType.RELATION == type)
            {
                // Recursively get the nodes for the sub-relation
                final Relation relationMember = (Relation) member.getEntity();
                indexAllNodesFromRelation(relationMember, builder, relationDepth + 1);
            }
            else
            {
                // Do nothing. We only care about walking the relations tree and indexing all Nodes.
            }
        }
    }

    /**
     * This is used by the {@link #subAtlas(Predicate)} to add the given {@link Edge} and its
     * reverse {@link Edge}.
     *
     * @param edge
     *            The {@link Edge} whose reverse {@link Edge} we intend to add
     * @param builder
     *            The {@link PackedAtlasBuilder} used to build the sub-atlas
     */
    private void indexEdge(final Edge edge, final PackedAtlasBuilder builder)
    {
        // Add the given edge
        if (builder.peek().edge(edge.getIdentifier()) == null)
        {
            builder.addEdge(edge.getIdentifier(), edge.asPolyLine(), edge.getTags());
        }

        // Add the reverse
        if (edge.hasReverseEdge())
        {
            // Skip sonar lint here as S3655 "Optional value should only be accessed after calling
            // isPresent()" will never be the case as the Edge has been verified to have a reverse
            // edge before.
            final Edge reverseEdge = edge.reversed().get(); // NOSONAR
            final long reverseEdgeIdentifier = reverseEdge.getIdentifier();
            if (builder.peek().edge(reverseEdgeIdentifier) == null)
            {
                builder.addEdge(reverseEdgeIdentifier, reverseEdge.asPolyLine(),
                        reverseEdge.getTags());
            }
        }
    }

    /**
     * Add all points, areas and lines that pass the given matcher to the sub-atlas, using the given
     * {@link PackedAtlasBuilder}.
     *
     * @param matcher
     *            The matcher to consider
     * @param builder
     *            The {@link PackedAtlasBuilder} used to build the sub-atlas
     */
    private void indexPointsAreasLines(final Predicate<AtlasEntity> matcher,
            final PackedAtlasBuilder builder)
    {
        for (final Point point : this.atlas.points(matcher::test))
        {
            builder.addPoint(point.getIdentifier(), point.getLocation(), point.getTags());
        }
        for (final Area area : this.atlas.areas(matcher::test))
        {
            builder.addArea(area.getIdentifier(), area.asPolygon(), area.getTags());
        }
        for (final Line line : this.atlas.lines(matcher::test))
        {
            builder.addLine(line.getIdentifier(), line.asPolyLine(), line.getTags());
        }
    }
}
