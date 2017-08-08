package org.openstreetmap.atlas.tags.filters;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.ByteArrayResource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.tags.Taggable;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author matthieun
 */
public class TaggableFilterTest
{
    @Test
    public void testParsing()
    {
        // amenity=bus_station OR highway=bus_stop OR ( (bus=* OR trolleybus=*) AND
        // public_transport=[stop_position OR platform OR station] )
        final String definition = "amenity->bus_station|highway->BUS_STOP|bus->*^trolleybus->*&public_transport->stop_position,platform,station";
        final TaggableFilter filter = new TaggableFilter(definition);

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
    public void testSerialization() throws ClassNotFoundException
    {
        final String definition = "amenity->bus_station|highway->BUS_STOP|bus->*^trolleybus->*&public_transport->stop_position,platform,station";
        final TaggableFilter filter = new TaggableFilter(definition);

        final WritableResource out = new ByteArrayResource();
        try (ObjectOutputStream outStream = new ObjectOutputStream(out.write()))
        {
            outStream.writeObject(filter);
        }
        catch (final IOException e)
        {
            throw new CoreException("Unable to write to {}", out, e);
        }

        try (ObjectInputStream inStream = new ObjectInputStream(out.read()))
        {
            final TaggableFilter result = (TaggableFilter) inStream.readObject();
            Assert.assertEquals(Iterables.asSortedSet(filter.checkAllowedTags()),
                    Iterables.asSortedSet(result.checkAllowedTags()));
        }
        catch (final IOException e)
        {
            throw new CoreException("Unable to read from {}", out, e);
        }
    }
}
