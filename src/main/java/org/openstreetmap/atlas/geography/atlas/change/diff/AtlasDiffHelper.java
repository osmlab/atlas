package org.openstreetmap.atlas.geography.atlas.change.diff;

import java.util.Iterator;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
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
            final Area afterArea)
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
                return Optional.of(new FeatureChange(ChangeType.ADD, bloatedArea));
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
            final Edge afterEdge, final boolean saveAllGeometries)
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
                /*
                 * Explicitly check for saveAllGeometry. This will resave if the featureChange was
                 * due to a geometry change, but that is OK. We want to ensure we save in the cases
                 * where a start/end node was changed (for visualization purposes).
                 */
                if (saveAllGeometries)
                {
                    bloatedEdge.withPolyLine(afterEdge.asPolyLine());
                }
                return Optional.of(new FeatureChange(ChangeType.ADD, bloatedEdge));
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
            final Line afterLine)
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
                return Optional.of(new FeatureChange(ChangeType.ADD, bloatedLine));
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
            final Node afterNode, final boolean saveAllGeometries)
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
            if (differentEdgeSet(beforeNode.inEdges(), afterNode.inEdges()))
            {
                bloatedNode.withInEdgeIdentifiers(new TreeSet<>(afterNode.inEdges().stream()
                        .map(Edge::getIdentifier).collect(Collectors.toSet())));
                if (saveAllGeometries)
                {
                    bloatedNode.withLocation(afterNode.getLocation());
                }
                featureChangeWouldBeUseful = true;
            }
            if (differentEdgeSet(beforeNode.outEdges(), afterNode.outEdges()))
            {
                bloatedNode.withOutEdgeIdentifiers(new TreeSet<>(afterNode.outEdges().stream()
                        .map(Edge::getIdentifier).collect(Collectors.toSet())));
                if (saveAllGeometries)
                {
                    bloatedNode.withLocation(afterNode.getLocation());
                }
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
            final AtlasEntity beforeEntity, final AtlasEntity afterEntity,
            final boolean saveAllGeometries)
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
             * Now, check to see if the afterEntity was different from the beforeEntity in any
             * relations in the afterAtlas. If they were not different, there is no feature change.
             */
            if (!entitiesWereDifferentInRelations(beforeEntity, afterEntity))
            {
                return Optional.empty();
            }

            /*
             * OK! We made it here because we have confirmed that the entities have some
             * relation-related difference. We create a feature change to reflect this.
             */
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
        catch (final Exception exception)
        {
            throw new CoreException("Unable to compare relations for {} and {}", beforeEntity,
                    afterEntity, exception);
        }
    }

    public static Optional<FeatureChange> getPointChangeIfNecessary(final Point beforePoint,
            final Point afterPoint)
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
                return Optional.of(new FeatureChange(ChangeType.ADD, bloatedPoint));
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
            final Relation beforeRelation, final Relation afterRelation)
    {
        try
        {
            boolean featureChangeWouldBeUseful = false;
            final BloatedRelation bloatedRelation = BloatedRelation.shallowFrom(afterRelation);
            final RelationMemberList beforeMembers = beforeRelation.members();
            final RelationMemberList afterMembers = afterRelation.members();
            if (!afterMembers.equals(beforeMembers))
            {
                bloatedRelation.withMembers(Optional.of(beforeRelation), afterRelation.members());
                featureChangeWouldBeUseful = true;
            }
            if (featureChangeWouldBeUseful)
            {
                return Optional.of(new FeatureChange(ChangeType.ADD, bloatedRelation));
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
            final AtlasEntity afterEntity, final boolean saveAllGeometries)
    {
        if (beforeEntity.getTags().equals(afterEntity.getTags()))
        {
            return Optional.empty();
        }

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
                final BloatedRelation relation = BloatedRelation.shallowFrom((Relation) afterEntity)
                        .withTags(afterEntity.getTags());
                bloatedEntity = relation;
                break;
            default:
                throw new CoreException("Unknown item type {}", afterEntity.getType());
        }
        return Optional.of(new FeatureChange(ChangeType.ADD, bloatedEntity));
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
            final Atlas atlas, final AtlasEntity entity)
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

    private static boolean entitiesWereDifferentInRelations(final AtlasEntity beforeEntity,
            final AtlasEntity afterEntity)
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

    private AtlasDiffHelper()
    {

    }
}
