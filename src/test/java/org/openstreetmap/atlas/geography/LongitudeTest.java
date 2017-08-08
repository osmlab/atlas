package org.openstreetmap.atlas.geography;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author matthieun
 * @author tony
 */
public class LongitudeTest
{
    @Test
    public void testAntimeridian()
    {
        final Longitude oneEighty = Longitude.degrees(180);
        Assert.assertEquals(1_800_000_000, oneEighty.asDm7());

        final Longitude minusOneEighty = Longitude.degrees(-180);
        Assert.assertEquals(-1_800_000_000, minusOneEighty.asDm7());

        Assert.assertTrue(oneEighty.equals(minusOneEighty));
    }

    @Test
    public void testCreation()
    {
        final Longitude one = Longitude.degrees(-122);
        Assert.assertEquals(-1_220_000_000, one.asDm7());
        try
        {
            Longitude.degrees(-122 - 100);
            fail("Longitude should not have been created!");
        }
        catch (final Exception e)
        {
            return;
        }
    }
}
