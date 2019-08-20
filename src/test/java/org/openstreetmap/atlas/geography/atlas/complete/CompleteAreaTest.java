package org.openstreetmap.atlas.geography.atlas.complete;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
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
public class CompleteAreaTest
{
    @Rule
    public CompleteTestRule rule = new CompleteTestRule();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testAreaShallowCopyNullBounds()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("bounds were null");

        final CompleteArea area = new CompleteArea(1L, null, null, null);
        CompleteArea.shallowFrom(area);
    }

    @Test
    public void testBloatedEquals()
    {
        final CompleteArea area11 = new CompleteArea(123L, null, null, null);
        final CompleteArea area12 = new CompleteArea(123L, null, null, null);
        final CompleteArea area21 = new CompleteArea(123L, Polygon.SILICON_VALLEY, null, null);
        final CompleteArea area22 = new CompleteArea(123L, Polygon.SILICON_VALLEY, null, null);
        final CompleteArea area23 = new CompleteArea(123L, Polygon.SILICON_VALLEY_2, null, null);
        final CompleteArea area31 = new CompleteArea(123L, null, Maps.hashMap("key", "value"),
                null);
        final CompleteArea area32 = new CompleteArea(123L, null, Maps.hashMap("key", "value"),
                null);
        final CompleteArea area33 = new CompleteArea(123L, null, Maps.hashMap(), null);
        final CompleteArea area41 = new CompleteArea(123L, null, null, Sets.hashSet(1L, 2L));
        final CompleteArea area42 = new CompleteArea(123L, null, null, Sets.hashSet(1L, 2L));
        final CompleteArea area43 = new CompleteArea(123L, null, null, Sets.hashSet(1L));

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
        final CompleteArea result = CompleteArea.from(source);
        Assert.assertEquals(source.getIdentifier(), result.getIdentifier());
        Assert.assertEquals(source.bounds(), result.bounds());
        Assert.assertEquals(source.asPolygon(), result.asPolygon());
        Assert.assertEquals(source.getTags(), result.getTags());
        Assert.assertEquals(
                source.relations().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toSet()),
                result.relations().stream().map(Relation::getIdentifier)
                        .collect(Collectors.toSet()));

        Assert.assertEquals(result, result.copy());
    }

    @Test
    public void testIsCompletelyShallow()
    {
        final CompleteArea superShallow = new CompleteArea(123L, null, null, null);
        Assert.assertTrue(superShallow.isShallow());
    }

    @Test
    public void testNonFullAreaCopy()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("but it was not full");

        final CompleteArea area = new CompleteArea(1L, null, null, null);
        CompleteArea.from(area);
    }

    @Test
    public void testShallow()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Area source = atlas.area(27);
        final CompleteArea result = CompleteArea.shallowFrom(source);
        Assert.assertEquals(source.getIdentifier(), result.getIdentifier());
        Assert.assertEquals(source.bounds(), result.bounds());
        result.withPolygon(Polygon.TEST_BUILDING);
        // When we update a polygon, the bounds should update to the bounds of the new polygon
        Assert.assertEquals(Rectangle.forLocated(Polygon.TEST_BUILDING), result.bounds());
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
        Assert.assertEquals(Rectangle.forLocated(Polygon.SILICON_VALLEY), result.bounds());
    }

    @Test
    public void testToWkt()
    {
        final CompleteArea area1 = new CompleteArea(123L);
        area1.withPolygon(
                Rectangle.forCorners(Location.forString("0,0"), Location.forString("1,1")));
        Assert.assertEquals("POLYGON ((0 0, 0 1, 1 1, 1 0, 0 0))", area1.toWkt());

        final CompleteArea area2 = new CompleteArea(123L);
        Assert.assertNull(area2.toWkt());
    }

    @Test
    public void testWithGeometry()
    {
        final CompleteArea area = new CompleteArea(1L);
        area.withGeometry(
                Arrays.asList(Location.COLOSSEUM, Location.CENTER, Location.EIFFEL_TOWER));
        Assert.assertEquals(
                new Polygon(
                        Arrays.asList(Location.COLOSSEUM, Location.CENTER, Location.EIFFEL_TOWER)),
                area.asPolygon());
    }
}
