package org.openstreetmap.atlas.geography.atlas.items.complex.water;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.geography.atlas.items.complex.islands.ComplexIsland;
import org.openstreetmap.atlas.geography.atlas.items.complex.islands.ComplexIslandFinder;

/**
 * @author Sid
 */
public class ComplexIslandTest extends AbstractWaterIslandTest
{
    private Atlas atlas;

    @Before
    public void setUp()
    {
        this.atlas = this.getAtlasBuilder().get();
    }

    @Test
    public void testComplexIslands()
    {
        int islands = 0;
        for (final ComplexIsland island : new ComplexIslandFinder().find(this.atlas,
                Finder::ignore))
        {
            Assert.assertTrue(island.getSource() instanceof Relation);
            islands += island.getGeometry().outers().size();
        }
        Assert.assertEquals("Number of islands should be equal to number of inner polygons", 2,
                islands);
    }

}
