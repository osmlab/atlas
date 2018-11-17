package org.openstreetmap.atlas.geography.atlas.bloated;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

/**
 * @author matthieun
 */
public class BloatedAtlasTest
{
    @Test
    public void testAssignment()
    {
        Assert.assertTrue(
                new BloatedNode(123).equals(BloatedAtlas.bloatedEntityFor(123, ItemType.NODE)));
        Assert.assertTrue(
                new BloatedEdge(123).equals(BloatedAtlas.bloatedEntityFor(123, ItemType.EDGE)));
        Assert.assertTrue(
                new BloatedArea(123).equals(BloatedAtlas.bloatedEntityFor(123, ItemType.AREA)));
        Assert.assertTrue(
                new BloatedLine(123).equals(BloatedAtlas.bloatedEntityFor(123, ItemType.LINE)));
        Assert.assertTrue(
                new BloatedPoint(123).equals(BloatedAtlas.bloatedEntityFor(123, ItemType.POINT)));
        Assert.assertTrue(new BloatedRelation(123)
                .equals(BloatedAtlas.bloatedEntityFor(123, ItemType.RELATION)));
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testMemberEquality()
    {
        final BloatedPoint point1 = new BloatedPoint(123);
        final BloatedPoint point11 = new BloatedPoint(123);
        final BloatedPoint point2 = new BloatedPoint(124);
        final BloatedArea area = new BloatedArea(777);

        Assert.assertTrue(point1.equals(point11));
        Assert.assertFalse(point1.equals(point2));
        Assert.assertFalse(point1.equals(area));
    }
}
