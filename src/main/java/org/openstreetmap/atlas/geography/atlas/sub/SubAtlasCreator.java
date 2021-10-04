package org.openstreetmap.atlas.geography.atlas.sub;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.AtlasSize;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.AtlasObject;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.LineItem;
import org.openstreetmap.atlas.geography.atlas.items.LocationItem;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for creating sub-atlases given a cut type and Atlas.
 *
 * @author mgostintsev
 * @author samgass
 */
public final class SubAtlasCreator
{
    private static final Logger logger = LoggerFactory.getLogger(SubAtlasCreator.class);

    private static final String CUT_START_MESSAGE = "Starting {} of Atlas {} with meta-data {}";
    private static final String CUT_STOP_MESSAGE = "Finished {} of Atlas {} in {}";
    private static final String SUB_ATLAS_NAME_POSTFIX = "_sub";

    private static final double HARD_CUT_RATIO_BUFFER = 0.5;
    private static final double SOFT_CUT_RATIO_BUFFER = 1.2;

    public static Optional<Atlas> hardCutAllEntities(final Atlas atlas,
            final GeometricSurface boundary)
    {
        logger.trace(CUT_START_MESSAGE, AtlasCutType.HARD_CUT_ALL, atlas.getName(),
                atlas.metaData());
        final Time begin = Time.now();

        final Iterable<Node> nodesWithin = getContainmentCachingSupplier(atlas,
                atlas.nodesWithin(boundary), ItemType.NODE).get();
        final Iterable<Edge> edgesWithin = getContainmentCachingSupplier(atlas,
                atlas.edgesWithin(boundary), ItemType.EDGE).get();
        final Iterable<Area> areasWithin = getContainmentCachingSupplier(atlas,
                atlas.areasWithin(boundary), ItemType.AREA).get();
        final Iterable<Line> linesWithin = getContainmentCachingSupplier(atlas,
                atlas.linesWithin(boundary), ItemType.LINE).get();
        final Iterable<Point> pointsWithin = getContainmentCachingSupplier(atlas,
                atlas.pointsWithin(boundary), ItemType.POINT).get();

        // Generate the size estimates and the builder. There is an edge case we need to
        // consider for node size estimating. Because of our underlying dependency on awt insideness
        // definition - we may be excluding some nodes that are exactly on the boundary of the
        // given polygon. To account for this, instead of doing a count to have an exact number, we
        // choose here to have an arbitrary 5% buffer on top of the nodes inside the polygon. This
        // mostly avoids resizing. No other entity needs the buffer since the resulting within calls
        // give us exact features we want to include.
        final long nodeEstimate = Math.round(Iterables.size(nodesWithin) * HARD_CUT_RATIO_BUFFER);
        final AtlasSize sizeEstimates = new AtlasSize(Iterables.size(edgesWithin), nodeEstimate,
                Iterables.size(areasWithin), Iterables.size(linesWithin),
                Iterables.size(pointsWithin),
                Iterables.size(atlas.relationsWithEntitiesIntersecting(boundary)));
        final PackedAtlasBuilder builder = getPackedAtlasBuilder(atlas, sizeEstimates);

        addNodes(nodesWithin, node -> !hasEntity(node, builder), builder);
        addEdges(edgesWithin, edge -> !hasEntity(edge, builder), builder);
        addAreas(areasWithin, builder);
        addLines(linesWithin, builder);
        addPoints(pointsWithin, builder);

        // Checks all relations not currently in the subatlas and adds in all that have valid
        // members (basically filters out empty relations)
        addRelations(atlas, atlas.relationsLowerOrderFirst(),
                relation -> !hasEntity(relation, builder),
                member -> hasEntity(member.getEntity(), builder), builder,
                AtlasCutType.HARD_CUT_ALL);

        final PackedAtlas result = (PackedAtlas) builder.get();
        if (result != null)
        {
            result.trim();
        }

        logger.trace(CUT_STOP_MESSAGE, AtlasCutType.HARD_CUT_ALL, atlas.getName(),
                begin.elapsedSince());
        return Optional.ofNullable(result);
    }

