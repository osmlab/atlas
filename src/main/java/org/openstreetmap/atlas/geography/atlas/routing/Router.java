package org.openstreetmap.atlas.geography.atlas.routing;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Route;

/**
 * A router that routes between two points
 *
 * @author matthieun
 */
public interface Router
{
    /**
     * Route from a start to an end
     *
     * @param start
     *            The start edge
     * @param end
     *            The end edge
     * @return The route corresponding, null if it can't find any
     */
    Route route(Edge start, Edge end);

    /**
     * Route from a start to an end
     *
     * @param start
     *            The start location
     * @param end
     *            The end location
     * @return The route corresponding, null if it can't find any
     */
    Route route(Location start, Location end);

    /**
     * Route from a start to an end
     *
     * @param start
     *            The start node
     * @param end
     *            The end node
     * @return The route corresponding, null if it can't find any
     */
    Route route(Node start, Node end);
}
