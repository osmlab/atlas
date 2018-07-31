package org.openstreetmap.atlas.utilities.caching;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.caching.strategies.ByteArrayCachingStrategy;
import org.openstreetmap.atlas.utilities.caching.strategies.CachingStrategy;
import org.openstreetmap.atlas.utilities.caching.strategies.NoCachingStrategy;
import org.openstreetmap.atlas.utilities.caching.strategies.SystemTemporaryFileCachingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachingTests
{
    private static final Logger logger = LoggerFactory.getLogger(CachingTests.class);

    private static final Path LOCAL_TEST_FILE = Paths.get("src/test/resources/log4j.properties")
            .toAbsolutePath();
    private static final URI LOCAL_TEST_FILE_URI = LOCAL_TEST_FILE.toUri();

    @Test
    public void testSimpleCacherWithByteArrayStrategy()
    {
        testSimpleCacheWithGivenStrategy(new ByteArrayCachingStrategy());
    }

    @Test
    public void testSimpleCacherWithLocalTemporaryStrategy()
    {
        testSimpleCacheWithGivenStrategy(new SystemTemporaryFileCachingStrategy());
    }

    @Test
    public void testSimpleCacherWithNoStrategy()
    {
        testSimpleCacheWithGivenStrategy(new NoCachingStrategy());
    }

    private Resource fetchLocalFileResource(final URI resourceURI)
    {
        final String filePath = resourceURI.getPath();
        return new File(filePath);
    }

    private void testSimpleCacheWithGivenStrategy(final CachingStrategy strategy)
    {
        logger.info("Testing with caching strategy {}", strategy.getName());

        final SimpleResourceCache resourceCache = new SimpleResourceCache()
                .withCachingStrategy(strategy).withFetcher(this::fetchLocalFileResource);

        // read the contents of the file
        final ByteArrayResource originalFileBytes = new ByteArrayResource();
        originalFileBytes.copyFrom(new File(LOCAL_TEST_FILE.toString()));
        final byte[] originalFileBytesArray = originalFileBytes.readBytesAndClose();

        // read contents of the file with cache, this will incur a cache miss
        final ByteArrayResource fileBytesCacheMiss = new ByteArrayResource();
        fileBytesCacheMiss
                .copyFrom(resourceCache.withResourceURI(LOCAL_TEST_FILE_URI).getResource().get());
        final byte[] fileBytesCacheMissArray = fileBytesCacheMiss.readBytesAndClose();
        Assert.assertArrayEquals(originalFileBytesArray, fileBytesCacheMissArray);

        // read contents again, this time with a cache hit
        final ByteArrayResource fileBytesCacheHit = new ByteArrayResource();
        fileBytesCacheHit
                .copyFrom(resourceCache.withResourceURI(LOCAL_TEST_FILE_URI).getResource().get());
        final byte[] fileBytesCacheHitArray = fileBytesCacheHit.readBytesAndClose();
        Assert.assertArrayEquals(originalFileBytesArray, fileBytesCacheHitArray);
    }
}
