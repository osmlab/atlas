package org.openstreetmap.atlas.utilities.testing;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * Example test case illustrating how to use custom rules, and {@link TestAtlasHandler} unit tests.
 *
 * @author cstaylor
 * @author bbreithaupt
 */
public class AtlasTestCase
{
    @Rule
    public AtlasTestCaseRule setup = new AtlasTestCaseRule();

    @Test
    public void allAnnotationsISOOverrideTest()
    {
        final Atlas atlas = this.setup.allAnnotationsISOOverrideAtlas();
        Assert.assertEquals(9, Iterables.asList(atlas.entities(
                atlasEntity -> atlasEntity.getTag(ISOCountryTag.KEY).orElse("").equals("DEU")))
                .size());
        Assert.assertEquals(1, Iterables.asList(atlas.entities(
                atlasEntity -> atlasEntity.getTag(ISOCountryTag.KEY).orElse("").equals("BBH")))
                .size());
    }

    @Test
    public void allAnnotationsISOTest()
    {
        Assert.assertEquals(10, Iterables.asList(this.setup.allAnnotationsISOAtlas().entities(
                atlasEntity -> atlasEntity.getTag(ISOCountryTag.KEY).orElse("").equals("DEU")))
                .size());
    }

    @Test
    public void allAnnotationsTest()
    {
        Assert.assertEquals(10,
                Iterables.asList(this.setup.allAnnotationsAtlas().entities(
                        atlasEntity -> atlasEntity.getTag(ISOCountryTag.KEY).orElse("").equals("")))
                        .size());
    }

    @Test
    public void verify()
    {
        Assert.assertNotNull(this.setup.atlas());
        Assert.assertNotNull(this.setup.atlas2());
        Assert.assertNotNull(this.setup.atlas3());
        final Area area = this.setup.atlas3().area(1L);
        Assert.assertNotNull(area);
        Assert.assertEquals(new Polygon(Location.TEST_1), area.getRawGeometry());
    }
}
