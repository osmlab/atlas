package org.openstreetmap.atlas.geography.atlas.bloated;

import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

/**
 * @author matthieun
 */
public class BloatedAreaTest
{
    @Rule
    public BloatedTestRule rule = new BloatedTestRule();

    @Test
    public void testBloatedEquals()
    {
        final BloatedArea area11 = new BloatedArea(123L, null, null, null);
        final BloatedArea area12 = new BloatedArea(123L, null, null, null);
        final BloatedArea area21 = new BloatedArea(123L, Polygon.SILICON_VALLEY, null, null);
        final BloatedArea area22 = new BloatedArea(123L, Polygon.SILICON_VALLEY, null, null);
        final BloatedArea area23 = new BloatedArea(123L, Polygon.SILICON_VALLEY_2, null, null);
        final BloatedArea area31 = new BloatedArea(123L, null, Maps.hashMap("key", "value"), null);
        final BloatedArea area32 = new BloatedArea(123L, null, Maps.hashMap("key", "value"), null);
        final BloatedArea area33 = new BloatedArea(123L, null, Maps.hashMap(), null);
        final BloatedArea area41 = new BloatedArea(123L, null, null, Sets.hashSet(1L, 2L));
        final BloatedArea area42 = new BloatedArea(123L, null, null, Sets.hashSet(1L, 2L));
        final BloatedArea area43 = new BloatedArea(123L, null, null, Sets.hashSet(1L));

        Assert.assertEquals(area11, area12);
        Assert.assertEquals(area21, area22);
        Assert.assertEquals(area31, area32);
        Assert.assertEquals(area41, area42);

        Assert.assertNotEquals(area11, area21);
        Assert.assertNotEquals(area11, area31);
        Assert.assertNotEquals(area11, area41);
        Assert.assertNotEquals(area21, area23);
        Assert.assertNotEquals(area31, area33);
        Assert.assertNotEquals(area41, area43);
    }

    @Test
    public void testFull()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Area source = atlas.area(27);
        final BloatedArea result = BloatedArea.from(source);
        Assert.assertEquals(source.getIdentifier(), result.getIdentifier());
        Assert.assertEquals(source.bounds(), result.bounds());
        Assert.assertEquals(source.asPolygon(), result.asPolygon());
        Assert.assertEquals(source.getTags(), result.getTags());
        Assert.assertEquals(
                source.relations().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toSet()),
                result.relations().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toSet()));
    }

    @Test
    public void testShallow()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Area source = atlas.area(27);
        final BloatedArea result = BloatedArea.shallowFrom(source);
        Assert.assertEquals(source.getIdentifier(), result.getIdentifier());
        Assert.assertEquals(source.bounds(), result.bounds());
        result.withPolygon(Polygon.TEST_BUILDING);
        // When we update a polygon, the bounds should expand to include the original polygon as
        // well as the updated polygon
        Assert.assertEquals(Rectangle.forLocated(source.bounds(), Polygon.TEST_BUILDING),
                result.bounds());
        final Map<String, String> tags = Maps.hashMap("key", "value");
        result.withTags(tags);
        Assert.assertEquals(tags, result.getTags());
        result.withRelationIdentifiers(source.relations().stream().map(Relation::getIdentifier)
                .collect(Collectors.toSet()));
        Assert.assertEquals(
                source.relations().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toSet()),
                result.relations().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toSet()));

        result.withPolygon(Polygon.SILICON_VALLEY);
        // When we update the polygon again, the bounds recalculation should "forget" about the
        // first update
        Assert.assertEquals(Rectangle.forLocated(source.bounds(), Polygon.SILICON_VALLEY),
                result.bounds());
    }
}
