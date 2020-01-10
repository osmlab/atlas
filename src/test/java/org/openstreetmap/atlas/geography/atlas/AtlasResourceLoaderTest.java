package org.openstreetmap.atlas.geography.atlas;

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
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * @author lcram
 */
public class AtlasResourceLoaderTest
{
    private static final long SIZE = 8192L;
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
    public void basicLoadTest()
    {
        Atlas atlas = new AtlasResourceLoader().load(getAtlasResource(this::getSinglePointAtlas));
        Assert.assertEquals(1, atlas.numberOfPoints());

        atlas = new AtlasResourceLoader().load(getTextAtlasResource(this::getSinglePointAtlas));
        Assert.assertEquals(1, atlas.numberOfPoints());
    }

    @Test
    public void multipleLoadTest()
    {
        final Atlas atlasLoadedFromAllPacked = new AtlasResourceLoader().load(
                getAtlasResource(this::getSinglePointAtlas),
                getAtlasResource(this::getMultiplePointAtlas));
        Assert.assertEquals(3, atlasLoadedFromAllPacked.numberOfPoints());
        Assert.assertEquals(Maps.hashMap("a", "b"), atlasLoadedFromAllPacked.point(1L).getTags());
        Assert.assertEquals(Maps.hashMap("c", "d"), atlasLoadedFromAllPacked.point(2L).getTags());
        Assert.assertEquals(Maps.hashMap("e", "f"), atlasLoadedFromAllPacked.point(3L).getTags());

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
        }
        finally
        {
            parent.deleteRecursively();
        }
    }

    private Resource getAtlasResource(final Supplier<Atlas> atlasSupplier)
    {
        final Atlas atlas = atlasSupplier.get();
        final ByteArrayResource resource = new ByteArrayResource(SIZE).withName("hello.atlas");
        atlas.save(resource);
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
        final ByteArrayResource resource = new ByteArrayResource(SIZE).withName("hello.atlas.txt");
        new TextAtlasBuilder().write(atlas, resource);
        return resource;
    }
}
