package org.openstreetmap.atlas.geography.atlas.bloated;

import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * @author matthieun
 */
public class BloatedPointTest
{
    @Rule
    public BloatedTestRule rule = new BloatedTestRule();

    @Test
    public void testFull()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Point source = atlas.point(33);
        final BloatedPoint result = BloatedPoint.fromPoint(source);
        Assert.assertEquals(source.getIdentifier(), result.getIdentifier());
        Assert.assertEquals(source.bounds(), result.bounds());
        Assert.assertEquals(source.getLocation(), result.getLocation());
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
        final Point source = atlas.point(33);
        final BloatedPoint result = BloatedPoint.shallowFromPoint(source);
        Assert.assertEquals(source.getIdentifier(), result.getIdentifier());
        Assert.assertEquals(source.bounds(), result.bounds());
        result.withLocation(Location.CENTER);
        // When we update a location, the bounds should expand to include the original location as
        // well as the updated location
        Assert.assertEquals(Rectangle.forLocated(source.bounds(), Location.CENTER),
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

        result.withLocation(Location.COLOSSEUM);
        // When we update the location again, the bounds recalculation should "forget" about the
        // first update
        Assert.assertEquals(Rectangle.forLocated(source.bounds(), Location.COLOSSEUM),
                result.bounds());
    }
}
