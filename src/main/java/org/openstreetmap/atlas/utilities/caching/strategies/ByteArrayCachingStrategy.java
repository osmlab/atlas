package org.openstreetmap.atlas.utilities.caching.strategies;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caching strategy that attempts to cache a {@link Resource} in a byte array, in memory.
 *
 * @author lcram
 */
public class ByteArrayCachingStrategy extends AbstractCachingStrategy
{
    /*
     * Default size is arbitrarily set to 2 MiB
     */
    private static final long DEFAULT_BYTE_ARRAY_SIZE = 1024L * 1024 * 2;
    private static final Logger logger = LoggerFactory.getLogger(ByteArrayCachingStrategy.class);

    private final Map<UUID, ByteArrayResource> resourceCache;
    private long initialArraySize;
    private boolean useExactResourceSize;

    public ByteArrayCachingStrategy()
    {
        this.resourceCache = new HashMap<>();
        this.initialArraySize = DEFAULT_BYTE_ARRAY_SIZE;
        this.useExactResourceSize = false;
    }

    @Override
    public Optional<Resource> attemptFetch(final URI resourceURI,
            final Function<URI, Resource> defaultFetcher)
    {
        final UUID resourceUUID = this.getUUIDForResourceURI(resourceURI);

        if (!this.resourceCache.containsKey(resourceUUID))
        {
            logger.info("Attempting to cache resource {} in byte array keyed on UUID {}",
                    resourceURI, resourceUUID.toString());
            final Resource resource = defaultFetcher.apply(resourceURI);
            final ByteArrayResource resourceBytes;
            if (this.useExactResourceSize)
            {
                final long resourceLength = resource.length();
                logger.info("Using extact resource length {}", resourceLength);
                resourceBytes = new ByteArrayResource(resourceLength);
            }
            else
            {
                logger.info("Using initial array size {}", this.initialArraySize);
                resourceBytes = new ByteArrayResource(this.initialArraySize);
            }
            resourceBytes.writeAndClose(resource.readBytesAndClose());
            this.resourceCache.put(resourceUUID, resourceBytes);
        }

        logger.info("Returning cached resource {} from byte array keyed on UUID {}", resourceURI,
                resourceUUID.toString());
        return Optional.of(this.resourceCache.get(resourceUUID));
    }

    @Override
    public String getName()
    {
        return "ByteArrayCachingStrategy";
    }

    @Override
    public void invalidate()
    {
        this.resourceCache.clear();
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
