package org.openstreetmap.atlas.tags;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link ProtectClassTag}.
 *
 * @author bbreithaupt
 */
public class ProtectClassTagTest
{

    @Test
    public void getValueNotNumberTest()
    {
        final Optional<Integer> tagValue = ProtectClassTag
                .getValue(Taggable.with("protect_class", "bad"));
        Assert.assertFalse(tagValue.isPresent());
    }

    @Test
    public void getValueTest()
    {
        final Optional<Integer> tagValue = ProtectClassTag
                .getValue(Taggable.with("protect_class", "1"));
        Assert.assertTrue(tagValue.isPresent());
        Assert.assertEquals((Integer) 1, tagValue.get());
    }

    @Test
    public void getValueWrongTagTest()
    {
        final Optional<Integer> tagValue = ProtectClassTag
                .getValue(Taggable.with("highway", "primary"));
        Assert.assertFalse(tagValue.isPresent());
    }
}
