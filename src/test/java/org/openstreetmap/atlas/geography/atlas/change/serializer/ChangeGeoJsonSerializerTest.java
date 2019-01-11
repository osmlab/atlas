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
import org.openstreetmap.atlas.geography.atlas.change.Change;
import org.openstreetmap.atlas.geography.atlas.change.ChangeBuilder;
import org.openstreetmap.atlas.geography.atlas.change.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.utilities.collections.Maps;
import org.openstreetmap.atlas.utilities.collections.Sets;

import com.google.common.collect.Lists;

/**
 * @author matthieun
 */
public class ChangeGeoJsonSerializerTest
{
    private static final Map<String, String> TAGS = Maps.hashMap("tagKey1", "tagValue1", "tagKey2",
            "tagValue2");
    private static final Set<Long> RELATIONS = Sets.hashSet(444L, 555L);

    @Test
    public void testSerialization()
    {
        final BloatedArea area = new BloatedArea(123L, Polygon.TEST_BUILDING, TAGS, RELATIONS);
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD, area);
        final BloatedEdge edge = new BloatedEdge(123L, PolyLine.TEST_POLYLINE, TAGS, 456L, 789L,
                RELATIONS);
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD, edge);
        final BloatedLine line = new BloatedLine(123L, PolyLine.TEST_POLYLINE, TAGS, RELATIONS);
        final FeatureChange featureChange3 = new FeatureChange(ChangeType.ADD, line);
        final BloatedNode node = new BloatedNode(123L, Location.COLOSSEUM, TAGS,
                Sets.treeSet(456L, 789L), Sets.treeSet(456L, 789L), RELATIONS);
        final FeatureChange featureChange4 = new FeatureChange(ChangeType.ADD, node);
        final BloatedPoint point = new BloatedPoint(123L, Location.COLOSSEUM, TAGS, RELATIONS);
        final FeatureChange featureChange5 = new FeatureChange(ChangeType.ADD, point);
        final RelationBean members = new RelationBean();
        members.addItem(456L, "role1", ItemType.EDGE);
        members.addItem(789L, "role2", ItemType.AREA);
        final BloatedRelation relation = new BloatedRelation(123L, TAGS, Rectangle.TEST_RECTANGLE,
                members, Lists.newArrayList(123L), members, 123L, RELATIONS);
        final FeatureChange featureChange6 = new FeatureChange(ChangeType.ADD, relation);

        final ChangeBuilder changeBuilder = new ChangeBuilder();
        changeBuilder.add(featureChange1);
        changeBuilder.add(featureChange2);
        changeBuilder.add(featureChange3);
        changeBuilder.add(featureChange4);
        changeBuilder.add(featureChange5);
        changeBuilder.add(featureChange6);

        final Change change = changeBuilder.get();
        assertEquals(change, "change.json");

    }

    private void assertEquals(final Change change, final String fileName)
    {
        final String expected = new InputStreamResource(
                () -> ChangeGeoJsonSerializerTest.class.getResourceAsStream(fileName)).all();
        Assert.assertEquals(expected, change.toJson());
        final File temporary = File.temporary();
        try
        {
            change.save(temporary);
            Assert.assertEquals(expected, temporary.all());
        }
        finally
        {
            temporary.delete();
        }
    }
}
