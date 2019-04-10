package org.openstreetmap.atlas.tags;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.testing.TestTaggable;

/**
 * Tests
 * for @{DestinationTag}, @{DestinationRefTag}, @{DestinationStreetTag}, @{DestinationRefToTag}
 * and @{DestinationIntRefTag}
 * 
 * @author sbhalekar
 */
public class DestinationTagTestCase
{
    @Test
    public void testDestinatioTag()
    {
        final TestTaggable taggable = new TestTaggable(DestinationTag.KEY, "San Jose");
        Assert.assertTrue(Validators.hasValuesFor(taggable, DestinationTag.class));
    }

    @Test
    public void testDestinationRefTag()
    {
        final TestTaggable taggable = new TestTaggable(DestinationRefTag.KEY, "KPE");
        Assert.assertTrue(Validators.hasValuesFor(taggable, DestinationRefTag.class));
    }

    @Test
    public void testDestinationStreetTag()
    {
        final TestTaggable taggable = new TestTaggable(DestinationStreetTag.KEY, "Bendemeer Road");
        Assert.assertTrue(Validators.hasValuesFor(taggable, DestinationStreetTag.class));
    }

    @Test
    public void testDestinationIntRefTag()
    {
        final TestTaggable taggable = new TestTaggable(DestinationIntRefTag.KEY, "E_94");
        Assert.assertTrue(Validators.hasValuesFor(taggable, DestinationIntRefTag.class));
    }
}
