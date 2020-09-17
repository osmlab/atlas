package org.openstreetmap.atlas.tags;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.testing.TestTaggable;

/**
 * Test for @{OpenDateTag}
 */
public class OpenDateTagTestCase
{
    @Test
    public void testOpenDateTag()
    {
        final TestTaggable taggable = new TestTaggable(OpenDateTag.KEY, "2020");
        Assert.assertTrue(Validators.hasValuesFor(taggable, OpenDateTag.class));
    }
}
