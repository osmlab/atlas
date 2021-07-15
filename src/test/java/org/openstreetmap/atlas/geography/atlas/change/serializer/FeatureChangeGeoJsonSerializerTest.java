package org.openstreetmap.atlas.geography.atlas.change.serializer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.change.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChangeUnitTestFactory;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

import com.google.common.collect.Lists;

/**
 * @author matthieun
 */
public class FeatureChangeGeoJsonSerializerTest
{
    private static final Map<String, String> TAGS = Maps.hashMap("tagKey1", "tagValue1", "tagKey2",
            "tagValue2");
    private static final Set<Long> RELATIONS = Sets.hashSet(444L, 555L);

    @Rule
    public FeatureChangeGeoJsonSerializerTestRule rule = new FeatureChangeGeoJsonSerializerTestRule();

    @Test
    public void testBigFeatureChange()
    {
        final Atlas longWaterWay = this.rule.longWaterWayAtlas();
        final Line waterway = longWaterWay.line(374902834000000L);
        final List<Location> points = Iterables.stream(waterway).collectToList();
        Collections.reverse(points);
        final FeatureChange featureChange = FeatureChange.add(
                (AtlasEntity) CompleteLine.shallowFrom(waterway).withGeometry(points), longWaterWay,
                FeatureChange.Options.OSC_IF_POSSIBLE);

        assertResourceEquals(featureChange, "serializedReverseWay.json", true);
    }

    @Test
    public void testDescriptionSerializationAddEdge()
    {
        final CompleteEdge edge = new CompleteEdge(123L, PolyLine.TEST_POLYLINE,
                Maps.hashMap("a", "1", "b", "2"), 1L, 2L, Sets.hashSet(1L, 2L));
        final FeatureChange featureChange = FeatureChange.add(edge);
        assertResourceEquals(featureChange, "serializedAddEdgeWithDescription.json", true);
    }

    @Test
    public void testDescriptionSerializationAddRelation()
    {
        final RelationBean bean = new RelationBean();
        bean.add(new RelationBean.RelationBeanItem(500L, "a", ItemType.POINT));
        bean.add(new RelationBean.RelationBeanItem(600L, "a", ItemType.AREA));
        final CompleteRelation relation = new CompleteRelation(123L, Maps.hashMap("a", "1"),
                Rectangle.TEST_RECTANGLE, bean, null, null, null, Sets.hashSet(1L, 2L));
        final FeatureChange featureChange = FeatureChange.add(relation);
        assertResourceEquals(featureChange, "serializedAddRelationWithDescription.json", true);
    }

    @Test
    public void testDescriptionSerializationRemoveArea()
    {
        final CompleteArea area = new CompleteArea(123L, Polygon.SILICON_VALLEY,
                Maps.hashMap("a", "1"), Sets.hashSet(1L));
        final FeatureChange featureChange = FeatureChange.remove(area);
        assertResourceEquals(featureChange, "serializedRemoveAreaWithDescription.json", true);
    }

    @Test
    public void testDescriptionSerializationUpdateEdge()
    {
        final CompleteEdge beforeEdge = new CompleteEdge(123L, PolyLine.TEST_POLYLINE,
                Maps.hashMap("a", "1", "b", "2"), 1L, 2L, Sets.hashSet(1L, 2L));
        final CompleteEdge afterEdge = new CompleteEdge(123L, PolyLine.TEST_POLYLINE_2,
                Maps.hashMap("b", "2a", "c", "3"), 10L, 20L, Sets.hashSet(2L, 3L));
        final FeatureChange featureChange = FeatureChangeUnitTestFactory.build(ChangeType.ADD,
                afterEdge, beforeEdge);
        assertResourceEquals(featureChange, "serializedUpdateEdgeWithDescription.json", true);
    }

    @Test
    public void testFullAreaSerialization()
    {
        final CompleteArea item = new CompleteArea(123L, Polygon.TEST_BUILDING, TAGS, RELATIONS);
        final FeatureChange featureChange = FeatureChange.add(item);
        featureChange.addMetaData("key", "value");
        assertResourceEquals(featureChange, "serializedAreaFull.json", false);
    }

