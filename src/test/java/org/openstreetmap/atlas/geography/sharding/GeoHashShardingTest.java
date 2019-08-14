package org.openstreetmap.atlas.geography.sharding;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author matthieun
 */
public class GeoHashShardingTest
{
    private final Sharding sharding = Sharding.forString("geohash@7");

    @Test
    public void testIntersection()
    {
        final Polygon bounds = new Polygon(Location.forWkt("POINT (-122.454 37.739)"),
                Location.forWkt("POINT (-122.4547 37.739)"),
                Location.forWkt("POINT (-122.4543 37.74)"));

        final Iterable<Shard> tiles = this.sharding.shards(bounds);
        Assert.assertEquals(2, Iterables.size(tiles));
        Assert.assertTrue(Iterables.equals(Iterables.from("9q8ytqp", "9q8ytqr"),
                Iterables.stream(tiles).map(tile -> (GeoHashTile) tile).map(GeoHashTile::getName)));
    }

    @Test
    public void testLocation()
    {
        final Location point = Location.forWkt("POINT (-122.454 37.739)");

        final Iterable<Shard> tiles = this.sharding.shardsCovering(point);
        Assert.assertEquals(1, Iterables.size(tiles));
        Assert.assertTrue(Iterables.equals(Iterables.from("9q8ytqp"),
                Iterables.stream(tiles).map(tile -> (GeoHashTile) tile).map(GeoHashTile::getName)));
    }

    @Test
    public void testNeighbors()
    {
        final Iterable<Shard> tiles = this.sharding.neighbors(GeoHashTile.forName("9q8ytqp"));
        Assert.assertEquals(8, Iterables.size(tiles));
        Assert.assertTrue(Iterables.equals(
                Iterables.from("9q8ytmy", "9q8ytmz", "9q8ytqn", "9q8ytqq", "9q8ytqr", "9q8yttb",
                        "9q8ytw0", "9q8ytw2"),
                Iterables.stream(tiles).map(tile -> (GeoHashTile) tile).map(GeoHashTile::getName)));
    }
}
