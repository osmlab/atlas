package org.openstreetmap.atlas.utilities.caching;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.caching.strategies.ByteArrayCachingStrategy;
import org.openstreetmap.atlas.utilities.caching.strategies.CachingStrategy;
import org.openstreetmap.atlas.utilities.caching.strategies.GlobalNamespaceCachingStrategy;
import org.openstreetmap.atlas.utilities.caching.strategies.NamespaceCachingStrategy;
import org.openstreetmap.atlas.utilities.caching.strategies.NoCachingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class CachingTests
{
    private static final Logger logger = LoggerFactory.getLogger(CachingTests.class);

    private static final String FEATURE_JSON = "feature.json";
    private static final String FILE_NO_EXTENSION = "fileNoExt";
    private static final String HELLO_WORLD_GZIP = "hello.txt.gz";

    @Test
    public void testBaseCacheWithByteArrayStrategy()
    {
        testBaseCacheWithGivenStrategy(new ByteArrayCachingStrategy());
    }

    @Test
    public void testBaseCacheWithGlobalNamespaceStrategy()
    {
        testBaseCacheWithGivenStrategy(new GlobalNamespaceCachingStrategy());
    }

    @Test
    public void testBaseCacheWithNoStrategy()
    {
        testBaseCacheWithGivenStrategy(new NoCachingStrategy());
    }

    @Test
    public void testGzippedNamespaceCache()
    {
        final ConcurrentResourceCache resourceCache = new ConcurrentResourceCache(
                new GlobalNamespaceCachingStrategy(), this::fetchLocalFileResource);

        try
        {
            URI resourceUri;
            try
            {
                resourceUri = CachingTests.class.getResource(HELLO_WORLD_GZIP).toURI();
            }
            catch (final URISyntaxException exception)
            {
                logger.error("Bad URI syntax", exception);
                resourceUri = null;
                Assert.fail();
            }

            Assert.assertEquals("hello world", resourceCache.get(resourceUri).get().all());
        }
        finally
        {
            resourceCache.invalidate();
        }
    }

    @Test
    public void testIndividualResourceInvalidation()
    {
        final ConcurrentResourceCache resourceCache = new ConcurrentResourceCache(
                new GlobalNamespaceCachingStrategy(), this::fetchLocalFileResource);

        try
        {
            URI resourceUri;
            try
            {
                resourceUri = CachingTests.class.getResource(FEATURE_JSON).toURI();
            }
            catch (final URISyntaxException exception)
            {
                logger.error("Bad URI syntax", exception);
                resourceUri = null;
                Assert.fail();
            }

            // read the contents of the file
            final ByteArrayResource originalFileBytes = new ByteArrayResource();
            originalFileBytes.copyFrom(new InputStreamResource(
                    () -> CachingTests.class.getResourceAsStream(FEATURE_JSON)));
            final byte[] originalFileBytesArray = originalFileBytes.readBytesAndClose();

            // read contents of the file with cache, this will incur a cache miss
            final ByteArrayResource fileBytesCacheMiss = new ByteArrayResource();
            fileBytesCacheMiss.copyFrom(resourceCache.get(resourceUri).get());
            final byte[] fileBytesCacheMissArray = fileBytesCacheMiss.readBytesAndClose();
            Assert.assertArrayEquals(originalFileBytesArray, fileBytesCacheMissArray);

            // read contents again, this time with a cache hit
            final ByteArrayResource fileBytesCacheHit = new ByteArrayResource();
            fileBytesCacheHit.copyFrom(resourceCache.get(resourceUri).get());
            final byte[] fileBytesCacheHitArray = fileBytesCacheHit.readBytesAndClose();
            Assert.assertArrayEquals(originalFileBytesArray, fileBytesCacheHitArray);

            resourceCache.invalidate(resourceUri);

            // read contents of the file with again cache, this will incur a cache miss
            final ByteArrayResource fileBytesCacheMiss2 = new ByteArrayResource();
            fileBytesCacheMiss2.copyFrom(resourceCache.get(resourceUri).get());
            final byte[] fileBytesCacheMissArray2 = fileBytesCacheMiss2.readBytesAndClose();
            Assert.assertArrayEquals(originalFileBytesArray, fileBytesCacheMissArray2);
        }
        finally
        {
            resourceCache.invalidate();
        }
    }

    @Test
    public void testLocalFileInMemoryCache()
    {
        final LocalFileInMemoryCache cache = new LocalFileInMemoryCache();

        try
        {
            URI resourceUri;
            try
            {
                resourceUri = CachingTests.class.getResource(FEATURE_JSON).toURI();
            }
            catch (final URISyntaxException exception)
            {
                logger.error("Bad URI syntax", exception);
                resourceUri = null;
                Assert.fail();
            }

            // read the contents of the file
            final ByteArrayResource originalFileBytes = new ByteArrayResource();
            originalFileBytes.copyFrom(new InputStreamResource(
                    () -> CachingTests.class.getResourceAsStream(FEATURE_JSON)));
            final byte[] originalFileBytesArray = originalFileBytes.readBytesAndClose();

            // read contents of the file with cache, this will incur a cache miss
            final ByteArrayResource fileBytesCacheMiss = new ByteArrayResource();
            fileBytesCacheMiss.copyFrom(cache.get(resourceUri).get());
            final byte[] fileBytesCacheMissArray = fileBytesCacheMiss.readBytesAndClose();
            Assert.assertArrayEquals(originalFileBytesArray, fileBytesCacheMissArray);

            // read contents again, this time with a cache hit
            final ByteArrayResource fileBytesCacheHit = new ByteArrayResource();
            fileBytesCacheHit.copyFrom(cache.get(resourceUri).get());
            final byte[] fileBytesCacheHitArray = fileBytesCacheHit.readBytesAndClose();
            Assert.assertArrayEquals(originalFileBytesArray, fileBytesCacheHitArray);
        }
        finally
        {
            cache.invalidate();
        }
    }

    @Test
    public void testMultipleNamespaceCaches()
    {
        final ConcurrentResourceCache resourceCache = new ConcurrentResourceCache(
                new NamespaceCachingStrategy("namespace1"), this::fetchLocalFileResource);
        final ConcurrentResourceCache resourceCache2 = new ConcurrentResourceCache(
                new NamespaceCachingStrategy("namespace2"), this::fetchLocalFileResource);

        resourceCache.invalidate();
        resourceCache2.invalidate();

        try
        {
            URI resourceUri;
            try
            {
                resourceUri = CachingTests.class.getResource(FEATURE_JSON).toURI();
            }
            catch (final URISyntaxException exception)
            {
                logger.error("Bad URI syntax", exception);
                resourceUri = null;
                Assert.fail();
            }

            URI resourceUri2;
            try
            {
                resourceUri2 = CachingTests.class.getResource(FILE_NO_EXTENSION).toURI();
            }
            catch (final URISyntaxException exception)
            {
                logger.error("Bad URI syntax", exception);
                resourceUri2 = null;
                Assert.fail();
            }

            // read the contents of the file
            final ByteArrayResource originalFileBytes = new ByteArrayResource();
            originalFileBytes.copyFrom(new InputStreamResource(
                    () -> CachingTests.class.getResourceAsStream(FEATURE_JSON)));
            final byte[] originalFileBytesArray = originalFileBytes.readBytesAndClose();

            // read the contents of the file
            final ByteArrayResource originalFileBytes2 = new ByteArrayResource();
            originalFileBytes2.copyFrom(new InputStreamResource(
                    () -> CachingTests.class.getResourceAsStream(FILE_NO_EXTENSION)));
            final byte[] originalFileBytesArray2 = originalFileBytes2.readBytesAndClose();

            // read contents of the file with cache, this will incur a cache miss
            final ByteArrayResource fileBytesCacheMiss = new ByteArrayResource();
            fileBytesCacheMiss.copyFrom(resourceCache.get(resourceUri).get());
            final byte[] fileBytesCacheMissArray = fileBytesCacheMiss.readBytesAndClose();
            Assert.assertArrayEquals(originalFileBytesArray, fileBytesCacheMissArray);

            // read contents of the file with cache2, this will incur a cache miss
            final ByteArrayResource fileBytesCacheMiss2 = new ByteArrayResource();
            fileBytesCacheMiss2.copyFrom(resourceCache2.get(resourceUri2).get());
            final byte[] fileBytesCacheMissArray2 = fileBytesCacheMiss2.readBytesAndClose();
            Assert.assertArrayEquals(originalFileBytesArray2, fileBytesCacheMissArray2);

            // read contents again, this time with a cache hit
            final ByteArrayResource fileBytesCacheHit = new ByteArrayResource();
            fileBytesCacheHit.copyFrom(resourceCache.get(resourceUri).get());
            final byte[] fileBytesCacheHitArray = fileBytesCacheHit.readBytesAndClose();
            Assert.assertArrayEquals(originalFileBytesArray, fileBytesCacheHitArray);

            // read contents again, this time with a cache2 hit
            final ByteArrayResource fileBytesCacheHit2 = new ByteArrayResource();
            fileBytesCacheHit2.copyFrom(resourceCache2.get(resourceUri2).get());
            final byte[] fileBytesCacheHitArray2 = fileBytesCacheHit2.readBytesAndClose();
            Assert.assertArrayEquals(originalFileBytesArray2, fileBytesCacheHitArray2);
        }
        finally
        {
            resourceCache.invalidate();
            resourceCache2.invalidate();
        }
    }

    private Optional<Resource> fetchLocalFileResource(final URI resourceURI)
    {
        final String filePath = resourceURI.getPath();
        return Optional.of(new File(filePath));
    }

    private void testBaseCacheWithGivenStrategy(final CachingStrategy strategy)
    {
        logger.trace("Testing with caching strategy {}", strategy.getName());

        final ConcurrentResourceCache resourceCache = new ConcurrentResourceCache(strategy,
                this::fetchLocalFileResource);

        try
        {
            URI resourceUri;
            try
            {
                resourceUri = CachingTests.class.getResource(FEATURE_JSON).toURI();
            }
            catch (final URISyntaxException exception)
            {
                logger.error("Bad URI syntax", exception);
                resourceUri = null;
                Assert.fail();
            }

            URI resourceUri2;
            try
            {
                resourceUri2 = CachingTests.class.getResource(FILE_NO_EXTENSION).toURI();
            }
            catch (final URISyntaxException exception)
            {
                logger.error("Bad URI syntax", exception);
                resourceUri2 = null;
                Assert.fail();
            }

            // read the contents of the file
            ByteArrayResource originalFileBytes = new ByteArrayResource();
            originalFileBytes.copyFrom(new InputStreamResource(
                    () -> CachingTests.class.getResourceAsStream(FEATURE_JSON)));
            byte[] originalFileBytesArray = originalFileBytes.readBytesAndClose();

            // read contents of the file with cache, this will incur a cache miss
            ByteArrayResource fileBytesCacheMiss = new ByteArrayResource();
            fileBytesCacheMiss.copyFrom(resourceCache.get(resourceUri).get());
            byte[] fileBytesCacheMissArray = fileBytesCacheMiss.readBytesAndClose();
            Assert.assertArrayEquals(originalFileBytesArray, fileBytesCacheMissArray);

            // read contents again, this time with a cache hit
            ByteArrayResource fileBytesCacheHit = new ByteArrayResource();
            fileBytesCacheHit.copyFrom(resourceCache.get(resourceUri).get());
            byte[] fileBytesCacheHitArray = fileBytesCacheHit.readBytesAndClose();
            Assert.assertArrayEquals(originalFileBytesArray, fileBytesCacheHitArray);

            // read the contents of the file
            originalFileBytes = new ByteArrayResource();
            originalFileBytes.copyFrom(new InputStreamResource(
                    () -> CachingTests.class.getResourceAsStream(FILE_NO_EXTENSION)));
            originalFileBytesArray = originalFileBytes.readBytesAndClose();

            // read contents of the file with cache, this will incur a cache miss
            fileBytesCacheMiss = new ByteArrayResource();
            fileBytesCacheMiss.copyFrom(resourceCache.get(resourceUri2).get());
            fileBytesCacheMissArray = fileBytesCacheMiss.readBytesAndClose();
            Assert.assertArrayEquals(originalFileBytesArray, fileBytesCacheMissArray);

            // read contents again, this time with a cache hit
            fileBytesCacheHit = new ByteArrayResource();
            fileBytesCacheHit.copyFrom(resourceCache.get(resourceUri2).get());
            fileBytesCacheHitArray = fileBytesCacheHit.readBytesAndClose();
            Assert.assertArrayEquals(originalFileBytesArray, fileBytesCacheHitArray);
        }
        finally
        {
            resourceCache.invalidate();
        }
    }
}
