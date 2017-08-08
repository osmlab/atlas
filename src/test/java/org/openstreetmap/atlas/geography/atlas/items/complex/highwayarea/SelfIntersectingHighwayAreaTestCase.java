package org.openstreetmap.atlas.geography.atlas.items.complex.highwayarea;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.complex.ComplexEntity;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * Test case for self intersecting highway areas
 *
 * @author isabellehillberg
 */
public class SelfIntersectingHighwayAreaTestCase
{
    @Rule
    public SelfIntersectingHighwayAreaTestCaseRule setup = new SelfIntersectingHighwayAreaTestCaseRule();

    @Test
    public void intersectingHighwayArea()
    {
        this.setup.findSelfIntersecting().map(ComplexEntity::getSource)
                .map(AtlasEntity::getOsmIdentifier).forEach(System.out::println);
        Assert.assertEquals(0, Iterables.count(this.setup.findSelfIntersecting(), i -> 1L));
    }
}
