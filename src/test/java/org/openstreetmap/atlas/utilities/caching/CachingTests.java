package org.openstreetmap.atlas.utilities.caching;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.caching.strategies.ByteArrayCachingStrategy;
import org.openstreetmap.atlas.utilities.caching.strategies.CachingStrategy;
import org.openstreetmap.atlas.utilities.caching.strategies.NoCachingStrategy;
import org.openstreetmap.atlas.utilities.caching.strategies.SystemTemporaryFileCachingStrategy;
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

    @Test
    public void testBaseCacheWithByteArrayStrategy()
    {
        testBaseCacheWithGivenStrategy(new ByteArrayCachingStrategy());
    }

    @Test
    public void testBaseCacheWithLocalTemporaryStrategy()
    {
        testBaseCacheWithGivenStrategy(new SystemTemporaryFileCachingStrategy());
    }

    @Test
    public void testBaseCacheWithNoStrategy()
    {
        testBaseCacheWithGivenStrategy(new NoCachingStrategy());
    }

    @Test
    public void testLocalFileInMemoryCache()
    {
        final LocalFileInMemoryCache cache = new LocalFileInMemoryCache();
        URI resourceUri;
        try
        {
            resourceUri = CachingTests.class.getResource(FEATURE_JSON).toURI();
        }
        catch (final URISyntaxException exception)
        {
            logger.error("{}", exception);
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

    private Resource fetchLocalFileResource(final URI resourceURI)
    {
        final String filePath = resourceURI.getPath();
        return new File(filePath);
    }

    private void testBaseCacheWithGivenStrategy(final CachingStrategy strategy)
    {
        logger.info("Testing with caching strategy {}", strategy.getName());

        final ConcurrentResourceCache resourceCache = new ConcurrentResourceCache(strategy,
                this::fetchLocalFileResource);

        URI resourceUri;
        try
        {
            resourceUri = CachingTests.class.getResource(FEATURE_JSON).toURI();
        }
        catch (final URISyntaxException exception)
        {
            logger.error("{}", exception);
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
            logger.error("{}", exception);
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
}
