package org.openstreetmap.atlas.geography.atlas.items;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.BareAtlasTestRule;

/**
 * @author matthieun
 */
public class ItemTypeTest
{
    @Rule
    public final BareAtlasTestRule rule = new BareAtlasTestRule();

    @Test
    public void testEntityForIdentifier()
    {
        final Atlas atlas = this.rule.getAtlas();
        Assert.assertEquals(atlas.node(1), ItemType.NODE.entityForIdentifier(atlas, 1));
        Assert.assertEquals(atlas.edge(1), ItemType.EDGE.entityForIdentifier(atlas, 1));
        Assert.assertEquals(atlas.area(1), ItemType.AREA.entityForIdentifier(atlas, 1));
        Assert.assertEquals(atlas.line(1), ItemType.LINE.entityForIdentifier(atlas, 1));
        Assert.assertEquals(atlas.point(1), ItemType.POINT.entityForIdentifier(atlas, 1));
        Assert.assertEquals(atlas.relation(1), ItemType.RELATION.entityForIdentifier(atlas, 1));
    }

    @Test
    public void testGetMemberClass()
    {
        Assert.assertEquals(Node.class, ItemType.NODE.getMemberClass());
        Assert.assertEquals(Edge.class, ItemType.EDGE.getMemberClass());
        Assert.assertEquals(Area.class, ItemType.AREA.getMemberClass());
        Assert.assertEquals(Line.class, ItemType.LINE.getMemberClass());
        Assert.assertEquals(Point.class, ItemType.POINT.getMemberClass());
        Assert.assertEquals(Relation.class, ItemType.RELATION.getMemberClass());
    }

    @Test
    public void testToShotsString()
    {
        Assert.assertEquals("N", ItemType.NODE.toShortString());
        Assert.assertEquals("E", ItemType.EDGE.toShortString());
        Assert.assertEquals("A", ItemType.AREA.toShortString());
        Assert.assertEquals("L", ItemType.LINE.toShortString());
        Assert.assertEquals("P", ItemType.POINT.toShortString());
        Assert.assertEquals("R", ItemType.RELATION.toShortString());

        Assert.assertEquals(ItemType.NODE, ItemType.shortValueOf("N"));
        Assert.assertEquals(ItemType.EDGE, ItemType.shortValueOf("E"));
        Assert.assertEquals(ItemType.AREA, ItemType.shortValueOf("A"));
        Assert.assertEquals(ItemType.LINE, ItemType.shortValueOf("L"));
        Assert.assertEquals(ItemType.POINT, ItemType.shortValueOf("P"));
        Assert.assertEquals(ItemType.RELATION, ItemType.shortValueOf("R"));
    }
}
