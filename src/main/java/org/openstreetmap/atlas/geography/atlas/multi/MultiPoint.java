package org.openstreetmap.atlas.geography.atlas.multi;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

import com.google.common.collect.Sets;

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

    private SubPointList subPoints;

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
        return getRepresentativeSubPoint().getLocation();
    }

    public SubPointList getSubPoints()
    {
        if (this.subPoints == null)
        {
            this.subPoints = multiAtlas().subPoints(this.identifier);
        }
        return this.subPoints;
    }

    @Override
    public Map<String, String> getTags()
    {
        return this.getRepresentativeSubPoint().getTags();
    }

    @Override
    public Set<Relation> relations()
    {
        Set<Relation> unionOfAllParentRelations = new HashSet<>();
        for (final Point subPoint : getSubPoints().getSubPoints())
        {
            final Set<Relation> currentSubPointParentRelations = multiAtlas()
                    .multifyRelations(subPoint);
            unionOfAllParentRelations = Sets.union(unionOfAllParentRelations,
                    currentSubPointParentRelations);
        }
        return unionOfAllParentRelations;
    }

    private Point getRepresentativeSubPoint()
    {
        return getSubPoints().getSubPoints().get(0);
    }

    private MultiAtlas multiAtlas()
    {
        return (MultiAtlas) this.getAtlas();
    }
}
