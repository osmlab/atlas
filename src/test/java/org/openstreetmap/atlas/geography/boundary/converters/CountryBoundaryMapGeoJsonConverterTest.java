package org.openstreetmap.atlas.geography.boundary.converters;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMapTest;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.StringResource;

/**
 * @author lcram
 */
public class CountryBoundaryMapGeoJsonConverterTest
{
    @Test
    public void testConvertToString()
    {
        final String expected = new StringResource(
                CountryBoundaryMapTest.class.getResourceAsStream("AAA_boundary.expected.json"))
                        .all();
        final CountryBoundaryMap mapWithGridIndex = CountryBoundaryMap
                .fromPlainText(new InputStreamResource(
                        CountryBoundaryMapTest.class.getResourceAsStream("AAA_boundary.txt")));
        final String jsonMap = new CountryBoundaryMapGeoJsonConverter().prettyPrint(true)
                .convertToString(mapWithGridIndex);
        Assert.assertEquals(expected, jsonMap);
    }
}
