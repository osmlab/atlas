package org.openstreetmap.atlas.geography.atlas.items;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * Unit tests for {@link AtlasEntity#isCountrySliced()}.
 *
 * @author bbreithaupt
 */
public class CountrySliceTest
{
    @Rule
    public final CountrySliceTestRule rule = new CountrySliceTestRule();

    @Test
    public void areaNotSliced()
    {
        Assert.assertFalse(this.rule.getAtlas().area(1000000L).isCountrySliced());
    }

    @Test
    public void areaSliced()
    {
        Assert.assertTrue(this.rule.getAtlas().area(2001000L).isCountrySliced());
    }

    @Test
    public void edgeNotSliced()
    {
        Assert.assertFalse(this.rule.getAtlas().edge(1000000L).isCountrySliced());
    }

    @Test
    public void edgeSliced()
    {
        Assert.assertTrue(this.rule.getAtlas().edge(2000000L).isCountrySliced());
    }

    @Test
    public void lineNotSliced()
    {
        Assert.assertFalse(this.rule.getAtlas().line(1000000L).isCountrySliced());
    }

    @Test
    public void lineSliced()
    {
        Assert.assertTrue(this.rule.getAtlas().line(2001000L).isCountrySliced());
    }

    @Test
    public void nodeNotSliced()
    {
        Assert.assertFalse(this.rule.getAtlas().node(1000000L).isCountrySliced());
    }

    @Test
    public void nodeSliced()
    {
        Assert.assertTrue(this.rule.getAtlas().node(3000000L).isCountrySliced());
    }

    @Test
    public void pointAlsoNotSliced()
    {
        Assert.assertFalse(this.rule.getAtlas().point(2000000L).isCountrySliced());
    }

    @Test
    public void pointNotSliced()
    {
        Assert.assertFalse(this.rule.getAtlas().point(1000000L).isCountrySliced());
    }

    @Test
    public void relationNotSliced()
    {
        Assert.assertFalse(this.rule.getAtlas().relation(1000000L).isCountrySliced());
    }

    @Test
    public void relationSliced()
    {
        Assert.assertTrue(this.rule.getAtlas().relation(2000000L).isCountrySliced());
    }
}
