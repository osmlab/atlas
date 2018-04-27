package org.openstreetmap.atlas.proto.adapters;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.utilities.arrays.PolygonArray;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class ProtoPolygonArrayAdapterTest
{
    private static final Logger logger = LoggerFactory
            .getLogger(ProtoPolygonArrayAdapterTest.class);
    private static final int TEST_SIZE = 10_000;
    private static final String TEST_NAME = "testarray";
    private final ProtoPolygonArrayAdapter adapter = new ProtoPolygonArrayAdapter();

    @Test
    public void testConsistency()
    {
        final List<Location> locationList = new ArrayList<>();
        locationList.add(new Location(Latitude.degrees(1), Longitude.degrees(1)));
        locationList.add(new Location(Latitude.degrees(2), Longitude.degrees(2)));
        locationList.add(new Location(Latitude.degrees(3), Longitude.degrees(3)));
        locationList.add(new Location(Latitude.degrees(4), Longitude.degrees(4)));
        locationList.add(new Location(Latitude.degrees(5), Longitude.degrees(5)));
        locationList.add(new Location(Latitude.degrees(1), Longitude.degrees(1)));

        final Polygon testPolygon = new Polygon(locationList);

        final PolygonArray polygonArray = new PolygonArray(TEST_SIZE, TEST_SIZE, TEST_SIZE);
        for (int index = 0; index < TEST_SIZE; index++)
        {
            polygonArray.add(testPolygon);
        }
        polygonArray.setName(TEST_NAME);

        Time startTime = Time.now();
        final byte[] contents = this.adapter.serialize(polygonArray);
        logger.info("Took {} to serialize PolygonArray", startTime.elapsedSince());

        startTime = Time.now();
        final PolygonArray parsedFrom = (PolygonArray) this.adapter.deserialize(contents);
        logger.info("Took {} to deserialize PolygonArray from bytestream",
                startTime.elapsedSince());

        logger.info("Testing equality...");
        Assert.assertEquals(polygonArray, parsedFrom);
    }

    @Test(expected = CoreException.class)
    public void testNullElements()
    {
        final PolygonArray polygonArray = new PolygonArray(10);

        Assert.assertTrue(polygonArray.getName() == null);

        Time startTime = Time.now();
        byte[] contents = this.adapter.serialize(polygonArray);
        logger.info("Took {} to serialize PolygonArray", startTime.elapsedSince());

        startTime = Time.now();
        final PolygonArray parsedFrom = (PolygonArray) this.adapter.deserialize(contents);
        logger.info("Took {} to deserialize PolygonArray from bytestream",
                startTime.elapsedSince());

        logger.info("Testing equality...");
        Assert.assertEquals(polygonArray, parsedFrom);

        logger.info("Testing proper handling of null elements...");
        polygonArray.add(new Polygon());
        polygonArray.add(null);
        contents = this.adapter.serialize(polygonArray);
    }
}
