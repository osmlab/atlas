package org.openstreetmap.atlas.geography.geojson.parser.domain.bbox;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Yazad Khambata
 */
public class DimensionsTest
{
    @Test
    public void test()
    {
        Assert.assertEquals(Dimensions.TWO_DIMENSIONAL.getNumberOfCoordinates(), 4);
        Assert.assertEquals(Dimensions.THREE_DIMENSIONAL.getNumberOfCoordinates(), 6);
    }
}
