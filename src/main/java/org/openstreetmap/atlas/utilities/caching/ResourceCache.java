package org.openstreetmap.atlas.utilities.caching;

import java.net.URI;
import java.util.Optional;

import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.caching.strategies.CachingStrategy;

/**
 * All {@link ResourceCache}s must conform to this interface. Of particular interest is the thread
 * safe implementation of this interface, the {@link ConcurrentResourceCache}.
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
    Optional<Resource> get(URI resourceURI);

    /**
     * Invalidate the contents of this cache. Generally, this method should rely on the
     * {@link CachingStrategy#invalidate} implementation of the underlying strategy. However this is
     * not enforced by the interface. since some implementations may need to do extra housekeeping
     * to perform an invalidation. See {@link ConcurrentResourceCache} for an example.
     */
    void invalidate();

    /**
     * Invalidate the cached {@link Resource} for a given {@link URI}, if it exists.
     *
     * @param resourceURI
     *            The {@link URI} of the {@link Resource} to invalidate
     */
    void invalidate(URI resourceURI);
}
