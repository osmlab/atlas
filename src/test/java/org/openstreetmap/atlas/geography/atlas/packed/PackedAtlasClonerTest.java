package org.openstreetmap.atlas.geography.atlas.packed;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.delta.AtlasDelta;

/**
 * @author matthieun
 */
public class PackedAtlasClonerTest
{
    @Test
    public void cloneTest()
    {
        for (int i = 0; i < 5; i++)
        {
            final Atlas atlas = RandomPackedAtlasBuilder.generate(50, 0);
            final PackedAtlasCloner cloner = new PackedAtlasCloner();
            final Atlas copy = cloner.cloneFrom(atlas);
            Assert.assertTrue(new AtlasDelta(atlas, copy).generate().getDifferences().isEmpty());
        }
    }
}
