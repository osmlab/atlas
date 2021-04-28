package org.openstreetmap.atlas.geography.atlas;

import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.atlas.builder.text.TextAtlasBuilder;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.streaming.compression.Compressor;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * @author lcram
 */
public class AtlasResourceLoaderTest
{
    private static final long BYTE_ARRAY_SIZE = 8192L;

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void attemptToLoadNonAtlasData()
    {
        final ByteArrayResource nonAtlasResource = new ByteArrayResource();
        nonAtlasResource.writeAndClose("some random data");

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Failed to load an atlas");
        new AtlasResourceLoader().load(nonAtlasResource);
    }

    @Test
    public void attemptToLoadResourcesWithMixedEmpty()
    {
        final ByteArrayResource emptyResource = new ByteArrayResource();
        final Resource atlasResource = getAtlasResource(this::getSinglePointAtlas);

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("had zero length!");
        new AtlasResourceLoader().load(atlasResource, emptyResource);
    }

    @Test
    public void attemptToLoadSingleEmptyResource()
    {
        final ByteArrayResource emptyResource = new ByteArrayResource();

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("had zero length!");
        new AtlasResourceLoader().load(emptyResource);
    }

    @Test
    public void basicCompressedLoadTest()
    {
        final Atlas atlas = new AtlasResourceLoader()
                .load(getCompressedAtlasResource(this::getSinglePointAtlas));
        Assert.assertEquals(1, atlas.numberOfPoints());

        final Atlas atlasFromText = new AtlasResourceLoader()
                .load(getCompressedTextAtlasResource(this::getSinglePointAtlas));
        Assert.assertEquals(1, atlasFromText.numberOfPoints());
    }

    @Test
    public void basicLoadTest()
    {
        final Atlas atlas = new AtlasResourceLoader()
                .load(getAtlasResource(this::getSinglePointAtlas));
        Assert.assertEquals(1, atlas.numberOfPoints());

        final Atlas atlasFromText = new AtlasResourceLoader()
                .load(getTextAtlasResource(this::getSinglePointAtlas));
        Assert.assertEquals(1, atlasFromText.numberOfPoints());
    }

    @Test
    public void multipleLoadTest()
    {
        final Atlas atlasLoadedFromAllPacked = new AtlasResourceLoader().withMultiAtlasName("foo")
                .load(getAtlasResource(this::getSinglePointAtlas),
                        getAtlasResource(this::getMultiplePointAtlas));
        Assert.assertEquals(3, atlasLoadedFromAllPacked.numberOfPoints());
        Assert.assertEquals(Maps.hashMap("a", "b"), atlasLoadedFromAllPacked.point(1L).getTags());
        Assert.assertEquals(Maps.hashMap("c", "d"), atlasLoadedFromAllPacked.point(2L).getTags());
        Assert.assertEquals(Maps.hashMap("e", "f"), atlasLoadedFromAllPacked.point(3L).getTags());
        Assert.assertEquals("foo", atlasLoadedFromAllPacked.getName());

        final Atlas atlasLoadedFromPackedTextMix = new AtlasResourceLoader().load(
                getAtlasResource(this::getSinglePointAtlas),
                getTextAtlasResource(this::getMultiplePointAtlas));
        Assert.assertEquals(3, atlasLoadedFromPackedTextMix.numberOfPoints());
        Assert.assertEquals(Maps.hashMap("a", "b"),
                atlasLoadedFromPackedTextMix.point(1L).getTags());
        Assert.assertEquals(Maps.hashMap("c", "d"),
                atlasLoadedFromPackedTextMix.point(2L).getTags());
        Assert.assertEquals(Maps.hashMap("e", "f"),
                atlasLoadedFromPackedTextMix.point(3L).getTags());

        final Atlas atlasLoadedFromAllText = new AtlasResourceLoader().load(
                getTextAtlasResource(this::getSinglePointAtlas),
                getTextAtlasResource(this::getMultiplePointAtlas));
        Assert.assertEquals(3, atlasLoadedFromAllText.numberOfPoints());
        Assert.assertEquals(Maps.hashMap("a", "b"), atlasLoadedFromAllText.point(1L).getTags());
        Assert.assertEquals(Maps.hashMap("c", "d"), atlasLoadedFromAllText.point(2L).getTags());
        Assert.assertEquals(Maps.hashMap("e", "f"), atlasLoadedFromAllText.point(3L).getTags());

        final Atlas filteredAtlas = new AtlasResourceLoader()
                .withAtlasEntityFilter(entity -> entity.getIdentifier() != 3)
                .load(getAtlasResource(this::getMultiplePointAtlas));
        Assert.assertEquals(2, filteredAtlas.numberOfPoints());
        Assert.assertEquals(Maps.hashMap("a", "b"), filteredAtlas.point(1L).getTags());
        Assert.assertEquals(Maps.hashMap("c", "d"), filteredAtlas.point(2L).getTags());
    }

