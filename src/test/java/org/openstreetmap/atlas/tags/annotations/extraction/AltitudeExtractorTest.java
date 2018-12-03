package org.openstreetmap.atlas.tags.annotations.extraction;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Altitude;

/**
 * Unit test for {@link AltitudeExtractor}.
 *
 * @author bbreithaupt
 */
public class AltitudeExtractorTest
{
    @Test
    public void validNumberTest()
    {
        Assert.assertEquals(Optional.of(Altitude.meters(20.5)),
                AltitudeExtractor.validateAndExtract("20.5"));
    }

    @Test
    public void validMetersTest()
    {
        Assert.assertEquals(Optional.of(Altitude.meters(20)),
                AltitudeExtractor.validateAndExtract("20 m"));
    }

    @Test
    public void validNegativeNumberTest()
    {
        Assert.assertEquals(Optional.of(Altitude.meters(-20.5)),
                AltitudeExtractor.validateAndExtract("-20.5"));
    }

    @Test
    public void validNegativeMetersTest()
    {
        Assert.assertEquals(Optional.of(Altitude.meters(-20)),
                AltitudeExtractor.validateAndExtract("-20 m"));
    }
}
