package org.openstreetmap.atlas.geography.sharding;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class GeoHashTileTest
{
    private static final Logger logger = LoggerFactory.getLogger(GeoHashTileTest.class);

    @Test
    public void testAllTiles()
    {
        for (int precision = 0; precision <= 4; precision++)
        {
            logger.info("Starting {}", precision);
            final Time start = Time.now();

            Assert.assertEquals(GeoHashTile.numberTilesAtPrecision(precision),
                    Iterables.size(GeoHashTile.allTiles(precision)));
            logger.info("Finished {} in {}", precision, start.elapsedSince());
        }
    }

    @Test
    public void testAllTilesBounded()
    {
        final Rectangle bounds = Rectangle.forLocations(Location.forWkt("POINT (-122.454 37.739)"),
                Location.forWkt("POINT (-122.4555 37.74)"));
        final Iterable<GeoHashTile> tiles = GeoHashTile.allTiles(7, bounds);
        Assert.assertEquals(4, Iterables.size(tiles));
        Assert.assertTrue(
                Iterables.equals(Iterables.from("9q8ytqn", "9q8ytqp", "9q8ytqq", "9q8ytqr"),
                        Iterables.stream(tiles).map(GeoHashTile::getName)));
        Assert.assertEquals(GeoHashTile.ROOT, GeoHashTile.allTiles(0, bounds).iterator().next());
    }

    @Test
    public void testAllTilesPolygonBounded()
    {
        final Polygon bounds = new Polygon(Location.forWkt("POINT (-122.454 37.739)"),
                Location.forWkt("POINT (-122.4547 37.739)"),
                Location.forWkt("POINT (-122.4543 37.74)"));
        final Iterable<GeoHashTile> tiles = GeoHashTile.allTiles(7, bounds);
        Assert.assertEquals(2, Iterables.size(tiles));
        Assert.assertTrue(Iterables.equals(Iterables.from("9q8ytqp", "9q8ytqr"),
                Iterables.stream(tiles).map(GeoHashTile::getName)));
    }

    @Test
    public void testBoundingBox()
    {
        final GeoHashTile tile = new GeoHashTile("9q8ytqp");
        Assert.assertEquals(
                Rectangle.wkt("POLYGON ((-122.4549866 37.7380371, -122.4549866 37.7394104, "
                        + "-122.4536133 37.7394104, -122.4536133 37.7380371, "
                        + "-122.4549866 37.7380371))"),
                tile.bounds());
        Assert.assertEquals(Rectangle.MAXIMUM, GeoHashTile.ROOT.bounds());
    }

    @Test
    public void testEncoding()
    {
        final Location point = Location.forWkt("POINT (-122.454 37.739)");

        final GeoHashTile tile0 = GeoHashTile.covering(point, 0);
        Assert.assertEquals(GeoHashTile.ROOT, tile0);
        Assert.assertEquals("", tile0.getName());

        final GeoHashTile tile3 = GeoHashTile.covering(point, 3);
        Assert.assertEquals("9q8", tile3.getName());

        final GeoHashTile tile7 = GeoHashTile.covering(point, 7);
        Assert.assertEquals("9q8ytqp", tile7.getName());

        final GeoHashTile tile12 = GeoHashTile.covering(point, 12);
        Assert.assertTrue(tile12.getName().startsWith("9q8ytqp"));
    }

    @Test(expected = CoreException.class)
    public void testPrecisionTooHigh()
    {
        GeoHashTile.allTiles(-1);
    }

    @Test(expected = CoreException.class)
    public void testPrecisionTooLow()
    {
        GeoHashTile.allTiles(GeoHashTile.MAXIMUM_PRECISION + 1);
    }
}
