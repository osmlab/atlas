package org.openstreetmap.atlas.geography.atlas;

import java.io.InputStream;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.streaming.resource.Resource;

/**
 * Test case for the isAtlas method
 *
 * @author cstaylor
 */
public class IsAtlasTestCase
{
    /**
     * Fake Resource for testing the getName method
     *
     * @author cstaylor
     */
    private static final class NamedResource implements Resource
    {
        private final String name;

        NamedResource(final String name)
        {
            this.name = name;
        }

        @Override
        public String getName()
        {
            return this.name;
        }

        @Override
        public long length()
        {
            return 0;
        }

        @Override
        public InputStream read()
        {
            return null;
        }
    }

    @Test
    public void atlasCapsName()
    {
        Assert.assertFalse(AtlasResourceLoader.IS_ATLAS.test(new NamedResource("somefile.ATLAS")));
    }

    @Test
    public void atlasCompressed()
    {
        Assert.assertTrue(
                AtlasResourceLoader.IS_ATLAS.test(new NamedResource("somefile.atlas.gz")));
    }

    @Test
    public void atlasName()
    {
        Assert.assertTrue(AtlasResourceLoader.IS_ATLAS.test(new NamedResource("somefile.atlas")));
    }

    @Test
    public void compressed()
    {
        Assert.assertFalse(AtlasResourceLoader.IS_ATLAS.test(new NamedResource("somefile.gz")));
    }

    @Test
    public void nonAtlasName()
    {
        Assert.assertFalse(AtlasResourceLoader.IS_ATLAS.test(new NamedResource("somefile.txt")));
    }

    @Test
    public void nullName()
    {
        Assert.assertTrue(AtlasResourceLoader.IS_ATLAS.test(new NamedResource(null)));
    }

}
