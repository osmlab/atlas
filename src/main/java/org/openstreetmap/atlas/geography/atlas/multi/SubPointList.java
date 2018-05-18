package org.openstreetmap.atlas.geography.atlas.multi;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Used by the {@link MultiPoint} to hold multiple versions of identical {@link Point}s. This is in
 * case one of the {@link Point}s has a parent {@link Relation} that was not contained in one of the
 * sub-{@link Atlas}es of the containing {@link MultiAtlas}.
 *
 * @author lcram
 */
public class SubPointList implements Iterable<Point>, Serializable
{
    private static final long serialVersionUID = -1413359659676228024L;

    private final List<Point> subPoints;

    public SubPointList(final List<Point> subPoints)
    {
        if (subPoints == null)
        {
            throw new CoreException("Cannot have a null list of sub points.");
        }
        this.subPoints = subPoints;
    }

    public List<Point> getSubPoints()
    {
        return this.subPoints;
    }

    @Override
    public Iterator<Point> iterator()
    {
        return this.subPoints.iterator();
    }

    public int size()
    {
        return this.subPoints.size();
    }
}
