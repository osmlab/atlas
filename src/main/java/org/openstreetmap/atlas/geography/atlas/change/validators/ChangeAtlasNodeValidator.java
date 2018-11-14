package org.openstreetmap.atlas.geography.atlas.change.validators;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.change.ChangeAtlas;
import org.openstreetmap.atlas.geography.atlas.change.ChangeNode;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class ChangeAtlasNodeValidator
{
    private static final Logger logger = LoggerFactory.getLogger(ChangeAtlasNodeValidator.class);

    private final ChangeAtlas atlas;

    public ChangeAtlasNodeValidator(final ChangeAtlas atlas)
    {
        this.atlas = atlas;
    }

    public void validate()
    {
        logger.trace("Starting Node validation of ChangeAtlas {}", this.atlas.getName());
        final Time start = Time.now();
        validateNodeToEdgeConnectivity();
        validateNodeToEdgeLocationAccuracy();
        logger.trace("Finished Node validation of ChangeAtlas {} in {}", this.atlas.getName(),
                start.elapsedSince());
    }

    public void validateNodeToEdgeConnectivity()
    {
        for (final Node node : this.atlas.nodes())
        {
            for (final Long edgeIdentifier : ((ChangeNode) node).inEdgeIdentifiers())
            {
                if (this.atlas.edge(edgeIdentifier) == null)
                {
                    throw new CoreException(
                            "Node {} is logically disconnected from some in edge. Referenced in edge {} does not exist.",
                            node.getIdentifier(), edgeIdentifier);
                }
            }
            for (final Long edgeIdentifier : ((ChangeNode) node).outEdgeIdentifiers())
            {
                if (this.atlas.edge(edgeIdentifier) == null)
                {
                    throw new CoreException(
                            "Node {} is logically disconnected from some out edge. Referenced out edge {} does not exist.",
                            node.getIdentifier(), edgeIdentifier);
                }
            }
        }
    }

    public void validateNodeToEdgeLocationAccuracy()
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
