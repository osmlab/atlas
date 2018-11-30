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
 * A helper class for {@link AtlasDiff}. Contains lots of static utilities. Ideally, it would be
 * nice to refactor this since there is a lot of duplicated code patterns.
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

    public static Optional<FeatureChange> getNodeChangeIfNecessary(final Node beforeNode,
            final Node afterNode, final boolean useGeometryMatching)
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
                featureChangeWouldBeUseful = true;
            }
            if (differentEdgeSet(beforeNode.outEdges(), afterNode.outEdges(), useGeometryMatching))
            {
                bloatedNode.withOutEdgeIdentifiers(new TreeSet<>(afterNode.outEdges().stream()
                        .map(edge -> edge.getIdentifier()).collect(Collectors.toSet())));
                featureChangeWouldBeUseful = true;
            }
            if (featureChangeWouldBeUseful)
            {
                return Optional.of(new FeatureChange(ChangeType.ADD, bloatedNode));
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
            final AtlasEntity beforeEntity, final AtlasEntity afterEntity)
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
                if (beforeMembers.get(beforeIndex).getEntity().getType() != afterMembers.get(afterIndex)
                        .getEntity().getType())
                {
                    // Type changed
                    return true;
                }
            }
        }
        catch (final Exception exception)
        {
            throw new CoreException("Unable to compare relations for {} and {}", beforeEntity,
                    afterEntity, exception);
        }

        return Optional.empty();
    }

    public static Optional<FeatureChange> getTagChangeIfNecessary(final AtlasEntity beforeEntity,
            final AtlasEntity entity)
    {
        if (!beforeEntity.getTags().equals(entity.getTags()))
        {
            FeatureChange featureChange;
            switch (entity.getType())
            {
                case AREA:
                    featureChange = new FeatureChange(ChangeType.ADD,
                            BloatedArea.shallowFrom((Area) entity).withTags(entity.getTags()));
                    break;
                case EDGE:
                    featureChange = new FeatureChange(ChangeType.ADD,
                            BloatedEdge.shallowFrom((Edge) entity).withTags(entity.getTags()));
                    break;
                case LINE:
                    featureChange = new FeatureChange(ChangeType.ADD,
                            BloatedLine.shallowFrom((Line) entity).withTags(entity.getTags()));
                    break;
                case NODE:
                    featureChange = new FeatureChange(ChangeType.ADD,
                            BloatedNode.shallowFrom((Node) entity).withTags(entity.getTags()));
                    break;
                case POINT:
                    featureChange = new FeatureChange(ChangeType.ADD,
                            BloatedPoint.shallowFrom((Point) entity).withTags(entity.getTags()));
                    break;
                case RELATION:
                    featureChange = new FeatureChange(ChangeType.ADD, BloatedRelation
                            .shallowFrom((Relation) entity).withTags(entity.getTags()));
                    break;
                default:
                    throw new CoreException("Unknown item type {}", entity.getType());
            }
            // featureChange should never be null
            return Optional.of(featureChange);
        }
        return Optional.empty();
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
        final boolean differentEdgeSetFirstTry = differentEdgeSetLinearCheck(beforeEdges, afterEdges);
        final boolean differentEdgeDeeperLook = differentEdgeSetQuadraticCheck(beforeEdges, afterEdges,
                useGeometryMatching);
        return differentEdgeSetFirstTry && differentEdgeDeeperLook;
    }

    private static boolean differentEdgeSetLinearCheck(final SortedSet<Edge> beforeEdges,
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

    private static boolean differentEdgeSetQuadraticCheck(final Set<Edge> beforeEdges,
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

    private AtlasDiffHelper()
    {

    }
}
