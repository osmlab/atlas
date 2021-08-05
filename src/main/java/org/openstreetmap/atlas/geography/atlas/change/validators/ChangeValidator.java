package org.openstreetmap.atlas.geography.atlas.change.validators;

import java.util.Optional;
import java.util.function.BiPredicate;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.change.Change;
import org.openstreetmap.atlas.geography.atlas.change.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.change.exception.EmptyChangeException;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolyLineConverter;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validate a {@link Change}
 *
 * @author matthieun
 */
public class ChangeValidator
{
    private static final Logger logger = LoggerFactory.getLogger(ChangeValidator.class);

    private final Change change;

    public ChangeValidator(final Change change)
    {
        this.change = change;
    }

    public void validate()
    {
        logger.trace("Starting validation of Change {}", this.change.getName());
        final Time start = Time.now();
        validateChangeNotEmpty();
        validateReverseEdgesHaveForwardMatchingCounterpart();
        validateGeometricRelationsUpdated();
        logger.trace("Finished validation of Change {} in {}", this.change.getName(),
                start.elapsedSince());
    }

    protected void validateChangeNotEmpty()
    {
        if (this.change.changeCount() == 0)
        {
            throw new EmptyChangeException();
        }
    }

    protected void validateGeometricRelationsUpdated()
    {
        this.change.changesFor(ItemType.EDGE)
                .filter(featureChange -> featureChange.getChangeType() != ChangeType.REMOVE)
                .filter(featureChange -> ((Edge) featureChange.getAfterView()).asPolyLine() != null)
                .filter(featureChange -> ((CompleteEdge) featureChange.getAfterView())
                        .geometricRelationIdentifiers() != null
                        && !((CompleteEdge) featureChange.getAfterView())
                                .geometricRelationIdentifiers().isEmpty())
                .filter(featureChange -> featureChange.getBeforeView() == null
                        || ((Edge) featureChange.getBeforeView()).asPolyLine() == null
                        || !((Edge) featureChange.getBeforeView()).asPolyLine()
                                .equals(((CompleteEdge) featureChange.getAfterView()).asPolyLine()))
                .forEach(featureChange ->
                {
                    final CompleteEdge after = (CompleteEdge) featureChange.getAfterView();
                    after.geometricRelationIdentifiers().forEach(relationId ->
                    {
                        final Optional<FeatureChange> changeRelationOptional = this.change
                                .changeFor(ItemType.RELATION, relationId);
                        if (changeRelationOptional.isEmpty())
                        {
                            throw new CoreException(
                                    "Geometric relation {} had no change for edge member {} with updated geometry!",
                                    relationId, after.getIdentifier());
                        }
                        if (!changeRelationOptional.get().getChangeType().equals(ChangeType.REMOVE))
                        {
                            final CompleteRelation updatedRelation = (CompleteRelation) changeRelationOptional
                                    .get().getAfterView();
                            if (!updatedRelation.isOverrideGeometry())
                            {
                                if (updatedRelation.getRemovedGeometry().isEmpty()
                                        && updatedRelation.getAddedGeometry().isEmpty())
                                {
                                    throw new CoreException(
                                            "Geometric relation {} had no change for edge member {} with updated geometry!",
                                            relationId, after.getIdentifier());
                                }
                                if (featureChange.getBeforeView() != null
                                        && ((Edge) featureChange.getBeforeView())
                                                .asPolyLine() != null
                                        && !updatedRelation.getRemovedGeometry()
                                                .contains(new JtsPolyLineConverter().convert(
                                                        ((Edge) featureChange.getBeforeView())
                                                                .asPolyLine())))
                                {
                                    throw new CoreException(
                                            "Geometric relation {} had no removed geometry for edge member {} with updated geometry!",
                                            relationId, after.getIdentifier());
                                }
                                if (!updatedRelation.getAddedGeometry().contains(
                                        new JtsPolyLineConverter().convert(after.asPolyLine())))
                                {
                                    throw new CoreException(
                                            "Geometric relation {} had no added geometry for edge member {} with updated geometry!",
                                            relationId, after.getIdentifier());
                                }
                            }
                        }
                    });
                });

        this.change.changesFor(ItemType.LINE)
                .filter(featureChange -> featureChange.getChangeType() != ChangeType.REMOVE)
                .filter(featureChange -> ((Line) featureChange.getAfterView()).asPolyLine() != null)
                .filter(featureChange -> ((CompleteLine) featureChange.getAfterView())
                        .geometricRelationIdentifiers() != null
                        && !((CompleteLine) featureChange.getAfterView())
                                .geometricRelationIdentifiers().isEmpty())
                .filter(featureChange -> featureChange.getBeforeView() == null
                        || ((Line) featureChange.getBeforeView()).asPolyLine() == null
                        || !((Line) featureChange.getBeforeView()).asPolyLine()
                                .equals(((CompleteLine) featureChange.getAfterView()).asPolyLine()))
                .forEach(featureChange ->
                {
                    final CompleteLine after = (CompleteLine) featureChange.getAfterView();
                    after.geometricRelationIdentifiers().forEach(relationId ->
                    {
                        final Optional<FeatureChange> changeRelationOptional = this.change
                                .changeFor(ItemType.RELATION, relationId);
                        if (changeRelationOptional.isEmpty())
                        {
                            throw new CoreException(
                                    "Geometric relation {} had no change for Line member {} with updated geometry!",
                                    relationId, after.getIdentifier());
                        }
                        if (!changeRelationOptional.get().getChangeType().equals(ChangeType.REMOVE))
                        {
                            final CompleteRelation updatedRelation = (CompleteRelation) changeRelationOptional
                                    .get().getAfterView();
                            if (!updatedRelation.isOverrideGeometry())
                            {
                                if (updatedRelation.getRemovedGeometry().isEmpty()
                                        && updatedRelation.getAddedGeometry().isEmpty())
                                {
                                    throw new CoreException(
                                            "Geometric relation {} had no change for Line member {} with updated geometry!",
                                            relationId, after.getIdentifier());
                                }
                                if (featureChange.getBeforeView() != null
                                        && ((Line) featureChange.getBeforeView())
                                                .asPolyLine() != null
                                        && !updatedRelation.getRemovedGeometry()
                                                .contains(new JtsPolyLineConverter().convert(
                                                        ((Line) featureChange.getBeforeView())
                                                                .asPolyLine())))
                                {
                                    throw new CoreException(
                                            "Geometric relation {} had no removed geometry for Line member {} with updated geometry!",
                                            relationId, after.getIdentifier());
                                }
                                if (!updatedRelation.getAddedGeometry().contains(
                                        new JtsPolyLineConverter().convert(after.asPolyLine())))
                                {
                                    throw new CoreException(
                                            "Geometric relation {} had no added geometry for Line member {} with updated geometry!",
                                            relationId, after.getIdentifier());
                                }
                            }
                        }
                    });
                });

        this.change.changesFor(ItemType.AREA)
                .filter(featureChange -> featureChange.getChangeType() != ChangeType.REMOVE)
                .filter(featureChange -> ((CompleteArea) featureChange.getAfterView())
                        .asPolygon() != null)
                .filter(featureChange -> ((CompleteArea) featureChange.getAfterView())
                        .geometricRelationIdentifiers() != null
                        && !((CompleteArea) featureChange.getAfterView())
                                .geometricRelationIdentifiers().isEmpty())
                .filter(featureChange -> featureChange.getBeforeView() == null
                        || ((Area) featureChange.getBeforeView()).asPolygon() == null
                        || !((Area) featureChange.getBeforeView()).asPolygon()
                                .equals(((CompleteArea) featureChange.getAfterView()).asPolygon()))
                .forEach(featureChange ->
                {
                    final CompleteArea after = (CompleteArea) featureChange.getAfterView();
                    after.geometricRelationIdentifiers().forEach(relationId ->
                    {
                        final Optional<FeatureChange> changeRelationOptional = this.change
                                .changeFor(ItemType.RELATION, relationId);
                        if (changeRelationOptional.isEmpty())
                        {
                            throw new CoreException(
                                    "Geometric relation {} had no change for area member {} with updated geometry!",
                                    relationId, after.getIdentifier());
                        }
                        if (!changeRelationOptional.get().getChangeType().equals(ChangeType.REMOVE))
                        {
                            final CompleteRelation updatedRelation = (CompleteRelation) changeRelationOptional
                                    .get().getAfterView();
                            if (!updatedRelation.isOverrideGeometry())
                            {
                                if (updatedRelation.getRemovedGeometry().isEmpty()
                                        && updatedRelation.getAddedGeometry().isEmpty())
                                {
                                    throw new CoreException(
                                            "Geometric relation {} had no change for area member {} with updated geometry!",
                                            relationId, after.getIdentifier());
                                }
                                if (featureChange.getBeforeView() != null
                                        && ((Area) featureChange.getBeforeView())
                                                .asPolygon() != null
                                        && !updatedRelation.getRemovedGeometry()
                                                .contains(new JtsPolyLineConverter().convert(
                                                        ((Area) featureChange.getBeforeView())
                                                                .asPolygon())))
                                {
                                    throw new CoreException(
                                            "Geometric relation {} had no removed geometry for area member {} with updated geometry!",
                                            relationId, after.getIdentifier());
                                }
                                if (!updatedRelation.getAddedGeometry().contains(
                                        new JtsPolyLineConverter().convert(after.asPolygon())))
                                {
                                    throw new CoreException(
                                            "Geometric relation {} had no added geometry for area member {} with updated geometry!",
                                            relationId, after.getIdentifier());
                                }
                            }
                        }
                    });
                });
    }

