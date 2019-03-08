package org.openstreetmap.atlas.geography.atlas.change;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
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
    public void testAddSameIdentifier()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Cannot merge two feature changes");

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(TEST_IDENTIFIER, null, Maps.hashMap(), null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.REMOVE,
                new CompleteArea(TEST_IDENTIFIER, null, null, null));
        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(featureChange1);
        builder.add(featureChange2);
    }

    @Test
    public void testAddSameIdentifierMerge()
    {
        final Change result = newChangeWith2Areas();
        final FeatureChange merged = result.changeFor(ItemType.AREA, TEST_IDENTIFIER).get();
        final Area area = (Area) merged.getUpdatedView();
        Assert.assertEquals(Polygon.TEST_BUILDING, area.asPolygon());
        Assert.assertEquals(Maps.hashMap("key", "value"), area.getTags());
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
}
