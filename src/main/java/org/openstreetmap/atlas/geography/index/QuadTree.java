package org.openstreetmap.atlas.geography.index;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Rectangle;

import com.vividsolutions.jts.index.quadtree.Quadtree;

/**
 * A JTS Quadtree wrapper.
 * <p>
 * Quadtree is a spatial index structure for efficient range querying of items bounded by 2D
 * rectangles. This Quadtree index provides a primary filter for range rectangle queries. The
 * various query methods return a list of all items which may intersect the query rectangle.
 * <p>
 * <b>Note that it may thus return items which do not in fact intersect the query rectangle</b>.
 * </p>
 * A secondary filter is required to test for actual intersection between the query rectangle and
 * the envelope of each candidate item. The secondary filter may be performed explicitly, or it may
 * be provided implicitly by subsequent operations executed on the items (for instance, if the index
 * query is followed by computing a spatial predicate between the query geometry and tree items, the
 * envelope intersection check is performed automatically.
 *
 * @param <T>
 *            The type to be indexed
 * @author tony
 */
public class QuadTree<T> implements JtsSpatialIndex<T>
{
    private static final long serialVersionUID = 7515245245282264428L;
    private final Quadtree tree = new Quadtree();
    private Rectangle bound;

    public static <K extends Located> QuadTree<K> forLocated(final Iterable<K> locatedIterable)
    {
        final QuadTree<K> toReturn = new QuadTree<>();
        locatedIterable.forEach(located -> toReturn.add(located.bounds(), located));
        return toReturn;
    }

    public static <K> QuadTree<K> forCollection(final Iterable<K> iterable,
            final Function<K, Rectangle> transform)
    {
        final QuadTree<K> toReturn = new QuadTree<>();
        iterable.forEach(item -> toReturn.add(transform.apply(item), item));
        return toReturn;
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
