package org.openstreetmap.atlas.utilities.caching.strategies;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.caching.ResourceFetchFunction;

/**
 * Caching strategy that attempts to cache a {@link Resource} in a byte array, in memory.
 *
 * @author lcram
 */
public class ByteArrayCachingStrategy extends AbstractCachingStrategy
{
    private final Map<UUID, ByteArrayResource> resourceCache;
    private long initialArraySize;
    public boolean useExactResourceSize;

    public ByteArrayCachingStrategy()
    {
        this.resourceCache = new HashMap<>();
        this.initialArraySize = Long.MAX_VALUE;
        this.useExactResourceSize = false;
    }

    @Override
    public Optional<Resource> attemptFetch(final URI resourceURI,
            final ResourceFetchFunction defaultFetcher)
    {
        final UUID resourceUUID = this.getUUIDForResourceURI(resourceURI);

        if (!this.resourceCache.containsKey(resourceUUID))
        {
            final Resource resource = defaultFetcher.fetch(resourceURI);
            final ByteArrayResource resourceBytes;
            if (this.useExactResourceSize)
            {
                resourceBytes = new ByteArrayResource(resource.length());
            }
            else
            {
                resourceBytes = new ByteArrayResource(this.initialArraySize);
            }
            resourceBytes.writeAndClose(resource.readBytesAndClose());
            this.resourceCache.put(resourceUUID, resourceBytes);
        }

        return Optional.of(this.resourceCache.get(resourceUUID));
    }

    /**
     * Use the exact resource size of the byte arrays of the cache. This may cause performance
     * degradation on cache misses, since some resources do not store their length as metadata.
     *
     * @return the configured {@link ByteArrayCachingStrategy}
     */
    public ByteArrayCachingStrategy useExactResourceSize()
    {
        this.useExactResourceSize = true;
        return this;
    }

    /**
     * Set an initial array size for the byte arrays of the cache.
     *
     * @param initialSize
     *            the initial size
     * @return the configured {@link ByteArrayCachingStrategy}
     */
    public ByteArrayCachingStrategy withInitialArraySize(final long initialSize)
    {
        this.initialArraySize = initialSize;
        return this;
    }
}