    public static Optional<Atlas> hardCutAllEntities(final Atlas atlas,
            final Predicate<AtlasEntity> matcher)
    {
        logger.trace(CUT_START_MESSAGE, AtlasCutType.HARD_CUT_ALL, atlas.getName(),
                atlas.metaData());
        final Time begin = Time.now();

        // Using a predicate here can create wild changes in entity counts. For example a predicate
        // would include only edges, but all the nodes would have to be pulled in. In that case, we
        // use the same size as the source Atlas, but we trim it at the end.
        final PackedAtlasBuilder builder = getPackedAtlasBuilder(atlas, atlas.size());

        // Identify all nodes that match the predicate, while avoiding "floating" Nodes that have no
        // connected Edges
        final Set<Long> matchedEdgeIdentifiers = Iterables.stream(atlas.edges(matcher::test))
                .map(Edge::getIdentifier).collectToSet();
        final Predicate<Node> validNodeFilter = node -> !hasEntity(node, builder)
                && node.connectedEdges().stream().anyMatch(connectedEdge -> matchedEdgeIdentifiers
                        .contains(connectedEdge.getIdentifier()));
        addNodes(atlas.nodes(matcher::test), validNodeFilter, builder);

        // Predicate that checks that both the start and end node were added, and that the edge has
        // not yet been added to the builder
        final Predicate<Edge> validEdgeFilter = edge -> !hasEntity(edge, builder)
                && builder.peek().node(edge.start().getIdentifier()) != null
                && builder.peek().node(edge.end().getIdentifier()) != null;
        addEdges(atlas.edges(matcher::test), validEdgeFilter, builder);

        addPoints(atlas.points(matcher::test), builder);
        addAreas(atlas.areas(matcher::test), builder);
        addLines(atlas.lines(matcher::test), builder);

        // Add all relations that matched, as long as they have at least one member left in the
        // subatlas (i.e. aren't empty)
        addRelations(atlas, atlas.relationsLowerOrderFirst(), matcher::test,
                member -> hasEntity(member.getEntity(), builder), builder,
                AtlasCutType.HARD_CUT_ALL);

        final PackedAtlas result = (PackedAtlas) builder.get();
        if (result != null)
        {
            result.trim();
        }

        logger.trace(CUT_STOP_MESSAGE, AtlasCutType.HARD_CUT_ALL, atlas.getName(),
                begin.elapsedSince());
        return Optional.ofNullable(result);
    }

    public static Optional<Atlas> hardCutRelationsOnly(final Atlas atlas,
            final Predicate<AtlasEntity> matcher)
    {
        logger.trace(CUT_START_MESSAGE, AtlasCutType.HARD_CUT_RELATIONS_ONLY, atlas.getName(),
                atlas.metaData());
        final Time begin = Time.now();

        // Using a predicate here can create wild changes in entity counts. For example a predicate
        // would include only edges, but all the nodes would have to be pulled in. In that case, we
        // use the same size as the source Atlas, but we trim it at the end.
        final PackedAtlasBuilder builder = getPackedAtlasBuilder(atlas, atlas.size());

        // Identify all nodes that match the predicate. This includes pulling in nodes that did not
        // match the predicate, but are required by matched edges
        addNodes(atlas.nodes(matcher::test), node -> !hasEntity(node, builder), builder);
        addNodesFromEdges(atlas.edges(matcher::test), builder);

        addEdges(atlas.edges(matcher::test), edge -> !hasEntity(edge, builder), builder);
        addPoints(atlas.points(matcher::test), builder);
        addAreas(atlas.areas(matcher::test), builder);
        addLines(atlas.lines(matcher::test), builder);

        // Add all relations that matched, as long as they have at least one member left in the
        // subatlas (i.e. aren't empty)
        addRelations(atlas, atlas.relationsLowerOrderFirst(), matcher::test,
                member -> hasEntity(member.getEntity(), builder), builder,
                AtlasCutType.HARD_CUT_RELATIONS_ONLY);

        final PackedAtlas result = (PackedAtlas) builder.get();
        if (result != null)
        {
            result.trim();
        }

        logger.trace(CUT_STOP_MESSAGE, AtlasCutType.HARD_CUT_RELATIONS_ONLY, atlas.getName(),
                begin.elapsedSince());
        return Optional.ofNullable(result);
    }

