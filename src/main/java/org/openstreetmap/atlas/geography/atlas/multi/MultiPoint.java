package org.openstreetmap.atlas.geography.atlas.multi;

import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * {@link Point} made from a {@link MultiAtlas}.
 *
 * @author matthieun
 */
public class MultiPoint extends Point
{
    private static final long serialVersionUID = 209103872813085178L;

    // Not index!
    private final long identifier;

    private Point subPoint;

    protected MultiPoint(final MultiAtlas atlas, final long identifier)
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
        return getSubPoint().getLocation();
    }

    @Override
    public Map<String, String> getTags()
    {
        return this.getSubPoint().getTags();
    }

    @Override
    public Set<Relation> relations()
    {
        return multiAtlas().multifyRelations(getSubPoint());
    }

    private Point getSubPoint()
    {
        if (this.subPoint == null)
        {
            this.subPoint = this.multiAtlas().subPoint(this.identifier);
        }
        return this.subPoint;
    }

    private MultiAtlas multiAtlas()
    {
        return (MultiAtlas) this.getAtlas();
    }
}
