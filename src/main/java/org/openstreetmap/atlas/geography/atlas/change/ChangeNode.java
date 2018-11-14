package org.openstreetmap.atlas.geography.atlas.change;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * @author matthieun
 */
public class ChangeNode extends Node // NOSONAR
{
    private static final long serialVersionUID = 4353679260691518275L;

    private final Node source;

    protected ChangeNode(final ChangeAtlas atlas, final Node source)
    {
        super(atlas);
        this.source = source;
    }

    @Override
    public long getIdentifier()
    {
        return this.source.getIdentifier();
    }

    @Override
    public Location getLocation()
    {
        return this.source.getLocation();
    }

    @Override
    public SortedSet<Edge> inEdges()
    {
        return this.source.inEdges().stream()
                .map(edge -> getChangeAtlas().edge(edge.getIdentifier()))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public SortedSet<Edge> outEdges()
    {
        return this.source.outEdges().stream()
                .map(edge -> getChangeAtlas().edge(edge.getIdentifier()))
                .collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public Set<Relation> relations()
    {
        throw new UnsupportedOperationException();
    }

    protected SortedSet<Long> inEdgeIdentifiers()
    {
        return this.source.inEdges().stream().map(Edge::getIdentifier)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    protected SortedSet<Long> outEdgeIdentifiers()
    {
        return this.source.outEdges().stream().map(Edge::getIdentifier)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private ChangeAtlas getChangeAtlas()
    {
        return (ChangeAtlas) getAtlas();
    }
}