    public static Optional<Atlas> silkCut(final Atlas atlas, final GeometricSurface boundary)
    {
        logger.debug(CUT_START_MESSAGE, AtlasCutType.SILK_CUT, atlas.getName(), atlas.metaData());
        final Time begin = Time.now();

        final Iterable<Node> nodesWithin = getIntersectingCachingSupplier(atlas,
                atlas.nodesWithin(boundary), ItemType.NODE).get();
        final Iterable<Edge> edgesIntersecting = getIntersectingCachingSupplier(atlas,
                atlas.edgesIntersecting(boundary), ItemType.EDGE).get();
        final Iterable<Area> areasIntersecting = getIntersectingCachingSupplier(atlas,
                atlas.areasIntersecting(boundary), ItemType.AREA).get();
        final Iterable<Line> linesIntersecting = getIntersectingCachingSupplier(atlas,
                atlas.linesIntersecting(boundary), ItemType.LINE).get();
        final Iterable<Point> pointsWithin = getIntersectingCachingSupplier(atlas,
                atlas.pointsWithin(boundary), ItemType.POINT).get();

        // Generate the size estimates, then the builder.
        // Nodes estimating is a bit tricky. We want to include all the nodes within the polygon,
        // but we also want to include those attached to edges that span outside the polygon.
        // Instead of doing a count to have an exact number, we choose here to have an arbitrary 20%
        // buffer on top of the nodes inside the polygon. This mostly avoids resizing.
        final long nodeNumber = Math.round(Iterables.size(nodesWithin) * SOFT_CUT_RATIO_BUFFER);
        final long edgeNumber = Math
                .round(Iterables.size(edgesIntersecting) * SOFT_CUT_RATIO_BUFFER);
        final long areaNumber = Math
                .round(Iterables.size(areasIntersecting) * SOFT_CUT_RATIO_BUFFER);
        final long lineNumber = Math
                .round(Iterables.size(linesIntersecting) * SOFT_CUT_RATIO_BUFFER);
        final long pointNumber = Math.round(Iterables.size(pointsWithin) * SOFT_CUT_RATIO_BUFFER);
        final long relationNumber = Math
                .round(Iterables.size(atlas.relationsWithEntitiesIntersecting(boundary))
                        * SOFT_CUT_RATIO_BUFFER);
        final AtlasSize sizeEstimates = new AtlasSize(edgeNumber, nodeNumber, areaNumber,
                lineNumber, pointNumber, relationNumber);

        final PackedAtlasBuilder builder = getPackedAtlasBuilder(atlas, sizeEstimates);

        // Add all the individual nodes and edge start and end nodes coming from the predicate
        addNodes(nodesWithin, node -> !hasEntity(node, builder), builder);
        addNodesFromEdges(edgesIntersecting, builder);

        addEdges(edgesIntersecting, edge -> !hasEntity(edge, builder), builder);
        addAreas(areasIntersecting, builder);
        addLines(linesIntersecting, builder);
        addPoints(pointsWithin, builder);
        addPointsForLines(atlas, linesIntersecting, builder);

        // Check all relations and filter out either hard-cut (filter out members not entirely
        // enclosed in the boundaries) or soft-cut (allow any members that intersected or were
        // within the boundaries)
        addRelations(atlas, atlas.relationsLowerOrderFirst(),
                relation -> !hasEntity(relation, builder),
                member -> hasEntity(member.getEntity(), builder), builder, AtlasCutType.SILK_CUT);

        final PackedAtlas result = (PackedAtlas) builder.get();
        if (result != null)
        {
            result.trim();
        }

        logger.trace(CUT_STOP_MESSAGE, AtlasCutType.SILK_CUT, atlas.getName(),
                begin.elapsedSince());
        return Optional.ofNullable(result);
    }

    /**
     * @param matcher
     *            The matcher to consider
     * @param atlas
     *            The {@link Atlas} to cut
     * @return a sub-atlas from this Atlas.
     */
    public static Optional<Atlas> silkCut(final Atlas atlas, final Predicate<AtlasEntity> matcher)
    {
        logger.trace(CUT_START_MESSAGE, AtlasCutType.SOFT_CUT, atlas.getName(), atlas.metaData());
        final Time begin = Time.now();

        // Using a predicate here can create wild changes in entity counts. For example a predicate
        // would include only edges, but all the nodes would have to be pulled in. In that case, we
        // use the same size as the source Atlas, but we trim it at the end.
        final PackedAtlasBuilder builder = getPackedAtlasBuilder(atlas, atlas.size());

        // First, add all the nodes contained by relations and all start and end nodes from edges
        // contained by relations
        atlas.relations(matcher::test).forEach(relation -> addNodesFromRelation(relation, builder));

        // Next, add all the individual nodes and edge start and end nodes coming from the predicate
        addNodes(atlas.nodes(matcher::test), node -> !hasEntity(node, builder), builder);
        addNodesFromEdges(atlas.edges(matcher::test), builder);

        // Next, add the Lines, Points, Areas and Edges. Edges are a little trickier - 1) They rely
        // on their start/end nodes to have already been added 2) They can potentially pull in nodes
        // that weren't matched by the given predicate. These two cases are handled above.
        // Similarly, Relations depend on all other entities to have been added, since they make up
        // the member list. For this pass, add all entities, except Relations, that match the given
        // Predicate to the builder.
        addEdges(atlas.edges(matcher::test), edge -> !hasEntity(edge, builder), builder);
        addPoints(atlas.points(matcher::test), builder);
        addAreas(atlas.areas(matcher::test), builder);
        addLines(atlas.lines(matcher::test), builder);
        addPointsForLines(atlas, atlas.lines(matcher::test), builder);

        // It's now safe to add Relations. There are two caveats: 1. A Relation member may not
        // have been pulled in by the given predicate. In order to maintain Relation validity, we
        // need to pull in those members. 2. The member may be a Relation that hasn't been
        // added yet. We check if any of the members are unadded relations and if so, we add
        // them.
        Iterables.filter(atlas.relationsLowerOrderFirst(), matcher::test)
                .forEach(relation -> addRelationMembers(relation, builder));
        addRelations(atlas, atlas.relationsLowerOrderFirst(), matcher::test, member -> true,
                builder, AtlasCutType.SOFT_CUT);

        final PackedAtlas result = (PackedAtlas) builder.get();
        if (result != null)
        {
            result.trim();
        }

        logger.trace(CUT_STOP_MESSAGE, AtlasCutType.SOFT_CUT, atlas.getName(),
                begin.elapsedSince());
        return Optional.ofNullable(result);
    }

