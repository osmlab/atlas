package org.openstreetmap.atlas.geography.atlas.routing;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Route;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Router that follows the simple A* algorithm
 *
 * @author matthieun
 */
public class AStarRouter extends AbstractRouter
{
    /**
     * A heuristic for an {@link AStarRouter}
     *
     * @author matthieun
     */
    public interface Heuristic
    {
        /**
         * Compute the cost of a candidate, given the start and end point.
         *
         * @param start
         *            The start of the route
         * @param candidate
         *            The candidate point of the route
         * @param end
         *            The end of the route
         * @return The cost
         */
        double cost(Node start, Node candidate, Node end);
    }

    /**
     * A candidate for an {@link AStarRouter}
     *
     * @author matthieun
     */
    private static class Candidate implements Comparable<Candidate>
    {
        private final Route route;
        private final double cost;

        Candidate(final Route route, final double cost)
        {
            this.route = route;
            this.cost = cost;
        }

        @Override
        public int compareTo(final Candidate other)
        {
            return this.getCost() > other.getCost() ? 1
                    : this.getCost() == other.getCost() ? 0 : -1;
        }

        public double getCost()
        {
            return this.cost;
        }

        public Route getRoute()
        {
            return this.route;
        }

        public Edge lastEdge()
        {
            return getRoute().end();
        }

        public Node lastNode()
        {
            return lastEdge().end();
        }

        @Override
        public String toString()
        {
            final StringBuilder builder = new StringBuilder();
            builder.append("[Candidate: ");
            builder.append(this.route.toString());
            builder.append(", Cost: ");
            builder.append(this.cost);
            builder.append("]");
            return builder.toString();
        }

        public Candidate withNewEdge(final Edge edge, final double edgeCost)
        {
            return new Candidate(getRoute().append(edge), getCost() + edgeCost);
        }
    }

    private final Heuristic heuristic;

    /**
     * @param atlas
     *            The {@link Atlas} on which the router works
     * @param threshold
     *            The threshold to look for edges in case of routing between locations
     * @return A balanced A* Router, which gives 75% cost to the distance from the end and 25% cost
     *         to the distance from the start.
     */
    public static AStarRouter balanced(final Atlas atlas, final Distance threshold)
    {
        final double distanceFromStartCostRatio = 0.25;
        return new AStarRouter(atlas, threshold,
                (start, candidate, end) -> distanceFromStartCostRatio
                        * start.getLocation().distanceTo(candidate.getLocation()).asMeters()
                        + (1 - distanceFromStartCostRatio)
                                * candidate.getLocation().distanceTo(end.getLocation()).asMeters());
    }

    /**
     * @param atlas
     *            The {@link Atlas} on which the router works
     * @param threshold
     *            The threshold to look for edges in case of routing between locations
     * @return A Dijkstra router (the heuristic looks at the distance from the start only)
     */
    public static AStarRouter dijkstra(final Atlas atlas, final Distance threshold)
    {
        return new AStarRouter(atlas, threshold, (start, candidate, end) -> start.getLocation()
                .distanceTo(candidate.getLocation()).asMeters());
    }

    /**
     * @param atlas
     *            The {@link Atlas} on which the router works
     * @param threshold
     *            The threshold to look for edges in case of routing between locations
     * @return A fast A* Router, which gives all cost to the distance from the end. The route will
     *         be found faster, but the result will be non-optimal
     */
    public static AStarRouter fastComputationAndSubOptimalRoute(final Atlas atlas,
            final Distance threshold)
    {
        return new AStarRouter(atlas, threshold, (start, candidate, end) -> candidate.getLocation()
                .distanceTo(end.getLocation()).asMeters());
    }

    /**
     * Construct
     *
     * @param atlas
     *            The map
     * @param threshold
     *            The threshold to look for edges in case of routing between locations
     * @param heuristic
     *            The heuristic of the {@link AStarRouter}
     */
    public AStarRouter(final Atlas atlas, final Distance threshold, final Heuristic heuristic)
    {
        super(atlas, threshold);
        this.heuristic = heuristic;
    }

    @Override
    public Route route(final Node start, final Node end)
    {
        if (start.equals(end))
        {
            return null;
        }
        if (start.outEdges().isEmpty())
        {
            return null;
        }
        if (end.inEdges().isEmpty())
        {
            return null;
        }

        // Real Routing
        final PriorityQueue<Candidate> candidates = new PriorityQueue<>();
        final Set<Edge> explored = new HashSet<>();
        // Initialize
        for (final Edge edge : start.outEdges())
        {
            if (end.equals(edge.end()))
            {
                return Route.forEdge(edge);
            }
            candidates.add(new Candidate(Route.forEdge(edge),
                    this.heuristic.cost(start, edge.end(), end)));
        }
        // Cycle
        while (!candidates.isEmpty())
        {
            final Candidate best = candidates.poll();
            if (end.equals(best.lastNode()))
            {
                return best.getRoute();
            }
            for (final Edge edge : best.lastNode().outEdges())
            {
                if (!explored.contains(edge))
                {
                    candidates.add(
                            best.withNewEdge(edge, this.heuristic.cost(start, edge.end(), end)));
                }
            }
            explored.add(best.lastEdge());
        }
        return null;
    }
}
