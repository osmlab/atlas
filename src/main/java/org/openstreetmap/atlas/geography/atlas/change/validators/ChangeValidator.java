package org.openstreetmap.atlas.geography.atlas.change.validators;

import java.util.Map;
import java.util.function.BiPredicate;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.change.Change;
import org.openstreetmap.atlas.geography.atlas.change.rule.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.rule.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Node;
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
        validateReverseEdgesHaveForwardMatchingCounterpart();
        logger.trace("Finished validation of Change {} in {}", this.change.getName(),
                start.elapsedSince());
    }

    protected void validateReverseEdgesHaveForwardMatchingCounterpart()
    {
        this.change.changesFor(ItemType.EDGE)
                .filter(featureChange -> !((Edge) featureChange.getReference()).isMasterEdge())
                .forEach(backwardFeatureChange ->
                {
                    final long backwardEdgeIdentifier = backwardFeatureChange.getReference()
                            .getIdentifier();
                    final long forwardEdgeIdentifier = -backwardEdgeIdentifier;
                    final FeatureChange forwardFeatureChange = this.change
                            .changeFor(ItemType.EDGE, forwardEdgeIdentifier)
                            .orElseThrow(() -> new CoreException(
                                    "Backward edge {} is {} but does not have a forward edge change reference present.",
                                    backwardEdgeIdentifier, backwardFeatureChange.getChangeType()));
                    if (forwardFeatureChange.getChangeType() != backwardFeatureChange
                            .getChangeType())
                    {
                        throw new CoreException("Forward edge {} is {} when backward edge is {}",
                                forwardEdgeIdentifier, forwardFeatureChange.getChangeType(),
                                backwardFeatureChange.getChangeType());
                    }
                    if (forwardFeatureChange.getChangeType() == ChangeType.ADD)
                    {
                        final Edge forwardEdge = (Edge) forwardFeatureChange.getReference();
                        final Edge backwardEdge = (Edge) backwardFeatureChange.getReference();
                        validateEdgeConnectedNodesMatch(forwardEdge, backwardEdge);
                        validateEdgePolyLinesMatch(forwardEdge, backwardEdge);
                        validateEdgeTagsMatch(forwardEdge, backwardEdge);
                    }
                });
    }

    private <T> boolean differ(final T left, final T right, final BiPredicate<T, T> equal)
    {
        if (left == null && right != null || left != null && right == null)
        {
            return true;
        }
        if (left != null/* right is implicitly not null here */)
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

    private void validateEdgeTagsMatch(final Edge forwardEdge, final Edge backwardEdge)
    {
        final BiPredicate<Map<String, String>, Map<String, String>> equal = (left, right) -> left
                .equals(right);
        final long forwardEdgeIdentifier = forwardEdge.getIdentifier();
        final Map<String, String> forwardTags = forwardEdge.getTags();
        final Map<String, String> backwardTags = backwardEdge.getTags();
        if (differ(forwardTags, backwardTags, equal))
        {
            throw new CoreException(
                    "Forward edge {} tags {} do not match its backward edge tags {}",
                    forwardEdgeIdentifier, forwardTags, backwardTags);
        }
    }
}
