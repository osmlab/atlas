package org.openstreetmap.atlas.utilities.identifiers;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
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

        final String goldenPropertyStringAll = "LINESTRING (1 1, 2 2);a=b,c=d";
        Assert.assertEquals(goldenPropertyStringAll,
                new EntityIdentifierGenerator.Configuration().useDefaults().getGenerator()
                        .getBasicPropertyString(edge)
                        + new EntityIdentifierGenerator().getTypeSpecificPropertyString(edge));

        final String goldenPropertyStringGeometryOnly = "LINESTRING (1 1, 2 2)";
        Assert.assertEquals(goldenPropertyStringGeometryOnly,
                new EntityIdentifierGenerator.Configuration().useGeometry().getGenerator()
                        .getBasicPropertyString(edge)
                        + new EntityIdentifierGenerator().getTypeSpecificPropertyString(edge));

        final String goldenPropertyStringTagsOnly = ";a=b,c=d";
        Assert.assertEquals(goldenPropertyStringTagsOnly,
                new EntityIdentifierGenerator.Configuration().useTags().getGenerator()
                        .getBasicPropertyString(edge)
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
    public void testGetTypeSpecificPropertyStringForRelation()
    {
        final RelationBean bean1 = new RelationBean();
        bean1.addItem(1L, "role", ItemType.POINT);
        bean1.addItem(10L, "role", ItemType.AREA);

        final RelationBean bean2 = new RelationBean();
        bean2.addItem(10L, "role", ItemType.AREA);
        bean2.addItem(1L, "role", ItemType.POINT);

        final CompleteRelation relation1 = new CompleteRelation(1L,
                Maps.hashMap("a", "b", "c", "d"), Rectangle.MINIMUM, bean1, null, null, null,
                Sets.hashSet());
        final CompleteRelation relation2 = new CompleteRelation(1L,
                Maps.hashMap("a", "b", "c", "d"), Rectangle.MINIMUM, bean2, null, null, null,
                Sets.hashSet());

        final String goldenPropertyString = ";RelationBean[(AREA,10,role)(POINT,1,role)]";
        Assert.assertEquals(goldenPropertyString,
                new EntityIdentifierGenerator().getTypeSpecificPropertyString(relation1));
        Assert.assertEquals(goldenPropertyString,
                new EntityIdentifierGenerator().getTypeSpecificPropertyString(relation2));
        Assert.assertTrue(
                new EntityIdentifierGenerator.Configuration().useDefaults().excludeRelationMembers()
                        .getGenerator().getTypeSpecificPropertyString(relation1).isEmpty());
        Assert.assertEquals(goldenPropertyString, new EntityIdentifierGenerator.Configuration()
                .useRelationMembers().getGenerator().getTypeSpecificPropertyString(relation2));
    }

    @Test
    public void testHashes()
    {
        final CompleteEdge edge = new CompleteEdge(1L, PolyLine.SIMPLE_POLYLINE,
                Maps.hashMap("a", "b", "c", "d"), 2L, 3L, Sets.hashSet());

        final long goldenHash1 = 6463671242943641314L;
        Assert.assertEquals(goldenHash1,
                new EntityIdentifierGenerator().generatePositiveIdentifierForEdge(edge));

        final CompletePoint point = new CompletePoint(1L, Location.CENTER,
                Maps.hashMap("a", "b", "c", "d"), Sets.hashSet());

        final long goldenHash2 = 4334702026426103264L;
        Assert.assertEquals(goldenHash2, new EntityIdentifierGenerator().generateIdentifier(point));
        Assert.assertEquals(goldenHash2, new EntityIdentifierGenerator.Configuration().useDefaults()
                .getGenerator().generateIdentifier(point));

        final long goldenHash3 = -2934213421148195810L;
        Assert.assertEquals(goldenHash3, new EntityIdentifierGenerator.Configuration().useGeometry()
                .getGenerator().generateIdentifier(point));

        final long goldenHash4 = 3739460904040018219L;
        Assert.assertEquals(goldenHash4, new EntityIdentifierGenerator.Configuration().useTags()
                .getGenerator().generateIdentifier(point));
    }
}
