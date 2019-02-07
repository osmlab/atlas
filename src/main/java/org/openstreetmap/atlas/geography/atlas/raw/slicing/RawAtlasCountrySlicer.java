package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.dynamic.DynamicAtlas;
import org.openstreetmap.atlas.geography.atlas.dynamic.policy.DynamicAtlasPolicy;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.tags.NaturalTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

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
    private static final String DYNAMIC_ATLAS_CREATION_TASK = "Dynamic Atlas Creation";

    private static final Logger logger = LoggerFactory.getLogger(RawAtlasCountrySlicer.class);

    private final Sharding sharding;

    private final Function<Shard, Optional<Atlas>> atlasFetcher;

    // The countries to be sliced with
    private final CountryBoundaryMap countryBoundaryMap;

    // The boundaries used for slicing
    private final Set<String> countries = new HashSet<>();

    private final List<Shard> loadedShards = new ArrayList<>();

    // Bring in all points -- this is important for proper sectioning later downstream, otherwise we
    // will bring in geometry definitions with no underlying points
    private final Predicate<AtlasEntity> pointPredicate = entity -> entity instanceof Point;

    // Bring in all relations that are tagged as Water or Coastline
    private final Predicate<AtlasEntity> relationPredicate = entity ->
    {
        return entity.getType().equals(ItemType.RELATION) && Validators.isOfType(entity,
                NaturalTag.class, NaturalTag.WATER, NaturalTag.COASTLINE);
    };

    // Dynamic expansion filter will be a combination of the water relations and points
    private final Predicate<AtlasEntity> dynamicAtlasExpansionFilter = entity -> this.pointPredicate
            .test(entity) || this.relationPredicate.test(entity);

    /**
     * The default constructor for the old, pre-water relation pipeline-- this method will slice
     * without dynamic expansion along water relations. Left in for legacy compatibility.
     *
     * @param countries
     *            the list of countries to slice
     * @param countryBoundaryMap
     *            The country boundary map
     */
    public RawAtlasCountrySlicer(final Set<String> countries,
            final CountryBoundaryMap countryBoundaryMap)
    {
        this.countries.addAll(countries);
        this.countryBoundaryMap = countryBoundaryMap;
        this.sharding = null;
        this.atlasFetcher = null;
    }

    /**
     * Updated constructor for the water relation pipeline-- this method will use the Atlas fetcher
     * function and sharding tree to dynamically expand on all water relations so they can be sliced
     * appropriately.
     *
     * @param country
     *            The list of countries to slice. Should really just be one country?
     * @param countryBoundaryMap
     *            The country boundary map
     * @param sharding
     *            The sharding tree
     * @param atlasFetcher
     *            A function return an atlas for a given shard. NOTE: The function expects line
     *            sliced Atlases. And since one shard can have multiple country sliced Atlas files,
     *            the fetcher should MultiAtlas these line sliced country Atlas files together
     *            before returning.
     */
    public RawAtlasCountrySlicer(final Set<String> country,
            final CountryBoundaryMap countryBoundaryMap, final Sharding sharding,
            final Function<Shard, Optional<Atlas>> atlasFetcher)
    {
        if (sharding == null || atlasFetcher == null)
        {
            throw new IllegalArgumentException(
                    "Must supply a valid sharding and fetcher function for sectioning!");
        }
        this.countryBoundaryMap = countryBoundaryMap;
        this.countries.addAll(country);
        this.sharding = sharding;
        this.atlasFetcher = atlasFetcher;
    }

    /**
     * The default constructor for the old, pre-water relation pipeline-- this method will slice
     * without dynamic expansion along water relations. Left in for legacy compatibility.
     *
     * @param country
     *            The country to slice
     * @param countryBoundaryMap
     *            The country boundary map
     */
    public RawAtlasCountrySlicer(final String country, final CountryBoundaryMap countryBoundaryMap)
    {
        this.countries.add(country);
        this.countryBoundaryMap = countryBoundaryMap;
        this.sharding = null;
        this.atlasFetcher = null;
    }

    /**
     * Updated constructor for the water relation pipeline-- this method will use the Atlas fetcher
     * function and sharding tree to dynamically expand on all water relations so they can be sliced
     * appropriately.
     *
     * @param country
     *            The country to slice.
     * @param countryBoundaryMap
     *            The country boundary map
     * @param sharding
     *            The sharding tree
     * @param atlasFetcher
     *            A function return an atlas for a given shard. NOTE: The function expects line
     *            sliced Atlases. And since one shard can have multiple country sliced Atlas files,
     *            the fetcher should MultiAtlas these line sliced country Atlas files together
     *            before returning.
     */
    public RawAtlasCountrySlicer(final String country, final CountryBoundaryMap countryBoundaryMap,
            final Sharding sharding, final Function<Shard, Optional<Atlas>> atlasFetcher)
    {
        if (sharding == null || atlasFetcher == null)
        {
            throw new IllegalArgumentException(
                    "Must supply a valid sharding and fetcher function for sectioning!");
        }
        this.countryBoundaryMap = countryBoundaryMap;
        this.countries.add(country);
        this.sharding = sharding;
        this.atlasFetcher = atlasFetcher;
    }

    /**
     * Legacy pipeline for compatibility that slices lines and relations in one go, without dynamic
     * expansion on water relations. Please slice lines and relations individually now using the
     * sliceLines and sliceRelations methods.
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
        logger.info("Started point and line slicing for shard {}", shardName);

        final RawAtlasSlicer pointAndLineSlicer = new RawAtlasPointAndLineSlicer(this.countries,
                this.countryBoundaryMap, rawAtlas);
        logger.info("Finished point and line slicing for shard {} in {}", shardName,
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
        logger.info("Started relation slicing for shard {}", shardName);
        final RawAtlasSlicer relationSlicer = new RawAtlasRelationSlicer(partiallySlicedAtlas, null,
                this.countries, this.countryBoundaryMap);
        logger.info("Finished relation slicing for shard {} in {}", shardName, time.elapsedSince());
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
        final String shardName = getShardOrAtlasName(partiallySlicedExpandedAtlas);
        logger.info("Started relation slicing for shard {}", shardName);

        final RawAtlasSlicer relationSlicer = new RawAtlasRelationSlicer(
                partiallySlicedExpandedAtlas, initialShard, this.countries,
                this.countryBoundaryMap);
        logger.info("Finished relation slicing for shard {} in {}", shardName, time.elapsedSince());
        return relationSlicer.slice();
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
        final Time dynamicAtlasTime = logTaskStartedAsInfo(DYNAMIC_ATLAS_CREATION_TASK,
                initialShard.getName());

        // Keep track of all loaded shards. This will be used to cut the sub-atlas for the shard
        // we're processing after all sectioning is completed. Initial shard will always be first!
        this.loadedShards.add(initialShard);

        final DynamicAtlasPolicy policy = new DynamicAtlasPolicy(this.atlasFetcher, this.sharding,
                initialShard, Rectangle.MAXIMUM).withDeferredLoading(true)
                        .withExtendIndefinitely(false).withAggressivelyExploreRelations(true)
                        .withAtlasEntitiesToConsiderForExpansion(
                                this.dynamicAtlasExpansionFilter::test);

        final DynamicAtlas atlas = new DynamicAtlas(policy);
        atlas.preemptiveLoad();

        logTaskAsInfo(COMPLETED_TASK_MESSAGE, DYNAMIC_ATLAS_CREATION_TASK, getShardOrAtlasName(),
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

    private void logTaskAsInfo(final String message, final Object... arguments)
    {
        logger.info(MessageFormatter.arrayFormat(message, arguments).getMessage());
    }

    private Time logTaskStartedAsInfo(final String taskname, final String shardName)
    {
        final Time time = Time.now();
        logger.info(STARTED_TASK_MESSAGE, taskname, shardName);
        return time;
    }

}
