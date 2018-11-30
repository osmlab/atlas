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
            final AtlasEntity beforeEntity, final AtlasEntity afterEntity, final Atlas beforeAtlas,
            final Atlas afterAtlas)
    {
        try
        {
            final Set<Long> beforeRelationIdentifiers = beforeEntity.relations().stream()
                    .map(relation -> relation.getIdentifier()).collect(Collectors.toSet());
            final Set<Long> afterRelationIdentifiers = afterEntity.relations().stream()
                    .map(relation -> relation.getIdentifier()).collect(Collectors.toSet());

            /*
             * In this case, we only care if the relation identifier sets were different. If the
             * features had their roles in the relations altered, this will be caught when we
             * actually diff the relations.
             */
            if (!beforeRelationIdentifiers.equals(afterRelationIdentifiers))
            {
                return Optional.empty();
            }

            /*
             * TODO Now that we confirmed the relation sets are different, we need to make sure this
             * is not because a relation was added to or removed from the after atlas. In that case,
             * the relation's ADD/REMOVE diff will take care of this feature's relation member set
             * for us. TODO This actually may not be necessary. We can just save redundant diffs, it
             * won't negatively impact anything. Plus, it's helpful for visualization.
             */

            /*
             * OK! We made it here because we have finally confirmed that the diff is due to a
             * simple update of a relation member list, where the relation was not
             * added/removed/shallow-pruned.
             */
            FeatureChange featureChange;
            switch (afterEntity.getType())
            {
                case AREA:
                    featureChange = new FeatureChange(ChangeType.ADD,
                            BloatedArea.shallowFrom((Area) afterEntity)
                                    .withRelationIdentifiers(afterRelationIdentifiers));
                    break;
                case EDGE:
                    featureChange = new FeatureChange(ChangeType.ADD,
                            BloatedEdge.shallowFrom((Edge) afterEntity)
                                    .withRelationIdentifiers(afterRelationIdentifiers));
                    break;
                case LINE:
                    featureChange = new FeatureChange(ChangeType.ADD,
                            BloatedLine.shallowFrom((Line) afterEntity)
                                    .withRelationIdentifiers(afterRelationIdentifiers));
                    break;
                case NODE:
                    featureChange = new FeatureChange(ChangeType.ADD,
                            BloatedNode.shallowFrom((Node) afterEntity)
                                    .withRelationIdentifiers(afterRelationIdentifiers));
                    break;
                case POINT:
                    featureChange = new FeatureChange(ChangeType.ADD,
                            BloatedPoint.shallowFrom((Point) afterEntity)
                                    .withRelationIdentifiers(afterRelationIdentifiers));
                    break;
                case RELATION:
                    featureChange = new FeatureChange(ChangeType.ADD,
                            BloatedRelation.shallowFrom((Relation) afterEntity)
                                    .withRelationIdentifiers(afterRelationIdentifiers));
                    break;
                default:
                    throw new CoreException("Unknown item type {}", afterEntity.getType());
            }
            // featureChange should never be null
            return Optional.of(featureChange);
        }
        catch (final Exception exception)
        {
            throw new CoreException("Unable to compare relations for {} and {}", beforeEntity,
                    afterEntity, exception);
        }
    }

    public static Optional<FeatureChange> getTagChangeIfNecessary(final AtlasEntity beforeEntity,
            final AtlasEntity afterEntity)
    {
        if (!beforeEntity.getTags().equals(afterEntity.getTags()))
        {
            FeatureChange featureChange;
            switch (afterEntity.getType())
            {
                case AREA:
                    featureChange = new FeatureChange(ChangeType.ADD, BloatedArea
                            .shallowFrom((Area) afterEntity).withTags(afterEntity.getTags()));
                    break;
                case EDGE:
                    featureChange = new FeatureChange(ChangeType.ADD, BloatedEdge
                            .shallowFrom((Edge) afterEntity).withTags(afterEntity.getTags()));
                    break;
                case LINE:
                    featureChange = new FeatureChange(ChangeType.ADD, BloatedLine
                            .shallowFrom((Line) afterEntity).withTags(afterEntity.getTags()));
                    break;
                case NODE:
                    featureChange = new FeatureChange(ChangeType.ADD, BloatedNode
                            .shallowFrom((Node) afterEntity).withTags(afterEntity.getTags()));
                    break;
                case POINT:
                    featureChange = new FeatureChange(ChangeType.ADD, BloatedPoint
                            .shallowFrom((Point) afterEntity).withTags(afterEntity.getTags()));
                    break;
                case RELATION:
                    featureChange = new FeatureChange(ChangeType.ADD, BloatedRelation
                            .shallowFrom((Relation) afterEntity).withTags(afterEntity.getTags()));
                    break;
                default:
                    throw new CoreException("Unknown item type {}", afterEntity.getType());
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

    private AtlasDiffHelper()
    {

    }
}
