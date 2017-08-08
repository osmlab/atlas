package org.openstreetmap.atlas.geography;

import static org.junit.Assert.fail;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author matthieun
 */
public class LatitudeTest
{
    @Test
    public void testCreation()
    {
        final Latitude one = Latitude.degrees(37);
        Assert.assertEquals(370_000_000, one.asDm7());
        try
        {
            Latitude.degrees(37 + 100);
            fail("Latitude should not have been created!");
        }
        catch (final Exception e)
        {
            return;
        }
    }
}
