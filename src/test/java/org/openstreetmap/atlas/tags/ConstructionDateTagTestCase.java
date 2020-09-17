package org.openstreetmap.atlas.tags;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.testing.TestTaggable;

/**
 * Test for @{ConstructionDateTag}
 */
public class ConstructionDateTagTestCase
{
    @Test
    public void testConstructionDateTag()
    {
        final TestTaggable taggable = new TestTaggable(ConstructionDateTag.KEY, "2020");
        Assert.assertTrue(Validators.hasValuesFor(taggable, ConstructionDateTag.class));
    }
}
