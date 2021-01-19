package org.openstreetmap.atlas.utilities.identifiers;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
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
    public void testEmptyConfig()
    {
        final CompletePoint point = new CompletePoint(1L, Location.CENTER,
                Maps.hashMap("a", "b", "c", "d"), Sets.hashSet());
        final CompleteEdge edge1 = new CompleteEdge(2L, PolyLine.SIMPLE_POLYLINE,
                Maps.hashMap("e", "f", "g", "h"), 2L, 3L, Sets.hashSet());
        final CompleteEdge edge2 = new CompleteEdge(3L, PolyLine.TEST_POLYLINE_2,
                Maps.hashMap("hello", "world"), 200L, 300L, Sets.hashSet());
        final CompleteArea area = new CompleteArea(3L, Polygon.SILICON_VALLEY,
                Maps.hashMap("i", "j", "k", "l"), Sets.hashSet());

        Assert.assertTrue((new EntityIdentifierGenerator.Configuration().getGenerator()
                .getBasicPropertyString(point)
                + new EntityIdentifierGenerator.Configuration().getGenerator()
                        .getTypeSpecificPropertyString(point)).isEmpty());
        Assert.assertTrue((new EntityIdentifierGenerator.Configuration().getGenerator()
                .getBasicPropertyString(edge1)
                + new EntityIdentifierGenerator.Configuration().getGenerator()
                        .getTypeSpecificPropertyString(edge1)).isEmpty());
        Assert.assertTrue((new EntityIdentifierGenerator.Configuration().getGenerator()
                .getBasicPropertyString(edge2)
                + new EntityIdentifierGenerator.Configuration().getGenerator()
                        .getTypeSpecificPropertyString(edge2)).isEmpty());
        Assert.assertTrue((new EntityIdentifierGenerator.Configuration().getGenerator()
                .getBasicPropertyString(area)
                + new EntityIdentifierGenerator.Configuration().getGenerator()
                        .getTypeSpecificPropertyString(area)).isEmpty());

        Assert.assertEquals(
                new EntityIdentifierGenerator.Configuration().getGenerator()
                        .generateIdentifier(point),
                new EntityIdentifierGenerator.Configuration().getGenerator()
                        .generateIdentifier(area));
        Assert.assertEquals(
                new EntityIdentifierGenerator.Configuration().getGenerator()
                        .generatePositiveIdentifierForEdge(edge1),
                new EntityIdentifierGenerator.Configuration().getGenerator()
                        .generatePositiveIdentifierForEdge(edge2));
    }

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
        Assert.assertEquals(goldenHash3, new EntityIdentifierGenerator.Configuration().useDefaults()
                .excludeTags().excludeRelationMembers().getGenerator().generateIdentifier(point));

        final long goldenHash4 = 3739460904040018219L;
        Assert.assertEquals(goldenHash4, new EntityIdentifierGenerator.Configuration().useTags()
                .getGenerator().generateIdentifier(point));
        Assert.assertEquals(goldenHash4,
                new EntityIdentifierGenerator.Configuration().useDefaults().excludeGeometry()
                        .excludeRelationMembers().getGenerator().generateIdentifier(point));
    }
}