    public static Optional<Atlas> softCut(final Atlas atlas, final GeometricSurface boundary,
            final boolean hardCutRelations)
    {
        logger.trace(CUT_START_MESSAGE,
                !hardCutRelations ? AtlasCutType.SOFT_CUT : AtlasCutType.HARD_CUT_RELATIONS_ONLY,
                atlas.getName(), atlas.metaData());
        final Time begin = Time.now();

        final Iterable<Node> nodesWithin = getIntersectingCachingSupplier(atlas,
                atlas.nodesWithin(boundary), ItemType.NODE).get();
        final Iterable<Edge> edgesIntersecting = getIntersectingCachingSupplier(atlas,
                atlas.edgesIntersecting(boundary), ItemType.EDGE).get();
        final Iterable<Area> areasIntersecting = getIntersectingCachingSupplier(atlas,
                atlas.areasIntersecting(boundary), ItemType.AREA).get();
        final Iterable<Line> linesIntersecting = getIntersectingCachingSupplier(atlas,
                atlas.linesIntersecting(boundary), ItemType.LINE).get();
        final Iterable<Point> pointsWithin = getIntersectingCachingSupplier(atlas,
                atlas.pointsWithin(boundary), ItemType.POINT).get();

        // Generate the size estimates, then the builder.
        // Nodes estimating is a bit tricky. We want to include all the nodes within the polygon,
        // but we also want to include those attached to edges that span outside the polygon.
        // Instead of doing a count to have an exact number, we choose here to have an arbitrary 20%
        // buffer on top of the nodes inside the polygon. This mostly avoids resizing.
        final long nodeNumber = Math.round(Iterables.size(nodesWithin) * SOFT_CUT_RATIO_BUFFER);
        final long edgeNumber = Math
                .round(Iterables.size(edgesIntersecting) * SOFT_CUT_RATIO_BUFFER);
        final long areaNumber = Math
                .round(Iterables.size(areasIntersecting) * SOFT_CUT_RATIO_BUFFER);
        final long lineNumber = Math
                .round(Iterables.size(linesIntersecting) * SOFT_CUT_RATIO_BUFFER);
        final long pointNumber = Math.round(Iterables.size(pointsWithin) * SOFT_CUT_RATIO_BUFFER);
        final long relationNumber = Math
                .round(Iterables.size(atlas.relationsWithEntitiesIntersecting(boundary))
                        * SOFT_CUT_RATIO_BUFFER);
        final AtlasSize sizeEstimates = new AtlasSize(edgeNumber, nodeNumber, areaNumber,
                lineNumber, pointNumber, relationNumber);

        final PackedAtlasBuilder builder = getPackedAtlasBuilder(atlas, sizeEstimates);

        // Add all the individual nodes and edge start and end nodes coming from the predicate
        addNodes(nodesWithin, node -> !hasEntity(node, builder), builder);
        addNodesFromEdges(edgesIntersecting, builder);

        addEdges(edgesIntersecting, edge -> !hasEntity(edge, builder), builder);
        addAreas(areasIntersecting, builder);
        addLines(linesIntersecting, builder);
        addPoints(pointsWithin, builder);

        final Predicate<RelationMember> validMemberTest;
        if (hardCutRelations)
        {
            validMemberTest = member ->
            {
                switch (member.getEntity().getType())
                {
                    case NODE:
                    case POINT:
                        return boundary.fullyGeometricallyEncloses(
                                ((LocationItem) member.getEntity()).getLocation());
                    case LINE:
                    case EDGE:
                        return boundary.fullyGeometricallyEncloses(
                                ((LineItem) member.getEntity()).asPolyLine());
                    case AREA:
                        return boundary.fullyGeometricallyEncloses(
                                ((Area) member.getEntity()).asPolygon());
                    case RELATION:
                        return hasEntity(member.getEntity(), builder);
                    default:
                        return false;
                }
            };
        }
        else
        {
            validMemberTest = member -> hasEntity(member.getEntity(), builder);
        }

        // Check all relations and filter out either hard-cut (filter out members not entirely
        // enclosed in the boundaries) or soft-cut (allow any members that intersected or were
        // within the boundaries)
        addRelations(atlas, atlas.relationsLowerOrderFirst(),
                relation -> !hasEntity(relation, builder), validMemberTest, builder,
                AtlasCutType.SOFT_CUT);

        final PackedAtlas result = (PackedAtlas) builder.get();
        if (result != null)
        {
            result.trim();
        }

        logger.trace(CUT_STOP_MESSAGE,
                !hardCutRelations ? AtlasCutType.SOFT_CUT : AtlasCutType.HARD_CUT_RELATIONS_ONLY,
                atlas.getName(), begin.elapsedSince());
        return Optional.ofNullable(result);
    }

