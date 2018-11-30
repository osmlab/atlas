package org.openstreetmap.atlas.geography.atlas.change;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedArea;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedLine;
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
                new BloatedArea(123L, null, Maps.hashMap(), null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.REMOVE,
                new BloatedLine(123L, null, null, null));
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
                new BloatedArea(123L, null, Maps.hashMap(), null));
        final FeatureChange featureChange2 = new FeatureChange(ChangeType.REMOVE,
                new BloatedArea(123L, null, null, null));
        final ChangeBuilder builder = new ChangeBuilder();
        builder.add(featureChange1);
        builder.add(featureChange2);
    }
}
