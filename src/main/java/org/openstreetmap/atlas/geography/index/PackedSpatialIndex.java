package org.openstreetmap.atlas.geography.index;

import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.Edge;

/**
 * This {@link PackedSpatialIndex} accepts a {@link Located} object (e.g. an {@link Edge}), but only
 * stores the packed information (identifier) into the real index. Spatial index can be an R-Tree or
 * Quad-Tree.
 *
 * @param <L>
 *            The type of {@link Located} item
 * @param <Packed>
 *            The type of packed item
 * @author tony
 */
public abstract class PackedSpatialIndex<L extends Located, Packed> implements SpatialIndex<L>
{
    private static final long serialVersionUID = 1747435801359663115L;
    private final JtsSpatialIndex<Packed> index;

    public PackedSpatialIndex(final JtsSpatialIndex<Packed> index)
    {
        this.index = index;
    }

    @Override
    public void add(final L located)
    {
        final Rectangle bounds = located.bounds();
        if (bounds != null)
        {
            this.index.add(bounds, compress(located));
        }
        else
        {
            throw new CoreException(
                    "Unable to get bounds for located item when building spatial index: {}",
                    located);
        }
    }

    @Override
    public Rectangle bounds()
    {
        return this.index.bounds();
    }

    @Override
    public Iterable<L> get(final Rectangle bound)
    {
        return ((ArrayList<Packed>) this.index.get(bound)).stream().map(this::restore)
                .collect(Collectors.toList());
    }

    @Override
    public Iterable<L> get(final Rectangle bound, final Predicate<L> predicate)
    {
        return ((ArrayList<Packed>) this.index.get(bound)).stream().map(this::restore)
                .filter(predicate).collect(Collectors.toList());
    }

    /**
     * Extract a packed object from the given located object
     *
     * @param located
     *            The {@link Located} item to extract
     * @return The packed object
     */
    protected abstract Packed compress(L located);

    protected abstract boolean isValid(L located, Rectangle bounds);

    /**
     * Restore the located object from packed one
     *
     * @param packed
     *            The packed object to restore
     * @return The restored object
     */
    protected abstract L restore(Packed packed);
}