    /**
     * @param matcher
     *            The matcher to consider
     * @param atlas
     *            The {@link Atlas} to cut
     * @return a sub-atlas from this Atlas.
     */
    public static Optional<Atlas> softCut(final Atlas atlas, final Predicate<AtlasEntity> matcher)
    {
        logger.trace(CUT_START_MESSAGE, AtlasCutType.SOFT_CUT, atlas.getName(), atlas.metaData());
        final Time begin = Time.now();

        // Using a predicate here can create wild changes in entity counts. For example a predicate
        // would include only edges, but all the nodes would have to be pulled in. In that case, we
        // use the same size as the source Atlas, but we trim it at the end.
        final PackedAtlasBuilder builder = getPackedAtlasBuilder(atlas, atlas.size());

        // First, add all the nodes contained by relations and all start and end nodes from edges
        // contained by relations
        atlas.relations(matcher::test).forEach(relation -> addNodesFromRelation(relation, builder));

        // Next, add all the individual nodes and edge start and end nodes coming from the predicate
        addNodes(atlas.nodes(matcher::test), node -> !hasEntity(node, builder), builder);
        addNodesFromEdges(atlas.edges(matcher::test), builder);

        // Next, add the Lines, Points, Areas and Edges. Edges are a little trickier - 1) They rely
        // on their start/end nodes to have already been added 2) They can potentially pull in nodes
        // that weren't matched by the given predicate. These two cases are handled above.
        // Similarly, Relations depend on all other entities to have been added, since they make up
        // the member list. For this pass, add all entities, except Relations, that match the given
        // Predicate to the builder.
        addEdges(atlas.edges(matcher::test), edge -> !hasEntity(edge, builder), builder);
        addPoints(atlas.points(matcher::test), builder);
        addAreas(atlas.areas(matcher::test), builder);
        addLines(atlas.lines(matcher::test), builder);

        // It's now safe to add Relations. There are two caveats: 1. A Relation member may not
        // have been pulled in by the given predicate. In order to maintain Relation validity, we
        // need to pull in those members. 2. The member may be a Relation that hasn't been
        // added yet. We check if any of the members are unadded relations and if so, we add
        // them.
        Iterables.filter(atlas.relationsLowerOrderFirst(), matcher::test)
                .forEach(relation -> addRelationMembers(relation, builder));
        addRelations(atlas, atlas.relationsLowerOrderFirst(), matcher::test, member -> true,
                builder, AtlasCutType.SOFT_CUT);

        final PackedAtlas result = (PackedAtlas) builder.get();
        if (result != null)
        {
            result.trim();
        }

        logger.trace(CUT_STOP_MESSAGE, AtlasCutType.SOFT_CUT, atlas.getName(),
                begin.elapsedSince());
        return Optional.ofNullable(result);
    }

