package org.openstreetmap.atlas.utilities.identifiers;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

/**
 * @author lcram
 */
public class EntityIdentifierGeneratorTest
{
    @Test
    public void testGetFullPropertyString()
    {
        final CompleteEdge edge = new CompleteEdge(1L, PolyLine.SIMPLE_POLYLINE,
                Maps.hashMap("a", "b", "c", "d"), 2L, 3L, Sets.hashSet());

        final String goldenPropertyString = "LINESTRING (1 1, 2 2);a=b,c=d;2;3";

        Assert.assertEquals(goldenPropertyString,
                new EntityIdentifierGenerator().getBasicPropertyString(edge)
                        + new EntityIdentifierGenerator().getTypeSpecificPropertyString(edge));
    }

    @Test
    public void testGetPropertyString()
    {
        final CompletePoint point = new CompletePoint(1L, Location.CENTER,
                Maps.hashMap("a", "b", "c", "d"), Sets.hashSet());

        final String goldenPropertyString = "POINT (0 0);a=b,c=d";
        Assert.assertEquals(goldenPropertyString,
                new EntityIdentifierGenerator().getBasicPropertyString(point));
    }

    @Test
    public void testGetTypeSpecificPropertyStringForEdge()
    {
        final CompleteEdge edge = new CompleteEdge(1L, PolyLine.SIMPLE_POLYLINE,
                Maps.hashMap("a", "b", "c", "d"), 2L, 3L, Sets.hashSet());

        final String goldenPropertyString = ";2;3";
        Assert.assertEquals(goldenPropertyString,
                new EntityIdentifierGenerator().getTypeSpecificPropertyString(edge));
    }

    @Test
    public void testGetTypeSpecificPropertyStringForNode()
    {
        final CompleteNode node = new CompleteNode(1L, Location.CENTER,
                Maps.hashMap("a", "b", "c", "d"), Sets.treeSet(1L, 2L), Sets.treeSet(3L, 4L),
                Sets.hashSet());

        final String goldenPropertyString = ";1,2,;3,4,";
        Assert.assertEquals(goldenPropertyString,
                new EntityIdentifierGenerator().getTypeSpecificPropertyString(node));
    }

    @Test
    public void testGetTypeSpecificPropertyStringForRelation()
    {
        final RelationBean bean = new RelationBean();
        bean.addItem(1L, "role", ItemType.POINT);
        bean.addItem(10L, "role", ItemType.AREA);

        final CompleteRelation relation = new CompleteRelation(1L, Maps.hashMap("a", "b", "c", "d"),
                Rectangle.MINIMUM, bean, null, null, null, Sets.hashSet());

        final String goldenPropertyString = ";RelationBean[(POINT,1,role)(AREA,10,role)]";
        Assert.assertEquals(goldenPropertyString,
                new EntityIdentifierGenerator().getTypeSpecificPropertyString(relation));
    }

    @Test
    public void testHash()
    {
        final CompleteEdge edge = new CompleteEdge(1L, PolyLine.SIMPLE_POLYLINE,
                Maps.hashMap("a", "b", "c", "d"), 2L, 3L, Sets.hashSet());

        final long goldenHash = 5515319119996140692L;
        Assert.assertEquals(5515319119996140692L,
                new EntityIdentifierGenerator().generatePositiveIdentifierForEdge(edge));
    }
}
