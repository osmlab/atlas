package org.openstreetmap.atlas.utilities.collections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * Test for example {@link ShardBucketCollection}s
 *
 * @author jklamer
 */
public class ShardBucketCollectionTest
{

    @Test
    public void testMultiPolygonSort()
    {
        final Rectangle maxBounds = SlippyTile.forName("1-0-0").bounds()
                .contract(Distance.ONE_METER);

        final ShardBucketCollectionTestClasses.MultiPolygonSort multiPolygonSort = new ShardBucketCollectionTestClasses.MultiPolygonSort(
                maxBounds, 4);

        final MultiPolygon firstShardMultiPolygon = this
                .multiPolygonForShard(SlippyTile.forName("4-0-0"));
        final MultiPolygon secondShardMultiPolygon = this
                .multiPolygonForShard(SlippyTile.forName("4-1-0"));
        final MultiPolygon combinedMultiPolygon = firstShardMultiPolygon
                .merge(secondShardMultiPolygon);

        // the combined multipolygon will be sorted into the two different buckets as the original
        // multipolygons
        multiPolygonSort.add(combinedMultiPolygon);
        Assert.assertTrue(multiPolygonSort.contains(firstShardMultiPolygon));
        Assert.assertTrue(multiPolygonSort.contains(secondShardMultiPolygon));

        // make a multipolygon with an outer for every shard and ensure they all get sorted into
        // their proper buckets
        multiPolygonSort.clear();
        multiPolygonSort.add(this.multiPolygonForShards(SlippyTile.allTiles(4, maxBounds)));
        Assert.assertEquals(64, multiPolygonSort.size());
        for (final Shard shard : SlippyTile.allTiles(4, maxBounds))
        {
            Assert.assertTrue(multiPolygonSort.getBucketCollectionForShard(shard).map(List::stream)
                    .map(multiPolygonStream -> multiPolygonStream.allMatch(
                            multiPolygon -> multiPolygon.equals(this.multiPolygonForShard(shard))))
                    .orElse(false));
        }
        Assert.assertEquals(64L,
                multiPolygonSort.getAllShardBucketCollectionPairs().entrySet().size());

        // make some big multipolygons that will go in multiple buckets but still touch all the
        // buckets
        multiPolygonSort.clear();
        multiPolygonSort.add(this.multiPolygonForShards(SlippyTile.allTiles(2, maxBounds)));
        Assert.assertEquals(64, multiPolygonSort.size());
        Assert.assertEquals(4, multiPolygonSort.distinctStream().count());
    }

    @Test
    public void testShardResolution()
    {
        final Rectangle maxBounds = SlippyTile.forName("1-0-0").bounds()
                .contract(Distance.ONE_METER);

        // collection with implemented shard resolution strategy
        final ShardBucketCollectionTestClasses.PolyLineBucketedAtStartLocation startLocationBuckets = new ShardBucketCollectionTestClasses.PolyLineBucketedAtStartLocation(
                maxBounds, 3);
        final Iterable<SlippyTile> shardsWithinBounds = SlippyTile.allTiles(3, maxBounds);

        // a polyline that will cover all 16 buckets but go into the first one
        final PolyLine allBucketsPolyline = this.polylineForShards(shardsWithinBounds);

        startLocationBuckets.add(allBucketsPolyline);
        Assert.assertEquals(1, startLocationBuckets.size());
        Assert.assertTrue(
                startLocationBuckets.getBucketCollectionForShard(SlippyTile.forName("3-0-0"))
                        .map(collection -> collection.contains(allBucketsPolyline)).orElse(false));
        startLocationBuckets.clear();

        // adding reversed polyline changes bucket it is stored in
        final PolyLine testLineAccrossBuckets = this.polylineForShards("3-0-0,3-3-3");
        startLocationBuckets.add(testLineAccrossBuckets);
        Assert.assertTrue(startLocationBuckets
                .getBucketCollectionForShard(SlippyTile.forName("3-0-0"))
                .map(collection -> collection.contains(testLineAccrossBuckets)).orElse(false));
        startLocationBuckets.add(testLineAccrossBuckets.reversed());
        Assert.assertTrue(
                startLocationBuckets.getBucketCollectionForShard(SlippyTile.forName("3-3-3"))
                        .map(collection -> collection.contains(testLineAccrossBuckets.reversed()))
                        .orElse(false));
        startLocationBuckets.clear();

        // add a polyline whose start location intersects with 4 shards and gets resolved to the
        // first as decided by the resolveShard method and slippy tile comparator
        final ArrayList<Location> locations = new ArrayList<>();
        locations.addAll(SlippyTile.forName("3-0-0").bounds());
        locations.retainAll(SlippyTile.forName("3-1-1").bounds());
        // at the corner of both shards
        final Location startLocation = locations.get(0);
        locations.clear();
        locations.addAll(SlippyTile.forName("3-1-1").bounds());
        locations.retainAll(SlippyTile.forName("3-2-0").bounds());
        // across to another corner
        final Location endLocation = locations.get(0);

        final PolyLine onTheEdges = new PolyLine(Arrays.asList(startLocation, endLocation));
        startLocationBuckets.add(onTheEdges);
        Assert.assertEquals(1, startLocationBuckets.size());
        Assert.assertTrue(
                startLocationBuckets.getBucketCollectionForShard(SlippyTile.forName("3-0-0"))
                        .map(collection -> collection.contains(onTheEdges)).orElse(false));

    }

