package org.openstreetmap.atlas.geography.atlas;

import java.nio.file.Paths;
import java.util.List;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader.AtlasFileSelector;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;

/**
 * Test for the AtlasResourceLoader
 *
 * @author cstaylor
 */
public class AtlasResourceLoaderTest
{
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void missingDirectory()
    {
        Assert.assertNull(new AtlasResourceLoader().load(new File(
                Paths.get(System.getProperty("user.home"), "FileThatDoesntExist").toString())));
    }

    @Test
    public void multipleFiles()
    {
        final File parent = File.temporaryFolder();
        try
        {
            final File atlas1 = parent.child("iAmAn.atlas");
            atlas1.writeAndClose("1");
            final File atlas2 = parent.child("iTooAmAn.atlas");
            atlas2.writeAndClose("2");
            final File other = parent.child("iAmNotAnAtlas.txt");
            other.writeAndClose("3");
            final List<Resource> selected = new AtlasFileSelector().select(parent);
            // This one does not filter on an Atlas.
            Assert.assertEquals(3, selected.size());
        }
        finally
        {
            parent.deleteRecursively();
        }
    }

    @Test
    public void nullFile()
    {
        final File nullfile = null;
        Assert.assertNull(new AtlasResourceLoader().load(nullfile));
    }

    @Test
    public void oneFile()
    {
        File temporary = null;
        try
        {
            temporary = File.temporary();
            temporary.writeAndClose("1");
            final List<Resource> selected = new AtlasFileSelector().select(temporary);
            Assert.assertEquals(1, selected.size());
            Assert.assertTrue(temporary == selected.get(0));
        }
        finally
        {
            temporary.delete();
        }
    }
}
