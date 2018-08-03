package org.openstreetmap.atlas.utilities.collections;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.utilities.maps.MultiMap;

/**
 * Class container for test classes
 *
 * @author jklamer
 */
public class ShardBucketCollectionTestClasses
{
    /**
     * Collection that will handle splitting multipolygons into buckets by overriding the add
     * function
     */
    public static class MultiPolygonSort
            extends ShardBucketCollection<MultiPolygon, ArrayList<MultiPolygon>>
    {
        public MultiPolygonSort(final Rectangle maxBounds, final Integer zoomLevel)
        {
            super(maxBounds, zoomLevel);
        }

        /**
         * This function only puts the part of the multipolygon with outers intersecting with the
         * shard
         *
         * @param item
         *            to add
         * @param collection
         *            to add to
         * @param shard
         *            shard associated with the collection
         * @return whether it was added successfully
         */
        @Override
        protected boolean addFunction(final MultiPolygon item,
                final ArrayList<MultiPolygon> collection, final Shard shard)
        {
            final Rectangle shardBounds = shard.bounds();
            final MultiMap<Polygon, Polygon> newMultiPolygon = new MultiMap<>();
            item.outers().forEach(outer ->
            {
                if (outer.overlaps(shardBounds))
                {
                    newMultiPolygon.put(outer, item.innersOf(outer));
                }
            });
            return collection.add(new MultiPolygon(newMultiPolygon));
        }

        @Override
        protected boolean allowMultipleBucketInsertion()
        {
            return true;
        }

        @Override
        protected ArrayList<MultiPolygon> initializeBucketCollection()
        {
            return new ArrayList<>();
        }
    }

    /**
     * Class where the polylines are put at the first bucket whose bounds overlap with the start
     */
    public static class PolyLineBucketedAtStartLocation
            extends ShardBucketCollection<PolyLine, HashSet<PolyLine>>
    {
        public PolyLineBucketedAtStartLocation(final Rectangle maxBounds, final Integer zoomLevel)
        {
            super(maxBounds, zoomLevel);
        }

        @Override
        protected boolean allowMultipleBucketInsertion()
        {
            return false;
        }

        @Override
        protected HashSet<PolyLine> initializeBucketCollection()
        {
            return new HashSet<>();
        }

        /**
         * Choose the first shard that overlaps the start point of the PolyLine as sorted by the
         * SlippyTile class
         *
         * @param item
         *            located item to insert
         * @param possibleBuckets
         *            possible buckets for the item to work into
         * @return
         */
        @Override
        protected Shard resolveShard(final PolyLine item,
                final List<? extends Shard> possibleBuckets)
        {
            final Location start = Iterables.head(item);
            return possibleBuckets.stream().map(shard -> (SlippyTile) shard)
                    .filter(shard -> shard.bounds().fullyGeometricallyEncloses(start)).sorted()
                    .findFirst().get();
        }
    }

    /**
     * Basic implementation for testing core functionality
     */
    public static class PolylineBuckets extends ShardBucketCollection<PolyLine, HashSet<PolyLine>>
    {
        public PolylineBuckets(final Rectangle maxBounds, final Integer zoomLevel)
        {
            super(maxBounds, zoomLevel);
        }

        @Override
        protected boolean allowMultipleBucketInsertion()
        {
            return true;
        }

        @Override
        protected HashSet<PolyLine> initializeBucketCollection()
        {
            return new HashSet<>();
        }
    }

    /**
     * Unimplemented resolve shard
     */
    public static class UnsupportedOperation
            extends ShardBucketCollection<PolyLine, HashSet<PolyLine>>
    {
        public UnsupportedOperation(final Rectangle maxBounds, final Integer zoomLevel)
        {
            super(maxBounds, zoomLevel);
        }

        @Override
        protected boolean allowMultipleBucketInsertion()
        {
            return false;
        }

        @Override
        protected HashSet<PolyLine> initializeBucketCollection()
        {
            return new HashSet<>();
        }
    }
}
