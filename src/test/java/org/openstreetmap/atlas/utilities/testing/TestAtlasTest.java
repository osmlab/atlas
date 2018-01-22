package org.openstreetmap.atlas.utilities.testing;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests {@link Atlas} creation from both JOSM and Osmosis XML files.
 *
 * @author matthieun
 */
public class TestAtlasTest
{
    private static final Logger logger = LoggerFactory.getLogger(TestAtlasTest.class);

    @Rule
    public final TestAtlasTestRule rule = new TestAtlasTestRule();

    @Test
    public void loadFromJosmOsmResourceTest()
    {
        final Atlas atlas = this.rule.getAtlasFromJosmOsmResource();
        assertAtlasCreation(atlas);
    }

    @Test
    public void loadFromOsmResourceTest()
    {
        final Atlas atlas = this.rule.getAtlasFromOsmResource();
        assertAtlasCreation(atlas);
    }

    private void assertAtlasCreation(final Atlas atlas)
    {
        logger.info("{}", atlas);
        Assert.assertEquals(4, atlas.numberOfNodes());
        Assert.assertEquals(4, atlas.numberOfEdges());
        Assert.assertEquals(3, atlas.numberOfAreas());
        Assert.assertEquals(1, atlas.numberOfLines());
        Assert.assertEquals(1, atlas.numberOfPoints());
        Assert.assertEquals(1, atlas.numberOfRelations());
    }

}
