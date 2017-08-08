package org.openstreetmap.atlas.tags.annotations.extraction;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.annotations.Tag;
import org.openstreetmap.atlas.tags.annotations.Tag.Range;
import org.openstreetmap.atlas.tags.annotations.Tag.Validation;

/**
 * Test case for {@link OrdinalExtractor}
 *
 * @author brian_l_davis
 */
public class OrdinalExtractorTestCase
{
    /**
     * @author brian_l_davis
     */
    @Tag(value = Validation.ORDINAL, range = @Range(min = 1, max = 10, exclude = { 5 }))
    private interface TestTag
    {
    }

    private static final Tag tag = TestTag.class.getDeclaredAnnotation(Tag.class);

    @Test
    public void testExtractorInRange()
    {
        final OrdinalExtractor extractor = new OrdinalExtractor();
        Assert.assertTrue(extractor.validateAndExtract("1", tag).isPresent());
        Assert.assertTrue(extractor.validateAndExtract("3", tag).isPresent());
        Assert.assertTrue(extractor.validateAndExtract("10", tag).isPresent());
    }

    @Test
    public void testExtractorNotInRange()
    {
        final OrdinalExtractor extractor = new OrdinalExtractor();
        Assert.assertFalse(extractor.validateAndExtract("0", tag).isPresent());
        Assert.assertFalse(extractor.validateAndExtract("5", tag).isPresent());
        Assert.assertFalse(extractor.validateAndExtract("11", tag).isPresent());
    }
}
