package org.openstreetmap.atlas.tags;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.testing.TestTaggable;

/**
 * Tests
 * for #{OpeningDateTag}
 *
 * @author brianjor
 */
public class OpeningDateTagTestCase
{
    @Test
    public void testOpeningDateTag()
    {
        final TestTaggable taggable = new TestTaggable(OpeningDateTag.KEY, "2020");
        Assert.assertTrue(Validators.hasValuesFor(taggable, OpeningDateTag.class));
    }
}