    @Test
    public void testFullEdgeSerialization()
    {
        final CompleteEdge item = new CompleteEdge(123L, PolyLine.TEST_POLYLINE, TAGS, 456L, 789L,
                RELATIONS);
        final FeatureChange featureChange = FeatureChange.add(item);
        featureChange.addMetaData("key", "value");
        assertResourceEquals(featureChange, "serializedEdgeFull.json", false);
    }

    @Test
    public void testFullLineSerialization()
    {
        final CompleteLine item = new CompleteLine(123L, PolyLine.TEST_POLYLINE, TAGS, RELATIONS);
        final FeatureChange featureChange = FeatureChange.add(item);
        featureChange.addMetaData("key", "value");
        assertResourceEquals(featureChange, "serializedLineFull.json", false);
    }

    @Test
    public void testFullNodeSerialization()
    {
        final CompleteNode item = new CompleteNode(123L, Location.COLOSSEUM, TAGS,
                Sets.treeSet(456L, 789L), Sets.treeSet(456L, 789L), RELATIONS);
        final FeatureChange featureChange = FeatureChange.add(item);
        featureChange.addMetaData("key", "value");
        assertResourceEquals(featureChange, "serializedNodeFull.json", false);
    }

    @Test
    public void testFullPointSerialization()
    {
        final CompletePoint item = new CompletePoint(123L, Location.COLOSSEUM, TAGS, RELATIONS);
        final FeatureChange featureChange = FeatureChange.add(item);
        featureChange.addMetaData("key", "value");
        assertResourceEquals(featureChange, "serializedPointFull.json", false);
    }

    @Test
    public void testFullRelationSerialization()
    {
        final RelationBean members = new RelationBean();
        members.addItem(456L, "role1", ItemType.EDGE);
        members.addItem(789L, "role2", ItemType.AREA);
        final CompleteRelation item = new CompleteRelation(123L, TAGS, Rectangle.TEST_RECTANGLE,
                members, Lists.newArrayList(123L), members, 123L, RELATIONS);
        final FeatureChange featureChange = FeatureChange.add(item);
        featureChange.addMetaData("key", "value");
        assertResourceEquals(featureChange, "serializedRelationFull.json", false);
    }

    @Test
    public void testNullAreaSerialization()
    {
        final CompleteArea item = new CompleteArea(123L, Polygon.TEST_BUILDING, null, null);
        final FeatureChange featureChange = FeatureChange.add(item);
        featureChange.addMetaData("key", "value");
        assertResourceEquals(featureChange, "serializedAreaNull.json", false);
    }

    @Test
    public void testNullEdgeSerialization()
    {
        final CompleteEdge item = new CompleteEdge(123L, PolyLine.TEST_POLYLINE, null, null, null,
                null);
        final FeatureChange featureChange = FeatureChange.add(item);
        featureChange.addMetaData("key", "value");
        assertResourceEquals(featureChange, "serializedEdgeNull.json", false);
    }

    @Test
    public void testNullLineSerialization()
    {
        final CompleteLine item = new CompleteLine(123L, PolyLine.TEST_POLYLINE, null, null);
        final FeatureChange featureChange = FeatureChange.add(item);
        featureChange.addMetaData("key", "value");
        assertResourceEquals(featureChange, "serializedLineNull.json", false);
    }

    @Test
    public void testNullNodeSerialization()
    {
        final CompleteNode item = new CompleteNode(123L, Location.COLOSSEUM, null, null, null,
                null);
        final FeatureChange featureChange = FeatureChange.add(item);
        featureChange.addMetaData("key", "value");
        assertResourceEquals(featureChange, "serializedNodeNull.json", false);
    }

    @Test
    public void testNullPointSerialization()
    {
        final CompletePoint item = new CompletePoint(123L, Location.COLOSSEUM, null, null);
        final FeatureChange featureChange = FeatureChange.add(item);
        featureChange.addMetaData("key", "value");
        assertResourceEquals(featureChange, "serializedPointNull.json", false);
    }

    @Test
    public void testNullRelationSerialization()
    {
        final RelationBean members = new RelationBean();
        members.addItem(456L, "role1", ItemType.EDGE);
        members.addItem(789L, "role2", ItemType.AREA);
        final CompleteRelation item = new CompleteRelation(123L, null, Rectangle.TEST_RECTANGLE,
                members, null, null, null, null);
        final FeatureChange featureChange = FeatureChange.add(item);
        featureChange.addMetaData("key", "value");
        assertResourceEquals(featureChange, "serializedRelationNull.json", false);
    }

