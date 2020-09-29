package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author Yazad Khambata
 */
public class CoordinatesTest
{

    private static <T> List<T> list(final T... items)
    {
        return Arrays.asList(items);
    }

    @Test
    public void forLineString()
    {
        final Coordinates<List<Position>> coordinates = Coordinates.forLineString(
                // Line
                list(
                        // Points
                        list(1d, 2d), list(3d, 4d), list(5d, 6d), list(7d, 8d)));

        Assert.assertTrue(coordinates.getValue() instanceof List);
        Assert.assertEquals(4, coordinates.getValue().size());
        Assert.assertEquals((Double) 1d, coordinates.getValue().get(0).getCoordinate1());
        Assert.assertEquals((Double) 2d, coordinates.getValue().get(0).getCoordinate2());

        Assert.assertEquals((Double) 3d, coordinates.getValue().get(1).getCoordinate1());
        Assert.assertEquals((Double) 4d, coordinates.getValue().get(1).getCoordinate2());

        Assert.assertEquals((Double) 5d, coordinates.getValue().get(2).getCoordinate1());
        Assert.assertEquals((Double) 6d, coordinates.getValue().get(2).getCoordinate2());

        Assert.assertEquals((Double) 7d, coordinates.getValue().get(3).getCoordinate1());
        Assert.assertEquals((Double) 8d, coordinates.getValue().get(3).getCoordinate2());
    }

    @Test
    public void forMultiLineString()
    {
        final Coordinates<List<List<Position>>> coordinates = Coordinates.forMultiLineString(
                // Lines
                list(
                        // Line1
                        list(
                                // Points1
                                list(1d, 2d), list(3d, 4d), list(5d, 6d), list(7d, 8d)),
                        // Line2
                        list(
                                // Points2
                                list(11d, 21d), list(31d, 41d), list(51d, 61d), list(71d, 81d)),
                        // Line3
                        list(
                                // Points3
                                list(12d, 22d), list(32d, 42d), list(52d, 62d), list(72d, 82d)),
                        // Line4
                        list(
                                // Points4
                                list(13d, 23d), list(33d, 43d), list(53d, 63d), list(73d, 83d))));

        Assert.assertTrue(coordinates.getValue() instanceof List);
        Assert.assertEquals(4, coordinates.getValue().size());
        coordinates.getValue().stream().forEach(line -> Assert.assertEquals(4, line.size()));

        Assert.assertEquals((Double) 42d, coordinates.getValue().get(2).get(1).getCoordinate2());
    }

    @Test
    public void forMultiPoint()
    {
        final Coordinates<List<Position>> coordinates = Coordinates
                .forMultiPoint(list(list(1d, 2d), list(3d, 4d), list(5d, 6d), list(7d, 8d)));
        Assert.assertTrue(coordinates.getValue() instanceof List);
        Assert.assertEquals(4, coordinates.getValue().size());
        Assert.assertEquals((Double) 1d, coordinates.getValue().get(0).getCoordinate1());
        Assert.assertEquals((Double) 2d, coordinates.getValue().get(0).getCoordinate2());

        Assert.assertEquals((Double) 3d, coordinates.getValue().get(1).getCoordinate1());
        Assert.assertEquals((Double) 4d, coordinates.getValue().get(1).getCoordinate2());

        Assert.assertEquals((Double) 5d, coordinates.getValue().get(2).getCoordinate1());
        Assert.assertEquals((Double) 6d, coordinates.getValue().get(2).getCoordinate2());

        Assert.assertEquals((Double) 7d, coordinates.getValue().get(3).getCoordinate1());
        Assert.assertEquals((Double) 8d, coordinates.getValue().get(3).getCoordinate2());
    }

    @Test
    public void forMultiPolygon()
    {
        final Coordinates<List<List<List<Position>>>> coordinates = Coordinates.forMultiPolygon(
                // Multi-Polygon
                list(
                        // Polygons
                        list(
                                // Polygon1
                                list(
                                        // Points1
                                        list(1d, 2d), list(3d, 4d), list(5d, 6d), list(7d, 8d)),
                                // Polygon2
                                list(
                                        // Points2
                                        list(11d, 21d), list(31d, 41d), list(51d, 61d),
                                        list(71d, 81d)),
                                // Polygon3
                                list(
                                        // Points3
                                        list(12d, 22d), list(32d, 42d), list(52d, 62d),
                                        list(72d, 82d)),
                                // Polygon4
                                list(
                                        // Points4
                                        list(13d, 23d), list(33d, 43d), list(53d, 63d),
                                        list(73d, 83d)))));

        Assert.assertTrue(coordinates.getValue() instanceof List);
        Assert.assertEquals(4, coordinates.getValue().get(0).size());
        coordinates.getValue().get(0).stream().forEach(line -> Assert.assertEquals(4, line.size()));

        Assert.assertEquals((Double) 42d,
                coordinates.getValue().get(0).get(2).get(1).getCoordinate2());
    }

    @Test
    public void forPoint()
    {
        final Coordinates<Position> coordinates = Coordinates.forPoint(list(1d, 2d));
        Assert.assertTrue(coordinates.getValue() instanceof Position);
        Assert.assertEquals((Double) 1d, coordinates.getValue().getCoordinate1());
        Assert.assertEquals((Double) 2d, coordinates.getValue().getCoordinate2());
    }

    @Test
    public void forPolygon()
    {
        final Coordinates<List<Position>> coordinates = Coordinates.forLineString(
                // Polygon
                list(
                        // Points
                        list(1d, 2d), list(3d, 4d), list(5d, 6d), list(7d, 8d)));

        Assert.assertTrue(coordinates.getValue() instanceof List);
        Assert.assertEquals(4, coordinates.getValue().size());
        Assert.assertEquals((Double) 1d, coordinates.getValue().get(0).getCoordinate1());
        Assert.assertEquals((Double) 2d, coordinates.getValue().get(0).getCoordinate2());

        Assert.assertEquals((Double) 3d, coordinates.getValue().get(1).getCoordinate1());
        Assert.assertEquals((Double) 4d, coordinates.getValue().get(1).getCoordinate2());

        Assert.assertEquals((Double) 5d, coordinates.getValue().get(2).getCoordinate1());
        Assert.assertEquals((Double) 6d, coordinates.getValue().get(2).getCoordinate2());

        Assert.assertEquals((Double) 7d, coordinates.getValue().get(3).getCoordinate1());
        Assert.assertEquals((Double) 8d, coordinates.getValue().get(3).getCoordinate2());
    }
}
