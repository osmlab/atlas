package org.openstreetmap.atlas.geography.atlas.change.diff;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedArea;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedEdge;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedLine;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedNode;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedPoint;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedRelation;
import org.openstreetmap.atlas.geography.atlas.change.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMember;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
import org.openstreetmap.atlas.geography.matching.PolyLineRoute;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class for {@link AtlasDiff}. Contains lots of static utilities.
 *
 * @author lcram
 */
public final class AtlasDiffHelper
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasDiffHelper.class);

    public static boolean edgeHasGeometryMatchAmong(final Edge edge,
            final Iterable<Edge> otherEdges, final boolean useGeometryMatching)
    {
        if (useGeometryMatching)
        {
            final PolyLine source = edge.asPolyLine();
            final List<PolyLine> candidates = Iterables.stream(otherEdges).map(Edge::asPolyLine)
                    .collectToList();
            final Optional<PolyLineRoute> match = source.costDistanceToOneWay(candidates)
                    .match(Distance.ZERO);
            if (match.isPresent() && match.get().getCost().isLessThanOrEqualTo(Distance.ZERO))
            {
                // The edge was probably split by way sectioning without changing itself.
                logger.trace("Edge {} from {} has no equal member but found a match with no cost.",
                        edge, edge.getAtlas().getName());
                return true;
            }
        }
        return false;
    }

    public static boolean edgeHasGeometryMatchInAtlas(final Edge edge, final Atlas atlas,
            final boolean useGeometryMatching)
    {
        if (useGeometryMatching)
        {
            final Iterable<Edge> intersectingEdgesWithSameOSMIdentifier = atlas.edgesIntersecting(
                    edge.bounds(),
                    otherEdge -> edge.getOsmIdentifier() == otherEdge.getOsmIdentifier());
            return edgeHasGeometryMatchAmong(edge, intersectingEdgesWithSameOSMIdentifier,
                    useGeometryMatching);
        }
        return false;
    }

    public static Optional<FeatureChange> getAreaChangeIfNecessary(final Area beforeArea,
            final Area afterArea, final boolean useBloatedEntities, final boolean saveAllGeometry)
    {
        try
        {
            boolean featureChangeWouldBeUseful = false;
            final BloatedArea bloatedArea = BloatedArea.shallowFrom(afterArea);
            if (!beforeArea.asPolygon().equals(afterArea.asPolygon()))
            {
                bloatedArea.withPolygon(afterArea.asPolygon());
                featureChangeWouldBeUseful = true;
            }
            if (featureChangeWouldBeUseful)
            {
                if (useBloatedEntities)
                {
                    return Optional.of(new FeatureChange(ChangeType.ADD, bloatedArea));
                }
                else
                {
                    return Optional.of(new FeatureChange(ChangeType.ADD, afterArea));
                }
            }
        }
        catch (final Exception exception)
        {
            throw new CoreException("Unable to compare areas {} and {}", beforeArea, afterArea,
                    exception);
        }
        return Optional.empty();
    }

    public static Optional<FeatureChange> getEdgeChangeIfNecessary(final Edge beforeEdge,
            final Edge afterEdge, final Atlas beforeAtlas, final Atlas afterAtlas,
            final boolean useGeometryMatching, final boolean useBloatedEntities,
            final boolean saveAllGeometry)
    {
        try
        {
            boolean featureChangeWouldBeUseful = false;
            final BloatedEdge bloatedEdge = BloatedEdge.shallowFrom(afterEdge);
            if (!beforeEdge.asPolyLine().equals(afterEdge.asPolyLine()))
            {
                bloatedEdge.withPolyLine(afterEdge.asPolyLine());
                featureChangeWouldBeUseful = true;
            }
            if (beforeEdge.start().getIdentifier() != afterEdge.start().getIdentifier())
            {
                bloatedEdge.withStartNodeIdentifier(afterEdge.start().getIdentifier());
                featureChangeWouldBeUseful = true;
            }
            if (beforeEdge.end().getIdentifier() != afterEdge.end().getIdentifier())
            {
                bloatedEdge.withEndNodeIdentifier(afterEdge.end().getIdentifier());
                featureChangeWouldBeUseful = true;
            }
            if (featureChangeWouldBeUseful)
            {
                if (useBloatedEntities)
                {
                    return Optional.of(new FeatureChange(ChangeType.ADD, bloatedEdge));
                }
                else
                {
                    return Optional.of(new FeatureChange(ChangeType.ADD, afterEdge));
                }
            }
        }
        catch (final Exception exception)
        {
            throw new CoreException("Unable to compare edges {} and {}", beforeEdge, afterEdge,
                    exception);
        }
        return Optional.empty();
    }

    public static Optional<FeatureChange> getLineChangeIfNecessary(final Line beforeLine,
            final Line afterLine, final boolean useBloatedEntities, final boolean saveAllGeometry)
    {
        try
        {
            boolean featureChangeWouldBeUseful = false;
            final BloatedLine bloatedLine = BloatedLine.shallowFrom(afterLine);
            if (!beforeLine.asPolyLine().equals(afterLine.asPolyLine()))
            {
                bloatedLine.withPolyLine(afterLine.asPolyLine());
                featureChangeWouldBeUseful = true;
            }
            if (featureChangeWouldBeUseful)
            {
                if (useBloatedEntities)
                {
                    return Optional.of(new FeatureChange(ChangeType.ADD, bloatedLine));
                }
                else
                {
                    return Optional.of(new FeatureChange(ChangeType.ADD, afterLine));
                }
            }
        }
        catch (final Exception exception)
        {
            throw new CoreException("Unable to compare lines {} and {}", beforeLine, afterLine,
                    exception);
        }
        return Optional.empty();
    }

    public static Optional<FeatureChange> getNodeChangeIfNecessary(final Node beforeNode,
            final Node afterNode, final boolean useGeometryMatching,
            final boolean useBloatedEntities, final boolean saveAllGeometries)
    {
        try
        {
            boolean featureChangeWouldBeUseful = false;
            final BloatedNode bloatedNode = BloatedNode.shallowFrom(afterNode);
            if (!beforeNode.getLocation().equals(afterNode.getLocation()))
            {
                bloatedNode.withLocation(afterNode.getLocation());
                featureChangeWouldBeUseful = true;
            }
            if (differentEdgeSet(beforeNode.inEdges(), afterNode.inEdges(), useGeometryMatching))
            {
                bloatedNode.withInEdgeIdentifiers(new TreeSet<>(afterNode.inEdges().stream()
                        .map(edge -> edge.getIdentifier()).collect(Collectors.toSet())));
                if (saveAllGeometries)
                {
                    bloatedNode.withLocation(afterNode.getLocation());
                }
                featureChangeWouldBeUseful = true;
            }
            if (differentEdgeSet(beforeNode.outEdges(), afterNode.outEdges(), useGeometryMatching))
            {
                bloatedNode.withOutEdgeIdentifiers(new TreeSet<>(afterNode.outEdges().stream()
                        .map(edge -> edge.getIdentifier()).collect(Collectors.toSet())));
                if (saveAllGeometries)
                {
                    bloatedNode.withLocation(afterNode.getLocation());
                }
                featureChangeWouldBeUseful = true;
            }
            if (featureChangeWouldBeUseful)
            {
                if (useBloatedEntities)
                {
                    return Optional.of(new FeatureChange(ChangeType.ADD, bloatedNode));
                }
                else
                {
                    return Optional.of(new FeatureChange(ChangeType.ADD, afterNode));
                }
            }
        }
        catch (final Exception exception)
        {
            throw new CoreException("Unable to compare nodes {} and {}", beforeNode, afterNode,
                    exception);
        }
        return Optional.empty();
    }

    public static Optional<FeatureChange> getParentRelationMembershipChangeIfNecessary(
            final AtlasEntity beforeEntity, final AtlasEntity afterEntity, final Atlas beforeAtlas,
            final Atlas afterAtlas, final boolean useBloatedEntities,
            final boolean saveAllGeometries)
    {
        try
        {
            final Set<Long> beforeRelationIdentifiers = beforeEntity.relations().stream()
                    .map(relation -> relation.getIdentifier()).collect(Collectors.toSet());
            final Set<Long> afterRelationIdentifiers = afterEntity.relations().stream()
                    .map(relation -> relation.getIdentifier()).collect(Collectors.toSet());

            /*
             * We never had any parent relations. We want to explicitly return empty so as not to
             * generate a misleading FeatureChange changing a null into an empty set.
             */
            if (beforeRelationIdentifiers.isEmpty() && afterRelationIdentifiers.isEmpty())
            {
                return Optional.empty();
            }

            /*
             * Now, check to see if the afterEntity was different from the beforeEntity in any
             * relations in the afterAtlas. If they were not different, there is no feature change.
             */
            if (!entitiesWereDifferentInRelations(beforeEntity, afterEntity, beforeAtlas,
                    afterAtlas))
            {
                return Optional.empty();
            }

            /*
             * OK! We made it here because we have confirmed that the entities have some
             * relation-related difference. We create a feature change to reflect this.
             */
            if (useBloatedEntities)
            {
                final AtlasEntity bloatedEntity;
                switch (afterEntity.getType())
                {
                    case AREA:
                        BloatedArea area = BloatedArea.shallowFrom((Area) afterEntity)
                                .withRelationIdentifiers(afterRelationIdentifiers);
                        if (saveAllGeometries)
                        {
                            area = area.withPolygon(((Area) afterEntity).asPolygon());
                        }
                        bloatedEntity = area;
                        break;
                    case EDGE:
                        BloatedEdge edge = BloatedEdge.shallowFrom((Edge) afterEntity)
                                .withRelationIdentifiers(afterRelationIdentifiers);
                        if (saveAllGeometries)
                        {
                            edge = edge.withPolyLine(((Edge) afterEntity).asPolyLine());
                        }
                        bloatedEntity = edge;
                        break;
                    case LINE:
                        BloatedLine line = BloatedLine.shallowFrom((Line) afterEntity)
                                .withRelationIdentifiers(afterRelationIdentifiers);
                        if (saveAllGeometries)
                        {
                            line = line.withPolyLine(((Line) afterEntity).asPolyLine());
                        }
                        bloatedEntity = line;
                        break;
                    case NODE:
                        BloatedNode node = BloatedNode.shallowFrom((Node) afterEntity)
                                .withRelationIdentifiers(afterRelationIdentifiers);
                        if (saveAllGeometries)
                        {
                            node = node.withLocation(((Node) afterEntity).getLocation());
                        }
                        bloatedEntity = node;
                        break;
                    case POINT:
                        BloatedPoint point = BloatedPoint.shallowFrom((Point) afterEntity)
                                .withRelationIdentifiers(afterRelationIdentifiers);
                        if (saveAllGeometries)
                        {
                            point = point.withLocation(((Point) afterEntity).getLocation());
                        }
                        bloatedEntity = point;
                        break;
                    case RELATION:
                        final BloatedRelation relation = BloatedRelation
                                .shallowFrom((Relation) afterEntity)
                                .withRelationIdentifiers(afterRelationIdentifiers);
                        bloatedEntity = relation;
                        break;
                    default:
                        throw new CoreException("Unknown item type {}", afterEntity.getType());
                }
                // featureChange should never be null
                return Optional.of(new FeatureChange(ChangeType.ADD, bloatedEntity));
            }
            return Optional.of(new FeatureChange(ChangeType.ADD, afterEntity));
        }
        catch (final Exception exception)
        {
            throw new CoreException("Unable to compare relations for {} and {}", beforeEntity,
                    afterEntity, exception);
        }
    }

    public static Optional<FeatureChange> getPointChangeIfNecessary(final Point beforePoint,
            final Point afterPoint, final boolean useBloatedEntities,
            final boolean saveAllGeometries)
    {
        try
        {
            boolean featureChangeWouldBeUseful = false;
            final BloatedPoint bloatedPoint = BloatedPoint.shallowFrom(afterPoint);
            if (!beforePoint.getLocation().equals(afterPoint.getLocation()))
            {
                bloatedPoint.withLocation(afterPoint.getLocation());
                featureChangeWouldBeUseful = true;
            }
            if (featureChangeWouldBeUseful)
            {
                if (useBloatedEntities)
                {
                    return Optional.of(new FeatureChange(ChangeType.ADD, bloatedPoint));
                }
                else
                {
                    return Optional.of(new FeatureChange(ChangeType.ADD, afterPoint));
                }
            }
        }
        catch (final Exception exception)
        {
            throw new CoreException("Unable to compare points {} and {}", beforePoint, afterPoint,
                    exception);
        }
        return Optional.empty();
    }

    public static Optional<FeatureChange> getRelationChangeIfNecessary(
            final Relation beforeRelation, final Relation afterRelation, final Atlas beforeAtlas,
            final Atlas afterAtlas, final boolean useGeometryMatching,
            final boolean useBloatedEntities, final boolean saveAllGeometry)
    {
        try
        {
            boolean featureChangeWouldBeUseful = false;
            final BloatedRelation bloatedRelation = BloatedRelation.shallowFrom(afterRelation);
            final RelationMemberList beforeMembers = beforeRelation.members();
            final RelationMemberList afterMembers = afterRelation.members();
            if (!afterMembers.equals(beforeMembers))
            {
                if (!geometryMatchInRelationMemberList(beforeMembers, afterMembers,
                        useGeometryMatching))
                {
                    // TODO this should be beforeRelation, right?
                    bloatedRelation.withMembers(Optional.of(beforeRelation),
                            afterRelation.members());
                    featureChangeWouldBeUseful = true;
                }
            }
            if (featureChangeWouldBeUseful)
            {
                if (useBloatedEntities)
                {
                    return Optional.of(new FeatureChange(ChangeType.ADD, bloatedRelation));
                }
                else
                {
                    return Optional.of(new FeatureChange(ChangeType.ADD, afterRelation));
                }
            }
        }
        catch (final Exception exception)
        {
            throw new CoreException("Unable to compare relations {} and {}", beforeRelation,
                    afterRelation, exception);
        }
        return Optional.empty();
    }

    public static Optional<FeatureChange> getTagChangeIfNecessary(final AtlasEntity beforeEntity,
            final AtlasEntity afterEntity, final boolean useBloatedEntities,
            final boolean saveAllGeometries)
    {
        if (beforeEntity.getTags().equals(afterEntity.getTags()))
        {
            return Optional.empty();
        }

        if (useBloatedEntities)
        {
            final AtlasEntity bloatedEntity;
            switch (afterEntity.getType())
            {
                case AREA:
                    BloatedArea area = BloatedArea.shallowFrom((Area) afterEntity)
                            .withTags(afterEntity.getTags());
                    if (saveAllGeometries)
                    {
                        area = area.withPolygon(((Area) afterEntity).asPolygon());
                    }
                    bloatedEntity = area;
                    break;
                case EDGE:
                    BloatedEdge edge = BloatedEdge.shallowFrom((Edge) afterEntity)
                            .withTags(afterEntity.getTags());
                    if (saveAllGeometries)
                    {
                        edge = edge.withPolyLine(((Edge) afterEntity).asPolyLine());
                    }
                    bloatedEntity = edge;
                    break;
                case LINE:
                    BloatedLine line = BloatedLine.shallowFrom((Line) afterEntity)
                            .withTags(afterEntity.getTags());
                    if (saveAllGeometries)
                    {
                        line = line.withPolyLine(((Line) afterEntity).asPolyLine());
                    }
                    bloatedEntity = line;
                    break;
                case NODE:
                    BloatedNode node = BloatedNode.shallowFrom((Node) afterEntity)
                            .withTags(afterEntity.getTags());
                    if (saveAllGeometries)
                    {
                        node = node.withLocation(((Node) afterEntity).getLocation());
                    }
                    bloatedEntity = node;
                    break;
                case POINT:
                    BloatedPoint point = BloatedPoint.shallowFrom((Point) afterEntity)
                            .withTags(afterEntity.getTags());
                    if (saveAllGeometries)
                    {
                        point = point.withLocation(((Point) afterEntity).getLocation());
                    }
                    bloatedEntity = point;
                    break;
                case RELATION:
                    final BloatedRelation relation = BloatedRelation
                            .shallowFrom((Relation) afterEntity).withTags(afterEntity.getTags());
                    bloatedEntity = relation;
                    break;
                default:
                    throw new CoreException("Unknown item type {}", afterEntity.getType());
            }
            return Optional.of(new FeatureChange(ChangeType.ADD, bloatedEntity));
        }
        return Optional.of(new FeatureChange(ChangeType.ADD, afterEntity));
    }

    public static FeatureChange simpleBloatedAreaChange(final ChangeType changeType,
            final Atlas atlas, final AtlasEntity entity, final boolean saveAllGeometries)
    {
        final Long entityIdentifier = entity.getIdentifier();
        if (changeType == ChangeType.REMOVE)
        {
            BloatedArea bloatedArea = BloatedArea.shallowFrom(atlas.area(entityIdentifier));
            if (saveAllGeometries)
            {
                bloatedArea = bloatedArea.withPolygon(((Area) entity).asPolygon());
            }
            return new FeatureChange(changeType, bloatedArea);
        }
        else
        {
            return new FeatureChange(changeType, BloatedArea.from(atlas.area(entityIdentifier)));
        }
    }

    public static FeatureChange simpleBloatedEdgeChange(final ChangeType changeType,
            final Atlas atlas, final AtlasEntity entity, final boolean saveAllGeometries)
    {
        final Long entityIdentifier = entity.getIdentifier();
        if (changeType == ChangeType.REMOVE)
        {
            BloatedEdge bloatedEdge = BloatedEdge.shallowFrom(atlas.edge(entityIdentifier));
            if (saveAllGeometries)
            {
                bloatedEdge = bloatedEdge.withPolyLine(((Edge) entity).asPolyLine());
            }
            return new FeatureChange(changeType, bloatedEdge);
        }
        else
        {
            return new FeatureChange(changeType, BloatedEdge.from(atlas.edge(entityIdentifier)));
        }
    }

    public static FeatureChange simpleBloatedLineChange(final ChangeType changeType,
            final Atlas atlas, final AtlasEntity entity, final boolean saveAllGeometries)
    {
        final Long entityIdentifier = entity.getIdentifier();
        if (changeType == ChangeType.REMOVE)
        {
            BloatedLine bloatedLine = BloatedLine.shallowFrom(atlas.line(entityIdentifier));
            if (saveAllGeometries)
            {
                bloatedLine = bloatedLine.withPolyLine(((Line) entity).asPolyLine());
            }
            return new FeatureChange(changeType, bloatedLine);
        }
        else
        {
            return new FeatureChange(changeType, BloatedLine.from(atlas.line(entityIdentifier)));
        }
    }

    public static FeatureChange simpleBloatedNodeChange(final ChangeType changeType,
            final Atlas atlas, final AtlasEntity entity, final boolean saveAllGeometries)
    {
        final Long entityIdentifier = entity.getIdentifier();
        if (changeType == ChangeType.REMOVE)
        {
            BloatedNode bloatedNode = BloatedNode.shallowFrom(atlas.node(entityIdentifier));
            if (saveAllGeometries)
            {
                bloatedNode = bloatedNode.withLocation(((Node) entity).getLocation());
            }
            return new FeatureChange(changeType, bloatedNode);
        }
        else
        {
            return new FeatureChange(changeType, BloatedNode.from(atlas.node(entityIdentifier)));
        }
    }

    public static FeatureChange simpleBloatedPointChange(final ChangeType changeType,
            final Atlas atlas, final AtlasEntity entity, final boolean saveAllGeometries)
    {
        final Long entityIdentifier = entity.getIdentifier();
        if (changeType == ChangeType.REMOVE)
        {
            BloatedPoint bloatedPoint = BloatedPoint.shallowFrom(atlas.point(entityIdentifier));
            if (saveAllGeometries)
            {
                bloatedPoint = bloatedPoint.withLocation(((Point) entity).getLocation());
            }
            return new FeatureChange(changeType, bloatedPoint);
        }
        else
        {
            return new FeatureChange(changeType, BloatedPoint.from(atlas.point(entityIdentifier)));
        }
    }

    public static FeatureChange simpleBloatedRelationChange(final ChangeType changeType,
            final Atlas atlas, final AtlasEntity entity, final boolean saveAllGeometries)
    {
        final Long entityIdentifier = entity.getIdentifier();
        if (changeType == ChangeType.REMOVE)
        {
            final BloatedRelation bloatedRelation = BloatedRelation
                    .shallowFrom(atlas.relation(entityIdentifier));
            return new FeatureChange(changeType, bloatedRelation);
        }
        else
        {
            return new FeatureChange(changeType,
                    BloatedRelation.from(atlas.relation(entityIdentifier)));
        }
    }

    private static boolean differentEdgeSet(final SortedSet<Edge> beforeEdges,
            final SortedSet<Edge> afterEdges, final boolean useGeometryMatching)
    {
        /*
         * Here, we try a simple equivalence check first, and only go to the more expensive deep
         * check if necessary. This is because the quick check will find false positive cases where
         * edges look different due to way sectioning, but have the same underlying geometry.
         */
        return differentEdgeSetQuickCheck(beforeEdges, afterEdges)
                && differentEdgeSetDeeperCheck(beforeEdges, afterEdges, useGeometryMatching);
    }

    private static boolean differentEdgeSetDeeperCheck(final Set<Edge> beforeEdges,
            final Set<Edge> afterEdges, final boolean useGeometryMatching)
    {
        if (beforeEdges.isEmpty() && afterEdges.isEmpty())
        {
            return false;
        }
        boolean beforeToAfterResult = beforeEdges.isEmpty();
        for (final Edge edge : beforeEdges)
        {
            if (afterEdges.isEmpty()
                    || !edgeHasGeometryMatchAmong(edge, afterEdges, useGeometryMatching))
            {
                beforeToAfterResult = true;
                break;
            }
        }
        boolean afterToBeforeResult = afterEdges.isEmpty();
        for (final Edge edge : afterEdges)
        {
            if (beforeEdges.isEmpty()
                    || !edgeHasGeometryMatchAmong(edge, beforeEdges, useGeometryMatching))
            {
                afterToBeforeResult = true;
                break;
            }
        }
        return beforeToAfterResult && afterToBeforeResult;
    }

    private static boolean differentEdgeSetQuickCheck(final SortedSet<Edge> beforeEdges,
            final SortedSet<Edge> afterEdges)
    {
        if (beforeEdges.size() != afterEdges.size())
        {
            return true;
        }
        final Iterator<Edge> beforeInEdgeIterator = beforeEdges.iterator();
        final Iterator<Edge> afterInEdgeIterator = afterEdges.iterator();
        for (int i = 0; i < beforeEdges.size(); i++)
        {
            final Edge beforeInEdge = beforeInEdgeIterator.next();
            final Edge afterInEdge = afterInEdgeIterator.next();
            if (beforeInEdge.getIdentifier() != afterInEdge.getIdentifier())
            {
                return true;
            }
        }
        return false;
    }

    private static boolean entitiesWereDifferentInRelations(final AtlasEntity beforeEntity,
            final AtlasEntity afterEntity, final Atlas beforeAtlas, final Atlas afterAtlas)
    {
        try
        {
            final Set<Relation> beforeRelations = beforeEntity.relations();
            final Set<Relation> afterRelations = afterEntity.relations();

            if (beforeRelations.size() != afterRelations.size())
            {
                return true;
            }
            for (final Relation beforeRelation : beforeRelations)
            {
                Relation afterRelation = null;
                for (final Relation afterRelationCandidate : afterRelations)
                {
                    if (afterRelationCandidate.getIdentifier() == beforeRelation.getIdentifier())
                    {
                        afterRelation = afterRelationCandidate;
                        break;
                    }
                }
                if (afterRelation == null)
                {
                    // The two relation sets are different
                    return true;
                }

                // Index of the member in the Relation's member list
                int beforeIndex = -1;
                int afterIndex = -1;
                final RelationMemberList beforeMembers = beforeRelation.members();
                final RelationMemberList afterMembers = afterRelation.members();
                for (int j = 0; j < beforeMembers.size(); j++)
                {
                    final RelationMember beforeMember = beforeMembers.get(j);
                    if (beforeMember.getEntity().getIdentifier() == beforeEntity.getIdentifier())
                    {
                        beforeIndex = j;
                    }
                }
                for (int j = 0; j < afterMembers.size(); j++)
                {
                    final RelationMember afterMember = afterMembers.get(j);
                    if (afterMember.getEntity().getIdentifier() == beforeEntity.getIdentifier())
                    {
                        afterIndex = j;
                    }
                }
                if (beforeIndex < 0 || afterIndex < 0)
                {
                    throw new CoreException("Corrupted Atlas dataset.");
                }
                if (beforeIndex != afterIndex)
                {
                    // Order changed
                    return true;
                }
                if (!beforeMembers.get(beforeIndex).getRole()
                        .equals(afterMembers.get(afterIndex).getRole()))
                {
                    // Role changed
                    return true;
                }
                if (beforeMembers.get(beforeIndex).getEntity().getType() != afterMembers
                        .get(afterIndex).getEntity().getType())
                {
                    // Type changed
                    return true;
                }
            }
            return false;
        }
        catch (final Exception exception)
        {
            throw new CoreException("Unable to compare relations for {} and {}", beforeEntity,
                    afterEntity, exception);
        }
    }

    private static boolean geometryMatchInRelationMemberList(final RelationMemberList beforeMembers,
            final RelationMemberList afterMembers, final boolean useGeometryMatching)
    {
        final SortedSet<Edge> beforeEdges = Iterables.stream(beforeMembers)
                .map(member -> member.getEntity()).filter(entity -> entity instanceof Edge)
                .map(entity -> (Edge) entity).collectToSortedSet();
        final SortedSet<Edge> afterEdges = Iterables.stream(afterMembers)
                .map(member -> member.getEntity()).filter(entity -> entity instanceof Edge)
                .map(entity -> (Edge) entity).collectToSortedSet();
        return differentEdgeSet(beforeEdges, afterEdges, useGeometryMatching);
    }

    private AtlasDiffHelper()
    {

    }
}
