package org.openstreetmap.atlas.geography.atlas.validators;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.change.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validate that a {@link FeatureChange} actually introduces changes. This will throw an exception
 * if the afterView is identical to the beforeView (which is computed from a provided atlas
 * context).
 *
 * @author lcram
 */
public class FeatureChangeUsefulnessValidator
{
    private static final Logger logger = LoggerFactory
            .getLogger(FeatureChangeUsefulnessValidator.class);

    private final FeatureChange featureChange;

    public FeatureChangeUsefulnessValidator(final FeatureChange featureChange)
    {
        this.featureChange = featureChange;
    }

    public void validate()
    {
        validateFeatureChange();
    }

    private void validateFeatureChange()
    {
        final ChangeType changeType = this.featureChange.getChangeType();
        final AtlasEntity beforeView = this.featureChange.getBeforeView();
        final AtlasEntity afterView = this.featureChange.getAfterView();

        /*
         * No need to validate a REMOVE. This will always be useful.
         */
        if (changeType == ChangeType.REMOVE)
        {
            return;
        }

        /*
         * beforeView will be null in the case of an ADD that is creating a brand new feature. In
         * that case, there is no need to validate since a brand new ADD will always be useful.
         */
        if (beforeView == null)
        {
            return;
        }

        if (beforeView.getType() != afterView.getType())
        {
            throw new CoreException("beforeView type {} did not match afterView type {} in {}",
                    beforeView, afterView, this.featureChange.toString());
        }

        if (beforeView.getTags() != null && afterView.getTags() != null
                && !beforeView.getTags().equals(afterView.getTags()))
        {
            return;
        }

        if (beforeView.relations() != null && afterView.relations() != null
                && !beforeView.relations().equals(afterView.relations()))
        {
            return;
        }

        switch (afterView.getType())
        {
            case AREA:
                final Area beforeArea = (Area) beforeView;
                final Area afterArea = (Area) afterView;
                if (beforeArea.asPolygon() != null && afterArea.asPolygon() != null
                        && !beforeArea.asPolygon().equals(afterArea.asPolygon()))
                {
                    return;
                }
                break;
            case EDGE:
                final Edge beforeEdge = (Edge) beforeView;
                final Edge afterEdge = (Edge) afterView;
                if (beforeEdge.asPolyLine() != null && afterEdge.asPolyLine() != null
                        && !beforeEdge.asPolyLine().equals(afterEdge.asPolyLine()))
                {
                    return;
                }
                if (beforeEdge.start() != null && afterEdge.start() != null
                        && !beforeEdge.start().equals(afterEdge.start()))
                {
                    return;
                }
                if (beforeEdge.end() != null && afterEdge.end() != null
                        && !beforeEdge.end().equals(afterEdge.end()))
                {
                    return;
                }
                break;
            case LINE:
                final Line beforeLine = (Line) beforeView;
                final Line afterLine = (Line) afterView;
                if (beforeLine.asPolyLine() != null && afterLine.asPolyLine() != null
                        && !beforeLine.asPolyLine().equals(afterLine.asPolyLine()))
                {
                    return;
                }
                break;
            case NODE:
                final Node beforeNode = (Node) beforeView;
                final Node afterNode = (Node) afterView;
                if (beforeNode.getLocation() != null && afterNode.getLocation() != null
                        && !beforeNode.getLocation().equals(afterNode.getLocation()))
                {
                    return;
                }
                if (beforeNode.inEdges() != null && afterNode.inEdges() != null
                        && !beforeNode.inEdges().equals(afterNode.inEdges()))
                {
                    return;
                }
                if (beforeNode.outEdges() != null && afterNode.outEdges() != null
                        && !beforeNode.outEdges().equals(afterNode.outEdges()))
                {
                    return;
                }
                break;
            case POINT:
                final Point beforePoint = (Point) beforeView;
                final Point afterPoint = (Point) afterView;
                if (beforePoint.getLocation() != null && afterPoint.getLocation() != null
                        && !beforePoint.getLocation().equals(afterPoint.getLocation()))
                {
                    return;
                }
                break;
            case RELATION:
                final Relation beforeRelation = (Relation) beforeView;
                final Relation afterRelation = (Relation) afterView;
                if (beforeRelation.members() != null && afterRelation.members() != null
                        && !beforeRelation.members().equals(afterRelation.members()))
                {
                    return;
                }
                if (beforeRelation.allRelationsWithSameOsmIdentifier() != null
                        && afterRelation.allRelationsWithSameOsmIdentifier() != null
                        && !beforeRelation.allRelationsWithSameOsmIdentifier()
                                .equals(afterRelation.allRelationsWithSameOsmIdentifier()))
                {
                    return;
                }
                if (beforeRelation.allKnownOsmMembers() != null
                        && afterRelation.allKnownOsmMembers() != null && !beforeRelation
                                .allKnownOsmMembers().equals(afterRelation.allKnownOsmMembers()))
                {
                    return;
                }
                if (beforeRelation.osmRelationIdentifier() != null
                        && afterRelation.osmRelationIdentifier() != null
                        && !beforeRelation.osmRelationIdentifier()
                                .equals(afterRelation.osmRelationIdentifier()))
                {
                    return;
                }
                break;
            default:
                throw new CoreException("Unknown ItemType {}", afterView.getType());
        }

        /*
         * If we made it all the way here, then we know this FeatureChange is not useful.
         */
        throw new CoreException(
                "FeatureChange is not useful: beforeView perfectly matched afterView: {} vs {}",
                beforeView, afterView);
    }
}
