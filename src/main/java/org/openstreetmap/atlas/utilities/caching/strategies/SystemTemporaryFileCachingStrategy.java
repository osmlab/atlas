package org.openstreetmap.atlas.utilities.caching.strategies;

import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.caching.ResourceFetchFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caching strategy that attempts to cache a resource in a temporary file at a system-defined
 * location.
 *
 * @author lcram
 */
public class SystemTemporaryFileCachingStrategy extends AbstractCachingStrategy
{
    private static final Logger logger = LoggerFactory
            .getLogger(SystemTemporaryFileCachingStrategy.class);
    private static final String PROPERTY_LOCAL_TEMPORARY_DIRECTORY = "java.io.tmpdir";
    private static final String TEMPORARY_DIRECTORY_STRING = System
            .getProperty(PROPERTY_LOCAL_TEMPORARY_DIRECTORY);

    @Override
    public Optional<Resource> attemptFetch(final URI resourceURI,
            final ResourceFetchFunction defaultFetcher)
    {
        if (TEMPORARY_DIRECTORY_STRING == null)
        {
            logger.error("Failed to read property {}, skipping cache fetch...",
                    PROPERTY_LOCAL_TEMPORARY_DIRECTORY);
            return Optional.empty();
        }

        if (resourceURI == null)
        {
            logger.warn("resourceURI was null, skipping cache fetch...");
            return Optional.empty();
        }

        final Path temporaryDirectory = Paths.get(TEMPORARY_DIRECTORY_STRING);
        final String cachedFileName = this.getUUIDForResourceURI(resourceURI).toString();
        final Path cachedFilePath = Paths.get(temporaryDirectory.toString(), cachedFileName);

        final File cachedFile = new File(cachedFilePath.toString());
        attemptToCacheFileLocally(cachedFile, defaultFetcher, resourceURI);

        // cache hit!
        if (cachedFile.exists())
        {
            logger.info("Cache hit on resource {}, returning local copy", resourceURI);
            return Optional.of(cachedFile);
        }
        else
        {
            logger.warn("Unexpected cache miss on resource {}", resourceURI);
            return Optional.empty();
        }
    }

    @Override
    public String getName()
    {
        return "SystemTemporaryFileCachingStrategy";
    }

    private void attemptToCacheFileLocally(final File cachedFile,
            final ResourceFetchFunction defaultFetcher, final URI resourceURI)
    {
        if (!cachedFile.exists())
        {
            logger.info("Attempting to cache resource {} in temporary file {}", resourceURI,
                    cachedFile.toString());

            final Resource resourceFromDefaultFetcher = defaultFetcher.fetch(resourceURI);
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
}
