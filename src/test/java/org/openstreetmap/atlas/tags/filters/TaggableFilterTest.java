package org.openstreetmap.atlas.tags.filters;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.utilities.testing.FreezeDryFunction;

/**
 * @author matthieun
 */
public class TaggableFilterTest
{
    @Test
    public void testAllValidComplex()
    {
        // In this case the OR(|) is ignored here.
        final String definition = "bus->*|";
        final TaggableFilter filter = TaggableFilter.forDefinition(definition);

        Assert.assertFalse(filter.test(Taggable.with()));
        Assert.assertFalse(filter.test(Taggable.with("highway", "primary")));
        Assert.assertTrue(filter.test(Taggable.with("bus", "lane")));
    }

    @Test
    public void testAllValidSimple()
    {
        final String definition = "";
        final TaggableFilter filter = TaggableFilter.forDefinition(definition);

        Assert.assertTrue(filter.test(Taggable.with()));
        Assert.assertTrue(filter.test(Taggable.with("highway", "primary")));
        Assert.assertTrue(filter.test(Taggable.with("bus", "lane")));
    }

    @Test
    public void testBackwardsCompatibility()
    {
        final String definition = "bus->*|water->*&bus->*^water->!canal";
        final TaggableFilter filter = TaggableFilter.forDefinition(definition);

        Assert.assertTrue(filter.test(Taggable.with("water", "pond")));
        Assert.assertFalse(filter.test(Taggable.with("water", "canal")));
        Assert.assertTrue(filter.test(Taggable.with("water", "canal", "bus", "stop")));

        Assert.assertEquals(definition.replace("^", "||"), filter.toString());
    }

    @Test
    public void testDepth()
    {
        final String definition = "bus->*|water->*&bus->*||water->!canal&&bus->!|||water->pond,canal&&&bus->*";
        final TaggableFilter filter = TaggableFilter.forDefinition(definition);

        Assert.assertTrue(filter.test(Taggable.with("water", "pond")));
        Assert.assertFalse(filter.test(Taggable.with("water", "canal")));
        Assert.assertTrue(filter.test(Taggable.with("water", "canal", "bus", "stop")));

        Assert.assertEquals(definition, filter.toString());
    }

    @Test
    public void testDoubleNegative()
    {
        final String definition = "water->!pond,!lake";
        final TaggableFilter filter = TaggableFilter.forDefinition(definition);

        Assert.assertTrue(filter.test(Taggable.with()));
        // Pond, but not lake -> true
        Assert.assertTrue(filter.test(Taggable.with("water", "pond")));
        // Lake, but not pond -> true
        Assert.assertTrue(filter.test(Taggable.with("water", "lake")));
        // Not ponf, not lake. -> true
        Assert.assertTrue(filter.test(Taggable.with("water", "sea")));
    }

    @Test
    public void testParsing()
    {
        // amenity=bus_station OR highway=bus_stop OR ( (bus=* OR trolleybus=*) AND
        // public_transport=[stop_position OR platform OR station] )
        final String definition = "amenity->bus_station|highway->BUS_STOP|bus->*||trolleybus->*&public_transport->stop_position,platform,station";
        final TaggableFilter filter = TaggableFilter.forDefinition(definition);

        final Taggable valid1 = Taggable.with("amenity", "bus_station");
        final Taggable valid2 = Taggable.with("highway", "bus_stop");
        final Taggable valid3 = Taggable.with("trolleybus", "yes", "public_transport", "platform");
        final Taggable valid4 = Taggable.with("bus", "hello", "public_transport", "station");
        final Taggable valid5 = Taggable.with("trolleybus", "bye", "public_transport",
                "stop_position");
        final Taggable valid6 = Taggable.with("amenity", "bus_stop", "trolleybus", "bye",
                "public_transport", "stop_position");

        final Taggable invalid1 = Taggable.with("amenity", "bus_stop");
        final Taggable invalid2 = Taggable.with("highway", "bus_station");
        final Taggable invalid3 = Taggable.with("trolleybus", "yes");
        final Taggable invalid4 = Taggable.with("bus", "hello", "public_transport", "terminal");
        final Taggable invalid5 = Taggable.with();

        Assert.assertTrue(filter.test(valid1));
        Assert.assertTrue(filter.test(valid2));
        Assert.assertTrue(filter.test(valid3));
        Assert.assertTrue(filter.test(valid4));
        Assert.assertTrue(filter.test(valid5));
        Assert.assertTrue(filter.test(valid6));

        Assert.assertFalse(filter.test(invalid1));
        Assert.assertFalse(filter.test(invalid2));
        Assert.assertFalse(filter.test(invalid3));
        Assert.assertFalse(filter.test(invalid4));
        Assert.assertFalse(filter.test(invalid5));
    }

    @Test
    public void testSerialization()
    {
        final String definition1 = "amenity->bus_station|highway->BUS_STOP|bus->*||trolleybus->*&public_transport->stop_position,platform,station";
        final TaggableFilter filter1 = TaggableFilter.forDefinition(definition1);

        final String definition2 = "";
        final TaggableFilter filter2 = TaggableFilter.forDefinition(definition2);

        final TaggableFilter filter12 = (TaggableFilter) new FreezeDryFunction<>().apply(filter1);
        Assert.assertEquals(filter1.getDefinition(), filter12.getDefinition());

        final TaggableFilter filter22 = (TaggableFilter) new FreezeDryFunction<>().apply(filter2);
        Assert.assertEquals(filter2.getDefinition(), filter22.getDefinition());
    }

    @Test
    public void z()
    {
        // TODO remove test
        final TaggableFilterToMatcherConverter converter = new TaggableFilterToMatcherConverter();
        final TaggableFilter filter = TaggableFilter.forDefinition(
                "highway->service&cycleway->lane||cycleway:lane->*&&cycleway->!|highway->!motorway");
        System.err.println(converter.convert(filter).getDefinition());

        final TaggableFilter filterA = TaggableFilter.forDefinition("highway->service,primary");
        System.err.println(converter.convert(filterA).getDefinition());

        final TaggableFilter filter2 = TaggableFilter.forDefinition("highway->!service");
        final Taggable taggable1 = Taggable.with("highway", "primary");
        final Taggable taggable2 = Taggable.with("highway", "service");
        final Taggable taggable3 = Taggable.with("natural", "lake");
        Assert.assertTrue(filter2.test(taggable1));
        Assert.assertFalse(filter2.test(taggable2));
        Assert.assertTrue(filter2.test(taggable3));

        final TaggableFilter filter3 = TaggableFilter.forDefinition("highway->service,primary");
        Assert.assertTrue(filter3.test(taggable1));
        Assert.assertTrue(filter3.test(taggable2));
        Assert.assertFalse(filter3.test(taggable3));

        final TaggableFilter filter4 = TaggableFilter.forDefinition("highway->primary,!,service");
        Assert.assertTrue(filter4.test(taggable1));
        Assert.assertFalse(filter4.test(taggable2));
        Assert.assertFalse(filter4.test(taggable3));
    }
}
