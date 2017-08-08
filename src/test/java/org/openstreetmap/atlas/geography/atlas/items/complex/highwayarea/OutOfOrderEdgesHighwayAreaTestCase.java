package org.openstreetmap.atlas.geography.atlas.items.complex.highwayarea;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * Test case for highway areas that have way-sectioned edged appearing out of order when retrieving
 * them from an Atlas
 *
 * @author cstaylor
 */
public class OutOfOrderEdgesHighwayAreaTestCase
{
    @Rule
    public OutOfOrderEdgesHighwayAreaTestCaseRule setup = new OutOfOrderEdgesHighwayAreaTestCaseRule();

    @Test
    public void lowestIdentifier()
    {
        final ComplexHighwayArea highwayArea = this.setup.invalidHighwayArea();
        Assert.assertEquals(highwayArea.getVisitedEdgeIdentifiers().first().longValue(),
                highwayArea.getSource().getIdentifier());
    }
}
