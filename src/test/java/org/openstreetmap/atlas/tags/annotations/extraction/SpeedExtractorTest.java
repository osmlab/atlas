package org.openstreetmap.atlas.tags.annotations.extraction;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.scalars.Speed;

/**
 * Unit tests for {@link SpeedExtractor}.
 *
 * @author bbreithaupt
 */
public class SpeedExtractorTest
{
    @Test
    public void validNumberTest()
    {
        Assert.assertEquals(Optional.of(Speed.kilometersPerHour(60)),
                SpeedExtractor.validateAndExtract("60"));
    }

    @Test
    public void validNumberKPHTest()
    {
        Assert.assertEquals(Optional.of(Speed.kilometersPerHour(60)),
                SpeedExtractor.validateAndExtract("60 kph"));
    }

    @Test
    public void validNumberMPHTest()
    {
        Assert.assertEquals(Optional.of(Speed.milesPerHour(60)),
                SpeedExtractor.validateAndExtract("60 mph"));
    }

    @Test
    public void validNumberKnotsTest()
    {
        Assert.assertEquals(Optional.of(Speed.knots(60)),
                SpeedExtractor.validateAndExtract("60 knots"));
    }

    @Test
    public void validImplicitTest()
    {
        Assert.assertEquals(Optional.of(Speed.kilometersPerHour(50)),
                SpeedExtractor.validateAndExtract("RO:urban"));
    }

    @Test
    public void invalidNoneTest()
    {
        Assert.assertEquals(Optional.empty(), SpeedExtractor.validateAndExtract("none"));
    }

    @Test
    public void invalidNumberMPHTest()
    {
        Assert.assertEquals(Optional.empty(), SpeedExtractor.validateAndExtract("60mph"));
    }

    @Test
    public void invalidNumberDelimitedTest()
    {
        Assert.assertEquals(Optional.empty(), SpeedExtractor.validateAndExtract("60; 50"));
    }

    @Test
    public void invalidImplicitDelimitedTest()
    {
        Assert.assertEquals(Optional.empty(),
                SpeedExtractor.validateAndExtract("RO:urban; RO:rural"));
    }
}
