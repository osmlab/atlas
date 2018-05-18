package org.openstreetmap.atlas.geography.atlas.multi;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.items.Area;

/**
 * @author lcram
 */
public class SubAreaList implements Iterable<Area>, Serializable
{
    private static final long serialVersionUID = -1413359659676228024L;

    private final List<Area> subAreas;

    public SubAreaList(final List<Area> subAreas)
    {
        if (subAreas == null)
        {
            throw new CoreException("Cannot have a null list of sub areas.");
        }
        this.subAreas = subAreas;
    }

    public List<Area> getSubAreas()
    {
        return this.subAreas;
    }

    @Override
    public Iterator<Area> iterator()
    {
        return this.subAreas.iterator();
    }

    public int size()
    {
        return this.subAreas.size();
    }
}
