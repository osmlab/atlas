package org.openstreetmap.atlas.geography.atlas.routing;

import java.util.Iterator;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Route;
import org.openstreetmap.atlas.geography.atlas.items.SnappedEdge;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of a {@link Router}.
 *
 * @author matthieun
 */
public abstract class AbstractRouter implements Router
{
    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(AbstractRouter.class);

    private final Atlas atlas;
    private final Distance threshold;

    /**
     * Construct
     *
     * @param atlas
     *            The map
     * @param threshold
     *            The threshold to look for edges in case of routing between locations
     */
    public AbstractRouter(final Atlas atlas, final Distance threshold)
    {
        this.atlas = atlas;
        this.threshold = threshold;
    }

    @Override
    public Route route(final Edge start, final Edge end)
    {
        if (start == null || end == null)
        {
            throw new CoreException(
                    "Cannot compute route on null arguments: start = {} and end = {}", start, end);
        }
        if (start.equals(end))
        {
            // Same edge
            return Route.forEdge(start);
        }
        if (start.end().equals(end.start()))
        {
            // Directly connected edges
            return Route.forEdge(start).append(end);
        }
        final Route result = route(start.end(), end.start());
        if (result != null)
        {
            // Re-populate the result with the start and end edges
            return Route.forEdge(start).append(result).append(end);
        }
        return null;
    }

    @Override
    public Route route(final Location start, final Location end)
    {
        if (start == null || end == null)
        {
            throw new CoreException(
                    "Cannot compute route on null arguments: start = {} and end = {}", start, end);
        }
        final List<SnappedEdge> startEdges = this.atlas.snaps(start, this.threshold);
        final List<SnappedEdge> endEdges = this.atlas.snaps(end, this.threshold);
        if (startEdges.isEmpty())
        {
            // logger.warn("Could not find a snap for start location {}", start);
            return null;
        }
        if (endEdges.isEmpty())
        {
            // logger.warn("Could not find a snap for end location {}", end);
            return null;
        }
        final Iterator<SnappedEdge> startIterator = startEdges.iterator();
        for (int i = 0; i < startEdges.size(); i++)
        {
            final Edge startEdge = startIterator.next().getEdge();
            final Iterator<SnappedEdge> endIterator = endEdges.iterator();
            for (int j = 0; j < endEdges.size(); j++)
            {
                final Edge endEdge = endIterator.next().getEdge();
                final Route route = route(startEdge, endEdge);
                if (route != null)
                {
                    return route;
                }
            }
        }
        return null;
    }
}
