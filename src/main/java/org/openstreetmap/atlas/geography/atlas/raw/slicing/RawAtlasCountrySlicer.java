package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.dynamic.DynamicAtlas;
import org.openstreetmap.atlas.geography.atlas.dynamic.policy.DynamicAtlasPolicy;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.sub.AtlasCutType;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entry point to initiate raw {@link Atlas} country-slicing.
 *
 * @author mgostintsev
 * @author samg
 */
public class RawAtlasCountrySlicer
{

    // Logging constants
    private static final String STARTED_TASK_MESSAGE = "Started {} for Shard {}";
    private static final String COMPLETED_TASK_MESSAGE = "Finished {} for Shard {} in {}";
    private static final String DYNAMIC_ATLAS_CREATION_TASK = "dynamic Atlas creation";
    private static final String POINT_AND_LINE_SLICING_TASK = "point and line slicing";
    private static final String RELATION_SLICING_TASK = "relation slicing";

    private static final Logger logger = LoggerFactory.getLogger(RawAtlasCountrySlicer.class);

    // Bring in all Relations that are tagged as Water or Coastline
    private final Predicate<AtlasEntity> relationPredicate;

    private final Sharding sharding;

    private final Function<Shard, Optional<Atlas>> atlasFetcher;

    private final AtlasLoadingOption loadingOption;

    /**
     * The default constructor from before water relations were handled -- this method will slice
     * without dynamic expansion along relations. Left in for legacy compatibility.
     *
     * @param loadingOption
     *            The {@link AtlasLoadingOption} to use
     */
    public RawAtlasCountrySlicer(final AtlasLoadingOption loadingOption)
    {
        this.loadingOption = loadingOption;
        this.relationPredicate = entity -> entity.getType().equals(ItemType.RELATION)
                && loadingOption.getRelationSlicingFilter().test(entity);
        this.sharding = null;
        this.atlasFetcher = null;
    }

    /**
     * Updated constructor for generating sliced Atlases-- this method will use the Atlas fetcher
     * function and sharding tree to dynamically expand on certain relations so they can be sliced
     * appropriately.
     *
     * @param loadingOption
     *            The {@link AtlasLoadingOption} to use
     * @param sharding
     *            The sharding tree
     * @param atlasFetcher
     *            A function return an atlas for a given shard. NOTE: The function expects line
     *            sliced Atlases. And since one shard can have multiple country sliced Atlas files,
     *            the fetcher should MultiAtlas these line sliced country Atlas files together
     *            before returning.
     */
    public RawAtlasCountrySlicer(final AtlasLoadingOption loadingOption, final Sharding sharding,
            final Function<Shard, Optional<Atlas>> atlasFetcher)
    {
        if (sharding == null || atlasFetcher == null)
        {
            throw new IllegalArgumentException(
                    "Must supply a valid sharding and fetcher function for slicing!");
        }
        this.loadingOption = loadingOption;
        this.relationPredicate = entity -> entity.getType().equals(ItemType.RELATION)
                && loadingOption.getRelationSlicingFilter().test(entity);
        this.sharding = sharding;
        this.atlasFetcher = atlasFetcher;
    }

    public AtlasLoadingOption getLoadingOption()
    {
        return this.loadingOption;
    }

    /**
     * Legacy entrypoint that slices lines and relations in one go, without dynamic expansion on
     * water relations. Please slice lines and relations individually now using the sliceLines and
     * sliceRelations methods.
     *
     * @param rawAtlas
     *            An unsliced Atlas
     * @return A fully sliced Atlas
     */
    public Atlas slice(final Atlas rawAtlas)
    {
        final Atlas partiallySlicedAtlas = sliceLines(rawAtlas);
        return sliceRelations(partiallySlicedAtlas);
    }

    /**
     * Slice all lines in a given unsliced Atlas.
     *
     * @param rawAtlas
     *            An unsliced Atlas
     * @return A line-sliced Atlas ready for relation slicing
     */
    public Atlas sliceLines(final Atlas rawAtlas)
    {
        final Time time = Time.now();
        final String shardName = getShardOrAtlasName(rawAtlas);
        logger.info(STARTED_TASK_MESSAGE, POINT_AND_LINE_SLICING_TASK, shardName);

        final RawAtlasSlicer pointAndLineSlicer = new RawAtlasPointAndLineSlicer(rawAtlas,
                this.loadingOption);
        logger.info(COMPLETED_TASK_MESSAGE, POINT_AND_LINE_SLICING_TASK, shardName,
                time.elapsedSince());
        return pointAndLineSlicer.slice();
    }

