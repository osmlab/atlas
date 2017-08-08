package org.openstreetmap.atlas.geography.atlas.builder.store;

import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.atlas.geography.Location;

/**
 * A temporary object store to hold missing objects when checking data integrity of
 * {@link AtlasPrimitiveObjectStore}
 *
 * @author tony
 */
public class TemporaryObjectStore
{
    private final Set<Long> nodes = new HashSet<>();
    private final Set<Long> points = new HashSet<>();
    private final Set<Long> edges = new HashSet<>();
    private final Set<Long> lines = new HashSet<>();
    private final Set<Long> areas = new HashSet<>();
    private final Set<Long> relations = new HashSet<>();
    private final Set<Location> locations = new HashSet<>();

    public void addArea(final long identifier)
    {
        this.areas.add(identifier);
    }

    public void addEdge(final long identifier)
    {
        this.edges.add(identifier);
    }

    public void addLine(final long identifier)
    {
        this.lines.add(identifier);
    }

    public void addLocation(final Location location)
    {
        this.locations.add(location);
    }

    public void addNode(final long identifier)
    {
        this.nodes.add(identifier);
    }

    public void addPoint(final long identifier)
    {
        this.points.add(identifier);
    }

    public void addRelation(final long identifier)
    {
        this.relations.add(identifier);
    }

    public Set<Long> getAreas()
    {
        return this.areas;
    }

    public Set<Long> getEdges()
    {
        return this.edges;
    }

    public Set<Long> getLines()
    {
        return this.lines;
    }

    public Set<Location> getLocations()
    {
        return this.locations;
    }

    public Set<Long> getNodes()
    {
        return this.nodes;
    }

    public Set<Long> getPoints()
    {
        return this.points;
    }

    public Set<Long> getRelations()
    {
        return this.relations;
    }

    public boolean isEmpty()
    {
        return this.nodes.isEmpty() && this.points.isEmpty() && this.edges.isEmpty()
                && this.lines.isEmpty() && this.areas.isEmpty() && this.relations.isEmpty()
                && this.locations.isEmpty();
    }

    public int size()
    {
        return this.nodes.size() + this.points.size() + this.edges.size() + this.lines.size()
                + this.areas.size() + this.relations.size() + this.locations.size();
    }

    public String toDebugString()
    {
        return "TemporaryIdentifierStore has " + this.nodes.size() + " nodes, " + this.points.size()
                + " points, " + this.edges.size() + " edges, " + this.lines.size() + " lines, "
                + this.areas.size() + " areas, " + this.relations.size() + " relations, "
                + this.locations.size() + " locations";
    }

}
