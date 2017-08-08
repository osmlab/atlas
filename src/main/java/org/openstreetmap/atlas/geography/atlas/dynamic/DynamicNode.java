package org.openstreetmap.atlas.geography.atlas.dynamic;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * @author matthieun
 */
public class DynamicNode extends Node
{
    private static final long serialVersionUID = 7046248083667389625L;

    // Not index!
    private final long identifier;

    protected DynamicNode(final DynamicAtlas atlas, final long identifier)
    {
        super(atlas);
        this.identifier = identifier;
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public Location getLocation()
    {
        return subNode().getLocation();
    }

    @Override
    public Map<String, String> getTags()
    {
        return subNode().getTags();
    }

    @Override
    public SortedSet<Edge> inEdges()
    {
        final SortedSet<Edge> result = new TreeSet<>();
        subNode().inEdges()
                .forEach(edge -> result.add(new DynamicEdge(dynamicAtlas(), edge.getIdentifier())));
        return result;
    }

    @Override
    public SortedSet<Edge> outEdges()
    {
        final SortedSet<Edge> result = new TreeSet<>();
        subNode().outEdges()
                .forEach(edge -> result.add(new DynamicEdge(dynamicAtlas(), edge.getIdentifier())));
        return result;
    }

    @Override
    public Set<Relation> relations()
    {
        return subNode().relations().stream()
                .map(relation -> new DynamicRelation(dynamicAtlas(), relation.getIdentifier()))
                .collect(Collectors.toSet());
    }

    private DynamicAtlas dynamicAtlas()
    {
        return (DynamicAtlas) this.getAtlas();
    }

    private Node subNode()
    {
        final Node result = dynamicAtlas().subNode(this.identifier);
        if (result != null)
        {
            return result;
        }
        else
        {
            throw new CoreException("DynamicAtlas {} moved too fast! {} {} is missing now.",
                    dynamicAtlas().getName(), this.getClass().getSimpleName(), this.identifier);
        }
    }
}
