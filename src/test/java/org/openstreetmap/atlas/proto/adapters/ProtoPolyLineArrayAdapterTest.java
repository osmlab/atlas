package org.openstreetmap.atlas.proto.adapters;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.utilities.arrays.PolyLineArray;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class ProtoPolyLineArrayAdapterTest
{
    private static final Logger logger = LoggerFactory
            .getLogger(ProtoPolyLineArrayAdapterTest.class);
    private static final int TEST_SIZE = 10_000;
    private static final String TEST_NAME = "testarray";
    private final ProtoPolyLineArrayAdapter adapter = new ProtoPolyLineArrayAdapter();

    @Test
    public void testConsistency()
    {
        final List<Location> locationList = new ArrayList<>();
        locationList.add(new Location(Latitude.degrees(1), Longitude.degrees(1)));
        locationList.add(new Location(Latitude.degrees(2), Longitude.degrees(2)));
        locationList.add(new Location(Latitude.degrees(3), Longitude.degrees(3)));
        locationList.add(new Location(Latitude.degrees(4), Longitude.degrees(4)));
        locationList.add(new Location(Latitude.degrees(5), Longitude.degrees(5)));

        final PolyLine testLine = new PolyLine(locationList);

        final PolyLineArray polyLineArray = new PolyLineArray(TEST_SIZE, TEST_SIZE, TEST_SIZE);
        for (int index = 0; index < TEST_SIZE; index++)
        {
            polyLineArray.add(testLine);
        }
        polyLineArray.setName(TEST_NAME);

        Time startTime = Time.now();
        final byte[] contents = this.adapter.serialize(polyLineArray);
        logger.info("Took {} to serialize PolyLineArray", startTime.elapsedSince());

        startTime = Time.now();
        final PolyLineArray parsedFrom = (PolyLineArray) this.adapter.deserialize(contents);
        logger.info("Took {} to deserialize PolyLineArray from bytestream",
                startTime.elapsedSince());

        logger.info("Testing equality...");
        Assert.assertEquals(polyLineArray, parsedFrom);
    }

    @Test(expected = CoreException.class)
    public void testNullElements()
    {
        final PolyLineArray polyLineArray = new PolyLineArray(10);

        Assert.assertTrue(polyLineArray.getName() == null);

        Time startTime = Time.now();
        byte[] contents = this.adapter.serialize(polyLineArray);
        logger.info("Took {} to serialize PolyLineArray", startTime.elapsedSince());

        startTime = Time.now();
        final PolyLineArray parsedFrom = (PolyLineArray) this.adapter.deserialize(contents);
        logger.info("Took {} to deserialize PolyLineArray from bytestream",
                startTime.elapsedSince());

        logger.info("Testing equality...");
        Assert.assertEquals(polyLineArray, parsedFrom);

        logger.info("Testing proper handling of null elements...");
        polyLineArray.add(new PolyLine());
        polyLineArray.add(null);
        contents = this.adapter.serialize(polyLineArray);
    }
}
