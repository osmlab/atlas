package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
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
import org.openstreetmap.atlas.tags.filters.TaggableFilter;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

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

    // Resource defining the filter for Relations that will be expanded on
    private static final String DYNAMIC_RELATION_TAG_FILTER_RESOURCE = "dynamic-relation-tag-filter.json";

    // The default dynamically expanded Relation tags
    private static List<TaggableFilter> defaultTaggableFilter = computeDefaultFilter();

    // Bring in all Relations that are tagged as Water or Coastline
    private static final Predicate<AtlasEntity> relationPredicate = entity -> entity.getType()
            .equals(ItemType.RELATION)
            && defaultTaggableFilter.stream().allMatch(filter -> filter.test(entity));

    // Dynamic expansion filter will be a combination of the water Relations and Points
    private static final Predicate<AtlasEntity> dynamicAtlasExpansionFilter = relationPredicate::test;

    private final Sharding sharding;

    private final Function<Shard, Optional<Atlas>> atlasFetcher;

    private final List<Shard> loadedShards = new ArrayList<>();

    private final AtlasLoadingOption loadingOption;

    private static List<TaggableFilter> computeDefaultFilter()
    {
        try (InputStreamReader reader = new InputStreamReader(RawAtlasCountrySlicer.class
                .getResourceAsStream(DYNAMIC_RELATION_TAG_FILTER_RESOURCE)))
        {
            final JsonElement element = new JsonParser().parse(reader);
            final JsonArray filters = element.getAsJsonObject().get("filters").getAsJsonArray();
            return StreamSupport.stream(filters.spliterator(), false)
                    .map(jsonElement -> TaggableFilter.forDefinition(jsonElement.getAsString()))
                    .collect(Collectors.toList());
        }
        catch (final Exception exception)
        {
            throw new CoreException(
                    "There was a problem parsing {}. Check if the JSON file has valid structure.",
                    DYNAMIC_RELATION_TAG_FILTER_RESOURCE, exception);
        }
    }

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
        this.sharding = sharding;
        this.atlasFetcher = atlasFetcher;
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

        // Keep track of all loaded shards. This will be used to cut the sub-atlas for the shard
        // we're processing after all slicing is completed. Initial shard will always be first!
        this.loadedShards.add(initialShard);

        final DynamicAtlasPolicy policy = new DynamicAtlasPolicy(this.atlasFetcher, this.sharding,
                initialShard, Rectangle.MAXIMUM).withDeferredLoading(true)
                        .withExtendIndefinitely(false).withAggressivelyExploreRelations(true)
                        .withAtlasEntitiesToConsiderForExpansion(dynamicAtlasExpansionFilter);

        final DynamicAtlas atlas = new DynamicAtlas(policy);
        atlas.preemptiveLoad();

        logger.info(COMPLETED_TASK_MESSAGE, DYNAMIC_ATLAS_CREATION_TASK, getShardOrAtlasName(),
                dynamicAtlasTime.elapsedSince());
        return atlas;
    }

    private String getShardOrAtlasName()
    {
        // Default to getting the Shard name, if available, otherwise fall back to atlas name
        if (!this.loadedShards.isEmpty())
        {
            return this.loadedShards.get(0).getName();
        }
        else
        {
            return StringUtils.EMPTY;
        }
    }

    private String getShardOrAtlasName(final Atlas atlas)
    {
        return atlas.metaData().getShardName().orElse(atlas.getName());
    }
}
