package org.openstreetmap.atlas.geography.atlas;

import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * @author lcram
 */
public class AtlasResourceLoader2Test
{
    @Test
    public void attemptToLoadNonAtlas()
    {
        final PackedAtlasBuilder builder = new PackedAtlasBuilder();
        builder.addPoint(1, new Location(Latitude.degrees(0), Longitude.degrees(0)),
                Maps.hashMap("a", "b"));
        final Atlas atlas = builder.get();
        final ByteArrayResource atlasResource = new ByteArrayResource().withName("hello.atlas");
        atlas.save(atlasResource);

        final ByteArrayResource nonAtlasResource = new ByteArrayResource();
        nonAtlasResource.writeAndClose("some random data");

        System.out.println(new AtlasResourceLoader2().load(atlasResource) == null);
    }

    @Test
    public void missingDirectory()
    {
        Assert.assertNull(new AtlasResourceLoader2().load(new File(
                Paths.get(System.getProperty("user.home"), "FileThatDoesntExist").toString())));
    }

    @Test
    public void missingDirectory2()
    {
        Assert.assertNull(new AtlasResourceLoader2().load(
                new File(Paths.get(System.getProperty("user.home")).toString()), new File("asd")));
    }

    @Test
    public void multipleFiles()
    {
        final File parent = File.temporaryFolder();
        try
        {
            new AtlasResourceLoader2().load(parent);
        }
        finally
        {
            parent.deleteRecursively();
        }
    }
}
