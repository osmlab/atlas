package org.openstreetmap.atlas.geography.atlas.dynamic;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * @author matthieun
 */
public class DynamicPoint extends Point
{
    private static final long serialVersionUID = 5290355290550015953L;

    // Not index!
    private final long identifier;

    protected DynamicPoint(final DynamicAtlas atlas, final long identifier)
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
        return subPoint().getLocation();
    }

    @Override
    public Map<String, String> getTags()
    {
        return subPoint().getTags();
    }

    @Override
    public Set<Relation> relations()
    {
        return subPoint().relations().stream()
                .map(relation -> new DynamicRelation(dynamicAtlas(), relation.getIdentifier()))
                .collect(Collectors.toSet());
    }

    private DynamicAtlas dynamicAtlas()
    {
        return (DynamicAtlas) this.getAtlas();
    }

    private Point subPoint()
    {
        final Point result = dynamicAtlas().subPoint(this.identifier);
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
