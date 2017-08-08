package org.openstreetmap.atlas.geography.atlas.packed;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasIntegrationTest;
import org.openstreetmap.atlas.geography.atlas.delta.AtlasDelta;

/**
 * @author matthieun
 */
public class PackedAtlasClonerIntegrationTest extends AtlasIntegrationTest
{
    @Test
    public void cloneRealCountry()
    {
        cloneAndCompare(loadCuba());
    }

    @Test
    public void cloneTest()
    {
        for (int i = 0; i < 10; i++)
        {
            final Atlas atlas = RandomPackedAtlasBuilder.generate(1000, 0);
            cloneAndCompare(atlas);
        }
    }

    private void cloneAndCompare(final Atlas atlas)
    {
        final PackedAtlasCloner cloner = new PackedAtlasCloner();
        final Atlas copy = cloner.cloneFrom(atlas);
        Assert.assertTrue(new AtlasDelta(atlas, copy).generate().getDifferences().isEmpty());
    }
}
