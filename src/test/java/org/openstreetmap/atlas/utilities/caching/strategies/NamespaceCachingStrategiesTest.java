package org.openstreetmap.atlas.utilities.caching.strategies;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.util.Optional;
import java.util.function.Function;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.collections.Maps;

import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

/**
 * Test both the {@link NamespaceCachingStrategy} and the {@link GlobalNamespaceCachingStrategy}.
 * 
 * @author lcram
 */
public class NamespaceCachingStrategiesTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testGlobalStrategy()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final int[] fetcherCount = new int[1];
            final Function<URI, Optional<Resource>> fetcher = uri ->
            {
                fetcherCount[0]++;
                final String path = uri.getPath();
                return Optional.of(new File(path, filesystem));
            };

            final GlobalNamespaceCachingStrategy strategy = new GlobalNamespaceCachingStrategy(
                    filesystem);
            final File atlasFile = new File("/Users/foo/test.atlas.txt", filesystem);
            final File cacheDirectory = new File(strategy.getStorageDirectory());

            // the cache directory should be empty here, with no fetcher calls
            Assert.assertTrue(cacheDirectory.listFiles().isEmpty());
            Assert.assertEquals(0, fetcherCount[0]);
            final Optional<Resource> atlasResource = strategy
                    .attemptFetch(atlasFile.toAbsolutePath().toUri(), fetcher);
            Assert.assertTrue(atlasResource.isPresent());
            Assert.assertNotNull(
                    new AtlasResourceLoader().load(atlasResource.get()).point(1000000L));
            Assert.assertTrue(atlasResource.get().getName().endsWith(FileSuffix.TEXT.toString()));
            // the cache directory should now have a single file, with one fetcher call
            Assert.assertEquals(1, cacheDirectory.listFiles().size());
            Assert.assertEquals(1, fetcherCount[0]);

            final Optional<Resource> atlasResourceAgain = strategy
                    .attemptFetch(atlasFile.toAbsolutePath().toUri(), fetcher);
            Assert.assertTrue(atlasResourceAgain.isPresent());
            Assert.assertTrue(
                    atlasResourceAgain.get().getName().endsWith(FileSuffix.TEXT.toString()));
            Assert.assertNotNull(
                    new AtlasResourceLoader().load(atlasResourceAgain.get()).point(1000000L));
            // fetcher calls should still be at one since we used a cached version of the file
            Assert.assertEquals(1, fetcherCount[0]);
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

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
            final File cacheDirectory = new File(strategy.getStorageDirectory());

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
            final File cacheDirectory = new File(strategy.getStorageDirectory());

            Assert.assertTrue(cacheDirectory.listFiles().isEmpty());
            final Optional<Resource> gzippedResource = strategy
                    .attemptFetch(gzippedFile.toAbsolutePath().toUri(), fetcher);
            Assert.assertTrue(gzippedResource.isPresent());
            Assert.assertEquals("hello world", gzippedResource.get().all());
            Assert.assertEquals(1, cacheDirectory.listFiles().size());
            Assert.assertTrue(gzippedResource.get().getName().endsWith(FileSuffix.GZIP.toString()));

            final Optional<Resource> gzippedResourceAgain = strategy
                    .attemptFetch(gzippedFile.toAbsolutePath().toUri(), fetcher);
            Assert.assertTrue(gzippedResourceAgain.isPresent());
            Assert.assertEquals("hello world", gzippedResourceAgain.get().all());
            Assert.assertTrue(
                    gzippedResourceAgain.get().getName().endsWith(FileSuffix.GZIP.toString()));
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testStrategyWithIllegalCharacterInNamespace()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);

            this.expectedException.expect(IllegalArgumentException.class);
            this.expectedException
                    .expectMessage("The namespace cannot contain characters '\\' or '/'");
            new NamespaceCachingStrategy("foo/bar", filesystem);
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    @Test
    public void testStrategyWithoutExtensionPreservation()
    {
        try (FileSystem filesystem = Jimfs.newFileSystem(Configuration.osX()))
        {
            setupFilesystem1(filesystem);
            final int[] fetcherCount = new int[1];
            final Function<URI, Optional<Resource>> fetcher = uri ->
            {
                fetcherCount[0]++;
                final String path = uri.getPath();
                return Optional.of(new File(path, filesystem));
            };

            final NamespaceCachingStrategy strategy = new NamespaceCachingStrategy("foo",
                    filesystem).withFileExtensionPreservation(false);
            final File textFile = new File("/Users/foo/text.txt", filesystem);
            final File cacheDirectory = new File(strategy.getStorageDirectory());

            // the cache directory should be empty here with no fetcher calls
            Assert.assertEquals(0, fetcherCount[0]);
            Assert.assertTrue(cacheDirectory.listFiles().isEmpty());
            final Optional<Resource> textResource = strategy
                    .attemptFetch(textFile.toAbsolutePath().toUri(), fetcher);
            Assert.assertTrue(textResource.isPresent());
            Assert.assertEquals("hello world", textResource.get().all());
            Assert.assertFalse(textResource.get().getName().endsWith(FileSuffix.TEXT.toString()));
            // the cache directory should now have a single file, with one fetcher call
            Assert.assertEquals(1, cacheDirectory.listFiles().size());
            Assert.assertEquals(1, fetcherCount[0]);

            final Optional<Resource> textResourceAgain = strategy
                    .attemptFetch(textFile.toAbsolutePath().toUri(), fetcher);
            Assert.assertTrue(textResourceAgain.isPresent());
            Assert.assertEquals("hello world", textResource.get().all());
            Assert.assertFalse(
                    textResourceAgain.get().getName().endsWith(FileSuffix.TEXT.toString()));
            // fetcher calls should still be at one since we used a cached version of the file
            Assert.assertEquals(1, fetcherCount[0]);
        }
        catch (final IOException exception)
        {
            throw new CoreException("FileSystem operation failed", exception);
        }
    }

    private void setupFilesystem1(final FileSystem filesystem)
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
         * The 'File' class by default enables gzip compression on all writes when it sees a file
         * name with a '.gz' extension. So when we call 'gzippedFile.copyFrom' with a 'File' that
         * has a '.gz' in the name, the 'copyFrom' method of 'File' will automatically apply gzip
         * compression to the byte stream it writes into the filesystem. Since the
         * InputStreamResource we are copying is sourced from gzipped data, we want to explicitly
         * enable gzip decompression on the InputStreamResource so that the 'File' class does not
         * re-compress data that is already compressed.
         */
        gzippedFile.copyFrom(new InputStreamResource(
                () -> NamespaceCachingStrategiesTest.class.getResourceAsStream("hello.txt.gz"))
                        .withDecompressor(Decompressor.GZIP));
    }
}
