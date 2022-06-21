package org.openstreetmap.atlas.tags;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link EstimatedWidthTag}.
 *
 * @author bbreithaupt
 */
public class EstimatedWidthTagTest
{
    @Test
    public void testInvalidValue()
    {
        Assert.assertFalse(EstimatedWidthTag.get(Taggable.with("est_width", "1;2")).isPresent());
    }

    @Test
    public void testMissingValue()
    {
        Assert.assertFalse(EstimatedWidthTag.get(Taggable.with("width", "1.2")).isPresent());
    }

    @Test
    public void testValidValue()
    {
        Assert.assertTrue(EstimatedWidthTag.get(Taggable.with("est_width", "1.2")).isPresent());
    }
}
