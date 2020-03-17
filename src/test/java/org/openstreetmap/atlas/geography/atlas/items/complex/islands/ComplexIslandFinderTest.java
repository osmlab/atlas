package org.openstreetmap.atlas.geography.atlas.items.complex.islands;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.items.complex.Finder;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author sbhalekar
 */
public class ComplexIslandFinderTest
{
    @Rule
    public ComplexIslandFinderTestRule rule = new ComplexIslandFinderTestRule();

    private final ComplexIslandFinder complexIslandFinder = new ComplexIslandFinder();

    @Test
    public void testInvalidAreaAtlas()
    {
        Assert.assertEquals(0, Iterables.size(this.complexIslandFinder
                .find(this.rule.getAtlasWithInvalidAreaIsland(), Finder::ignore)));
    }

    @Test
    public void testValidAreaAtlas()
    {
        Assert.assertEquals(1, Iterables.size(this.complexIslandFinder
                .find(this.rule.getAtlasWithValidAreaIsland(), Finder::ignore)));
    }

    @Test
    public void testValidRelationIsland()
    {
        Assert.assertEquals(1, Iterables.size(this.complexIslandFinder
                .find(this.rule.getValidIsletRelationAtlas(), Finder::ignore)));
    }
}
