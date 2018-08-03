package org.openstreetmap.atlas.utilities.caching;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.caching.strategies.ByteArrayCachingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class ByteArrayCachingStrategyResizeTest
{
    private static final Logger logger = LoggerFactory
            .getLogger(ByteArrayCachingStrategyResizeTest.class);
    private static final Path LOCAL_TEST_FILE = Paths.get("src/test/resources/log4j.properties")
            .toAbsolutePath();
    private static final URI LOCAL_TEST_FILE_URI = LOCAL_TEST_FILE.toUri();

    @Test
    public void testForResizes()
    {
        final byte[] values = new byte[2000];
        for (int i = 0; i < 2000; i++)
        {
            values[i] = 10;
        }
        final ByteArrayResource resource = new ByteArrayResource(2048);
        resource.writeAndClose(values);

        final ConcurrentResourceCache cache = new ConcurrentResourceCache(
                new ByteArrayCachingStrategy(), uri -> new File(uri.getPath()));
        cache.get(LOCAL_TEST_FILE_URI);
        cache.get(LOCAL_TEST_FILE_URI);
        cache.get(LOCAL_TEST_FILE_URI);
    }
}
