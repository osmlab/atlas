package org.openstreetmap.atlas.tags;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author matthieun
 */
public class FerryTagTest
{
    private final Taggable taggable1 = Taggable.with("ferry", "service");
    private final Taggable taggable2 = Taggable.with("ferry", "footway");

    @Test
    public void isCarNavigableFerryTest()
    {
        Assert.assertTrue(FerryTag.isCarNavigableFerry(this.taggable1));
        Assert.assertFalse(FerryTag.isCarNavigableFerry(this.taggable2));
    }

    @Test
    public void isPedestrianNavigableFerryTest()
    {
        Assert.assertFalse(FerryTag.isPedestrianNavigableFerry(this.taggable1));
        Assert.assertTrue(FerryTag.isPedestrianNavigableFerry(this.taggable2));
    }
}
