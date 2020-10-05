package org.openstreetmap.atlas.tags.annotations.extraction;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Unit test for {@link LengthExtractor}.
 *
 * @author bbreithaupt
 */
public class LengthExtractorTest
{
    @Test
    public void invalidMetersTest()
    {
        Assert.assertEquals(Optional.empty(), LengthExtractor.validateAndExtract("20m"));
    }

    @Test
    public void invalidNumberTest()
    {
        Assert.assertEquals(Optional.empty(), LengthExtractor.validateAndExtract("20.s"));
    }

    @Test
    public void validFeetInchesTest()
    {
        Assert.assertEquals(Optional.of(Distance.feetAndInches(20, 5)),
                LengthExtractor.validateAndExtract("20'5\""));
    }

    @Test
    public void validFeetTest()
    {
        Assert.assertEquals(Optional.of(Distance.feet(20)),
                LengthExtractor.validateAndExtract("20'"));
    }

    @Test
    public void validInchesTest()
    {
        Assert.assertEquals(Optional.of(Distance.inches(5)),
                LengthExtractor.validateAndExtract("5\""));
    }

    @Test
    public void validKilometersTest()
    {
        Assert.assertEquals(Optional.of(Distance.kilometers(20.5)),
                LengthExtractor.validateAndExtract("20.5 km"));
    }

    @Test
    public void validMetersCapsTest()
    {
        Assert.assertEquals(Optional.of(Distance.meters(20)),
                LengthExtractor.validateAndExtract("20 M"));
    }

    @Test
    public void validMetersTest()
    {
        Assert.assertEquals(Optional.of(Distance.meters(20)),
                LengthExtractor.validateAndExtract("20 m"));
    }

    @Test
    public void validMilesTest()
    {
        Assert.assertEquals(Optional.of(Distance.miles(20.54)),
                LengthExtractor.validateAndExtract("20.54 mi"));
    }

    @Test
    public void validMilesWithCommaDecimalSeparatorTest()
    {
        Assert.assertEquals(Optional.of(Distance.miles(20.54)),
                LengthExtractor.validateAndExtract("20,54 mi"));
    }

    @Test
    public void validNauticalMilesMixedCapsTest()
    {
        Assert.assertEquals(Optional.of(Distance.nauticalMiles(20.543)),
                LengthExtractor.validateAndExtract("20.543 nMI"));
    }

    @Test
    public void validNauticalMilesTest()
    {
        Assert.assertEquals(Optional.of(Distance.nauticalMiles(20.543)),
                LengthExtractor.validateAndExtract("20.543 nmi"));
    }

    @Test
    public void validNumberTest()
    {
        Assert.assertEquals(Optional.of(Distance.meters(20.5)),
                LengthExtractor.validateAndExtract("20.5"));
    }

    @Test
    public void validNumberWithCommaDecimalSeparatorTest()
    {
        Assert.assertEquals(Optional.of(Distance.meters(20.5)),
                LengthExtractor.validateAndExtract("20,5"));
    }
}