    protected void validateReverseEdgesHaveForwardMatchingCounterpart()
    {
        this.change.changesFor(ItemType.EDGE)
                .filter(featureChange -> !((Edge) featureChange.getAfterView()).isMainEdge())
                .filter(featureChange -> featureChange.getChangeType() != ChangeType.REMOVE)
                .forEach(backwardFeatureChange ->
                {
                    final long backwardEdgeIdentifier = backwardFeatureChange.getAfterView()
                            .getIdentifier();
                    final long forwardEdgeIdentifier = -backwardEdgeIdentifier;

                    final Edge backwardEdge = (Edge) backwardFeatureChange.getAfterView();
                    final Optional<FeatureChange> forwardFeatureChangeOption = this.change
                            .changeFor(ItemType.EDGE, forwardEdgeIdentifier);
                    if (forwardFeatureChangeOption.isPresent())
                    {
                        final FeatureChange forwardFeatureChange = forwardFeatureChangeOption.get();
                        if (forwardFeatureChange.getChangeType() != backwardFeatureChange
                                .getChangeType())
                        {
                            throw new CoreException(
                                    "Forward edge {} is {} when backward edge is {}",
                                    forwardEdgeIdentifier, forwardFeatureChange.getChangeType(),
                                    backwardFeatureChange.getChangeType());
                        }
                        if (forwardFeatureChange.getChangeType() == ChangeType.ADD)
                        {
                            final Edge forwardEdge = (Edge) forwardFeatureChange.getAfterView();
                            validateEdgeConnectedNodesMatch(forwardEdge, backwardEdge);
                            validateEdgePolyLinesMatch(forwardEdge, backwardEdge);
                        }
                    }
                });
    }

