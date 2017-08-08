package org.openstreetmap.atlas.geography.atlas.items.complex.highwayarea;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.ComplexEntity;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * Test case for size zero highway areas
 *
 * @author isabellehillberg
 */
public class ZeroSizeHighwayAreaTestCase
{
    @Rule
    public ZeroSizeHighwayAreaTestCaseRule setup = new ZeroSizeHighwayAreaTestCaseRule();

    @Test
    public void zeroSizeOTA()
    {
        System.out.print("Zero size ota: ");
        this.setup.findZeroSized().map(ComplexEntity::getSource).map(AtlasEntity::getOsmIdentifier)
                .forEach(System.out::println);
        Assert.assertEquals(0, Iterables.count(this.setup.findZeroSized(), i -> 1L));
    }
}
