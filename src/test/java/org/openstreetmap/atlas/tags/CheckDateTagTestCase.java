package org.openstreetmap.atlas.tags;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.testing.TestTaggable;

/**
 * Test for @{CheckDateTag}
 *
 * @author brianjor
 */
public class CheckDateTagTestCase
{
    @Test
    public void testCheckDateTag()
    {
        final TestTaggable taggable = new TestTaggable(CheckDateTag.KEY, "2020");
        Assert.assertTrue(Validators.hasValuesFor(taggable, CheckDateTag.class));
    }
}
