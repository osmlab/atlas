package org.openstreetmap.atlas.geography.atlas.multi;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.items.Line;

/**
 * @author lcram
 */
public class SubLineList implements Iterable<Line>, Serializable
{
    private static final long serialVersionUID = -1413359659676228024L;

    private final List<Line> subLines;

    public SubLineList(final List<Line> subLines)
    {
        if (subLines == null)
        {
            throw new CoreException("Cannot have a null list of sub lines.");
        }
        this.subLines = subLines;
    }

    public List<Line> getSubLines()
    {
        return this.subLines;
    }

    @Override
    public Iterator<Line> iterator()
    {
        return this.subLines.iterator();
    }

    public int size()
    {
        return this.subLines.size();
    }
}