    private static void addAllSubRelations(final Atlas atlas, final Relation parentRelation,
            final PackedAtlasBuilder builder)
    {
        final Set<Long> subrelations = parentRelation.flattenRelations();
        for (final Relation relation : atlas.relationsLowerOrderFirst())
        {
            final Long relationId = relation.getIdentifier();
            if (subrelations.contains(relationId) && !hasEntity(relation, builder))
            {
                builder.addRelation(relationId, relation.getOsmIdentifier(), relation.getBean(),
                        relation.getTags());
            }
            // we shouldn't ever have lower order subrelations after the parent relation!
            // returning here prevents infintite loops and unecessary computations
            if (relationId == parentRelation.getIdentifier())
            {
                return;
            }
        }
    }

    /**
     * Adds all {@link Area}s from the {@link Iterable} to the {@link PackedAtlasBuilder}
     *
     * @param areas
     *            An {@link Iterable} of {@link Area}s to add to the {@link PackedAtlasBuilder}
     * @param builder
     *            The {@link PackedAtlasBuilder} used to build the sub{@link Atlas}
     */
    private static void addAreas(final Iterable<Area> areas, final PackedAtlasBuilder builder)
    {
        areas.forEach(
                area -> builder.addArea(area.getIdentifier(), area.asPolygon(), area.getTags()));
    }

    /**
     * Adds all {@link Edge}s from the {@link Iterable} that pass the filter to the
     * {@link PackedAtlasBuilder}. Uses a special consumer to make sure reverse {@link Edge}s get
     * added as well, for consistency.
     *
     * @param edges
     *            An {@link Iterable} of {@link Edge}s to add to the {@link PackedAtlasBuilder}
     * @param validEdgeFilter
     *            A {@link Predicate} to filter out invalid {@link Edge}s
     * @param builder
     *            The {@link PackedAtlasBuilder} used to build the sub{@link Atlas}
     */
    private static void addEdges(final Iterable<Edge> edges, final Predicate<Edge> validEdges,
            final PackedAtlasBuilder builder)
    {
        final Consumer<Edge> edgeAdder = edge ->
        {
            // Here, making sure that edge identifiers are not 0 to work around an issue in
            // unit tests: https://github.com/osmlab/atlas/issues/252
            if (edge.getIdentifier() != 0 && edge.hasReverseEdge())
            {
                final Edge reverse = edge.reversed().get();
                if (!hasEntity(reverse, builder))
                {
                    builder.addEdge(reverse.getIdentifier(), reverse.asPolyLine(),
                            reverse.getTags());
                }
            }
            builder.addEdge(edge.getIdentifier(), edge.asPolyLine(), edge.getTags());
        };
        Iterables.stream(edges).filter(validEdges).forEach(edgeAdder::accept);
    }

    /**
     * Adds all {@link Line}s from the {@link Iterable} to the {@link PackedAtlasBuilder}
     *
     * @param lines
     *            An {@link Iterable} of {@link Line}s to add to the {@link PackedAtlasBuilder}
     * @param builder
     *            The {@link PackedAtlasBuilder} used to build the sub{@link Atlas}
     */
    private static void addLines(final Iterable<Line> lines, final PackedAtlasBuilder builder)
    {
        lines.forEach(
                line -> builder.addLine(line.getIdentifier(), line.asPolyLine(), line.getTags()));
    }

    /**
     * Adds all {@link Node}s from the {@link Iterable} that pass the filter to the
     * {@link PackedAtlasBuilder}
     *
     * @param nodes
     *            An {@link Iterable} of {@link Node}s to add to the {@link PackedAtlasBuilder}
     * @param validNodeFilter
     *            A {@link Predicate} to filter out invalid {@link Node}s
     * @param builder
     *            The {@link PackedAtlasBuilder} used to build the sub{@link Atlas}
     */
    private static void addNodes(final Iterable<Node> nodes, final Predicate<Node> validNodeFilter,
            final PackedAtlasBuilder builder)
    {
        Iterables.stream(nodes).filter(validNodeFilter).forEach(
                node -> builder.addNode(node.getIdentifier(), node.getLocation(), node.getTags()));
    }

    /**
     * Adds any start or end {@link Node}s from the {@link Iterable} of {@link Edge}s that weren't
     * already in the {@link PackedAtlasBuilder}
     *
     * @param edges
     *            An {@link Iterable} of {@link Edge}s to add {@link Node}s from to the
     *            {@link PackedAtlasBuilder}
     * @param builder
     *            The {@link PackedAtlasBuilder} used to build the sub{@link Atlas}
     */
    private static void addNodesFromEdges(final Iterable<Edge> edges,
            final PackedAtlasBuilder builder)
    {
        edges.forEach(edge ->
        {
            final Node start = edge.start();
            final Node end = edge.end();

            if (!hasEntity(start, builder))
            {
                builder.addNode(start.getIdentifier(), start.getLocation(), start.getTags());
            }
            if (!hasEntity(end, builder))
            {
                builder.addNode(end.getIdentifier(), end.getLocation(), end.getTags());
            }
        });
    }

