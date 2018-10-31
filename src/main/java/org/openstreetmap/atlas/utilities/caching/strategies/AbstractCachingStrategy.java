package org.openstreetmap.atlas.utilities.caching.strategies;

import java.net.URI;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An incomplete implementation of the {@link CachingStrategy} interface. Provides some additional
 * functionality for subclasses to leverage.
 *
 * @author lcram
 */
public abstract class AbstractCachingStrategy implements CachingStrategy
{
    private static final Logger logger = LoggerFactory.getLogger(AbstractCachingStrategy.class);

    /*
     * Cache the UUIDs for each URI so we only have to compute them once. Caching them in a
     * comparatively small map is significantly faster than recomputing them every time. Subclasses
     * may want to use this cache to associate a UUID with a given URI.
     */
    private final Map<String, UUID> uriStringToUUIDCache;

    private final UUID strategyID;

    public AbstractCachingStrategy()
    {
        this.uriStringToUUIDCache = new ConcurrentHashMap<>();
        this.strategyID = UUID.randomUUID();
        logger.info("Initialized strategy {} with ID {}", this.getClass().getName(),
                this.strategyID);
    }

    /**
     * Get a {@link UUID} for this strategy instance. This is useful for logging.
     *
     * @return The strategy instance {@link UUID}
     */
    protected UUID getStrategyID()
    {
        return this.strategyID;
    }

    /**
     * Given a URI, compute a universally unique identifier ({@link UUID}) for that URI. This method
     * uses the {@link String} representation of a URI to compute the UUID. It will also cache
     * computed UUIDs, so subsequent fetches will not incur a re-computation performance penalty.
     *
     * @param resourceURI
     *            the {@link URI}
     * @return the {@link UUID} of the given {@link URI}
     */
    protected UUID getUUIDForResourceURI(final URI resourceURI)
    {
        final String uriString = resourceURI.toString();

        if (!this.uriStringToUUIDCache.containsKey(uriString))
        {
            // As of Java 8, this method computes the MD5 sum of the URI string.
            // This can be relatively slow (10 digests per 1 ms), so we will cache the result in
            // memory for subsequent requests.
            final UUID newUUID = UUID.nameUUIDFromBytes(uriString.getBytes());
            this.uriStringToUUIDCache.put(uriString, newUUID);
            return newUUID;
        }
        else
        {
            return this.uriStringToUUIDCache.get(uriString);
        }
    }
}
