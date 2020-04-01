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
import org.openstreetmap.atlas.geography.atlas.change.Change;
import org.openstreetmap.atlas.geography.atlas.change.ChangeBuilder;
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
public class ChangeGeoJsonSerializerTest
{
    private static final Map<String, String> TAGS = Maps.hashMap("tagKey1", "tagValue1", "tagKey2",
            "tagValue2");
    private static final Set<Long> RELATIONS = Sets.hashSet(444L, 555L);

    @Test
    public void testSerialization()
    {
        final CompleteArea area = new CompleteArea(123L, Polygon.TEST_BUILDING, TAGS, RELATIONS);
        final FeatureChange featureChange1 = FeatureChange.add(area);
        final CompleteEdge edge = new CompleteEdge(123L, PolyLine.TEST_POLYLINE, TAGS, 456L, 789L,
                RELATIONS);
        final FeatureChange featureChange2 = FeatureChange.add(edge);
        final CompleteLine line = new CompleteLine(123L, PolyLine.TEST_POLYLINE, TAGS, RELATIONS);
        final FeatureChange featureChange3 = FeatureChange.add(line);
        final CompleteNode node = new CompleteNode(123L, Location.COLOSSEUM, TAGS,
                Sets.treeSet(456L, 789L), Sets.treeSet(456L, 789L), RELATIONS);
        final FeatureChange featureChange4 = FeatureChange.add(node);
        final CompletePoint point = new CompletePoint(123L, Location.COLOSSEUM, TAGS, RELATIONS);
        final FeatureChange featureChange5 = FeatureChange.add(point);
        final RelationBean members = new RelationBean();
        members.addItem(456L, "role1", ItemType.EDGE);
        members.addItem(789L, "role2", ItemType.AREA);
        final CompleteRelation relation = new CompleteRelation(123L, TAGS, Rectangle.TEST_RECTANGLE,
                members, Lists.newArrayList(123L), members, 123L, RELATIONS);
        final FeatureChange featureChange6 = FeatureChange.add(relation);

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
        Assert.assertEquals(expected, change.toJson(false));
        final File temporary = File.temporary();
        try
        {
            change.save(temporary, false);
            Assert.assertEquals(expected, temporary.all());
        }
        finally
        {
            temporary.delete();
        }
    }
}
