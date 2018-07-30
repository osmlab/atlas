package org.openstreetmap.atlas.utilities.caching;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The abstract resource caching helper implementation. This resource caching helper loads a
 * resource using a default {@link ResourceFetchFunction}. It will then cache the resource locally
 * upon fetch, so that a subsequent fetches will leverage the local cache instead of using the
 * default fetcher.
 *
 * @author lcram
 */
public abstract class AbstractResourceCachingHelper
{
    private static final String PROPERTY_LOCAL_TEMPORARY_DIRECTORY = "java.io.tmpdir";
    private static final Logger logger = LoggerFactory
            .getLogger(AbstractResourceCachingHelper.class);

    private boolean cachingEnabled;
    private ResourceFetchFunction defaultFetcher;
    private String pathToResource;

    /*
     * Cache the UUIDs so we only have to compute them once. Caching them in a comparatively small
     * map is significantly faster than recomputing them every time.
     */
    private final Map<String, UUID> resourceToUUIDCache;

    /**
     * Create a new helper. Caching is enabled by default.
     */
    public AbstractResourceCachingHelper()
    {
        this(null);
    }

    /**
     * Create a new helper with the given fetcher. Caching is enabled by default.
     *
     * @param fetcher
     *            the default fetcher
     */
    public AbstractResourceCachingHelper(final ResourceFetchFunction fetcher)
    {
        this.cachingEnabled = true;
        this.defaultFetcher = fetcher;
        this.pathToResource = null;
        this.resourceToUUIDCache = new HashMap<>();
    }

    /**
     * Turn local caching off for this helper.
     */
    public void disableLocalCaching()
    {
        this.cachingEnabled = false;
    }

    /**
     * Turn local caching on for this helper.
     */
    public void enableLocalCaching()
    {
        this.cachingEnabled = true;
    }

    /**
     * Get the path to the currently selected resource.
     *
     * @return the resource path
     */
    public String getPathToResource()
    {
        return this.pathToResource;
    }

    /**
     * Implementations of {@link AbstractResourceCachingHelper} must override this method to define
     * functionality.
     *
     * @return the {@link Resource} that corresponds with this helper's configuration
     */
    public abstract Optional<Resource> getResource();

    /**
     * Set the default fetcher for this caching helper.
     *
     * @param fetcher
     *            the desired resource fetcher
     * @return the configured {@link AbstractResourceCachingHelper}
     */
    public AbstractResourceCachingHelper withFetcher(final ResourceFetchFunction fetcher)
    {
        this.defaultFetcher = fetcher;
        return this;
    }

    /**
     * Set the path to the resource the helper should fetch.
     *
     * @param pathToResource
     *            the path to the desired resource
     * @return the configured {@link AbstractResourceCachingHelper}
     */
    public AbstractResourceCachingHelper withPathToResource(final String pathToResource)
    {
        this.pathToResource = pathToResource;
        return this;
    }

    protected Optional<Resource> getResourceWithCachingStrategy()
    {
        if (this.pathToResource == null)
        {
            throw new CoreException("Cannot fetch resource without a supplied path.");
        }

        final String temporaryDirectoryString = System
                .getProperty(PROPERTY_LOCAL_TEMPORARY_DIRECTORY);

        if (!this.cachingEnabled || temporaryDirectoryString == null)
        {
            if (this.cachingEnabled)
            {
                logger.warn(
                        "Caching was enabled but could not get property {}, falling back on default fetcher...",
                        PROPERTY_LOCAL_TEMPORARY_DIRECTORY);
            }
            return Optional.of(getResourceDirectly());
        }

        final Path temporaryDirectory = Paths.get(temporaryDirectoryString);
        final String cachedFileName = getResourceUUID(this.pathToResource).toString();
        final Path cachedFilePath = Paths.get(temporaryDirectory.toString(), cachedFileName);

        final File cachedFile = new File(cachedFilePath.toString());
        attemptToCacheFileLocally(cachedFile);

        // cache hit!
        if (cachedFile.exists())
        {
            logger.info("Cache hit on resource {}, returning local copy", this.pathToResource);
            return Optional.of(cachedFile);
        }
        else
        {
            logger.info("Cache miss on resource {}, falling back on default fetcher",
                    this.pathToResource);
            return Optional.of(getResourceDirectly());
        }
    }

    private void attemptToCacheFileLocally(final File cachedFile)
    {
        if (!cachedFile.exists())
        {
            logger.info("Attempting to write cache file {}", cachedFile.toString());

            final Resource resourceFromDefaultFetcher = this.defaultFetcher.fetch();
            final File temporaryLocalFile = File.temporary();
            try
            {
                resourceFromDefaultFetcher.copyTo(temporaryLocalFile);
            }
            catch (final Exception exception)
            {
                logger.error("Something went wrong copying {} to temporary local file {}",
                        resourceFromDefaultFetcher.toString(), temporaryLocalFile.toString());
                return;
            }

            // now that we have pulled down the file to a unique temporary location, attempt to
            // atomically move it to the cache after re-checking for existence
            if (!cachedFile.exists())
            {
                try
                {
                    final Path temporaryLocalFilePath = Paths.get(temporaryLocalFile.getPath());
                    final Path cachedFilePath = Paths.get(cachedFile.getPath());
                    Files.move(temporaryLocalFilePath, cachedFilePath,
                            StandardCopyOption.ATOMIC_MOVE);
                }
                catch (final FileAlreadyExistsException exception)
                {
                    logger.info("File {} is already cached", cachedFile.toString());
                    return;
                }
                catch (final Exception exception)
                {
                    logger.error("Something went wrong moving {} to {}",
                            temporaryLocalFile.toString(), cachedFile.toString());
                }
            }
        }
    }

    /*
     * Use the default fetcher to get the resource directly, without caching.
     */
    private Resource getResourceDirectly()
    {
        if (this.defaultFetcher == null)
        {
            throw new CoreException("Cannot fetch resource without a default fetcher.");
        }
        return this.defaultFetcher.fetch();
    }

    private UUID getResourceUUID(final String pathToResource)
    {
        if (!this.resourceToUUIDCache.containsKey(pathToResource))
        {
            final UUID newUUID = UUID.nameUUIDFromBytes(pathToResource.getBytes());
            this.resourceToUUIDCache.put(pathToResource, newUUID);
            return newUUID;
        }
        else
        {
            return this.resourceToUUIDCache.get(pathToResource);
        }
    }
}