    @Test
    public void testPointTagMapOrderingSerialization()
    {
        final Map<String, String> tags = Maps.hashMap("tagKey5", "tagValue5", "tagKey1",
                "tagValue1", "tagKey3", "tagValue3", "tagKey6", "tagValue6", "tagKey4", "tagValue4",
                "tagKey2", "tagValue2");
        final CompletePoint item = new CompletePoint(123L, Location.COLOSSEUM, tags, RELATIONS);
        final FeatureChange featureChange = FeatureChange.add(item);
        featureChange.addMetaData("key3", "value3");
        featureChange.addMetaData("key1", "value1");
        featureChange.addMetaData("key2", "value2");
        assertResourceEquals(featureChange, "serializedPointWithTags.json", false);
    }

    @Test
    public void testRemoveAreaSerialization()
    {
        final CompleteArea item = CompleteArea
                .shallowFrom(new CompleteArea(123L, Polygon.TEST_BUILDING, null, null));
        final FeatureChange featureChange = FeatureChange.remove(item);
        featureChange.addMetaData("key", "value");
        assertResourceEquals(featureChange, "serializedAreaRemove.json", false);
    }

    @Test
    public void testRemoveEdgeSerialization()
    {
        final CompleteEdge item = CompleteEdge.shallowFrom(
                new CompleteEdge(123L, PolyLine.TEST_POLYLINE, null, null, null, null));
        final FeatureChange featureChange = FeatureChange.remove(item);
        featureChange.addMetaData("key", "value");
        assertResourceEquals(featureChange, "serializedEdgeRemove.json", false);
    }

    @Test
    public void testRemoveLineSerialization()
    {
        final CompleteLine item = CompleteLine
                .shallowFrom(new CompleteLine(123L, PolyLine.TEST_POLYLINE, null, null));
        final FeatureChange featureChange = FeatureChange.remove(item);
        featureChange.addMetaData("key", "value");
        assertResourceEquals(featureChange, "serializedLineRemove.json", false);
    }

    @Test
    public void testRemoveNodeSerialization()
    {
        final CompleteNode item = CompleteNode
                .shallowFrom(new CompleteNode(123L, Location.COLOSSEUM, null, null, null, null));
        final FeatureChange featureChange = FeatureChange.remove(item);
        featureChange.addMetaData("key", "value");
        assertResourceEquals(featureChange, "serializedNodeRemove.json", false);
    }

    @Test
    public void testRemovePointSerialization()
    {
        final CompletePoint item = CompletePoint
                .shallowFrom(new CompletePoint(123L, Location.COLOSSEUM, null, null));
        final FeatureChange featureChange = FeatureChange.remove(item);
        featureChange.addMetaData("key", "value");
        assertResourceEquals(featureChange, "serializedPointRemove.json", false);
    }

    @Test
    public void testRemoveRelationSerialization()
    {
        final RelationBean members = new RelationBean();
        members.addItem(456L, "role1", ItemType.EDGE);
        members.addItem(789L, "role2", ItemType.AREA);
        final CompleteRelation temporary = new CompleteRelation(123L, null,
                Rectangle.TEST_RECTANGLE, members, null, null, null, null);
        final CompleteRelation item = CompleteRelation.shallowFrom(temporary);
        final FeatureChange featureChange = FeatureChange.remove(item);
        featureChange.addMetaData("key", "value");
        assertResourceEquals(featureChange, "serializedRelationRemove.json", false);
    }

    private void assertResourceEquals(final FeatureChange featureChange, final String fileName,
            final boolean checkDescription)
    {
        final String expected = new InputStreamResource(
                () -> FeatureChangeGeoJsonSerializerTest.class.getResourceAsStream(fileName)).all();
        Assert.assertEquals(expected, featureChange.toPrettyGeoJson(checkDescription));
        Assert.assertEquals(expected.replaceAll(System.lineSeparator() + " *", "")
                .replaceAll("(?<!Member): *", ":"), featureChange.toGeoJson(checkDescription));
        final File temporary = File.temporary();
        try
        {
            featureChange.save(temporary, checkDescription);
            Assert.assertEquals(expected, temporary.all());
        }
        finally
        {
            temporary.delete();
        }
    }
}
