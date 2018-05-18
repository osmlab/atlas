package org.openstreetmap.atlas.geography.atlas.multi;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.items.Point;

/**
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
