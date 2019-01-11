package org.openstreetmap.atlas.geography.atlas.change.serializer;

import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.builder.RelationBean;
import org.openstreetmap.atlas.geography.atlas.change.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteRelation;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
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

    @Test
    public void testFullAreaSerialization()
    {
        final CompleteArea item = new CompleteArea(123L, Polygon.TEST_BUILDING, TAGS, RELATIONS);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        assertEquals(featureChange, "serializedAreaFull.json");
    }

    @Test
    public void testFullEdgeSerialization()
    {
        final CompleteEdge item = new CompleteEdge(123L, PolyLine.TEST_POLYLINE, TAGS, 456L, 789L,
                RELATIONS);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        assertEquals(featureChange, "serializedEdgeFull.json");
    }

    @Test
    public void testFullLineSerialization()
    {
        final CompleteLine item = new CompleteLine(123L, PolyLine.TEST_POLYLINE, TAGS, RELATIONS);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        assertEquals(featureChange, "serializedLineFull.json");
    }

    @Test
    public void testFullNodeSerialization()
    {
        final CompleteNode item = new CompleteNode(123L, Location.COLOSSEUM, TAGS,
                Sets.treeSet(456L, 789L), Sets.treeSet(456L, 789L), RELATIONS);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        assertEquals(featureChange, "serializedNodeFull.json");
    }

    @Test
    public void testFullPointSerialization()
    {
        final CompletePoint item = new CompletePoint(123L, Location.COLOSSEUM, TAGS, RELATIONS);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        assertEquals(featureChange, "serializedPointFull.json");
    }

    @Test
    public void testFullRelationSerialization()
    {
        final RelationBean members = new RelationBean();
        members.addItem(456L, "role1", ItemType.EDGE);
        members.addItem(789L, "role2", ItemType.AREA);
        final CompleteRelation item = new CompleteRelation(123L, TAGS, Rectangle.TEST_RECTANGLE,
                members, Lists.newArrayList(123L), members, 123L, RELATIONS);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        assertEquals(featureChange, "serializedRelationFull.json");
    }

    @Test
    public void testNullAreaSerialization()
    {
        final CompleteArea item = new CompleteArea(123L, Polygon.TEST_BUILDING, null, null);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        assertEquals(featureChange, "serializedAreaNull.json");
    }

    @Test
    public void testNullEdgeSerialization()
    {
        final CompleteEdge item = new CompleteEdge(123L, PolyLine.TEST_POLYLINE, null, null, null,
                null);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        assertEquals(featureChange, "serializedEdgeNull.json");
    }

    @Test
    public void testNullLineSerialization()
    {
        final CompleteLine item = new CompleteLine(123L, PolyLine.TEST_POLYLINE, null, null);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        assertEquals(featureChange, "serializedLineNull.json");
    }

    @Test
    public void testNullNodeSerialization()
    {
        final CompleteNode item = new CompleteNode(123L, Location.COLOSSEUM, null, null, null,
                null);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        assertEquals(featureChange, "serializedNodeNull.json");
    }

    @Test
    public void testNullPointSerialization()
    {
        final CompletePoint item = new CompletePoint(123L, Location.COLOSSEUM, null, null);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        assertEquals(featureChange, "serializedPointNull.json");
    }

    @Test
    public void testNullRelationSerialization()
    {
        final RelationBean members = new RelationBean();
        members.addItem(456L, "role1", ItemType.EDGE);
        members.addItem(789L, "role2", ItemType.AREA);
        final CompleteRelation item = new CompleteRelation(123L, null, Rectangle.TEST_RECTANGLE,
                members, null, null, null, null);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, item);
        assertEquals(featureChange, "serializedRelationNull.json");
    }

    @Test
    public void testRemoveAreaSerialization()
    {
        final CompleteArea item = CompleteArea
                .shallowFrom(new CompleteArea(123L, Polygon.TEST_BUILDING, null, null));
        final FeatureChange featureChange = new FeatureChange(ChangeType.REMOVE, item);
        assertEquals(featureChange, "serializedAreaRemove.json");
    }

    @Test
    public void testRemoveEdgeSerialization()
    {
        final CompleteEdge item = CompleteEdge.shallowFrom(
                new CompleteEdge(123L, PolyLine.TEST_POLYLINE, null, null, null, null));
        final FeatureChange featureChange = new FeatureChange(ChangeType.REMOVE, item);
        assertEquals(featureChange, "serializedEdgeRemove.json");
    }

    @Test
    public void testRemoveLineSerialization()
    {
        final CompleteLine item = CompleteLine
                .shallowFrom(new CompleteLine(123L, PolyLine.TEST_POLYLINE, null, null));
        final FeatureChange featureChange = new FeatureChange(ChangeType.REMOVE, item);
        assertEquals(featureChange, "serializedLineRemove.json");
    }

    @Test
    public void testRemoveNodeSerialization()
    {
        final CompleteNode item = CompleteNode
                .shallowFrom(new CompleteNode(123L, Location.COLOSSEUM, null, null, null, null));
        final FeatureChange featureChange = new FeatureChange(ChangeType.REMOVE, item);
        assertEquals(featureChange, "serializedNodeRemove.json");
    }

    @Test
    public void testRemovePointSerialization()
    {
        final CompletePoint item = CompletePoint
                .shallowFrom(new CompletePoint(123L, Location.COLOSSEUM, null, null));
        final FeatureChange featureChange = new FeatureChange(ChangeType.REMOVE, item);
        assertEquals(featureChange, "serializedPointRemove.json");
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
        final FeatureChange featureChange = new FeatureChange(ChangeType.REMOVE, item);
        assertEquals(featureChange, "serializedRelationRemove.json");
    }

    private void assertEquals(final FeatureChange featureChange, final String fileName)
    {
        final String expected = new InputStreamResource(
                () -> FeatureChangeGeoJsonSerializerTest.class.getResourceAsStream(fileName)).all();
        Assert.assertEquals(expected, featureChange.toJson());
        final File temporary = File.temporary();
        try
        {
            featureChange.save(temporary);
            Assert.assertEquals(expected, temporary.all());
        }
        finally
        {
            temporary.delete();
        }
    }
}
