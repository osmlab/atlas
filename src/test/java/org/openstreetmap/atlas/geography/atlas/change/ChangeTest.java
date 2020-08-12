package org.openstreetmap.atlas.geography.atlas.change;

import java.util.HashSet;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEdge;
import org.openstreetmap.atlas.geography.atlas.complete.CompletePoint;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * @author matthieun
 * @author Yazad Khambata
 */
public class ChangeTest extends AbstractChangeTest
{
    @Test
    public void testAdd()
    {
        final Change change = newChangeWithAreaAndLine();

        Assert.assertTrue(change.changeFor(ItemType.AREA, TEST_IDENTIFIER).isPresent());
        Assert.assertTrue(change.changeFor(ItemType.LINE, TEST_IDENTIFIER).isPresent());
    }

    @Test
    public void testAddRemoveSameIdentifier()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Cannot merge two feature changes");

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(TEST_IDENTIFIER, Polygon.CENTER, Maps.hashMap(), null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.REMOVE,
                new CompleteArea(TEST_IDENTIFIER, Polygon.CENTER, null, null));
        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(featureChange1);
        builder.add(featureChange2);
    }

    @Test
    public void testAddSameIdentifierMerge()
    {
        final Change result = newChangeWith2Areas();
        final FeatureChange merged = result.changeFor(ItemType.AREA, TEST_IDENTIFIER).get();
        final Area area = (Area) merged.getAfterView();
        Assert.assertEquals(Polygon.TEST_BUILDING, area.asPolygon());
        Assert.assertEquals(Maps.hashMap("key", "value"), area.getTags());
    }

    @Test
    public void testAllChangesMappedByAtlasEntityKey()
    {
        final CompletePoint point = new CompletePoint(123L, Location.COLOSSEUM, Maps.stringMap(),
                new HashSet<>());
        final Change change = ChangeBuilder.newInstance().add(FeatureChange.add(point)).get();
        final Map<AtlasEntityKey, FeatureChange> allChangesMappedByAtlasEntityKey1 = change
                .allChangesMappedByAtlasEntityKey();
        final Map<AtlasEntityKey, FeatureChange> allChangesMappedByAtlasEntityKey2 = change
                .allChangesMappedByAtlasEntityKey();
        Assert.assertSame(allChangesMappedByAtlasEntityKey1, allChangesMappedByAtlasEntityKey2);
        Assert.assertEquals(1, allChangesMappedByAtlasEntityKey1.size());
        Assert.assertNotNull(
                allChangesMappedByAtlasEntityKey1.get(new AtlasEntityKey(ItemType.POINT, 123L)));
    }

    @Test
    public void testEdgeNodeValidation()
    {
        final CompleteEdge edge1 = new CompleteEdge(1L, PolyLine.SIMPLE_POLYLINE,
                Maps.hashMap("key1", "value1", "newKey", "newValue"), null, null, null);
        final CompleteEdge edge1Reverse = new CompleteEdge(-1L, PolyLine.SIMPLE_POLYLINE.reversed(),
                Maps.hashMap("key1", "value1", "newKey", "newValue"), 2L, 1L, null);
        final Change change1 = ChangeBuilder.newInstance()
                .addAll(FeatureChange.add(edge1), FeatureChange.add(edge1Reverse)).get();
        Assert.assertEquals(2, change1.changeCount());
    }

    @Test
    public void testEdgeNodeValidationFail()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Forward edge 1 start node CompleteNode");
        final CompleteEdge edge2 = new CompleteEdge(1L, PolyLine.SIMPLE_POLYLINE,
                Maps.hashMap("key1", "value1", "newKey", "newValue"), 3L, 2L, null);
        final CompleteEdge edge2Reverse = new CompleteEdge(-1L, PolyLine.SIMPLE_POLYLINE.reversed(),
                Maps.hashMap("key1", "value1", "newKey", "newValue"), 2L, 1L, null);
        final Change change2 = ChangeBuilder.newInstance()
                .addAll(FeatureChange.add(edge2), FeatureChange.add(edge2Reverse)).get();
    }

    @Test
    public void testEqualsAndHashCode()
    {
        final Change changeWithAreaAndLine1 = newChangeWithAreaAndLine();
        final Change changeWithAreaAndLine2 = newChangeWithAreaAndLine();

        Assert.assertTrue(changeWithAreaAndLine1 != changeWithAreaAndLine2);

        Assert.assertEquals(changeWithAreaAndLine1, changeWithAreaAndLine2);
        Assert.assertEquals(changeWithAreaAndLine1.hashCode(), changeWithAreaAndLine2.hashCode());

        final Change changeWith2Areas1 = newChangeWith2Areas();
        final Change changeWith2Areas2 = newChangeWith2Areas();

        Assert.assertTrue(changeWith2Areas1 != changeWith2Areas2);

        Assert.assertEquals(changeWith2Areas1, changeWith2Areas2);
        Assert.assertEquals(changeWith2Areas1.hashCode(), changeWith2Areas2.hashCode());

        Assert.assertNotEquals(changeWithAreaAndLine1, changeWith2Areas1);
        Assert.assertNotEquals(changeWithAreaAndLine1.hashCode(), changeWith2Areas1.hashCode());
    }

    @Test
    public void testUnequalAndDifferentHashCodeRelations()
    {
        final Change changeRelation1 = newChangeWithRelationMemberSet1();
        final Change changeRelation2 = newChangeWithRelationMemberSet2();

        Assert.assertNotSame(changeRelation1, changeRelation2);
        Assert.assertNotEquals(changeRelation1, changeRelation2);
        Assert.assertNotEquals(changeRelation1.hashCode(), changeRelation2.hashCode());
    }
}
