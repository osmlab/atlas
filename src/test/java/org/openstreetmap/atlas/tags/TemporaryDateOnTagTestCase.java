package org.openstreetmap.atlas.tags;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.testing.TestTaggable;

/**
 * Test for @{TemporaryDateOnTag}
 */
public class TemporaryDateOnTagTestCase
{
    @Test
    public void testTemporaryDateOnTag()
    {
        final TestTaggable taggable = new TestTaggable(TemporaryDateOnTag.KEY, "2020");
        Assert.assertTrue(Validators.hasValuesFor(taggable, TemporaryDateOnTag.class));
    }
}