    /**
     * Uses {@link Relation#flatten()} to get the list of all non-{@link Relation} members
     * recursively, then adds all {@link Node} members, then finally adds all {@link Node} members
     * from {@link Edge}s that may not have been members directly
     *
     * @param relation
     *            The {@link Relation} which we're exploring
     * @param builder
     *            The {@link PackedAtlasBuilder} used to build the sub{@link Atlas}
     */
    @SuppressWarnings("unchecked")
    private static void addNodesFromRelation(final Relation relation,
            final PackedAtlasBuilder builder)
    {
        final Set<AtlasObject> relationMembers = relation.flatten();

        final Set<Edge> edgeMembers = (Set<Edge>) (Set<?>) relationMembers.stream()
                .filter(member -> member instanceof Edge).collect(Collectors.toSet());
        final Set<Node> nodeMembers = (Set<Node>) (Set<?>) relationMembers.stream()
                .filter(member -> member instanceof Node).collect(Collectors.toSet());

        addNodes(nodeMembers, node -> !hasEntity(node, builder), builder);
        addNodesFromEdges(edgeMembers, builder);
    }

    /**
     * Adds all points from the {@link Iterable} to the {@link PackedAtlasBuilder}
     *
     * @param points
     *            An {@link Iterable} of {@link Point}s to add to the {@link PackedAtlasBuilder}
     * @param builder
     *            The {@link PackedAtlasBuilder} used to build the sub{@link Atlas}
     */
    private static void addPoints(final Iterable<Point> points, final PackedAtlasBuilder builder)
    {
        points.forEach(point -> builder.addPoint(point.getIdentifier(), point.getLocation(),
                point.getTags()));
    }

    private static void addPointsForLines(final Atlas atlas, final Iterable<Line> lines,
            final PackedAtlasBuilder builder)
    {
        lines.forEach(line -> line.getRawGeometry()
                .forEach(location -> atlas.pointsAt(location).forEach(point ->
                {
                    if (!hasEntity(point, builder))
                    {
                        builder.addPoint(point.getIdentifier(), point.getLocation(),
                                point.getTags());
                    }
                })));
    }

    /**
     * Uses {@link Relation#flatten()} to get the list of all non-{@link Relation} members
     * recursively, then adds all {@link Edge}, {@link Area}, {@link Line}, and {@link Point}
     * members
     *
     * @param relation
     *            The {@link Relation} to add members from
     * @param builder
     *            The {@link PackedAtlasBuilder} used to build the sub{@link Atlas}
     */
    @SuppressWarnings("unchecked")
    private static void addRelationMembers(final Relation relation,
            final PackedAtlasBuilder builder)
    {
        final Set<AtlasObject> relationMembers = relation.flatten();
        addEdges(
                (Set<Edge>) (Set<?>) relationMembers.stream()
                        .filter(member -> member instanceof Edge).collect(Collectors.toSet()),
                edge -> !hasEntity(edge, builder), builder);

        addAreas((Set<Area>) (Set<?>) relationMembers.stream()
                .filter(member -> member instanceof Area && !hasEntity((Area) member, builder))
                .collect(Collectors.toSet()), builder);
        addLines((Set<Line>) (Set<?>) relationMembers.stream()
                .filter(member -> member instanceof Line && !hasEntity((Line) member, builder))
                .collect(Collectors.toSet()), builder);
        addPoints((Set<Point>) (Set<?>) relationMembers.stream()
                .filter(member -> member instanceof Point && !hasEntity((Point) member, builder))
                .collect(Collectors.toSet()), builder);

    }

