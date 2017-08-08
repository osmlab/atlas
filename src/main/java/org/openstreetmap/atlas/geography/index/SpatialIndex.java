package org.openstreetmap.atlas.geography.index;

import java.io.Serializable;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Rectangle;

/**
 * @author matthieun
 * @param <T>
 *            The type to be indexed
 */
public interface SpatialIndex<T extends Located> extends Located, Serializable
{
    /**
     * Populate the spatial index
     *
     * @param feature
     *            The feature to add
     */
    void add(T feature);

    /**
     * Get an {@link Iterable} over the features that are within or intersecting some bounds.
     *
     * @param bounds
     *            The bounds to query
     * @return An {@link Iterable} of features within or intersecting the bounds.
     */
    Iterable<T> get(Rectangle bounds);

    /**
     * Queries the index for all items whose bounds intersect the given {@link Rectangle} and match
     * the given predicate
     *
     * @param bound
     *            The bound to query
     * @param predicate
     *            a predicate to apply to each item to determine if it should be included
     * @return An {@link Iterable} of features within or intersecting the bound.
     */
    Iterable<T> get(Rectangle bound, Predicate<T> predicate);
}
