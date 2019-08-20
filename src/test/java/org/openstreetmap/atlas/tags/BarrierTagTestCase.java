package org.openstreetmap.atlas.tags;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * Test for {@link org.openstreetmap.atlas.tags.BarrierTag}
 *
 * @author alexhsieh
 */
public class BarrierTagTestCase
{
    @Test
    public void busTrapIsBlockingTestCase()
    {
        final Taggable taggable = Taggable.with("barrier", "bus_trap");
        Assert.assertTrue(BarrierTag.isBarrier(taggable));
    }

    @Test
    public void busTrapTestCase()
    {
        final Taggable taggable = Taggable.with("barrier", "bus_trap");
        Assert.assertTrue(Validators.hasValuesFor(taggable, BarrierTag.class));
    }
}
