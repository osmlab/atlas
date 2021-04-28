package org.openstreetmap.atlas.utilities.testing;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests {@link Atlas} creation from both JOSM and Osmosis XML files.
 *
 * @author matthieun
 * @author bbreithaupt
 */
public class TestAtlasTest
{
    private static final Logger logger = LoggerFactory.getLogger(TestAtlasTest.class);
    private static final String USA = "USA";

    @Rule
    public final TestAtlasTestRule rule = new TestAtlasTestRule();

    @Test
    public void loadFromJosmOsmResourceISOTest()
    {
        final Atlas atlas = this.rule.getAtlasFromJosmOsmResourceISO();
        assertAtlasCreation(atlas);
        Assert.assertEquals(14,
                Iterables
                        .asList(atlas.entities(
                                entity -> entity.getTag(ISOCountryTag.KEY).orElse("").equals(USA)))
                        .size());
    }

    @Test
    public void loadFromJosmOsmResourceTest()
    {
        final Atlas atlas = this.rule.getAtlasFromJosmOsmResource();
        assertAtlasCreation(atlas);
    }

    @Test
    public void loadFromOsmResourceISOTest()
    {
        final Atlas atlas = this.rule.getAtlasFromOsmResourceISO();
        assertAtlasCreation(atlas);
        Assert.assertEquals(14,
                Iterables
                        .asList(atlas.entities(
                                entity -> entity.getTag(ISOCountryTag.KEY).orElse("").equals(USA)))
                        .size());
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
