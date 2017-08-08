package org.openstreetmap.atlas.geography.atlas.dynamic;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * @author matthieun
 */
public class DynamicEdge extends Edge
{
    private static final long serialVersionUID = -3839789846949424342L;

    // Not index!
    private final long identifier;

    protected DynamicEdge(final DynamicAtlas atlas, final long identifier)
    {
        super(atlas);
        this.identifier = identifier;
    }

    @Override
    public PolyLine asPolyLine()
    {
        return subEdge().asPolyLine();
    }

    @Override
    public Node end()
    {
        return new DynamicNode(dynamicAtlas(), subEdge().end().getIdentifier());
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public Map<String, String> getTags()
    {
        return subEdge().getTags();
    }

    @Override
    public Set<Relation> relations()
    {
        return subEdge().relations().stream()
                .map(relation -> new DynamicRelation(dynamicAtlas(), relation.getIdentifier()))
                .collect(Collectors.toSet());
    }

    @Override
    public Node start()
    {
        return new DynamicNode(dynamicAtlas(), subEdge().start().getIdentifier());
    }

    private DynamicAtlas dynamicAtlas()
    {
        return (DynamicAtlas) this.getAtlas();
    }

    private Edge subEdge()
    {
        final Edge result = dynamicAtlas().subEdge(this.identifier);
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
