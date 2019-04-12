package org.openstreetmap.atlas.geography.boundary;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;

/**
 * Tests OceanCountryBoundaryMap generation.
 *
 * @author jamesgage
 */
public class OceanCountryBoundaryMapTest
{
    @Test
    public void testOceanBoundary()
    {
        final CountryBoundaryMap testBoundaryMap = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(OceanCountryBoundaryMapTest.class
                        .getResourceAsStream("oceanTestBoundary.txt")));
        final Iterable<SlippyTile> allPossibleOceanTiles = SlippyTile.allTiles(3);
        final CountryBoundaryMap oceanBoundaryMap = OceanCountryBoundaryMap
                .generateOceanBoundaryMap(testBoundaryMap, allPossibleOceanTiles);
        // ensure that the correct number of ocean boundaries are generated
        Assert.assertEquals(57, oceanBoundaryMap.size());
    }
}
