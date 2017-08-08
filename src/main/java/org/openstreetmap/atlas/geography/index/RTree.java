package org.openstreetmap.atlas.geography.index;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.Rectangle;

import com.vividsolutions.jts.index.strtree.STRtree;

/**
 * A wrapper of JTS STRtree.
 * <p>
 * A query-only R-tree created using the Sort-Tile-Recursive (STR) algorithm. For two-dimensional
 * spatial data. The STR packed R-tree is simple to implement and maximizes space utilization; that
 * is, as many leaves as possible are filled to capacity. Overlap between nodes is far less than in
 * a basic R-tree. However, once the tree has been built (explicitly or on the first call to
 * #query), items may not be added or removed.
 * </p>
 *
 * @param <T>
 *            The type to be indexed
 * @author tony
 */
public class RTree<T> implements JtsSpatialIndex<T>
{
    private static final long serialVersionUID = -3672714932238885163L;
    private final STRtree tree;
    private Rectangle bound;

    public RTree()
    {
        this.tree = new STRtree();
    }

    public RTree(final int nodeCapacity)
    {
        this.tree = new STRtree(nodeCapacity);
    }

    @Override
    public void add(final Rectangle bound, final T item)
    {
        this.tree.insert(bound.asEnvelope(), item);
        if (this.bound != null)
        {
            this.bound = this.bound.combine(bound);
        }
        else
        {
            this.bound = bound;
        }
    }

    @Override
    public Rectangle bounds()
    {
        return this.bound;
    }

    public void build()
    {
        this.tree.build();
    }

    public int depth()
    {
        return this.tree.depth();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<T> get(final Rectangle bound)
    {
        return this.tree.query(bound.asEnvelope());
    }

    @Override
    public List<T> get(final Rectangle bound, final Predicate<T> predicate)
    {
        return get(bound).stream().filter(predicate).collect(Collectors.toList());
    }

    public boolean isEmpty()
    {
        return this.tree.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public List<T> itemsTree()
    {
        return this.tree.itemsTree();
    }

    @Override
    /**
     * Note that the remove operation won't adjust this.bound accordingly
     */
    public boolean remove(final Rectangle bound, final T item)
    {
        return this.tree.remove(bound.asEnvelope(), item);
    }

    @Override
    public int size()
    {
        return this.tree.size();
    }
}
