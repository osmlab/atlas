package org.openstreetmap.atlas.geography.atlas.multi;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Used by the {@link MultiLine} to hold multiple versions of identical {@link Line}s. This is in
 * case one of the {@link Line}s has a parent {@link Relation} that was not contained in one of the
 * sub-{@link Atlas}es of the containing {@link MultiAtlas}.
 *
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
