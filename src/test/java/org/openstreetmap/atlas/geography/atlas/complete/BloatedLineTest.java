package org.openstreetmap.atlas.geography.atlas.bloated;

import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

/**
 * @author matthieun
 */
public class BloatedLineTest
{
    @Rule
    public BloatedTestRule rule = new BloatedTestRule();

    @Test
    public void testBloatedEquals()
    {
        final BloatedLine line11 = new BloatedLine(123L, null, null, null);
        final BloatedLine line12 = new BloatedLine(123L, null, null, null);
        final BloatedLine line21 = new BloatedLine(123L, PolyLine.TEST_POLYLINE, null, null);
        final BloatedLine line22 = new BloatedLine(123L, PolyLine.TEST_POLYLINE, null, null);
        final BloatedLine line23 = new BloatedLine(123L, Polygon.SILICON_VALLEY_2, null, null);
        final BloatedLine line31 = new BloatedLine(123L, null, Maps.hashMap("key", "value"), null);
        final BloatedLine line32 = new BloatedLine(123L, null, Maps.hashMap("key", "value"), null);
        final BloatedLine line33 = new BloatedLine(123L, null, Maps.hashMap(), null);
        final BloatedLine line41 = new BloatedLine(123L, null, null, Sets.hashSet(1L, 2L));
        final BloatedLine line42 = new BloatedLine(123L, null, null, Sets.hashSet(1L, 2L));
        final BloatedLine line43 = new BloatedLine(123L, null, null, Sets.hashSet(1L));

        Assert.assertEquals(line11, line12);
        Assert.assertEquals(line21, line22);
        Assert.assertEquals(line31, line32);
        Assert.assertEquals(line41, line42);

        Assert.assertNotEquals(line11, line21);
        Assert.assertNotEquals(line11, line31);
        Assert.assertNotEquals(line11, line41);
        Assert.assertNotEquals(line21, line23);
        Assert.assertNotEquals(line31, line33);
        Assert.assertNotEquals(line41, line43);
    }

    @Test
    public void testFull()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Line source = atlas.line(18);
        final BloatedLine result = BloatedLine.from(source);
        Assert.assertEquals(source.getIdentifier(), result.getIdentifier());
        Assert.assertEquals(source.bounds(), result.bounds());
        Assert.assertEquals(source.asPolyLine(), result.asPolyLine());
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
        final Line source = atlas.line(18);
        final BloatedLine result = BloatedLine.shallowFrom(source);
        Assert.assertEquals(source.getIdentifier(), result.getIdentifier());
        Assert.assertEquals(source.bounds(), result.bounds());
        result.withPolyLine(PolyLine.TEST_POLYLINE);
        Assert.assertEquals(PolyLine.TEST_POLYLINE.bounds(), result.bounds());
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
    }
}
