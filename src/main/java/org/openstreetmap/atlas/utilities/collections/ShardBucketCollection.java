package org.openstreetmap.atlas.utilities.collections;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.index.RTree;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.utilities.scalars.Counter;

/**
 * A collection wrapper for a set of collections associated with shards containing located items,
 * such as CheckFlags or AtlasEntities. Supports concurrent add, contains, remove. The term bucket
 * is used because of the usability of the collections mostly as storage for various location based
 * sorting tasks. Does not support safe iteration while modifying.
 *
 * @param <LocatedType>
 *            Located item type
 * @param <CollectionType>
 *            Type of the Collection of LocatedType associated with shards
 * @author jklamer
 */
public abstract class ShardBucketCollection<LocatedType extends Located & Serializable, CollectionType extends Collection<LocatedType> & Serializable>
        implements Collection<LocatedType>, Serializable
{

    /**
     * A helper class that associates a shard with the index that its collection is at in the
     * collectionBuckets array.
     */
    private static class ShardToCollectionIndex implements Located, Serializable
    {
        private static final long serialVersionUID = 4050100671815503794L;
        private final int index;
        private final Shard shard;

        ShardToCollectionIndex(final int index, final Shard shard)
        {
            this.index = index;
            this.shard = shard;
        }

        @Override
        public Rectangle bounds()
        {
            return this.getShard().bounds();
        }

        public int getIndex()
        {
            return this.index;
        }

        public Shard getShard()
        {
            return this.shard;
        }
    }

    private static final long serialVersionUID = -7892704554302160820L;

    private final CollectionType[] collectionBuckets;
    private final RTree<ShardToCollectionIndex> collectionIndex;
    private final HashMap<Shard, ShardToCollectionIndex> initializedShards = new HashMap<>();
    private Rectangle maximumBounds;

    public ShardBucketCollection(final Rectangle maximumBounds, final Integer zoomLevel)
    {
        this(maximumBounds, SlippyTile.allTiles(zoomLevel, maximumBounds));
    }

    public ShardBucketCollection(final Rectangle maximumBounds, final Sharding sharding)
    {
        this(maximumBounds, sharding.shards(maximumBounds));
    }

    /**
     * Construct the collection by allocating space for a collection for each of the shards and
     * assigning the index back.
     * 
     * @param maximumBounds
     *            maximum bound of the collection
     * @param shards
     *            shards to use
     */
    @SuppressWarnings("unchecked")
    private ShardBucketCollection(final Rectangle maximumBounds,
            final Iterable<? extends Shard> shards)
    {
        this.maximumBounds = maximumBounds;
        this.collectionIndex = new RTree<>();
        final Counter counter = new Counter();
        shards.forEach(shardBucket ->
        {
            final ShardToCollectionIndex shardToCollectionIndex = new ShardToCollectionIndex(
                    (int) counter.getValueAndIncrement(), shardBucket);
            this.collectionIndex.add(shardToCollectionIndex.bounds(), shardToCollectionIndex);
        });
        this.collectionBuckets = (CollectionType[]) Array.newInstance(
                this.initializeBucketCollection().getClass(), (int) counter.getValue());
    }

    @Override
    public final boolean add(final LocatedType item)
    {
        if (Objects.nonNull(item) && item.bounds().overlaps(this.maximumBounds))
        {
            final List<ShardToCollectionIndex> indexes = this.collectionIndex.get(item.bounds());
            if (indexes.size() == 1)
            {
                return this.addFunction(item, this.getOrCreateBucketCollectionAt(indexes.get(0)),
                        indexes.get(0).getShard());
            }
            else if (this.allowMultipleBucketInsertion())
            {
                final long addedAmount = indexes.stream()
                        .filter(index -> this.addFunction(item,
                                this.getOrCreateBucketCollectionAt(index), index.getShard()))
                        .count();
                return addedAmount > 0;
            }
            else
            {
                final Shard toInsertAt = this.resolveShard(item, indexes.stream()
                        .map(ShardToCollectionIndex::getShard).collect(Collectors.toList()));
                final Optional<ShardToCollectionIndex> toAddTo = indexes.stream()
                        .filter(index -> toInsertAt.equals(index.getShard())).findFirst();
                return toAddTo
                        .map(index -> this.addFunction(item,
                                this.getOrCreateBucketCollectionAt(index), index.getShard()))
                        .orElse(false);
            }
        }
        return false;
    }

    @Override
    public boolean addAll(final Collection<? extends LocatedType> collection)
    {
        if (Objects.nonNull(collection))
        {
            final long addCount = collection.stream().filter(this::add).count();
            return addCount > 0;
        }
        return false;
    }

    @Override
    public void clear()
    {
        synchronized (this.collectionBuckets)
        {
            for (int i = 0; i < this.collectionBuckets.length; i++)
            {
                this.collectionBuckets[i] = null;
            }
            this.initializedShards.clear();
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean contains(final Object object)
    {
        final Optional<LocatedType> typedItem = this.castToLocatedType(object);
        if (typedItem.isPresent())
        {
            final LocatedType item = typedItem.get();
            return this.getBucketCollectionsForBounds(item.bounds())
                    .anyMatch(collection -> collection.contains(item));
        }
        return false;
    }

    @Override
    public boolean containsAll(final Collection<?> collection)
    {
        if (Objects.nonNull(collection))
        {
            return collection.stream().allMatch(this::contains);
        }
        return false;
    }

    /**
     * A stream of only distinct elements in the collection. Useful if allowing multiple bucket
     * insertion
     *
     * @return A disticnt stream of the collection
     */
    public Stream<LocatedType> distinctStream()
    {
        return this.stream().distinct();
    }

    /**
     * @return a stream of all initialized bucket collections
     */
    public Stream<CollectionType> getAllBucketCollections()
    {
        return Arrays.stream(this.collectionBuckets).filter(Objects::nonNull);
    }

    /**
     * Get a map of all Shards to their corresponding collection. This will only return shard
     * collection entries with initialized collections.
     *
     * @return Map of shard to collection
     */
    public Map<Shard, CollectionType> getAllShardBucketCollectionPairs()
    {
        return this.initializedShards.values().stream()
                .collect(Collectors.toMap(ShardToCollectionIndex::getShard, this::getCollectionAt));
    }

    /**
     * Get the collection for a given shard. It will return optional empty if the shard's collection
     * isn't initialized
     *
     * @param shard
     *            shard whose collection you want.
     * @return Optional of the initialized Collection
     */
    public Optional<CollectionType> getBucketCollectionForShard(final Shard shard)
    {
        return Optional.ofNullable(this.initializedShards.get(shard)).map(this::getCollectionAt)
                .filter(Objects::nonNull);
    }

    /**
     * A stream of all the bucket collects whose associated shard overlaps with the bounds. Note it
     * will return more than one collection if given a shard bound because the edges of shards
     * overlap
     *
     * @param bounds
     *            to use
     * @return stream of a subset of bucket collections
     */
    public Stream<CollectionType> getBucketCollectionsForBounds(final Rectangle bounds)
    {
        return this.collectionIndex.get(bounds).stream().map(this::getCollectionAt)
                .filter(Objects::nonNull);
    }

    /**
     * Get the max bounds of what located can be in the collection.
     *
     * @return rectangle bounds
     */
    public Rectangle getMaximumBounds()
    {
        return this.maximumBounds;
    }

    @Override
    public boolean isEmpty()
    {
        return this.size() == 0;
    }

    @Override
    public Iterator<LocatedType> iterator()
    {
        return this.stream().iterator();
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(final Object object)
    {
        final Optional<LocatedType> typedItem = this.castToLocatedType(object);
        if (typedItem.isPresent())
        {
            final LocatedType item = typedItem.get();
            if (item.bounds().overlaps(this.maximumBounds))
            {
                final long removeCount = this.getBucketCollectionsForBounds(item.bounds())
                        .filter(collection -> collection.remove(item)).count();
                return removeCount > 0;
            }
        }
        return false;
    }

    @Override
    public boolean removeAll(final Collection<?> collection)
    {
        if (Objects.nonNull(collection))
        {
            final long removeCount = collection.stream().filter(this::remove).count();
            return removeCount > 0;
        }
        return false;
    }

    @Override
    public boolean retainAll(final Collection<?> collection)
    {
        final List<LocatedType> toRemove = this.stream().filter(item -> !collection.contains(item))
                .collect(Collectors.toList());
        return this.removeAll(toRemove);
    }

    @Override
    public int size()
    {
        synchronized (this.collectionBuckets)
        {
            return this.getAllBucketCollections().mapToInt(CollectionType::size).sum();
        }
    }

    @Override
    public Stream<LocatedType> stream()
    {
        return this.getAllBucketCollections().flatMap(CollectionType::stream);
    }

    @Override
    public Object[] toArray()
    {
        Object[] toReturn = new Object[0];
        synchronized (this.collectionBuckets)
        {
            final Iterator<CollectionType> bucketIterator = this.getAllBucketCollections()
                    .iterator();
            while (bucketIterator.hasNext())
            {
                toReturn = ArrayUtils.addAll(toReturn, bucketIterator.next().toArray());
            }
        }
        return toReturn;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T1> T1[] toArray(final T1[] otherArray)
    {
        return ArrayUtils.addAll(otherArray, (T1[]) this.toArray());
    }

    /**
     * To add the item into the collection in a special way or dependent on the shard, this function
     * can be overridden. The return contract is the same of {@link Collection}'s add. This function
     * must be deterministic.
     *
     * @param item
     *            to add
     * @param collection
     *            to add to
     * @param shard
     *            shard associated with the collection
     * @return true if the collection has been added to successfully and changed as a result, false
     *         otherwise
     */
    protected boolean addFunction(final LocatedType item, final CollectionType collection,
            final Shard shard)
    {
        return collection.add(item);
    }

    /**
     * @return true if items are allowed to be in multiple buckets. False otherwise
     */
    protected abstract boolean allowMultipleBucketInsertion();

    /**
     * The collection should be agnostic to the shard. Deciding the collection to insert into and
     * how to insert into based on the shard should be handled by resolveShard and addFunction
     * respectively.
     * 
     * @return an intialized empty bucket collection.
     */
    protected abstract CollectionType initializeBucketCollection();

    /**
     * Resolve which of multiple overlapping shards. Note these shards overlap due to a bounds call,
     * agnostic of the items geometry.
     * 
     * @param item
     *            located item to insert
     * @param possibleBuckets
     *            possible buckets for the item to work into
     * @return Shard whose collection you should add to.
     */
    protected Shard resolveShard(final LocatedType item,
            final List<? extends Shard> possibleBuckets)
    {
        throw new UnsupportedOperationException(
                "Implement this method when not allowing multiple bucket insertion");
    }

    private void createBucketCollectionAt(final ShardToCollectionIndex index)
    {
        synchronized (this.collectionBuckets)
        {
            if (Objects.isNull(this.collectionBuckets[index.getIndex()]))
            {
                this.collectionBuckets[index.getIndex()] = this.initializeBucketCollection();
                this.initializedShards.put(index.getShard(), index);
            }
        }
    }

    private CollectionType getCollectionAt(final ShardToCollectionIndex index)
    {
        synchronized (this.collectionBuckets)
        {
            return this.collectionBuckets[index.getIndex()];
        }
    }

    private CollectionType getOrCreateBucketCollectionAt(final ShardToCollectionIndex index)
    {
        final CollectionType collection = this.getCollectionAt(index);
        if (Objects.isNull(collection))
        {
            this.createBucketCollectionAt(index);
            return this.getCollectionAt(index);
        }
        else
        {
            return collection;
        }
    }

    @SuppressWarnings("unchecked")
    private Optional<LocatedType> castToLocatedType(final Object object)
    {
        try
        {
            return Optional.ofNullable(object).map(cast -> (LocatedType) cast);
        }
        catch (final ClassCastException e)
        {
            return Optional.empty();
        }
    }

}
