package org.openstreetmap.atlas.geography.index;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Rectangle;

/**
 * Define some common methods for spatial indices
 *
 * @param <T>
 *            The type to be indexed
 * @author tony
 */
public interface JtsSpatialIndex<T> extends Located, Serializable
{
    /**
     * Inserts an spatial item with an extent specified by the given {@link Rectangle} to the index
     *
     * @param bound
     *            The bound the object belongs to
     * @param item
     *            The item to insert
     */
    void add(Rectangle bound, T item);

    /**
     * Queries the index for all items whose bounds intersect the given {@link Rectangle}
     *
     * @param bound
     *            The bound to query
     * @return An {@link List} of features within or intersecting the bound.
     */
    List<T> get(Rectangle bound);

    /**
     * Queries the index for all items whose bounds intersect the given {@link Rectangle} and match
     * the given predicate
     *
     * @param bound
     *            The bound to query
     * @param predicate
     *            a predicate to apply to each item to determine if it should be included
     * @return An {@link List} of features within or intersecting the bound.
     */
    List<T> get(Rectangle bound, Predicate<T> predicate);

    /**
     * Removes a single item from the tree.
     *
     * @param bound
     *            The bound the object belongs to
     * @param item
     *            The item to remove
     * @return <code>true</code> if the item was found
     */
    boolean remove(Rectangle bound, T item);

    /**
     * @return the number of items in the index
     */
    int size();
}
