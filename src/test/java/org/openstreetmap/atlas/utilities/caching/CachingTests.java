package org.openstreetmap.atlas.utilities.caching;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.caching.strategies.ByteArrayCachingStrategy;
import org.openstreetmap.atlas.utilities.caching.strategies.CachingStrategy;
import org.openstreetmap.atlas.utilities.caching.strategies.NoCachingStrategy;
import org.openstreetmap.atlas.utilities.caching.strategies.SystemTemporaryFileCachingStrategy;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class CachingTests
{
    /**
     * @author lcram
     */
    private class CacheTask implements Runnable
    {
        private final CachingStrategy strategy;
        private final Function<URI, Resource> fetcher;
        private final int size;

        public CacheTask(final CachingStrategy strategy, final Function<URI, Resource> fetcher,
                final int size)
        {
            this.strategy = strategy;
            this.fetcher = fetcher;
            this.size = size;
        }

        @Override
        public void run()
        {
            final ConcurrentResourceCache cache = new ConcurrentResourceCache(this.strategy,
                    this.fetcher);
            for (int i = 0; i < this.size; i++)
            {
                cache.get(LOCAL_TEST_FILE_URI);
            }
        }
    }

    private static final int TASK_SIZE = 10000;

    private static final Logger logger = LoggerFactory.getLogger(CachingTests.class);
    private static final Path LOCAL_TEST_FILE = Paths.get("src/test/resources/log4j.properties")
            .toAbsolutePath();

    private static final URI LOCAL_TEST_FILE_URI = LOCAL_TEST_FILE.toUri();

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

        // read the contents of the file
        final ByteArrayResource originalFileBytes = new ByteArrayResource();
        originalFileBytes.copyFrom(new File(LOCAL_TEST_FILE.toString()));
        final byte[] originalFileBytesArray = originalFileBytes.readBytesAndClose();

        // read contents of the file with cache, this will incur a cache miss
        final ByteArrayResource fileBytesCacheMiss = new ByteArrayResource();
        fileBytesCacheMiss.copyFrom(cache.get(LOCAL_TEST_FILE.toString()).get());
        final byte[] fileBytesCacheMissArray = fileBytesCacheMiss.readBytesAndClose();
        Assert.assertArrayEquals(originalFileBytesArray, fileBytesCacheMissArray);

        // read contents again, this time with a cache hit
        final ByteArrayResource fileBytesCacheHit = new ByteArrayResource();
        fileBytesCacheHit.copyFrom(cache.get(LOCAL_TEST_FILE.toString()).get());
        final byte[] fileBytesCacheHitArray = fileBytesCacheHit.readBytesAndClose();
        Assert.assertArrayEquals(originalFileBytesArray, fileBytesCacheHitArray);
    }

    @Test
    public void testRepeatedCacheReads() throws InterruptedException
    {
        final Thread thread1 = new Thread(new CacheTask(new ByteArrayCachingStrategy(),
                this::fetchLocalFileResource, TASK_SIZE));
        final Thread thread2 = new Thread(new CacheTask(new ByteArrayCachingStrategy(),
                this::fetchLocalFileResource, TASK_SIZE));
        final Time time1 = Time.now();
        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();
        final Duration duration1 = time1.elapsedSince();

        final Thread thread3 = new Thread(new CacheTask(new SystemTemporaryFileCachingStrategy(),
                this::fetchLocalFileResource, TASK_SIZE));
        final Thread thread4 = new Thread(new CacheTask(new SystemTemporaryFileCachingStrategy(),
                this::fetchLocalFileResource, TASK_SIZE));
        final Time time2 = Time.now();
        thread3.start();
        thread4.start();
        thread3.join();
        thread4.join();
        final Duration duration2 = time2.elapsedSince();

        final Thread thread5 = new Thread(
                new CacheTask(new NoCachingStrategy(), this::fetchLocalFileResource, TASK_SIZE));
        final Thread thread6 = new Thread(
                new CacheTask(new NoCachingStrategy(), this::fetchLocalFileResource, TASK_SIZE));
        final Time time3 = Time.now();
        thread5.start();
        thread6.start();
        thread5.join();
        thread6.join();
        final Duration duration3 = time3.elapsedSince();

        logger.info("File in memory ELAPSED: {}", duration1);
        logger.info("System temp file ELAPSED: {}", duration2);
        logger.info("No strategy ELAPSED: {}", duration3);
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

        // read the contents of the file
        final ByteArrayResource originalFileBytes = new ByteArrayResource();
        originalFileBytes.copyFrom(new File(LOCAL_TEST_FILE.toString()));
        final byte[] originalFileBytesArray = originalFileBytes.readBytesAndClose();

        // read contents of the file with cache, this will incur a cache miss
        final ByteArrayResource fileBytesCacheMiss = new ByteArrayResource();
        fileBytesCacheMiss.copyFrom(resourceCache.get(LOCAL_TEST_FILE_URI).get());
        final byte[] fileBytesCacheMissArray = fileBytesCacheMiss.readBytesAndClose();
        Assert.assertArrayEquals(originalFileBytesArray, fileBytesCacheMissArray);

        // read contents again, this time with a cache hit
        final ByteArrayResource fileBytesCacheHit = new ByteArrayResource();
        fileBytesCacheHit.copyFrom(resourceCache.get(LOCAL_TEST_FILE_URI).get());
        final byte[] fileBytesCacheHitArray = fileBytesCacheHit.readBytesAndClose();
        Assert.assertArrayEquals(originalFileBytesArray, fileBytesCacheHitArray);
    }
}
