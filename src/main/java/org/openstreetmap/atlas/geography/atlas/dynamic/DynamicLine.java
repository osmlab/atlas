package org.openstreetmap.atlas.geography.atlas.dynamic;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * @author matthieun
 */
public class DynamicLine extends Line
{
    private static final long serialVersionUID = -7689258557279641445L;

    // Not index!
    private final long identifier;

    protected DynamicLine(final DynamicAtlas atlas, final long identifier)
    {
        super(atlas);
        this.identifier = identifier;
    }

    @Override
    public PolyLine asPolyLine()
    {
        return subLine().asPolyLine();
    }

    @Override
    public long getIdentifier()
    {
        return this.identifier;
    }

    @Override
    public Map<String, String> getTags()
    {
        return subLine().getTags();
    }

    @Override
    public Set<Relation> relations()
    {
        return subLine().relations().stream()
                .map(relation -> new DynamicRelation(dynamicAtlas(), relation.getIdentifier()))
                .collect(Collectors.toSet());
    }

    private DynamicAtlas dynamicAtlas()
    {
        return (DynamicAtlas) this.getAtlas();
    }

    private Line subLine()
    {
        final Line result = dynamicAtlas().subLine(this.identifier);
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
