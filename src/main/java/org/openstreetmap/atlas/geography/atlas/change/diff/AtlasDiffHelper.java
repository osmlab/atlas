package org.openstreetmap.atlas.geography.atlas.change.diff;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.change.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.RelationMemberList;
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

    public static Optional<FeatureChange> getAreaChangeIfNecessary(final Area beforeArea,
            final Area afterArea, final Atlas beforeViewAtlas)
    {
        try
        {
            boolean featureChangeWouldBeUseful = false;
            final CompleteArea completeArea = CompleteArea.shallowFrom(afterArea);
            if (!beforeArea.asPolygon().equals(afterArea.asPolygon()))
            {
                completeArea.withPolygon(afterArea.asPolygon());
                featureChangeWouldBeUseful = true;
            }
            if (featureChangeWouldBeUseful)
            {
                return Optional.of(FeatureChange.add(completeArea, beforeViewAtlas));
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
            final Edge afterEdge, final Atlas beforeViewAtlas, final boolean saveAllGeometries)
    {
        try
        {
            boolean featureChangeWouldBeUseful = false;
            final CompleteEdge completeEdge = CompleteEdge.shallowFrom(afterEdge);
            if (!beforeEdge.asPolyLine().equals(afterEdge.asPolyLine()))
            {
                completeEdge.withPolyLine(afterEdge.asPolyLine());
                featureChangeWouldBeUseful = true;
            }
            if (beforeEdge.start().getIdentifier() != afterEdge.start().getIdentifier())
            {
                completeEdge.withStartNodeIdentifier(afterEdge.start().getIdentifier());
                featureChangeWouldBeUseful = true;
            }
            if (beforeEdge.end().getIdentifier() != afterEdge.end().getIdentifier())
            {
                completeEdge.withEndNodeIdentifier(afterEdge.end().getIdentifier());
                featureChangeWouldBeUseful = true;
            }
            if (featureChangeWouldBeUseful)
            {
                /*
                 * Explicitly check for saveAllGeometry. This will resave if the featureChange was
                 * due to a geometry change, but that is OK. We want to ensure we save in the cases
                 * where a start/end node was changed (for visualization purposes).
                 */
                if (saveAllGeometries)
                {
                    completeEdge.withPolyLine(afterEdge.asPolyLine());
                }
                return Optional.of(FeatureChange.add(completeEdge, beforeViewAtlas));
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
            final Line afterLine, final Atlas beforeViewAtlas)
    {
        try
        {
            boolean featureChangeWouldBeUseful = false;
            final CompleteLine completeLine = CompleteLine.shallowFrom(afterLine);
            if (!beforeLine.asPolyLine().equals(afterLine.asPolyLine()))
            {
                completeLine.withPolyLine(afterLine.asPolyLine());
                featureChangeWouldBeUseful = true;
            }
            if (featureChangeWouldBeUseful)
            {
                return Optional.of(FeatureChange.add(completeLine, beforeViewAtlas));
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
            final Node afterNode, final Atlas beforeViewAtlas, final boolean saveAllGeometries)
    {
        try
        {
            boolean featureChangeWouldBeUseful = false;
            final CompleteNode completeNode = CompleteNode.shallowFrom(afterNode);
            if (!beforeNode.getLocation().equals(afterNode.getLocation()))
            {
                completeNode.withLocation(afterNode.getLocation());
                featureChangeWouldBeUseful = true;
            }
            if (differentEdgeSet(beforeNode.inEdges(), afterNode.inEdges()))
            {
                completeNode.withInEdgeIdentifiersAndSource(new TreeSet<>(afterNode.inEdges()
                        .stream().map(Edge::getIdentifier).collect(Collectors.toSet())),
                        beforeNode);
                if (saveAllGeometries)
                {
                    completeNode.withLocation(afterNode.getLocation());
                }
                featureChangeWouldBeUseful = true;
            }
            if (differentEdgeSet(beforeNode.outEdges(), afterNode.outEdges()))
            {
                completeNode.withOutEdgeIdentifiersAndSource(new TreeSet<>(afterNode.outEdges()
                        .stream().map(Edge::getIdentifier).collect(Collectors.toSet())),
                        beforeNode);
                if (saveAllGeometries)
                {
                    completeNode.withLocation(afterNode.getLocation());
                }
                featureChangeWouldBeUseful = true;
            }
            if (featureChangeWouldBeUseful)
            {
                return Optional.of(FeatureChange.add(completeNode, beforeViewAtlas));
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
            final AtlasEntity beforeEntity, final AtlasEntity afterEntity,
            final Atlas beforeViewAtlas, final boolean saveAllGeometries)
    {
        try
        {
            final Set<Long> beforeRelationIdentifiers = beforeEntity.relations().stream()
                    .map(Relation::getIdentifier).collect(Collectors.toSet());
            final Set<Long> afterRelationIdentifiers = afterEntity.relations().stream()
                    .map(Relation::getIdentifier).collect(Collectors.toSet());

            /*
             * We never had any parent relations. We want to explicitly return empty so as not to
             * generate a misleading FeatureChange changing a null into an empty set.
             */
            if (beforeRelationIdentifiers.isEmpty() && afterRelationIdentifiers.isEmpty())
            {
                return Optional.empty();
            }

            /*
             * If the relation identifier sets are equivalent, then there was no parent relation
             * membership change.
             */
            if (beforeRelationIdentifiers.equals(afterRelationIdentifiers))
            {
                return Optional.empty();
            }

            /*
             * OK! We made it here because we have confirmed that the entities have differing
             * declared parent relation identifiers. We create a feature change to reflect this.
             */
            final AtlasEntity completeEntity;
            switch (afterEntity.getType())
            {
                case AREA:
                    CompleteArea area = CompleteArea.shallowFrom((Area) afterEntity)
                            .withRelationIdentifiers(afterRelationIdentifiers);
                    if (saveAllGeometries)
                    {
                        area = area.withPolygon(((Area) afterEntity).asPolygon());
                    }
                    completeEntity = area;
                    break;
                case EDGE:
                    CompleteEdge edge = CompleteEdge.shallowFrom((Edge) afterEntity)
                            .withRelationIdentifiers(afterRelationIdentifiers);
                    if (saveAllGeometries)
                    {
                        edge = edge.withPolyLine(((Edge) afterEntity).asPolyLine());
                    }
                    completeEntity = edge;
                    break;
                case LINE:
                    CompleteLine line = CompleteLine.shallowFrom((Line) afterEntity)
                            .withRelationIdentifiers(afterRelationIdentifiers);
                    if (saveAllGeometries)
                    {
                        line = line.withPolyLine(((Line) afterEntity).asPolyLine());
                    }
                    completeEntity = line;
                    break;
                case NODE:
                    CompleteNode node = CompleteNode.shallowFrom((Node) afterEntity)
                            .withRelationIdentifiers(afterRelationIdentifiers);
                    if (saveAllGeometries)
                    {
                        node = node.withLocation(((Node) afterEntity).getLocation());
                    }
                    completeEntity = node;
                    break;
                case POINT:
                    CompletePoint point = CompletePoint.shallowFrom((Point) afterEntity)
                            .withRelationIdentifiers(afterRelationIdentifiers);
                    if (saveAllGeometries)
                    {
                        point = point.withLocation(((Point) afterEntity).getLocation());
                    }
                    completeEntity = point;
                    break;
                case RELATION:
                    final CompleteRelation relation = CompleteRelation
                            .shallowFrom((Relation) afterEntity)
                            .withRelationIdentifiers(afterRelationIdentifiers);
                    completeEntity = relation;
                    break;
                default:
                    throw new CoreException("Unknown item type {}", afterEntity.getType());
            }
            // featureChange should never be null
            return Optional.of(FeatureChange.add(completeEntity, beforeViewAtlas));
        }
        catch (final Exception exception)
        {
            throw new CoreException("Unable to compare relations for {} and {}", beforeEntity,
                    afterEntity, exception);
        }
    }

    public static Optional<FeatureChange> getPointChangeIfNecessary(final Point beforePoint,
            final Point afterPoint, final Atlas beforeViewAtlas)
    {
        try
        {
            boolean featureChangeWouldBeUseful = false;
            final CompletePoint completePoint = CompletePoint.shallowFrom(afterPoint);
            if (!beforePoint.getLocation().equals(afterPoint.getLocation()))
            {
                completePoint.withLocation(afterPoint.getLocation());
                featureChangeWouldBeUseful = true;
            }
            if (featureChangeWouldBeUseful)
            {
                return Optional.of(FeatureChange.add(completePoint, beforeViewAtlas));
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
            final Relation beforeRelation, final Relation afterRelation,
            final Atlas beforeViewAtlas)
    {
        try
        {
            boolean featureChangeWouldBeUseful = false;
            final CompleteRelation completeRelation = CompleteRelation.shallowFrom(afterRelation);
            final RelationMemberList beforeMembers = beforeRelation.members();
            final RelationMemberList afterMembers = afterRelation.members();
            if (!afterMembers.equals(beforeMembers))
            {
                completeRelation.withMembersAndSource(afterRelation.members(), beforeRelation);
                featureChangeWouldBeUseful = true;
            }
            if (featureChangeWouldBeUseful)
            {
                return Optional.of(FeatureChange.add(completeRelation, beforeViewAtlas));
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
            final AtlasEntity afterEntity, final Atlas beforeViewAtlas,
            final boolean saveAllGeometries)
    {
        if (beforeEntity.getTags().equals(afterEntity.getTags()))
        {
            return Optional.empty();
        }

        final AtlasEntity completeEntity;
        switch (afterEntity.getType())
        {
            case AREA:
                CompleteArea area = CompleteArea.shallowFrom((Area) afterEntity)
                        .withTags(afterEntity.getTags());
                if (saveAllGeometries)
                {
                    area = area.withPolygon(((Area) afterEntity).asPolygon());
                }
                completeEntity = area;
                break;
            case EDGE:
                CompleteEdge edge = CompleteEdge.shallowFrom((Edge) afterEntity)
                        .withTags(afterEntity.getTags());
                if (saveAllGeometries)
                {
                    edge = edge.withPolyLine(((Edge) afterEntity).asPolyLine());
                }
                completeEntity = edge;
                break;
            case LINE:
                CompleteLine line = CompleteLine.shallowFrom((Line) afterEntity)
                        .withTags(afterEntity.getTags());
                if (saveAllGeometries)
                {
                    line = line.withPolyLine(((Line) afterEntity).asPolyLine());
                }
                completeEntity = line;
                break;
            case NODE:
                CompleteNode node = CompleteNode.shallowFrom((Node) afterEntity)
                        .withTags(afterEntity.getTags());
                if (saveAllGeometries)
                {
                    node = node.withLocation(((Node) afterEntity).getLocation());
                }
                completeEntity = node;
                break;
            case POINT:
                CompletePoint point = CompletePoint.shallowFrom((Point) afterEntity)
                        .withTags(afterEntity.getTags());
                if (saveAllGeometries)
                {
                    point = point.withLocation(((Point) afterEntity).getLocation());
                }
                completeEntity = point;
                break;
            case RELATION:
                final CompleteRelation relation = CompleteRelation
                        .shallowFrom((Relation) afterEntity).withTags(afterEntity.getTags());
                completeEntity = relation;
                break;
            default:
                throw new CoreException("Unknown item type {}", afterEntity.getType());
        }
        return Optional.of(FeatureChange.add(completeEntity, beforeViewAtlas));
    }

    public static FeatureChange simpleCompleteAreaChange(final ChangeType changeType,
            final Atlas atlasContainingTheEntity, final Atlas beforeViewAtlas,
            final AtlasEntity entity, final boolean saveAllGeometries)
    {
        final Long entityIdentifier = entity.getIdentifier();
        if (changeType == ChangeType.REMOVE)
        {
            CompleteArea completeArea = CompleteArea
                    .shallowFrom(atlasContainingTheEntity.area(entityIdentifier));
            if (saveAllGeometries)
            {
                completeArea = completeArea.withPolygon(((Area) entity).asPolygon());
            }
            return FeatureChange.remove(completeArea, beforeViewAtlas);
        }
        else
        {
            return FeatureChange.add(
                    CompleteArea.from(atlasContainingTheEntity.area(entityIdentifier)),
                    beforeViewAtlas);
        }
    }

    public static FeatureChange simpleCompleteEdgeChange(final ChangeType changeType,
            final Atlas atlasContainingTheEntity, final Atlas beforeViewAtlas,
            final AtlasEntity entity, final boolean saveAllGeometries)
    {
        final Long entityIdentifier = entity.getIdentifier();
        if (changeType == ChangeType.REMOVE)
        {
            CompleteEdge completeEdge = CompleteEdge
                    .shallowFrom(atlasContainingTheEntity.edge(entityIdentifier));
            if (saveAllGeometries)
            {
                completeEdge = completeEdge.withPolyLine(((Edge) entity).asPolyLine());
            }
            return FeatureChange.remove(completeEdge, beforeViewAtlas);
        }
        else
        {
            return FeatureChange.add(
                    CompleteEdge.from(atlasContainingTheEntity.edge(entityIdentifier)),
                    beforeViewAtlas);
        }
    }

    public static FeatureChange simpleCompleteLineChange(final ChangeType changeType,
            final Atlas atlasContainingTheEntity, final Atlas beforeViewAtlas,
            final AtlasEntity entity, final boolean saveAllGeometries)
    {
        final Long entityIdentifier = entity.getIdentifier();
        if (changeType == ChangeType.REMOVE)
        {
            CompleteLine completeLine = CompleteLine
                    .shallowFrom(atlasContainingTheEntity.line(entityIdentifier));
            if (saveAllGeometries)
            {
                completeLine = completeLine.withPolyLine(((Line) entity).asPolyLine());
            }
            return FeatureChange.remove(completeLine, beforeViewAtlas);
        }
        else
        {
            return FeatureChange.add(
                    CompleteLine.from(atlasContainingTheEntity.line(entityIdentifier)),
                    beforeViewAtlas);
        }
    }

    public static FeatureChange simpleCompleteNodeChange(final ChangeType changeType,
            final Atlas atlasContainingTheEntity, final Atlas beforeViewAtlas,
            final AtlasEntity entity, final boolean saveAllGeometries)
    {
        final Long entityIdentifier = entity.getIdentifier();
        if (changeType == ChangeType.REMOVE)
        {
            CompleteNode completeNode = CompleteNode
                    .shallowFrom(atlasContainingTheEntity.node(entityIdentifier));
            if (saveAllGeometries)
            {
                completeNode = completeNode.withLocation(((Node) entity).getLocation());
            }
            return FeatureChange.remove(completeNode, beforeViewAtlas);
        }
        else
        {
            return FeatureChange.add(
                    CompleteNode.from(atlasContainingTheEntity.node(entityIdentifier)),
                    beforeViewAtlas);
        }
    }

    public static FeatureChange simpleCompletePointChange(final ChangeType changeType,
            final Atlas atlasContainingTheEntity, final Atlas beforeViewAtlas,
            final AtlasEntity entity, final boolean saveAllGeometries)
    {
        final Long entityIdentifier = entity.getIdentifier();
        if (changeType == ChangeType.REMOVE)
        {
            CompletePoint completePoint = CompletePoint
                    .shallowFrom(atlasContainingTheEntity.point(entityIdentifier));
            if (saveAllGeometries)
            {
                completePoint = completePoint.withLocation(((Point) entity).getLocation());
            }
            return FeatureChange.remove(completePoint, beforeViewAtlas);
        }
        else
        {
            return FeatureChange.add(
                    CompletePoint.from(atlasContainingTheEntity.point(entityIdentifier)),
                    beforeViewAtlas);
        }
    }

    public static FeatureChange simpleCompleteRelationChange(final ChangeType changeType,
            final Atlas atlasContainingTheEntity, final Atlas beforeViewAtlas,
            final AtlasEntity entity)
    {
        final Long entityIdentifier = entity.getIdentifier();
        if (changeType == ChangeType.REMOVE)
        {
            final CompleteRelation completeRelation = CompleteRelation
                    .shallowFrom(atlasContainingTheEntity.relation(entityIdentifier));
            return FeatureChange.remove(completeRelation, beforeViewAtlas);
        }
        else
        {
            return FeatureChange.add(
                    CompleteRelation.from(atlasContainingTheEntity.relation(entityIdentifier)),
                    beforeViewAtlas);
        }
    }

    /*
     * NOTE: this method considers two edges to be distinct if and only if they have distinct
     * identifiers.
     */
    private static boolean differentEdgeSet(final SortedSet<Edge> beforeEdges,
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

    private AtlasDiffHelper()
    {

    }
}
