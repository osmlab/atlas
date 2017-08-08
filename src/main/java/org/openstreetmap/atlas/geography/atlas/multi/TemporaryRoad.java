package org.openstreetmap.atlas.geography.atlas.multi;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Route;
import org.openstreetmap.atlas.utilities.collections.StringList;

/**
 * All the edges coming from the same sub-atlas and that have the same osm identifier
 *
 * @author matthieun
 */
public class TemporaryRoad
{
    private final SortedSet<Edge> members;
    private final long osmIdentifier;
    private final Atlas subAtlas;
    private Route route;

    public TemporaryRoad(final Atlas subAtlas, final long osmIdentifier)
    {
        this.members = new TreeSet<>();
        this.subAtlas = subAtlas;
        this.osmIdentifier = osmIdentifier;
    }

    public void add(final Edge edge)
    {
        if (this.route != null)
        {
            throw new CoreException("Cannot add new edges when the route has already been created");
        }

        // Skip those, they will be re-constructed
        if (edge.getIdentifier() < 0)
        {
            return;
        }

        // Avoiding .equals() is intentional here!
        if (this.subAtlas != edge.getAtlas())
        {
            throw new CoreException("Cannot add an edge that is not from the right Atlas");
        }

        this.members.add(edge);
    }

    public Set<Edge> getMembers()
    {
        return this.members;
    }

    public Route getRoute()
    {
        if (this.route == null)
        {
            this.route = Route.fromNonArrangedEdgeSet(this.members, false);
        }
        return this.route;
    }

    public Iterable<Location> locations()
    {
        final List<Location> locations = new ArrayList<>();
        boolean processedFirstEdge = false;
        // Process nodes and calculate number of occurrences
        for (final Edge member : this.getMembers())
        {
            boolean isFirstNode = true;
            for (final Location location : member.getRawGeometry())
            {
                // We want to skip first nodes for each edge except the first one, because neighbor
                // edges share nodes
                if (processedFirstEdge && isFirstNode)
                {
                    isFirstNode = false;
                    continue;
                }
                locations.add(location);
            }
            processedFirstEdge = true;
        }
        return locations;
    }

    @Override
    public String toString()
    {
        final StringList memberList = new StringList();
        this.members.forEach(member -> memberList.add(member.toString()));
        return "[TemporaryRoad: subAtlas = " + this.subAtlas.getName() + ", osm = "
                + this.osmIdentifier + ", members = \n\t" + memberList.join("\n\t") + "\n]";
    }
}
