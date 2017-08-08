package org.openstreetmap.atlas.tags.annotations.extraction;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.annotations.Tag;

/**
 * Test case for {@link LongExtractor}
 *
 * @author brian_l_davis
 */
public class LongExtractorTestCase
{
    /**
     * @author brian_l_davis
     */
    @Tag(value = Tag.Validation.LONG, range = @Tag.Range(min = 1, max = 10, exclude = { 5 }))
    private interface TestTag
    {
    }

    private static final Tag tag = TestTag.class.getDeclaredAnnotation(Tag.class);

    @Test
    public void testExtractorInRange()
    {
        final LongExtractor extractor = new LongExtractor();
        Assert.assertTrue(extractor.validateAndExtract("1", tag).isPresent());
        Assert.assertTrue(extractor.validateAndExtract("3", tag).isPresent());
        Assert.assertTrue(extractor.validateAndExtract("10", tag).isPresent());
    }

    @Test
    public void testExtractorNotInRange()
    {
        final LongExtractor extractor = new LongExtractor();
        Assert.assertFalse(extractor.validateAndExtract("0", tag).isPresent());
        Assert.assertFalse(extractor.validateAndExtract("5", tag).isPresent());
        Assert.assertFalse(extractor.validateAndExtract("11", tag).isPresent());
    }
}
