package org.openstreetmap.atlas.utilities.caching.strategies;

import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.AbstractResource;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.caching.ConcurrentResourceCache;
import org.openstreetmap.atlas.utilities.runtime.Retry;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Caching strategy that attempts to cache a {@link Resource} within a user-defined namespace at the
 * standard system temporary location. It should be noted that this strategy has no inherent
 * concurrency safety. Since the namespaces are implemented as directories in the underlying
 * filesystem, two {@link NamespaceCachingStrategy} objects with the same namespace can possibly
 * step on each other's toes if used improperly. It is up to the users of the strategy to prevent
 * concurrent access to {@link NamespaceCachingStrategy} objects that share a namespace. One way to
 * ensure concurrency safety is to carefully associate a given namespace (and its
 * {@link NamespaceCachingStrategy}) with exactly one {@link ConcurrentResourceCache} object
 * throughout your code, and stick to this restriction consistently.
 *
 * @author lcram
 */
public class NamespaceCachingStrategy extends AbstractCachingStrategy
{
    private static final Logger logger = LoggerFactory.getLogger(NamespaceCachingStrategy.class);
    private static final String FILE_EXTENSION_DOT = ".";
    private static final String PROPERTY_LOCAL_TEMPORARY_DIRECTORY = "java.io.tmpdir";
    private static final String TEMPORARY_DIRECTORY_STRING = System
            .getProperty(PROPERTY_LOCAL_TEMPORARY_DIRECTORY);
    private static final int RETRY_NUMBER = 5;
    private static final Retry RETRY = new Retry(RETRY_NUMBER, Duration.ONE_SECOND)
            .withQuadratic(true);

    private final String namespace;
    private boolean preserveFileExtension;
    private final FileSystem fileSystem;

    public NamespaceCachingStrategy(final String namespace)
    {
        this(namespace, FileSystems.getDefault());
    }

    public NamespaceCachingStrategy(final String namespace, final FileSystem fileSystem)
    {
        super();
        if (namespace.contains("/") || namespace.contains("\\"))
        {
            throw new IllegalArgumentException(
                    "The namespace cannot contain characters '\\' or '/'");
        }
        this.namespace = this.getName() + "_" + namespace + "_"
                + UUID.nameUUIDFromBytes(namespace.getBytes()).toString();
        this.preserveFileExtension = true;
        this.fileSystem = fileSystem;
    }

    @Override
    public Optional<Resource> attemptFetch(final URI resourceURI,
            final Function<URI, Optional<Resource>> defaultFetcher)
    {
        if (TEMPORARY_DIRECTORY_STRING == null)
        {
            logger.error("StrategyID {}: failed to read property {}, skipping cache fetch...",
                    this.getStrategyID(), PROPERTY_LOCAL_TEMPORARY_DIRECTORY);
            return Optional.empty();
        }

        if (resourceURI == null)
        {
            logger.warn("StrategyID {}: resourceURI was null, skipping cache fetch...",
                    this.getStrategyID());
            return Optional.empty();
        }

        final File cachedFile = getCachedFile(resourceURI);
        attemptToCacheFileLocally(cachedFile, defaultFetcher, resourceURI);

        if (cachedFile.exists())
        {
            logger.trace("StrategyID {}: returning local copy of resource {}", this.getStrategyID(),
                    resourceURI);
            return Optional.of(cachedFile);
        }

        // If we got here, something went wrong in attemptToCacheFileLocally().
        logger.warn("StrategyID {}: could not find local copy of resource {}", this.getStrategyID(),
                resourceURI);
        return Optional.empty();
    }

    @Override
    public String getName()
    {
        return "NamespaceCachingStrategy";
    }

    @Override
    public void invalidate()
    {
        final Path storageDirectory = this.getStorageDirectory();
        try
        {
            new File(storageDirectory.toString(), this.fileSystem).deleteRecursively();
        }
        catch (final Exception exception)
        {
            logger.warn("StrategyID {}: invalidate failed due to {}", this.getStrategyID(),
                    exception.getClass().getName(), exception);
        }
    }

    @Override
    public void invalidate(final URI resourceURI)
    {
        try
        {
            getCachedFile(resourceURI).delete();
        }
        catch (final Exception exception)
        {
            logger.warn("StrategyID {}: invalidate of resource {} failed due to {}",
                    this.getStrategyID(), resourceURI, exception.getClass().getName(), exception);
        }
    }

