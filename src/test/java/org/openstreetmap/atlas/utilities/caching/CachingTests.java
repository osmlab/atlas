package org.openstreetmap.atlas.utilities.caching;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
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
        private final ConcurrentResourceCache cache;
        private final int size;

        CacheTask(final ConcurrentResourceCache cache, final int size)
        {
            this.cache = cache;
            this.size = size;
        }

        @Override
        public void run()
        {
            final Resource resource1 = new File(LOCAL_TEST_FILE.toString());
            final Resource resource2 = new File(LOCAL_TEST_FILE_2.toString());
            final Random random = new Random();
            for (int i = 0; i < this.size; i++)
            {
                if (random.nextBoolean())
                {
                    compareFetchedResource(LOCAL_TEST_FILE_URI, resource1);
                }
                else
                {
                    compareFetchedResource(LOCAL_TEST_FILE_2_URI, resource2);
                }
            }
        }

        private void compareFetchedResource(final URI resourceURI, final Resource goldenResource)
        {
            final Resource resource = this.cache.get(resourceURI).get();
            if (!resource.readAndClose().equals(goldenResource.readAndClose()))
            {
                throw new CoreException("Unexpected resource discrepancy");
            }
        }
    }

    private static final int TASK_SIZE = 10000;

    private static final Logger logger = LoggerFactory.getLogger(CachingTests.class);
    private static final Path LOCAL_TEST_FILE = Paths.get("src/test/resources/log4j.properties")
            .toAbsolutePath();
    private static final URI LOCAL_TEST_FILE_URI = LOCAL_TEST_FILE.toUri();
    private static final Path LOCAL_TEST_FILE_2 = Paths
            .get("src/test/resources/org/openstreetmap/atlas/utilities/configuration/feature.json")
            .toAbsolutePath();
    private static final URI LOCAL_TEST_FILE_2_URI = LOCAL_TEST_FILE_2.toUri();

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
        try
        {
            final ConcurrentResourceCache sharedCache1 = new ConcurrentResourceCache(
                    new ByteArrayCachingStrategy(), this::fetchLocalFileResource);
            final Thread thread1 = new Thread(new CacheTask(sharedCache1, TASK_SIZE));
            final Thread thread2 = new Thread(new CacheTask(sharedCache1, TASK_SIZE));
            final Thread thread3 = new Thread(new CacheTask(sharedCache1, TASK_SIZE));
            final Thread thread4 = new Thread(new CacheTask(sharedCache1, TASK_SIZE));
            final Thread thread5 = new Thread(new CacheTask(sharedCache1, TASK_SIZE));
            final Thread thread6 = new Thread(new CacheTask(sharedCache1, TASK_SIZE));
            final Time time1 = Time.now();
            thread1.start();
            thread2.start();
            thread3.start();
            thread4.start();
            thread5.start();
            thread6.start();
            thread1.join();
            thread2.join();
            thread3.join();
            thread4.join();
            thread5.join();
            thread6.join();
            final Duration duration1 = time1.elapsedSince();

            final ConcurrentResourceCache sharedCache2 = new ConcurrentResourceCache(
                    new NoCachingStrategy(), this::fetchLocalFileResource);
            final Thread thread7 = new Thread(new CacheTask(sharedCache2, TASK_SIZE));
            final Thread thread8 = new Thread(new CacheTask(sharedCache2, TASK_SIZE));
            final Thread thread9 = new Thread(new CacheTask(sharedCache2, TASK_SIZE));
            final Thread thread10 = new Thread(new CacheTask(sharedCache2, TASK_SIZE));
            final Thread thread11 = new Thread(new CacheTask(sharedCache2, TASK_SIZE));
            final Thread thread12 = new Thread(new CacheTask(sharedCache2, TASK_SIZE));
            final Time time2 = Time.now();
            thread7.start();
            thread8.start();
            thread9.start();
            thread10.start();
            thread11.start();
            thread12.start();
            thread7.join();
            thread8.join();
            thread9.join();
            thread10.join();
            thread11.join();
            thread12.join();
            final Duration duration2 = time2.elapsedSince();

            logger.info("File in memory ELAPSED: {}", duration1);
            logger.info("No strategy ELAPSED: {}", duration2);
        }
        catch (final CoreException exception)
        {
            Assert.fail("A multithreaded discrepancy test failed");
        }
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
