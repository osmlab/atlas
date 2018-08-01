package org.openstreetmap.atlas.utilities.caching;

import java.net.URI;
import java.util.Optional;
import java.util.function.Function;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.caching.strategies.CachingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The basic resource cache implementation. This cache loads a resource using a given {@link URI},
 * {@link CachingStrategy}, and default {@link ResourceFetchFunction}. Since using raw URIs can
 * often be cumbersome, users of this class are encouraged to extend it and encapsulate URI
 * construction in appropriate subclass helpers.
 *
 * @author lcram
 */
public class ResourceCache
{
    private static final Logger logger = LoggerFactory.getLogger(ResourceCache.class);

    private URI resourceURI;
    private CachingStrategy cachingStrategy;
    private Function<URI, Resource> defaultFetcher;

    /**
     * Create a new helper. Caching is enabled by default.
     */
    public ResourceCache()
    {
        this(null, null, null);
    }

    /**
     * Create a new helper with the given fetcher and strategy.
     *
     * @param resourceURI
     *            the {@link URI} of the desired {@link Resource}
     * @param cachingStrategy
     *            the caching strategy
     * @param fetcher
     *            the default fetcher
     */
    public ResourceCache(final URI resourceURI, final CachingStrategy cachingStrategy,
            final Function<URI, Resource> fetcher)
    {
        this.resourceURI = resourceURI;
        this.cachingStrategy = cachingStrategy;
        this.defaultFetcher = fetcher;
    }

    /**
     * Attempt to get the current resource using the current caching strategy.
     *
     * @return an {@link Optional} wrapping the {@link Resource}
     */
    public Optional<Resource> getResource()
    {
        throwCoreExceptionIfResourceURIWasNull();

        if (this.cachingStrategy == null)
        {
            throw new CoreException("Could not get resource. cachingStrategy was null");
        }

        if (this.defaultFetcher == null)
        {
            throw new CoreException("Could not get resource. defaultFetcher was null");
        }

        Optional<Resource> cachedResource = this.cachingStrategy.attemptFetch(this.resourceURI,
                this.defaultFetcher);

        if (!cachedResource.isPresent())
        {
            logger.warn("Cache fetch failed, falling back to default fetcher...");
            cachedResource = Optional.of(this.getResourceDirectly());
        }

        return cachedResource;
    }

    /**
     * Attempt to get the given resource using the current caching strategy.
     *
     * @param resourceURI
     *            the {@link URI} of the resource to fetch
     * @return an {@link Optional} wrapping the {@link Resource}
     */
    public Optional<Resource> getResource(final URI resourceURI)
    {
        this.setResourceURI(resourceURI);
        return this.getResource();
    }

    /**
     * Get the path to the currently selected resource.
     *
     * @return the resource {@link URI}
     */
    public URI getResourceURI()
    {
        return this.resourceURI;
    }

    /**
     * Set the caching strategy for this caching helper.
     *
     * @param strategy
     *            the caching strategy
     * @return the configured {@link ResourceCache}
     */
    public ResourceCache withCachingStrategy(final CachingStrategy strategy)
    {
        this.cachingStrategy = strategy;
        return this;
    }

    /**
     * Set the default fetcher for this caching helper.
     *
     * @param fetcher
     *            the desired resource fetcher
     * @return the configured {@link ResourceCache}
     */
    public ResourceCache withFetcher(final Function<URI, Resource> fetcher)
    {
        this.defaultFetcher = fetcher;
        return this;
    }

    /**
     * Set the URI by passing in a URI string.
     *
     * @param stringURI
     *            the {@link URI} in string format
     * @return the configured {@link ResourceCache}
     */
    public ResourceCache withResourceString(final String stringURI)
    {
        this.resourceURI = URI.create(stringURI);
        return this;
    }

    /**
     * Set the URI of the desired resource.
     *
     * @param resourceURI
     *            the URI of the desired resource
     * @return the configured {@link ResourceCache}
     */
    public ResourceCache withResourceURI(final URI resourceURI)
    {
        this.resourceURI = resourceURI;
        return this;
    }

    /*
     * This method can be used by subclasses to provide their own custom implementations of the
     * "with" builder functions.
     */
    protected void setCachingStrategy(final CachingStrategy strategy)
    {
        this.cachingStrategy = strategy;
    }

    /*
     * This method can be used by subclasses to provide their own custom implementations of the
     * "with" builder functions.
     */
    protected void setDefaultFetcher(final Function<URI, Resource> fetcher)
    {
        this.defaultFetcher = fetcher;
    }

    /*
     * This method can be used by subclasses to provide their own custom implementations of the
     * "with" builder functions.
     */
    protected void setResourceURI(final URI newURI)
    {
        this.resourceURI = newURI;
    }

    /*
     * Use the default fetcher to get the resource directly, without caching.
     */
    private Resource getResourceDirectly()
    {
        if (this.defaultFetcher == null)
        {
            throw new CoreException(
                    "defaultFetcher was null. Cannot fetch resource without a default fetcher.");
        }
        throwCoreExceptionIfResourceURIWasNull();
        return this.defaultFetcher.apply(this.resourceURI);
    }

    private void throwCoreExceptionIfResourceURIWasNull()
    {
        if (this.resourceURI == null)
        {
            throw new CoreException("resourceURI was null. Cannot fetch resource without a URI.");
        }
    }
}
