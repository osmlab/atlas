package org.openstreetmap.atlas.utilities.identifiers;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

/**
 * @author lcram
 */
public class EntityIdentifierGeneratorTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testEmptyConfigException()
    {
        final CompletePoint point = new CompletePoint(1L, Location.CENTER,
                Maps.hashMap("a", "b", "c", "d"), Sets.hashSet());

        Assert.assertTrue((new EntityIdentifierGenerator.Configuration().getGenerator()
                .getBasicPropertyString(point)
                + new EntityIdentifierGenerator.Configuration().getGenerator()
                        .getTypeSpecificPropertyString(point)).isEmpty());

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage(
                "EntityIdentifierGenerator.Configuration was empty! Please set at least one of geometry, tags, or relation members.");
        new EntityIdentifierGenerator.Configuration().getGenerator().generateIdentifier(point);
    }

    @Test
    public void testForcePositiveIDForEdge()
    {
        final CompleteEdge edge = new CompleteEdge(1L, PolyLine.SIMPLE_POLYLINE,
                Maps.hashMap("a", "b", "c", "d"), 2L, 3L, Sets.hashSet());

        this.expectedException.expect(IllegalArgumentException.class);
        this.expectedException
                .expectMessage("For type EDGE, please use generatePositiveIdentifierForEdge");
        new EntityIdentifierGenerator.Configuration().useDefaults().getGenerator()
                .generateIdentifier(edge);
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

        final File atlas1File = new File("/Users/lucascram/Desktop/ARG_7-41-77.atlas");
        final File atlas2File = new File("/Users/lucascram/Desktop/ARG_7-41-78.atlas");
        final File atlas3File = new File("/Users/lucascram/Desktop/ARG_7-39-82.atlas");
        final EntityIdentifierGenerator generator = new EntityIdentifierGenerator();
        final Atlas atlas1 = new AtlasResourceLoader().load(atlas1File);
        final Atlas atlas2 = new AtlasResourceLoader().load(atlas2File);
        final Atlas atlas3 = new AtlasResourceLoader().load(atlas3File);

        final CompleteRelation relation1 = CompleteRelation
                .from(atlas1.relation(5464611279205775725L));
        final CompleteRelation relation2 = CompleteRelation
                .from(atlas2.relation(5464611279205775725L));
        final CompleteRelation relation3 = CompleteRelation
                .from(atlas3.relation(5464611279205775725L));
        System.err.println(new EntityIdentifierGenerator().getBasicPropertyString(relation1) + ";"
                + new EntityIdentifierGenerator().getTypeSpecificPropertyString(relation1));
        System.err.println(new EntityIdentifierGenerator().getBasicPropertyString(relation2) + ";"
                + new EntityIdentifierGenerator().getTypeSpecificPropertyString(relation2));
        System.err.println(new EntityIdentifierGenerator().getBasicPropertyString(relation3) + ";"
                + new EntityIdentifierGenerator().getTypeSpecificPropertyString(relation3));

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

    @Test
    public void testNonRelationInvariantConfigException()
    {
        final EntityIdentifierGenerator nonRelationInvariantGenerator = new EntityIdentifierGenerator.Configuration()
                .useRelationMembers().getGenerator();

        final RelationBean bean1 = new RelationBean();
        bean1.addItem(1L, "role", ItemType.POINT);
        bean1.addItem(10L, "role", ItemType.AREA);
        final CompleteRelation relation1 = new CompleteRelation(1L,
                Maps.hashMap("a", "b", "c", "d"), Rectangle.MINIMUM, bean1, null, null, null,
                Sets.hashSet());
        Assert.assertEquals(6707509058043000459L,
                nonRelationInvariantGenerator.generateIdentifier(relation1));

        final CompletePoint point = new CompletePoint(1L, Location.CENTER,
                Maps.hashMap("a", "b", "c", "d"), Sets.hashSet());
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage(
                "EntityIdentifierGenerator.Configuration was non-relation invariant! Please set at least one of geometry or tags to generate IDs for non-relation type entities.");
        nonRelationInvariantGenerator.generateIdentifier(point);
    }

    @Test
    public void testUnsetGeometryException()
    {
        final CompletePoint pointNoGeometry = new CompletePoint(1L, null,
                Maps.hashMap("a", "b", "c", "d"), Sets.hashSet());
        final CompletePoint pointNoTags = new CompletePoint(1L, Location.CENTER, null,
                Sets.hashSet());

        new EntityIdentifierGenerator.Configuration().useTags().getGenerator()
                .generateIdentifier(pointNoGeometry);
        new EntityIdentifierGenerator.Configuration().useGeometry().getGenerator()
                .generateIdentifier(pointNoTags);

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Geometry must be set for entity");
        new EntityIdentifierGenerator().generateIdentifier(pointNoGeometry);
    }

    @Test
    public void testUnsetRelationMembersException()
    {
        final RelationBean bean1 = new RelationBean();
        bean1.addItem(1L, "role", ItemType.POINT);
        bean1.addItem(10L, "role", ItemType.AREA);
        final CompleteRelation relationWithMembers = new CompleteRelation(1L,
                Maps.hashMap("a", "b", "c", "d"), Rectangle.MINIMUM, bean1, null, null, null,
                Sets.hashSet());
        final CompleteRelation relationNoMembers = new CompleteRelation(1L,
                Maps.hashMap("a", "b", "c", "d"), Rectangle.MINIMUM, null, null, null, null,
                Sets.hashSet());

        new EntityIdentifierGenerator.Configuration().useDefaults().getGenerator()
                .generateIdentifier(relationWithMembers);
        new EntityIdentifierGenerator.Configuration().useDefaults().excludeRelationMembers()
                .getGenerator().generateIdentifier(relationNoMembers);

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Relation members must be set for entity");
        new EntityIdentifierGenerator().generateIdentifier(relationNoMembers);
    }

    @Test
    public void testUnsetTagsException()
    {
        final CompletePoint pointNoGeometry = new CompletePoint(1L, null,
                Maps.hashMap("a", "b", "c", "d"), Sets.hashSet());
        final CompletePoint pointNoTags = new CompletePoint(1L, Location.CENTER, null,
                Sets.hashSet());

        new EntityIdentifierGenerator.Configuration().useTags().getGenerator()
                .generateIdentifier(pointNoGeometry);
        new EntityIdentifierGenerator.Configuration().useGeometry().getGenerator()
                .generateIdentifier(pointNoTags);

        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Tags must be set for entity");
        new EntityIdentifierGenerator().generateIdentifier(pointNoTags);
    }
}
