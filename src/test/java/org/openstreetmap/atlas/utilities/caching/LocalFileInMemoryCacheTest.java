package org.openstreetmap.atlas.utilities.caching;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author lcram
 */
public class LocalFileInMemoryCacheTest
{
    @Test
    public void test()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);

            final LocalFileInMemoryCache cache = new LocalFileInMemoryCache(filesystem);

            final Optional<Resource> text1 = cache.get("/Users/foo/text1.txt");
            Assert.assertTrue(text1.isPresent());
            Assert.assertEquals("hello world", text1.get().all());
            final Optional<Resource> text2 = cache.get("/Users/foo/text2.txt");
            Assert.assertTrue(text2.isPresent());
            Assert.assertEquals("foo bar", text2.get().all());
            final Optional<Resource> text3 = cache.get("/Users/foo/text3.txt");
            Assert.assertTrue(text3.isEmpty());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    private void setupFilesystem1(final FileSystem filesystem)
    {
        final File textFile1 = new File("/Users/foo/text1.txt", filesystem);
        textFile1.writeAndClose("hello world");
        final File textFile2 = new File("/Users/foo/text2.txt", filesystem);
        textFile2.writeAndClose("foo bar");
    }
}