    /**
     * Slice all lines in a given line-sliced Atlas. Note that this version of the method does NOT
     * dynamically expand-- it simply takes the given Atlas file and relation slices it. Useful for
     * testing in some cases.
     *
     * @param partiallySlicedAtlas
     *            An Atlas that has been line-sliced
     * @return A fully sliced (lines and relations) Atlas
     */
    public Atlas sliceRelations(final Atlas partiallySlicedAtlas)
    {
        final Time time = Time.now();
        final String shardName = getShardOrAtlasName(partiallySlicedAtlas);
        logger.info(STARTED_TASK_MESSAGE, RELATION_SLICING_TASK, shardName);
        final RawAtlasSlicer relationSlicer = new RawAtlasRelationSlicer(partiallySlicedAtlas, null,
                this.loadingOption);
        logger.info(COMPLETED_TASK_MESSAGE, RELATION_SLICING_TASK, shardName, time.elapsedSince());
        return relationSlicer.slice();
    }

    /**
     * Slice all lines in a given line-sliced Atlas. Uses the Atlas fetcher function to fetch data
     * for the initial shard and any expanded shards, expanding along water relations.
     *
     * @param initialShard
     *            The initial shard being sliced
     * @return A fully sliced (lines and relations) Atlas with water relations expanded upon
     */
    public Atlas sliceRelations(final Shard initialShard)
    {
        final Time time = Time.now();
        final Atlas partiallySlicedExpandedAtlas = buildExpandedAtlas(initialShard);
        final Set<Long> relationsForInitialShard = new HashSet<>();
        final Optional<Atlas> initialShardOptional = this.atlasFetcher.apply(initialShard);
        if (initialShardOptional.isPresent())
        {
            initialShardOptional.get().relations()
                    .forEach(relation -> relationsForInitialShard.add(relation.getIdentifier()));
        }
        else
        {
            throw new CoreException(
                    "Could not get data for initial shard {} during relation slicing!",
                    initialShard.getName());
        }

        final Predicate<AtlasEntity> filter = entity ->
        {
            if (entity.getType().equals(ItemType.RELATION))
            {
                return relationsForInitialShard.contains(entity.getIdentifier());
            }
            return true;
        };
        final Optional<Atlas> subAtlasOptional = partiallySlicedExpandedAtlas.subAtlas(filter,
                AtlasCutType.SILK_CUT);
        if (subAtlasOptional.isPresent())
        {
            final Atlas partiallySlicedExpandedAtlasWithExtraRelationsRemoved = subAtlasOptional
                    .get();
            final String shardName = getShardOrAtlasName(
                    partiallySlicedExpandedAtlasWithExtraRelationsRemoved);
            logger.info(STARTED_TASK_MESSAGE, RELATION_SLICING_TASK, shardName);

            final RawAtlasSlicer relationSlicer = new RawAtlasRelationSlicer(
                    partiallySlicedExpandedAtlasWithExtraRelationsRemoved, initialShard,
                    this.loadingOption);
            logger.info(COMPLETED_TASK_MESSAGE, RELATION_SLICING_TASK, shardName,
                    time.elapsedSince());
            return relationSlicer.slice();
        }
        else
        {
            throw new CoreException(
                    "No data after sub-atlasing to remove new relations from partially expanded Atlas {}!",
                    partiallySlicedExpandedAtlas.getName());
        }

    }

    /**
     * Grabs the atlas for the initial shard, in its entirety. Then proceeds to expand out to
     * surrounding shards if there are any edges bleeding over the shard bounds plus
     * {@link #SHARD_EXPANSION_DISTANCE}. Finally, will return the constructed Atlas.
     *
     * @param initialShard
     *            The initial {@link Shard} being processed
     * @param sharding
     *            The {@link Sharding} used to identify which shards to fetch
     * @param partiallySlicedAtlasFetcher
     *            The fetcher policy to retrieve an Atlas file for each shard
     * @return the expanded {@link Atlas}
     */
    private Atlas buildExpandedAtlas(final Shard initialShard)
    {
        final Time dynamicAtlasTime = Time.now();
        logger.info(DYNAMIC_ATLAS_CREATION_TASK, initialShard.getName());

        final DynamicAtlasPolicy policy = new DynamicAtlasPolicy(this.atlasFetcher, this.sharding,
                initialShard, Rectangle.MAXIMUM).withDeferredLoading(true)
                        .withExtendIndefinitely(false).withAggressivelyExploreRelations(true)
                        .withAtlasEntitiesToConsiderForExpansion(this.relationPredicate);

        final DynamicAtlas atlas = new DynamicAtlas(policy);
        atlas.preemptiveLoad();

        logger.info(COMPLETED_TASK_MESSAGE, DYNAMIC_ATLAS_CREATION_TASK, initialShard.getName(),
                dynamicAtlasTime.elapsedSince());
        return atlas;
    }

    private String getShardOrAtlasName(final Atlas atlas)
    {
        return atlas.metaData().getShardName().orElse(atlas.getName());
    }
}
