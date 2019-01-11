package org.openstreetmap.atlas.geography.atlas.change;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteArea;
import org.openstreetmap.atlas.geography.atlas.complete.CompleteLine;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.utilities.collections.Maps;

/**
 * @author matthieun
 */
public class ChangeTest
{
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testAdd()
    {
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, null, Maps.hashMap(), null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.REMOVE,
                new CompleteLine(123L, null, null, null));
        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(featureChange1);
        builder.add(featureChange2);
        final Change change = builder.get();

        Assert.assertTrue(change.changeFor(ItemType.AREA, 123L).isPresent());
        Assert.assertTrue(change.changeFor(ItemType.LINE, 123L).isPresent());
    }

    @Test
    public void testAddSameIdentifier()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("Cannot merge two feature changes");

        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, null, Maps.hashMap(), null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.REMOVE,
                new CompleteArea(123L, null, null, null));
        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(featureChange1);
        builder.add(featureChange2);
    }

    @Test
    public void testAddSameIdentifierMerge()
    {
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, null, Maps.hashMap("key", "value"), null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.ADD,
                new CompleteArea(123L, Polygon.TEST_BUILDING, null, null));
        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(featureChange1);
        builder.add(featureChange2);
        final Change result = builder.get();
        final FeatureChange merged = result.changeFor(ItemType.AREA, 123L).get();
        final Area area = (Area) merged.getReference();
        Assert.assertEquals(Polygon.TEST_BUILDING, area.asPolygon());
        Assert.assertEquals(Maps.hashMap("key", "value"), area.getTags());
    }
}
