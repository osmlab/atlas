package org.openstreetmap.atlas.geography.atlas.multi;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Relation;

/**
 * Used by the {@link MultiEdge} to hold multiple versions of identical {@link Edge}s. This is in
 * case one of the {@link Edge}s has a parent {@link Relation} that was not contained in one of the
 * sub-{@link Atlas}es of the containing {@link MultiAtlas}.
 *
 * @author lcram
 * @author matthieun
 */
public class SubEdgeList implements Iterable<Edge>, Serializable
{
    private static final long serialVersionUID = 4338093791628259315L;

    private final List<Edge> subEdges;

    SubEdgeList(final List<Edge> subEdges)
    {
        if (subEdges == null)
        {
            throw new CoreException("Cannot have a null list of sub edges.");
        }
        this.subEdges = subEdges;
    }

    public List<Edge> getSubEdges()
    {
        return this.subEdges;
    }

    @Override
    public Iterator<Edge> iterator()
    {
        return this.subEdges.iterator();
    }

    public int size()
    {
        return this.subEdges.size();
    }
}
