package org.openstreetmap.atlas.geography.atlas.validators;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class AtlasNodeValidator
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasNodeValidator.class);

    private final Atlas atlas;

    public AtlasNodeValidator(final Atlas atlas)
    {
        this.atlas = atlas;
    }

    public void validate()
    {
        logger.trace("Starting Node validation of Atlas {}", this.atlas.getName());
        final Time start = Time.now();
        validateNodeToEdgeConnectivity();
        validateNodeToEdgeLocationAccuracy();
        logger.trace("Finished Node validation of Atlas {} in {}", this.atlas.getName(),
                start.elapsedSince());
    }

    protected void validateNodeToEdgeConnectivity()
    {
        for (final Node node : this.atlas.nodes())
        {
            for (final Edge edge : node.inEdges())
            {
                if (edge == null)
                {
                    throw new CoreException(
                            "Node {} is logically disconnected from some referenced in edge.",
                            node.getIdentifier());
                }
            }
            for (final Edge edge : node.outEdges())
            {
                if (edge == null)
                {
                    throw new CoreException("Node {} is logically disconnected from some out edge.",
                            node.getIdentifier());
                }
            }
        }
    }

    protected void validateNodeToEdgeLocationAccuracy()
    {
        for (final Node node : this.atlas.nodes())
        {
            final Location nodeLocation = node.getLocation();
            for (final Edge edge : node.outEdges())
            {
                final Location edgeStartLocation = edge.asPolyLine().first();
                if (!nodeLocation.equals(edgeStartLocation))
                {
                    throw new CoreException(
                            "Edge {} with start location {} does not match with its start Node {} at location: {}",
                            edge.getIdentifier(), edgeStartLocation, edge.start().getIdentifier(),
                            nodeLocation);
                }
            }
            for (final Edge edge : node.inEdges())
            {
                final Location edgeEndLocation = edge.asPolyLine().last();
                if (!nodeLocation.equals(edgeEndLocation))
                {
                    throw new CoreException(
                            "Edge {} with end location {} does not match with its end Node {} at location: {}",
                            edge.getIdentifier(), edgeEndLocation, edge.end().getIdentifier(),
                            nodeLocation);
                }
            }
        }
    }
}
