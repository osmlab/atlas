package org.openstreetmap.atlas.geography.atlas.complete;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.complete.EmptyAtlas;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;

/**
 * @author matthieun
 */
public class EmptyAtlasTest
{
    @Test
    public void testAssignment()
    {
        Assert.assertTrue(
                new CompleteNode(123).equals(new EmptyAtlas().entity(123, ItemType.NODE)));
        Assert.assertTrue(
                new CompleteEdge(123).equals(new EmptyAtlas().entity(123, ItemType.EDGE)));
        Assert.assertTrue(
                new CompleteArea(123).equals(new EmptyAtlas().entity(123, ItemType.AREA)));
        Assert.assertTrue(
                new CompleteLine(123).equals(new EmptyAtlas().entity(123, ItemType.LINE)));
        Assert.assertTrue(
                new CompletePoint(123).equals(new EmptyAtlas().entity(123, ItemType.POINT)));
        Assert.assertTrue(
                new CompleteRelation(123).equals(new EmptyAtlas().entity(123, ItemType.RELATION)));
    }

    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testMemberEquality()
    {
        final CompletePoint point1 = new CompletePoint(123);
        final CompletePoint point11 = new CompletePoint(123);
        final CompletePoint point2 = new CompletePoint(124);
        final CompleteArea area = new CompleteArea(777);

        Assert.assertTrue(point1.equals(point11));
        Assert.assertFalse(point1.equals(point2));
        Assert.assertFalse(point1.equals(area));
    }
}