    @Test
    public void safeLoadTest()
    {
        Optional<Atlas> atlas = new AtlasResourceLoader()
                .safeLoad(getAtlasResource(this::getSinglePointAtlas));
        Assert.assertTrue(atlas.isPresent());
        Assert.assertEquals(1, atlas.get().numberOfPoints());

        final ByteArrayResource nonAtlasResource = new ByteArrayResource();
        nonAtlasResource.writeAndClose("some random data");

        atlas = new AtlasResourceLoader().safeLoad(nonAtlasResource);
        Assert.assertFalse(atlas.isPresent());
    }

    @Test
    public void testLoadRecursively()
    {
        final File parent = File.temporaryFolder();
        final File subFolder = parent.child("subfolder");
        subFolder.mkdirs();
        try
        {
            final File atlasFile1 = parent.child("hello1.atlas.txt");
            new TextAtlasBuilder().write(getSinglePointAtlas(), atlasFile1);

            final File atlasFile2 = subFolder.child("hello2.atlas");
            getMultiplePointAtlas().save(atlasFile2);

            final File notAtlas1 = parent.child("random1.txt");
            notAtlas1.writeAndClose("hello world");

            final File notAtlas2 = subFolder.child("random2.txt");
            notAtlas2.writeAndClose("hello world again");

            final Atlas atlas = new AtlasResourceLoader().loadRecursively(parent);
            Assert.assertEquals(3, atlas.numberOfPoints());
            Assert.assertEquals(Maps.hashMap("a", "b"), atlas.point(1L).getTags());
            Assert.assertEquals(Maps.hashMap("c", "d"), atlas.point(2L).getTags());
            Assert.assertEquals(Maps.hashMap("e", "f"), atlas.point(3L).getTags());

            final Atlas filteredAtlas = new AtlasResourceLoader()
                    .withAtlasEntityFilter(entity -> entity.getIdentifier() != 3L)
                    .loadRecursively(parent);
            Assert.assertEquals(2, filteredAtlas.numberOfPoints());
            Assert.assertEquals(Maps.hashMap("a", "b"), filteredAtlas.point(1L).getTags());
            Assert.assertEquals(Maps.hashMap("c", "d"), filteredAtlas.point(2L).getTags());
        }
        finally
        {
            parent.deleteRecursively();
        }
    }

    @Test
    public void testLoadSingleFileRecursively()
    {
        final File parent = File.temporaryFolder();
        try
        {
            final File atlasFile = parent.child("hello.atlas");
            getMultiplePointAtlas().save(atlasFile);

            final Atlas atlas = new AtlasResourceLoader().loadRecursively(atlasFile);
            Assert.assertEquals(3, atlas.numberOfPoints());
            Assert.assertEquals(Maps.hashMap("a", "b"), atlas.point(1L).getTags());
            Assert.assertEquals(Maps.hashMap("c", "d"), atlas.point(2L).getTags());
            Assert.assertEquals(Maps.hashMap("e", "f"), atlas.point(3L).getTags());
        }
        finally
        {
            parent.deleteRecursively();
        }
    }

    @Test
    public void testResourceFilter()
    {
        final StringResource stringResource = new StringResource().withName("hello.atlas.txt");
        new TextAtlasBuilder().write(getSinglePointAtlas(), stringResource);

        final Resource otherResource = getAtlasResource(this::getMultiplePointAtlas);

        final Atlas atlas = new AtlasResourceLoader()
                .withResourceFilter(resource -> resource instanceof StringResource)
                .load(stringResource, otherResource);

        Assert.assertEquals(1, atlas.numberOfPoints());
        Assert.assertEquals(Maps.hashMap("a", "b"), atlas.point(1L).getTags());
    }

    @Test
    public void testSafeLoadRecursively()
    {
        final File parent = File.temporaryFolder();
        final File subFolder = parent.child("subfolder");
        subFolder.mkdirs();
        try
        {
            final File atlasFile1 = parent.child("hello1.atlas.txt");
            new TextAtlasBuilder().write(getSinglePointAtlas(), atlasFile1);

            final File atlasFile2 = subFolder.child("hello2.atlas");
            getMultiplePointAtlas().save(atlasFile2);

            final File notAtlas1 = parent.child("random1.txt");
            notAtlas1.writeAndClose("hello world");

            final File notAtlas2 = subFolder.child("random2.txt");
            notAtlas2.writeAndClose("hello world again");

            final Optional<Atlas> atlas = new AtlasResourceLoader().safeLoadRecursively(parent);
            Assert.assertTrue(atlas.isPresent());
            Assert.assertEquals(3, atlas.get().numberOfPoints());
            Assert.assertEquals(Maps.hashMap("a", "b"), atlas.get().point(1L).getTags());
            Assert.assertEquals(Maps.hashMap("c", "d"), atlas.get().point(2L).getTags());
            Assert.assertEquals(Maps.hashMap("e", "f"), atlas.get().point(3L).getTags());
        }
        finally
        {
            parent.deleteRecursively();
        }
    }