    /**
     * Adds all relations from the {@link Iterable} that pass the {@link Relation} filter test and
     * have at least one member that was added to the sub{@link Atlas}. If the cut type is a
     * {@link AtlasCutType.SOFT_CUT}, then any {@link Relation} that passed the filter will have all
     * its members that pass the valid member filter added to the sub{@link Atlas} if they don't
     * already exist.
     *
     * @param relations
     *            An {@link Iterable} of Relations to add to the builder
     * @param validRelationTest
     *            A filter for the {@link Relation}s
     * @param validMemberTest
     *            A filter for the members of any valid {@link Relation}s
     * @param builder
     *            The {@link PackedAtlasBuilder} used to build the sub{@link Atlas}
     * @param cutType
     *            The {@link AtlasCutType} representing the kind of cut being performed
     */
    private static void addRelations(final Atlas atlas, final Iterable<Relation> relations,
            final Predicate<Relation> validRelationTest,
            final Predicate<RelationMember> validMemberTest, final PackedAtlasBuilder builder,
            final AtlasCutType cutType)
    {
        Iterables.stream(relations).filter(validRelationTest).forEach(relation ->
        {
            final List<RelationMember> validMembers = relation.members().stream()
                    .filter(validMemberTest).collect(Collectors.toList());
            if (!validMembers.isEmpty())
            {
                if (AtlasCutType.SOFT_CUT.equals(cutType) || AtlasCutType.SILK_CUT.equals(cutType))
                {
                    validMembers.forEach(member ->
                    {
                        if (member.getEntity() instanceof Relation
                                && !hasEntity(member.getEntity(), builder))
                        {
                            addAllSubRelations(atlas, (Relation) member.getEntity(), builder);
                        }
                    });
                }
                final RelationBean structure = new RelationBean();
                validMembers.forEach(validMember -> structure.addItem(
                        validMember.getEntity().getIdentifier(), validMember.getRole(),
                        ItemType.forEntity(validMember.getEntity())));
                if (relation.asMultiPolygon().isPresent())
                {
                    builder.addRelation(relation.getIdentifier(), relation.getOsmIdentifier(),
                            structure, relation.getTags(), relation.asMultiPolygon().get());
                }
                else
                {
                    builder.addRelation(relation.getIdentifier(), relation.getOsmIdentifier(),
                            structure, relation.getTags());
                }
            }
            else
            {
                logger.trace(
                        "Excluding relation {} from sub-atlas since none of its members pass the {} cut.",
                        relation.getIdentifier(), AtlasCutType.HARD_CUT_ALL);
            }
        });
    }

    private static <M extends AtlasEntity> Supplier<Iterable<M>> getContainmentCachingSupplier(
            final Atlas atlas, final Iterable<M> source, final ItemType type)
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
                        .map(entityIdentifier -> atlas.entity(entityIdentifier, type)).collect();
            }
        };
        return result;
    }

    private static <M extends AtlasEntity> Supplier<Iterable<M>> getIntersectingCachingSupplier(
            final Atlas atlas, final Iterable<M> source, final ItemType type)
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
                        .map(entityIdentifier -> atlas.entity(entityIdentifier, type)).collect();
            }
        };
        return result;
    }

    /**
     * Instantiates a {@link PackedAtlasBuilder}, used in all sub{@link Atlas} types. Ensures all
     * sub{@link Atlas} cut types are consistent wrt naming, metadata, etc of returned Atlas
     *
     * @param sizeEstimates
     *            The {@link AtlasSize} to use for the {@link PackedAtlasBuilder} -- this changes
     *            depending on {@link AtlasCutType}, some will use the base {@link Atlas} size,
     *            others make their own estimates
     * @return {@link PackedAtlasBuilder} instantiated with size estimates and name/metadata from
     *         base Atlas
     */
    private static PackedAtlasBuilder getPackedAtlasBuilder(final Atlas atlas,
            final AtlasSize sizeEstimates)
    {
        return new PackedAtlasBuilder().withSizeEstimates(sizeEstimates)
                .withMetaData(atlas.metaData())
                .withName(String.format("%s%s", atlas.getName(), SUB_ATLAS_NAME_POSTFIX))
                .withEnhancedRelationGeometry();
    }

    /**
     * Checks the {@link PackedAtlasBuilder} to see if the {@link AtlasEntity} has been added or not
     *
     * @param entity
     *            {@link AtlasEntity} to check existence of
     * @param builder
     *            The {@link PackedAtlasBuilder} used to build the sub{@link Atlas}
     * @return True if an {@link AtlasEntity} with the same {@link ItemType} and identifier exists
     *         in the {@link PackedAtlasBuilder}, false otherwise
     */
    private static boolean hasEntity(final AtlasEntity entity, final PackedAtlasBuilder builder)
    {
        switch (entity.getType())
        {
            case POINT:
                return builder.peek().point(entity.getIdentifier()) != null;
            case NODE:
                return builder.peek().node(entity.getIdentifier()) != null;
            case EDGE:
                return builder.peek().edge(entity.getIdentifier()) != null;
            case LINE:
                return builder.peek().line(entity.getIdentifier()) != null;
            case AREA:
                return builder.peek().area(entity.getIdentifier()) != null;
            case RELATION:
                return builder.peek().relation(entity.getIdentifier()) != null;
            default:
                return false;
        }
    }

    private SubAtlasCreator()
    {
    }
}