    /**
     * Preserve the file extension of the cached URI when saving it as a file to the temporary
     * location. For example, if the URI of the resource was "hdfs://foo/bar/baz.txt", then after
     * computing the hash of the URI, {@link NamespaceCachingStrategy} will append a '.txt'
     * extension to the filename. This is useful for e.g. in cases where resource loading code may
     * be looking for specific file extensions in order to decide between various load strategies.
     * 
     * @param preserveFileExtension
     *            if true, preserve the original extension
     * @return this instance for chaining
     */
    public NamespaceCachingStrategy withFileExtensionPreservation(
            final boolean preserveFileExtension)
    {
        this.preserveFileExtension = preserveFileExtension;
        return this;
    }

    protected void validateLocalFile(final File localFile)
    {
        // Do nothing here, leave to extensions to decide.
    }

    String getNamespace()
    {
        return this.namespace;
    }

    private void attemptToCacheFileLocally(final File cachedFile,
            final Function<URI, Optional<Resource>> defaultFetcher, final URI resourceURI)
    {
        if (!cachedFile.exists())
        {
            logger.trace("StrategyID {}: attempting to cache resource {} in temporary file {}",
                    this.getStrategyID(), resourceURI, cachedFile);

            final Optional<Resource> resourceFromDefaultFetcher = defaultFetcher.apply(resourceURI);
            if (resourceFromDefaultFetcher.isEmpty())
            {
                logger.warn(
                        "StrategyID {}: application of default fetcher for {} returned empty Optional!",
                        this.getStrategyID(), resourceURI);
                return;
            }

            final File temporaryLocalFile = File.temporary(this.fileSystem);
            RETRY.run(() ->
            {
                try
                {
                    /*
                     * We have to explicitly set the decompressor here. Why? Because if the resource
                     * ends with a '.gz' extension, the 'copyTo' method will apply GZIP
                     * decompression to it. The problem? When the user goes to fetch the contents of
                     * the cached copy, it will still have the '.gz' extension but it will now be
                     * decompressed. So our automatic decompression code will run on an uncompressed
                     * file! This will cause the contents fetch to fail since Java's GZIPInputStream
                     * won't be able to find the GZIP magic number!
                     */
                    final AbstractResource abstractResource = (AbstractResource) resourceFromDefaultFetcher
                            .get();
                    abstractResource.setDecompressor(Decompressor.NONE);
                    abstractResource.copyTo(temporaryLocalFile);
                    validateLocalFile(temporaryLocalFile);
                }
                catch (final Exception exception)
                {
                    throw new CoreException(
                            "StrategyID {}: something went wrong copying {} to temporary local file {}",
                            this.getStrategyID(), resourceFromDefaultFetcher, temporaryLocalFile,
                            exception);
                }
            });

            // now that we have pulled down the file to a unique temporary location, attempt to
            // atomically move it to the cache after re-checking for existence
            if (!cachedFile.exists())
            {
                try
                {
                    final Path temporaryLocalFilePath = this.fileSystem
                            .getPath(temporaryLocalFile.getPathString());
                    final Path cachedFilePath = this.fileSystem.getPath(cachedFile.getPathString());
                    Files.move(temporaryLocalFilePath, cachedFilePath,
                            StandardCopyOption.ATOMIC_MOVE);
                    validateLocalFile(cachedFile);
                }
                catch (final FileAlreadyExistsException exception)
                {
                    logger.trace("StrategyID {}: file {} is already cached", this.getStrategyID(),
                            cachedFile);
                }
                catch (final Exception exception)
                {
                    throw new CoreException("StrategyID {}: something went wrong moving {} to {}",
                            this.getStrategyID(), temporaryLocalFile, cachedFile, exception);
                }
            }
        }
    }

    private File getCachedFile(final URI resourceURI)
    {
        final Path storageDirectory = getStorageDirectory();
        final Optional<String> resourceExtensionOptional = getFileExtensionFromURI(resourceURI);
        final String cachedFileName;
        cachedFileName = resourceExtensionOptional
                .map(extension -> this.getUUIDForResourceURI(resourceURI).toString()
                        + FILE_EXTENSION_DOT + extension)
                .orElseGet(() -> this.getUUIDForResourceURI(resourceURI).toString());
        final Path cachedFilePath = this.fileSystem.getPath(storageDirectory.toString(),
                cachedFileName);

        return new File(cachedFilePath.toString(), this.fileSystem);
    }

    private Optional<String> getFileExtensionFromURI(final URI resourceURI)
    {
        if (!this.preserveFileExtension)
        {
            return Optional.empty();
        }

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

    private Path getStorageDirectory()
    {
        return this.fileSystem.getPath(TEMPORARY_DIRECTORY_STRING, this.namespace);
    }
}
