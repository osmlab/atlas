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
            final Function<URI, Optional<Resource>> defaultFetcher)
    {
        final UUID resourceUUID = this.getUUIDForResourceURI(resourceURI);

        if (!this.resourceCache.containsKey(resourceUUID))
        {
            logger.trace(
                    "StrategyID {}: attempting to cache resource {} in byte array keyed on UUID {}",
                    this.getStrategyID(), resourceURI, resourceUUID);

            final Optional<Resource> resource = defaultFetcher.apply(resourceURI);
            if (resource.isEmpty())
            {
                logger.warn(
                        "StrategyID {}: application of default fetcher for {} returned empty Optional!",
                        this.getStrategyID(), resourceURI);
                return Optional.empty();
            }

            final ByteArrayResource resourceBytes;
            if (this.useExactResourceSize)
            {
                final long resourceLength = resource.get().length();
                logger.trace("StrategyID {}: using exact resource length {}", this.getStrategyID(),
                        resourceLength);
                resourceBytes = new ByteArrayResource(resourceLength);
            }
            else
            {
                logger.trace("StrategyID {}: using initial array size {}", this.getStrategyID(),
                        this.initialArraySize);
                resourceBytes = new ByteArrayResource(this.initialArraySize);
            }
            resourceBytes.writeAndClose(resource.get().readBytesAndClose());
            this.resourceCache.put(resourceUUID, resourceBytes);
        }
        logger.trace("StrategyID {}: returning cached resource {} from byte array keyed on UUID {}",
                this.getStrategyID(), resourceURI, resourceUUID);

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

    @Override
    public void invalidate(final URI resourceURI)
    {
        final UUID resourceUUID = this.getUUIDForResourceURI(resourceURI);
        this.resourceCache.remove(resourceUUID);
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
