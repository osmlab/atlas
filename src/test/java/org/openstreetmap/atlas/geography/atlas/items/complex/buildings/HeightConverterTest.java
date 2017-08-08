package org.openstreetmap.atlas.geography.atlas.items.complex.buildings;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * @author matthieun
 */
public class HeightConverterTest
{
    @Test
    public void testConversion()
    {
        Assert.assertEquals(Distance.ONE_METER, new HeightConverter().convert("1 m"));
        Assert.assertEquals(Distance.ONE_METER, new HeightConverter().convert("1"));
        Assert.assertEquals(0.3556, new HeightConverter().convert("1\'2\"").asMeters(), 2);
    }
}