    @Test
    public void testSafeLoadRecursivelyFail()
    {
        final File parent = File.temporaryFolder();
        final File subFolder = parent.child("subfolder");
        subFolder.mkdirs();
        try
        {
            final File atlasFile1 = parent.child("hello1.atlas.txt");
            new TextAtlasBuilder().write(getSinglePointAtlas(), atlasFile1);

            final File atlasFile2 = subFolder.child("hello2.atlas");
            getMultiplePointAtlas().save(atlasFile2);

            final File corruptedAtlas1 = parent.child("corrupted.atlas");
            corruptedAtlas1.writeAndClose("some random non-atlas data");

            final File notAtlas1 = parent.child("random1.txt");
            notAtlas1.writeAndClose("hello world");

            final Optional<Atlas> atlas = new AtlasResourceLoader().safeLoadRecursively(parent);
            Assert.assertFalse(atlas.isPresent());
        }
        finally
        {
            parent.deleteRecursively();
        }
    }

    @Test
    public void tryFilterThatEmptiesAnAtlas()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Entity filter resulted in an empty atlas");
        final Atlas filteredAtlas = new AtlasResourceLoader()
                .withAtlasEntityFilter(entity -> entity.getIdentifier() != 1)
                .load(getAtlasResource(this::getSinglePointAtlas));
    }

    @Test
    public void tryToLoadDirectoryNonRecursively()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage(
                "was of type File but it was a directory. Try loadRecursively instead.");
        final Atlas atlas = new AtlasResourceLoader()
                .load(new File(Paths.get(System.getProperty("user.home")).toString()));
    }

    @Test
    public void tryToLoadNonExistentFile()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("was of type File but it could not be found");
        new AtlasResourceLoader().load(new File(
                Paths.get(System.getProperty("user.home"), "SomeFileThatDoesNotExist").toString()));
    }

    @Test
    public void tryToLoadNonFileRecursively()
    {
        final Resource resource = getAtlasResource(this::getSinglePointAtlas);

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("was not a File, instead was");
        final Atlas atlas = new AtlasResourceLoader().loadRecursively(resource);
    }

    private Resource getAtlasResource(final Supplier<Atlas> atlasSupplier)
    {
        final Atlas atlas = atlasSupplier.get();
        final ByteArrayResource resource = new ByteArrayResource(BYTE_ARRAY_SIZE)
                .withName("hello.atlas");
        atlas.save(resource);
        return resource;
    }

    private Resource getCompressedAtlasResource(final Supplier<Atlas> atlasSupplier)
    {
        final Atlas atlas = atlasSupplier.get();
        final ByteArrayResource resource = new ByteArrayResource(BYTE_ARRAY_SIZE)
                .withName("hello.atlas.gz");
        resource.setCompressor(Compressor.GZIP);
        atlas.save(resource);
        return resource;
    }

    private Resource getCompressedTextAtlasResource(final Supplier<Atlas> atlasSupplier)
    {
        final Atlas atlas = atlasSupplier.get();
        final ByteArrayResource resource = new ByteArrayResource(BYTE_ARRAY_SIZE)
                .withName("hello.atlas.txt.gz");
        resource.setCompressor(Compressor.GZIP);
        new TextAtlasBuilder().write(atlas, resource);
        return resource;
    }

    private Atlas getMultiplePointAtlas()
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        builder.addPoint(1, new Location(Latitude.degrees(1), Longitude.degrees(1)),
                Maps.hashMap("a", "b"));
        builder.addPoint(2, new Location(Latitude.degrees(2), Longitude.degrees(2)),
                Maps.hashMap("c", "d"));
        builder.addPoint(3, new Location(Latitude.degrees(3), Longitude.degrees(3)),
                Maps.hashMap("e", "f"));
        return builder.get();
    }

    private Atlas getSinglePointAtlas()
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        builder.addPoint(1, new Location(Latitude.degrees(1), Longitude.degrees(1)),
                Maps.hashMap("a", "b"));
        return builder.get();
    }

    private Resource getTextAtlasResource(final Supplier<Atlas> atlasSupplier)
    {
        final Atlas atlas = atlasSupplier.get();
        final ByteArrayResource resource = new ByteArrayResource(BYTE_ARRAY_SIZE)
                .withName("hello.atlas.txt");
        new TextAtlasBuilder().write(atlas, resource);
        return resource;
    }
}
