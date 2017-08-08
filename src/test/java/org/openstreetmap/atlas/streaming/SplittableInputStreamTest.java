package org.openstreetmap.atlas.streaming;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.random.RandomTextGenerator;
import org.openstreetmap.atlas.utilities.scalars.Duration;
import org.openstreetmap.atlas.utilities.threads.Pool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class SplittableInputStreamTest
{
    private static final Logger logger = LoggerFactory.getLogger(SplittableInputStreamTest.class);

    public static void main(final String[] args)
    {
        new SplittableInputStreamTest().largerTest();
    }

    public void largerTest()
    {
        final RandomTextGenerator text = new RandomTextGenerator();
        try (SplittableInputStream split = new SplittableInputStream(text.infiniteStream());
                Pool pool = new Pool(2, "testSplitStream", Duration.ONE_HOUR))
        {
            final InputStream in2 = split.split();
            logger.info("Starting!");
            pool.queue(() -> new InputStreamResource(split).lines()
                    .forEach(line -> logger.info("IN-1: {}", line)));
            pool.queue(() -> new InputStreamResource(in2).lines()
                    .forEach(line -> logger.info("IN-2: {}", line)));
            Duration.ONE_HOUR.sleep();
        }
        catch (final IOException e)
        {
            throw new CoreException("Failed split stream", e);
        }
    }

    @Test
    public void splitTest()
    {
        final InputStream input = new StringInputStream("line1: blah\nline2: haha");
        final SplittableInputStream split = new SplittableInputStream(input);
        final InputStream in2 = split.split();
        logger.info("{}", Iterables.asList(new InputStreamResource(split).lines()));
        logger.info("{}", Iterables.asList(new InputStreamResource(in2).lines()));
        Streams.close(split);
    }
}
