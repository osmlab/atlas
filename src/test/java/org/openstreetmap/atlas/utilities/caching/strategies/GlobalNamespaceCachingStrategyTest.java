package org.openstreetmap.atlas.utilities.caching.strategies;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.util.Optional;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.streaming.compression.Compressor;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.collections.Maps;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * @author lcram
 */
public class GlobalNamespaceCachingStrategyTest
{
    private static final String CACHE_DIRECTORY = "/var/folders/q5/h09jgyjs6hqc433z67j0q17m0000gn/T/NamespaceCachingStrategy_3707740A818531237051A0F1E086CF701E2C38483675FCD1AAD8F5C5C33F19BC_d3b65e87-dc9d-3c8c-916b-99124624bb6c";

    @Test
    public void testInvalidate()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final Function<URI, Optional<Resource>> fetcher = uri ->
            {
                final String path = uri.getPath();
                return Optional.of(new File(path, filesystem));
            };

            final GlobalNamespaceCachingStrategy strategy = new GlobalNamespaceCachingStrategy(
                    filesystem);
            final File atlasFile = new File("/Users/foo/test.atlas.txt", filesystem);
            final File textFile = new File("/Users/foo/text.txt", filesystem);
            final File cacheDirectory = new File(CACHE_DIRECTORY, filesystem);

            // the cache directory should be empty here
            Assert.assertTrue(cacheDirectory.listFiles().isEmpty());
            strategy.attemptFetch(atlasFile.toAbsolutePath().toUri(), fetcher);
            // the cache directory should now have a single file
            Assert.assertEquals(1, cacheDirectory.listFiles().size());

            strategy.attemptFetch(textFile.toAbsolutePath().toUri(), fetcher);
            // the cache directory should now have two files
            Assert.assertEquals(2, cacheDirectory.listFiles().size());

            strategy.invalidate(atlasFile.toAbsolutePath().toUri());
            // the cache directory should now have one file again
            Assert.assertEquals(1, cacheDirectory.listFiles().size());

            strategy.attemptFetch(atlasFile.toAbsolutePath().toUri(), fetcher);
            // the cache directory should now have two files again
            Assert.assertEquals(2, cacheDirectory.listFiles().size());

            // this final invalidate will delete the cache directory
            strategy.invalidate();
            Assert.assertFalse(cacheDirectory.exists());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testStrategy()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final Function<URI, Optional<Resource>> fetcher = uri ->
            {
                final String path = uri.getPath();
                return Optional.of(new File(path, filesystem));
            };

            final GlobalNamespaceCachingStrategy strategy = new GlobalNamespaceCachingStrategy(
                    filesystem);
            final File atlasFile = new File("/Users/foo/test.atlas.txt", filesystem);
            final File cacheDirectory = new File(CACHE_DIRECTORY, filesystem);

            // the cache directory should be empty here
            Assert.assertTrue(cacheDirectory.listFiles().isEmpty());
            final Optional<Resource> atlasResource = strategy
                    .attemptFetch(atlasFile.toAbsolutePath().toUri(), fetcher);
            Assert.assertTrue(atlasResource.isPresent());
            Assert.assertNotNull(
                    new AtlasResourceLoader().load(atlasResource.get()).point(1000000L));
            // the cache directory should now have a single file
            Assert.assertEquals(1, cacheDirectory.listFiles().size());

            final Optional<Resource> atlasResourceAgain = strategy
                    .attemptFetch(atlasFile.toAbsolutePath().toUri(), fetcher);
            Assert.assertTrue(atlasResourceAgain.isPresent());
            Assert.assertNotNull(
                    new AtlasResourceLoader().load(atlasResourceAgain.get()).point(1000000L));
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testStrategyWithGzippedFile()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final Function<URI, Optional<Resource>> fetcher = uri ->
            {
                final String path = uri.getPath();
                return Optional.of(new File(path, filesystem));
            };

            final GlobalNamespaceCachingStrategy strategy = new GlobalNamespaceCachingStrategy(
                    filesystem);
            final File gzippedFile = new File("/Users/foo/hello.txt.gz", filesystem);
            final File cacheDirectory = new File(CACHE_DIRECTORY, filesystem);

            Assert.assertTrue(cacheDirectory.listFiles().isEmpty());
            final Optional<Resource> gzippedResource = strategy
                    .attemptFetch(gzippedFile.toAbsolutePath().toUri(), fetcher);
            Assert.assertTrue(gzippedResource.isPresent());
            Assert.assertEquals("hello world", gzippedResource.get().all());
            Assert.assertEquals(1, cacheDirectory.listFiles().size());

            final Optional<Resource> gzippedResourceAgain = strategy
                    .attemptFetch(gzippedFile.toAbsolutePath().toUri(), fetcher);
            Assert.assertTrue(gzippedResourceAgain.isPresent());
            Assert.assertEquals("hello world", gzippedResourceAgain.get().all());
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    private void setupFilesystem1(final FileSystem filesystem) throws IOException
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();

        builder.addPoint(1000000L, Location.forWkt("POINT(1 1)"), Maps.hashMap("foo", "bar"));

        final Atlas atlas = builder.get();
        final File atlasFile = new File("/Users/foo/test.atlas.txt", filesystem);
        assert atlas != null;
        atlas.saveAsText(atlasFile);

        final File textFile = new File("/Users/foo/text.txt", filesystem);
        textFile.writeAndClose("hello world\n");

        final File gzippedFile = new File("/Users/foo/hello.txt.gz", filesystem);
        /*
         * We have to explicitly disable compression here so that the subsequent copy into the jimfs
         * filesystem does not compress the file twice.
         */
        gzippedFile.setCompressor(Compressor.NONE);
        gzippedFile.copyFrom(new InputStreamResource(() -> GlobalNamespaceCachingStrategyTest.class
                .getResourceAsStream("hello.txt.gz")));
    }
}
