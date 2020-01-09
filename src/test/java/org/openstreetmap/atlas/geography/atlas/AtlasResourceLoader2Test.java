package org.openstreetmap.atlas.geography.atlas;

import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.File;

/**
 * @author lcram
 */
public class AtlasResourceLoader2Test
{
    @Test
    public void attemptToLoadNonAtlas()
    {
        final ByteArrayResource nonAtlasResource = new ByteArrayResource();
        nonAtlasResource.writeAndClose("some random data");

        new AtlasResourceLoader2().load(nonAtlasResource);
    }

    @Test
    public void missingDirectory()
    {
        Assert.assertNull(new AtlasResourceLoader2().load(new File(
                Paths.get(System.getProperty("user.home"), "FileThatDoesntExist").toString())));
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
