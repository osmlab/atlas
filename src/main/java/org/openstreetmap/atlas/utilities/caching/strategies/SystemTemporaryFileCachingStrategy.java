package org.openstreetmap.atlas.utilities.caching.strategies;

import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.function.Function;

import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caching strategy that attempts to cache a resource in a temporary file at a system-defined
 * location.
 *
 * @author lcram
 * @author matthieun
 * @author mgostintsev
 */
public class SystemTemporaryFileCachingStrategy extends AbstractCachingStrategy
{
    private static final Logger logger = LoggerFactory
            .getLogger(SystemTemporaryFileCachingStrategy.class);
    private static final String FILE_EXTENSION_DOT = ".";
    private static final String PROPERTY_LOCAL_TEMPORARY_DIRECTORY = "java.io.tmpdir";
    private static final String TEMPORARY_DIRECTORY_STRING = System
            .getProperty(PROPERTY_LOCAL_TEMPORARY_DIRECTORY);

    @Override
    public Optional<Resource> attemptFetch(final URI resourceURI,
            final Function<URI, Optional<Resource>> defaultFetcher)
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
        final Optional<String> resourceExtension = getFileExtensionFromURI(resourceURI);
        final String cachedFileName;
        if (resourceExtension.isPresent())
        {
            cachedFileName = this.getUUIDForResourceURI(resourceURI).toString() + FILE_EXTENSION_DOT
                    + resourceExtension.get();
        }
        else
        {
            cachedFileName = this.getUUIDForResourceURI(resourceURI).toString();
        }
        final Path cachedFilePath = Paths.get(temporaryDirectory.toString(), cachedFileName);

        final File cachedFile = new File(cachedFilePath.toString());
        attemptToCacheFileLocally(cachedFile, defaultFetcher, resourceURI);

        if (cachedFile.exists())
        {
            logger.trace("Returning local copy of resource {}", resourceURI);
            return Optional.of(cachedFile);
        }

        // If we got here, something went wrong in attemptToCacheFileLocally().
        logger.warn("Could not find local copy of resource {}", resourceURI);
        return Optional.empty();
    }

    @Override
    public String getName()
    {
        return "SystemTemporaryFileCachingStrategy";
    }

    @Override
    public void invalidate()
    {
        throw new UnsupportedOperationException("Operation not supported at this time.");
    }

    @Override
    public void invalidate(final URI uri)
    {
        throw new UnsupportedOperationException("Operation not supported at this time.");
    }

    private void attemptToCacheFileLocally(final File cachedFile,
            final Function<URI, Optional<Resource>> defaultFetcher, final URI resourceURI)
    {
        if (!cachedFile.exists())
        {
            logger.trace("Attempting to cache resource {} in temporary file {}", resourceURI,
                    cachedFile.toString());

            final Optional<Resource> resourceFromDefaultFetcher = defaultFetcher.apply(resourceURI);
            if (!resourceFromDefaultFetcher.isPresent())
            {
                logger.warn("Application of default fetcher for {} returned empty Optional!",
                        resourceURI);
                return;
            }

            final File temporaryLocalFile = File.temporary();
            try
            {
                resourceFromDefaultFetcher.get().copyTo(temporaryLocalFile);
            }
            catch (final Exception exception)
            {
                logger.error("Something went wrong copying {} to temporary local file {}",
                        resourceFromDefaultFetcher.toString(), temporaryLocalFile.toString(),
                        exception);
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
                    logger.trace("File {} is already cached", cachedFile.toString());
                    return;
                }
                catch (final Exception exception)
                {
                    logger.error("Something went wrong moving {} to {}",
                            temporaryLocalFile.toString(), cachedFile.toString(), exception);
                }
            }
        }
    }

    private Optional<String> getFileExtensionFromURI(final URI resourceURI)
    {
        final String asciiString = resourceURI.toASCIIString();
        final int lastIndexOfDot = asciiString.lastIndexOf(FILE_EXTENSION_DOT);

        if (lastIndexOfDot < 0)
        {
            return Optional.empty();
        }

        final String extension = asciiString.substring(lastIndexOfDot + 1);
        if (extension.isEmpty())
        {
            return Optional.empty();
        }
        else
        {
            return Optional.of(extension);
        }
    }
}