    private <T> boolean differ(final T left, final T right, final BiPredicate<T, T> equal)
    {
        if (left != null && right != null)
        {
            return !equal.test(left, right);
        }
        else
        {
            return false;
        }
    }

    private void validateEdgeConnectedNodesMatch(final Edge forwardEdge, final Edge backwardEdge)
    {
        final BiPredicate<Node, Node> equal = (left,
                right) -> left.getIdentifier() == right.getIdentifier();
        final long forwardEdgeIdentifier = forwardEdge.getIdentifier();
        final Node forwardStartNode = forwardEdge.start();
        final Node backwardEndNode = backwardEdge.end();
        if (differ(forwardStartNode, backwardEndNode, equal))
        {
            throw new CoreException(
                    "Forward edge {} start node {} does not match its backward edge end node {}",
                    forwardEdgeIdentifier, forwardStartNode, backwardEndNode);
        }
        final Node forwardEndNode = forwardEdge.end();
        final Node backwardStartNode = backwardEdge.start();
        if (differ(forwardEndNode, backwardStartNode, equal))
        {
            throw new CoreException(
                    "Forward edge {} end node {} does not match its backward edge start node {}",
                    forwardEdgeIdentifier, forwardEndNode, backwardStartNode);
        }
    }

    private void validateEdgePolyLinesMatch(final Edge forwardEdge, final Edge backwardEdge)
    {
        final BiPredicate<PolyLine, PolyLine> equal = (left, right) -> left
                .equals(right.reversed());
        final long forwardEdgeIdentifier = forwardEdge.getIdentifier();
        final PolyLine forwardPolyLine = forwardEdge.asPolyLine();
        final PolyLine backwardPolyLine = backwardEdge.asPolyLine();
        if (differ(forwardPolyLine, backwardPolyLine, equal))
        {
            throw new CoreException(
                    "Forward edge {} polyline {} does not match its backward edge polyline {}",
                    forwardEdgeIdentifier, forwardPolyLine, backwardPolyLine);
        }
    }
}
