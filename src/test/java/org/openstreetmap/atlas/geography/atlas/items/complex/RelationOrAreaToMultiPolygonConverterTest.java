package org.openstreetmap.atlas.geography.atlas.items.complex;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.MultiPolygon;

/**
 * Unit tests for {@link RelationOrAreaToMultiPolygonConverter}.
 *
 * @author bbreithaupt
 */
public class RelationOrAreaToMultiPolygonConverterTest
{
    public static final RelationOrAreaToMultiPolygonConverter CONVERTER = new RelationOrAreaToMultiPolygonConverter();

    @Rule
    public RelationOrAreaToMultiPolygonConverterTestRule setup = new RelationOrAreaToMultiPolygonConverterTestRule();

    @Test
    // Test an outer ring inside an inner ring inside an outer ring
    public void innerOuterMultiPolygonTest()
    {
        final MultiPolygon multiPolygon = CONVERTER
                .convert(setup.innerOuterMultiPolygonAtlas().relation(1447306000000L));
        Assert.assertEquals(2, multiPolygon.outers().size());
        // Both inner rings should be mapped to one of the outer rings
        Assert.assertTrue(
                multiPolygon.outers().stream().map(outer -> multiPolygon.innersOf(outer).size())
                        .allMatch(count -> count == 0 || count == 2));
    }
}
