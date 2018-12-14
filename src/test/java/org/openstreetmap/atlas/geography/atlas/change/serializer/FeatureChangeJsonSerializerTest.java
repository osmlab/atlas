package org.openstreetmap.atlas.geography.atlas.change.serializer;

import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedArea;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedEdge;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedLine;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedNode;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedPoint;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedRelation;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.change.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

import com.google.common.collect.Lists;

/**
 * @author matthieun
 */
public class FeatureChangeJsonSerializerTest
{
    private static final Map<String, String> TAGS = Maps.hashMap("tagKey1", "tagValue1", "tagKey2",
            "tagValue2");
    private static final Set<Long> RELATIONS = Sets.hashSet(444L, 555L);

    @Test
    public void testFullAreaSerialization()
    {
        final BloatedArea item = new BloatedArea(123L, Polygon.TEST_BUILDING, TAGS, RELATIONS);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        final String result = new FeatureChangeJsonSerializer().convert(featureChange);
        Assert.assertEquals(new InputStreamResource(() -> FeatureChangeJsonSerializerTest.class
                .getResourceAsStream("serializedAreaFull.json")).all(), result);
    }

    @Test
    public void testFullEdgeSerialization()
    {
        final BloatedEdge item = new BloatedEdge(123L, PolyLine.TEST_POLYLINE, TAGS, 456L, 789L,
                RELATIONS);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        final String result = new FeatureChangeJsonSerializer().convert(featureChange);
        Assert.assertEquals(new InputStreamResource(() -> FeatureChangeJsonSerializerTest.class
                .getResourceAsStream("serializedEdgeFull.json")).all(), result);
    }

    @Test
    public void testFullLineSerialization()
    {
        final BloatedLine item = new BloatedLine(123L, PolyLine.TEST_POLYLINE, TAGS, RELATIONS);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        final String result = new FeatureChangeJsonSerializer().convert(featureChange);
        Assert.assertEquals(new InputStreamResource(() -> FeatureChangeJsonSerializerTest.class
                .getResourceAsStream("serializedLineFull.json")).all(), result);
    }

    @Test
    public void testFullNodeSerialization()
    {
        final BloatedNode item = new BloatedNode(123L, Location.COLOSSEUM, TAGS,
                Sets.treeSet(456L, 789L), Sets.treeSet(456L, 789L), RELATIONS);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        final String result = new FeatureChangeJsonSerializer().convert(featureChange);
        Assert.assertEquals(new InputStreamResource(() -> FeatureChangeJsonSerializerTest.class
                .getResourceAsStream("serializedNodeFull.json")).all(), result);
    }

    @Test
    public void testFullPointSerialization()
    {
        final BloatedPoint item = new BloatedPoint(123L, Location.COLOSSEUM, TAGS, RELATIONS);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        final String result = new FeatureChangeJsonSerializer().convert(featureChange);
        Assert.assertEquals(new InputStreamResource(() -> FeatureChangeJsonSerializerTest.class
                .getResourceAsStream("serializedPointFull.json")).all(), result);
    }

    @Test
    public void testFullRelationSerialization()
    {
        final RelationBean members = new RelationBean();
        members.addItem(456L, "role1", ItemType.EDGE);
        members.addItem(789L, "role2", ItemType.AREA);
        final BloatedRelation item = new BloatedRelation(123L, TAGS, Rectangle.TEST_RECTANGLE,
                members, Lists.newArrayList(123L), members, 123L, RELATIONS);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        final String result = new FeatureChangeJsonSerializer().convert(featureChange);
        Assert.assertEquals(new InputStreamResource(() -> FeatureChangeJsonSerializerTest.class
                .getResourceAsStream("serializedRelationFull.json")).all(), result);
    }

    @Test
    public void testNullAreaSerialization()
    {
        final BloatedArea item = new BloatedArea(123L, Polygon.TEST_BUILDING, null, null);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        final String result = new FeatureChangeJsonSerializer().convert(featureChange);
        Assert.assertEquals(new InputStreamResource(() -> FeatureChangeJsonSerializerTest.class
                .getResourceAsStream("serializedAreaNull.json")).all(), result);
    }

    @Test
    public void testNullEdgeSerialization()
    {
        final BloatedEdge item = new BloatedEdge(123L, PolyLine.TEST_POLYLINE, null, null, null,
                null);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        final String result = new FeatureChangeJsonSerializer().convert(featureChange);
        Assert.assertEquals(new InputStreamResource(() -> FeatureChangeJsonSerializerTest.class
                .getResourceAsStream("serializedEdgeNull.json")).all(), result);
    }

    @Test
    public void testNullLineSerialization()
    {
        final BloatedLine item = new BloatedLine(123L, PolyLine.TEST_POLYLINE, null, null);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        final String result = new FeatureChangeJsonSerializer().convert(featureChange);
        Assert.assertEquals(new InputStreamResource(() -> FeatureChangeJsonSerializerTest.class
                .getResourceAsStream("serializedLineNull.json")).all(), result);
    }

    @Test
    public void testNullNodeSerialization()
    {
        final BloatedNode item = new BloatedNode(123L, Location.COLOSSEUM, null, null, null, null);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        final String result = new FeatureChangeJsonSerializer().convert(featureChange);
        Assert.assertEquals(new InputStreamResource(() -> FeatureChangeJsonSerializerTest.class
                .getResourceAsStream("serializedNodeNull.json")).all(), result);
    }

    @Test
    public void testNullPointSerialization()
    {
        final BloatedPoint item = new BloatedPoint(123L, Location.COLOSSEUM, null, null);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        final String result = new FeatureChangeJsonSerializer().convert(featureChange);
        Assert.assertEquals(new InputStreamResource(() -> FeatureChangeJsonSerializerTest.class
                .getResourceAsStream("serializedPointNull.json")).all(), result);
    }

    @Test
    public void testNullRelationSerialization()
    {
        final RelationBean members = new RelationBean();
        members.addItem(456L, "role1", ItemType.EDGE);
        members.addItem(789L, "role2", ItemType.AREA);
        final BloatedRelation item = new BloatedRelation(123L, null, Rectangle.TEST_RECTANGLE,
                members, null, null, null, null);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        final String result = new FeatureChangeJsonSerializer().convert(featureChange);
        Assert.assertEquals(new InputStreamResource(() -> FeatureChangeJsonSerializerTest.class
                .getResourceAsStream("serializedRelationNull.json")).all(), result);
    }
}