    @Test
    public void testSimplePolylineCollectionFunctionality()
    {
        final Rectangle maxBounds = SlippyTile.forName("1-0-0").bounds()
                .contract(Distance.ONE_METER);

        final Iterable<SlippyTile> shardsWithinBounds = SlippyTile.allTiles(3, maxBounds);
        // a polyline that will go into all 16 buckets
        final PolyLine allBucketsPolyline = this.polylineForShards(shardsWithinBounds);
        // create Zoom level three buckets within the bounds of the first zoom = 1 slippy tile
        final ShardBucketCollectionTestClasses.PolylineBuckets polylineBuckets1 = new ShardBucketCollectionTestClasses.PolylineBuckets(
                maxBounds, 3);

        // test add, contains, size, stream
        polylineBuckets1.add(allBucketsPolyline);
        Assert.assertTrue(polylineBuckets1.contains(allBucketsPolyline));
        Assert.assertFalse(polylineBuckets1.contains(new Integer(0)));
        Assert.assertFalse(polylineBuckets1.contains(null));
        Assert.assertTrue(polylineBuckets1.getAllBucketCollections()
                .allMatch(bucket -> bucket.contains(allBucketsPolyline)));
        Assert.assertEquals(16L, polylineBuckets1.stream().count());
        Assert.assertTrue(polylineBuckets1.stream().allMatch(allBucketsPolyline::equals));
        Assert.assertEquals(16L, polylineBuckets1.size());
        Assert.assertEquals(polylineBuckets1.size(), polylineBuckets1.toArray().length);
        Assert.assertEquals(1L, polylineBuckets1.distinctStream().count());

        polylineBuckets1.clear();
        Assert.assertEquals(0L, polylineBuckets1.size());
        Assert.assertFalse(polylineBuckets1.iterator().hasNext());

        polylineBuckets1.add(allBucketsPolyline);
        polylineBuckets1.remove(allBucketsPolyline);
        Assert.assertTrue(polylineBuckets1.isEmpty());

        // Test with bucket unique items
        // reinitialize
        final ShardBucketCollectionTestClasses.PolylineBuckets polylineBuckets2 = new ShardBucketCollectionTestClasses.PolylineBuckets(
                maxBounds, 3);

        final PolyLine firstBucketPolyline = this
                .polylineForShards(Iterables.first(shardsWithinBounds).get().split(4));
        final PolyLine lastBucketPolyline = this
                .polylineForShards(Iterables.last(shardsWithinBounds).get().split(4));

        // test getting buckets
        polylineBuckets2.add(firstBucketPolyline);
        polylineBuckets2.add(lastBucketPolyline);
        Assert.assertFalse(polylineBuckets2.isEmpty());
        Assert.assertEquals(2, polylineBuckets2.size());
        Assert.assertTrue("Bucket does not exist or doesn't contain polyline",
                polylineBuckets2.getBucketCollectionForShard(SlippyTile.forName("3-0-0"))
                        .map(collection -> collection.contains(firstBucketPolyline)).orElse(false));
        Assert.assertTrue(
                polylineBuckets2.getBucketCollectionsForBounds(lastBucketPolyline.bounds())
                        .allMatch(collection -> collection.contains(lastBucketPolyline)));

        // outside of bounds shard
        Assert.assertFalse(polylineBuckets2.getBucketCollectionForShard(SlippyTile.forName("3-4-0"))
                .isPresent());
        // wrong zoom shard
        Assert.assertFalse(polylineBuckets2.getBucketCollectionForShard(SlippyTile.forName("4-0-0"))
                .isPresent());

        // test remove
        Assert.assertTrue(polylineBuckets2
                .containsAll(Arrays.asList(firstBucketPolyline, lastBucketPolyline)));
        Assert.assertTrue(
                polylineBuckets2.removeAll(Arrays.asList(firstBucketPolyline, lastBucketPolyline)));
        Assert.assertFalse(polylineBuckets2.remove(firstBucketPolyline));
        Assert.assertFalse(polylineBuckets2.remove(new Integer(0)));
        Assert.assertFalse(polylineBuckets2
                .containsAll(Arrays.asList(firstBucketPolyline, lastBucketPolyline)));

        // test retain
        Assert.assertTrue(
                polylineBuckets2.addAll(Arrays.asList(firstBucketPolyline, lastBucketPolyline)));
        Assert.assertFalse(
                polylineBuckets2.retainAll(Arrays.asList(firstBucketPolyline, lastBucketPolyline)));
        Assert.assertTrue(polylineBuckets2
                .containsAll(Arrays.asList(firstBucketPolyline, lastBucketPolyline)));
        Assert.assertTrue(polylineBuckets2.retainAll(Arrays.asList(firstBucketPolyline)));
        Assert.assertTrue(polylineBuckets2.containsAll(Arrays.asList(firstBucketPolyline)));

        // test array transfer
        polylineBuckets1.clear();
        polylineBuckets2.clear();
        polylineBuckets1.add(firstBucketPolyline);
        polylineBuckets2.addAll(Arrays.asList(firstBucketPolyline, lastBucketPolyline));
        final Object[] firstCollectionArray = polylineBuckets1.toArray();
        final PolyLine[] array1 = new PolyLine[] { firstBucketPolyline };
        Assert.assertArrayEquals(array1, firstCollectionArray);
        final PolyLine[] array2 = new PolyLine[] { firstBucketPolyline, firstBucketPolyline,
                lastBucketPolyline };
        final PolyLine[] allArray = polylineBuckets2.toArray(array1);
        Assert.assertArrayEquals(array2, allArray);

        // get code coverage
        Assert.assertEquals(maxBounds, polylineBuckets1.getMaximumBounds());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testUnimplemented()
    {
        final Rectangle maxBounds = SlippyTile.forName("1-0-0").bounds()
                .contract(Distance.ONE_METER);

        // collection without implemented resolve shard strategy
        final ShardBucketCollectionTestClasses.UnsupportedOperation unsupportedOperation = new ShardBucketCollectionTestClasses.UnsupportedOperation(
                maxBounds, 3);
        // a polyline that will intersect with multiple buckets
        final PolyLine testPolyLine1 = this.polylineForShards(SlippyTile.allTiles(3, maxBounds));
        unsupportedOperation.add(testPolyLine1);
    }

    /**
     * Multipolygon for shard with outer smaller than the bounds
     */
    private MultiPolygon multiPolygonForShard(final Shard shard)
    {
        final Rectangle bounds = shard.bounds();
        final Rectangle outer = bounds.contract(Distance.ONE_METER);
        final Rectangle inner = outer.contract(Distance.ONE_METER);
        final MultiMap<Polygon, Polygon> multiMap = new MultiMap<>();
        multiMap.put(outer, Arrays.asList(inner));
        return new MultiPolygon(multiMap);
    }

    /**
     * multipolygon that resides in all shards. With an outer in each shard
     */
    private MultiPolygon multiPolygonForShards(final Iterable<? extends Shard> shards)
    {
        MultiPolygon toReturn = this.multiPolygonForShard(Iterables.head(shards));
        for (final Shard shard : Iterables.tail(shards))
        {
            toReturn = toReturn.merge(this.multiPolygonForShard(shard));
        }
        return toReturn;
    }

    /**
     * Polyline for csv SlippyTile names
     */
    private PolyLine polylineForShards(final String names)
    {
        return polylineForShards(Arrays.stream(names.split(",")).map(SlippyTile::forName)
                .collect(Collectors.toList()));
    }

    /**
     * Construct a polyline from the centers of the provided shards
     */
    private PolyLine polylineForShards(final Iterable<? extends Shard> shards)
    {
        return new PolyLine(
                new StreamIterable<>(shards).map(tile -> tile.bounds().center()).collectToList());
    }
}
