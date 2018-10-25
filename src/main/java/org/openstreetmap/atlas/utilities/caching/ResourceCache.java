package org.openstreetmap.atlas.utilities.caching;

import java.net.URI;
import java.util.Optional;

import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.caching.strategies.CachingStrategy;

/**
 * All {@link ResourceCache}s must conform to this interface. Of particular interest is the
 * threadsafe implementation of this interface, the {@link ConcurrentResourceCache}.
 *
 * @author lcram
 */
public interface ResourceCache
{
    /**
     * Attempt to get the resource specified by the given string URI.
     *
     * @param resourceURIString
     *            the resource {@link URI} as a {@link String}
     * @return an {@link Optional} wrapping the {@link Resource}
     */
    default Optional<Resource> get(final String resourceURIString)
    {
        return this.get(URI.create(resourceURIString));
    }

    /**
     * Attempt to get the resource specified by the given URI.
     *
     * @param resourceURI
     *            the resource {@link URI}
     * @return an {@link Optional} wrapping the {@link Resource}
     */
    Optional<Resource> get(final URI resourceURI);

    /**
     * Get the {@link CachingStrategy} object in use by this cache.
     * 
     * @return the {@link CachingStrategy}
     */
    CachingStrategy getCachingStrategy();

    /**
     * Invalidate the underlying caching strategy.
     */
    default void invalidate()
    {
        this.getCachingStrategy().invalidate();
    }
}
