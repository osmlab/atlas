package org.openstreetmap.atlas.geography.atlas.validators;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class AtlasEdgeValidator
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasEdgeValidator.class);

    private final Atlas atlas;

    public AtlasEdgeValidator(final Atlas atlas)
    {
        this.atlas = atlas;
    }

    public void validate()
    {
        logger.trace("Starting Edge validation of Atlas {}", this.atlas.getName());
        final Time start = Time.now();
        validateEdgeToNodeConnectivity();
        validateEdgeToNodeLocationAccuracy();
        validateReverseEdgePolyLineUpdated();
        logger.trace("Finished Edge validation of Atlas {} in {}", this.atlas.getName(),
                start.elapsedSince());
    }

    protected void validateEdgeToNodeConnectivity()
    {
        for (final Edge edge : this.atlas.edges())
        {
            final Node start = edge.start();
            if (start == null)
            {
                throw new CoreException(
                        "Edge {} is logically disconnected at its start. Referenced Node does not exist.",
                        edge.getIdentifier());
            }
            if (start.outEdges().stream()
                    .noneMatch(edgeAtNode -> edgeAtNode.getIdentifier() == edge.getIdentifier()))
            {
                throw new CoreException("Edge {} references start Node {}. It is not reciprocal.",
                        edge.getIdentifier(), start.getIdentifier());
            }
            final Node end = edge.end();
            if (end == null)
            {
                throw new CoreException(
                        "Edge {} is logically disconnected at its end. Referenced Node does not exist.",
                        edge.getIdentifier());
            }
            if (end.inEdges().stream()
                    .noneMatch(edgeAtNode -> edgeAtNode.getIdentifier() == edge.getIdentifier()))
            {
                throw new CoreException("Edge {} references end Node {}. It is not reciprocal.",
                        edge.getIdentifier(), end.getIdentifier());
            }
        }
    }

    protected void validateEdgeToNodeLocationAccuracy()
    {
        for (final Edge edge : this.atlas.edges())
        {
            final Location startNodeLocation = edge.start().getLocation();
            final Location edgeStartLocation = edge.asPolyLine().first();
            if (!startNodeLocation.equals(edgeStartLocation))
            {
                throw new CoreException(
                        "Edge {} with start location {} does not match with its start Node {} at location: {}",
                        edge.getIdentifier(), edgeStartLocation, edge.start().getIdentifier(),
                        startNodeLocation);
            }
            final Location endNodeLocation = edge.end().getLocation();
            final Location edgeEndLocation = edge.asPolyLine().last();
            if (!endNodeLocation.equals(edgeEndLocation))
            {
                throw new CoreException(
                        "Edge {} with end location {} does not match with its end Node {} at location: {}",
                        edge.getIdentifier(), edgeEndLocation, edge.end().getIdentifier(),
                        endNodeLocation);
            }
        }
    }

    protected void validateReverseEdgePolyLineUpdated()
    {
        for (final Edge edge : this.atlas
                .edges(edge -> edge.hasReverseEdge() && edge.isMasterEdge()))
        {
            final Edge reversed = edge.reversed().orElseThrow(() -> new CoreException(
                    "Edge {} should have a reverse, but does not.", edge.getIdentifier()));
            final PolyLine forward = edge.asPolyLine();
            final PolyLine backward = reversed.asPolyLine();
            if (!forward.equals(backward.reversed()))
            {
                throw new CoreException(
                        "Edge {} and its reverse {} have mismatching PolyLines: Forward = {}, Backward = {}",
                        edge.getIdentifier(), reversed.getIdentifier(), forward, backward);
            }
        }
    }
}
