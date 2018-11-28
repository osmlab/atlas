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

/**
 * @author matthieun
 */
public class BloatedAreaTest
{
    @Rule
    public BloatedTestRule rule = new BloatedTestRule();

    @Test
    public void testFull()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Area source = atlas.area(27);
        final BloatedArea result = BloatedArea.fromArea(source);
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
        final BloatedArea result = BloatedArea.shallowFromArea(source);
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
