package org.openstreetmap.atlas.geography.atlas.items.complex;

import java.util.function.Consumer;

import org.openstreetmap.atlas.geography.atlas.Atlas;

import com.google.common.collect.Iterables;

/**
 * @author matthieun
 * @author cstaylor
 * @param <T>
 *            the type of ComplexEntity we'll be searching for
 */
public interface Finder<T extends ComplexEntity>
{
    /**
     * Helper method that can be used when searching a Finder so we skip any bad entities
     *
     * @param badEntity
     *            the entity considered bad by the finder implementation
     * @param <T>
     *            the type of bad complex entity we want to ignore
     */
    static <T> void ignore(final T badEntity)
    {

    }

    /**
     * @param atlas
     *            The {@link Atlas} to browse.
     * @return The simple entities first, then the complex ones.
     */
    Iterable<T> find(Atlas atlas);

    /**
     * Automatically filters out invalid complex entities and passes them to the badEntityConsumer
     * for further processing
     *
     * @param atlas
     *            the atlas we're searching
     * @param badEntityConsumer
     *            the consumer receiving each invalid complex entity
     * @return an iterable of only valid complex entities
     */
    default Iterable<T> find(final Atlas atlas, final Consumer<T> badEntityConsumer)
    {
        return Iterables.filter(find(atlas), entity ->
        {
            final boolean wasEntityValid = entity.isValid();
            if (!wasEntityValid)
            {
                badEntityConsumer.accept(entity);
            }
            return wasEntityValid;
        });
    }
}
