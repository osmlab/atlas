package org.openstreetmap.atlas.geography.atlas.items.complex.highwayarea;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * Verifies test cases for Highway Areas from input files with valid and invalid cases
 *
 * @author isabellehillberg
 */
public class ComplexHighwayAreaTestCase
{
    @Rule
    public ComplexHighwayAreaTestCaseRule setup = new ComplexHighwayAreaTestCaseRule();

    @Test
    public void failedToGetHighwayArea()
    {
        Assert.assertEquals(0, Iterables.count(this.setup.invalidHighwayArea(), i -> 1L));
    }

    @Test
    public void shouldHaveHighwayArea()
    {
        Assert.assertEquals(1, Iterables.count(this.setup.validHighwayArea(), i -> 1L));
        Assert.assertEquals(1, Iterables.count(this.setup.validHighwayArea1(), i -> 1L));
        Assert.assertEquals(4, Iterables.count(this.setup.validHighwayArea2(), i -> 1L));
    }

    @Test
    public void shouldNotHaveHighwayArea()
    {
        Assert.assertEquals(0, Iterables.count(this.setup.noHighwayArea(), i -> 1L));
    }
}
