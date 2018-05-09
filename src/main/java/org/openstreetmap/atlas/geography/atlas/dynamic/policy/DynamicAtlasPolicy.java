package org.openstreetmap.atlas.geography.atlas.dynamic.policy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.dynamic.DynamicAtlas;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class DynamicAtlasPolicy
{
    private static final Logger logger = LoggerFactory.getLogger(DynamicAtlasPolicy.class);

    private final Polygon maximumBounds;
    private final Sharding sharding;
    private final Function<Shard, Optional<Atlas>> atlasFetcher;
    private final Set<Shard> initialShards;
    private boolean extendIndefinitely = true;
    private boolean deferLoading = false;
    private Consumer<Set<Shard>> shardSetChecker = set ->
    {
    };
    private Predicate<AtlasEntity> atlasEntitiesToConsiderForExpansion = entity -> true;
    private boolean aggressivelyExploreRelations = false;
    // In case the initial shards were found using a Polygon or a MultiPolygon, remember it to
    // provide the initial shards shape. This will be useful to not over-extend when using
    // extendIndefinitely=false
    private Optional<MultiPolygon> shapeCoveringInitialShards = Optional.empty();

    public DynamicAtlasPolicy(final Function<Shard, Optional<Atlas>> atlasFetcher,
            final Sharding sharding, final MultiPolygon shapeCoveringInitialShards,
            final Polygon maximumBounds)
    {
        this.initialShards = new HashSet<>();
        this.shapeCoveringInitialShards = Optional.of(shapeCoveringInitialShards);
        sharding.shards(shapeCoveringInitialShards).forEach(this.initialShards::add);
        this.atlasFetcher = atlasFetcher;
        this.maximumBounds = maximumBounds;
        this.sharding = sharding;
    }

    public DynamicAtlasPolicy(final Function<Shard, Optional<Atlas>> atlasFetcher,
            final Sharding sharding, final Polygon shapeCoveringInitialShards,
            final Polygon maximumBounds)
    {
        this.initialShards = new HashSet<>();
        this.shapeCoveringInitialShards = Optional
                .of(MultiPolygon.forPolygon(shapeCoveringInitialShards));
        sharding.shards(shapeCoveringInitialShards).forEach(this.initialShards::add);
        this.atlasFetcher = atlasFetcher;
        this.maximumBounds = maximumBounds;
        this.sharding = sharding;
    }

    public DynamicAtlasPolicy(final Function<Shard, Optional<Atlas>> atlasFetcher,
            final Sharding sharding, final Set<Shard> initialShards, final Polygon maximumBounds)
    {
        this.initialShards = initialShards;
        this.atlasFetcher = atlasFetcher;
        this.maximumBounds = maximumBounds;
        this.sharding = sharding;
    }

    public DynamicAtlasPolicy(final Function<Shard, Optional<Atlas>> atlasFetcher,
            final Sharding sharding, final Shard initialShard, final Polygon maximumBounds)
    {
        this.initialShards = new HashSet<>();
        this.initialShards.add(initialShard);
        this.atlasFetcher = atlasFetcher;
        this.maximumBounds = maximumBounds;
        this.sharding = sharding;
    }

    public Predicate<AtlasEntity> getAtlasEntitiesToConsiderForExpansion()
    {
        return this.atlasEntitiesToConsiderForExpansion;
    }

    public Function<Shard, Optional<Atlas>> getAtlasFetcher()
    {
        // Here, make sure to not load outside the bounds.
        return shard ->
        {
            if (this.maximumBounds.overlaps(shard.bounds()))
            {
                try
                {
                    return this.atlasFetcher.apply(shard);
                }
                catch (final Throwable error)
                {
                    logger.error("Could not load shard {}, skipping.", shard.getName(), error);
                }
            }
            else
            {
                logger.debug(
                        "Skipping atlasFetcher for {} because shard bounds are outside the policy's maximumBounds",
                        shard.getName());
            }

            return Optional.empty();
        };
    }

    public Set<Shard> getInitialShards()
    {
        return this.initialShards;
    }

    public MultiPolygon getInitialShardsBounds()
    {
        if (this.shapeCoveringInitialShards.isPresent())
        {
            return this.shapeCoveringInitialShards.get();
        }
        final MultiMap<Polygon, Polygon> outerToInners = new MultiMap<>();
        this.initialShards.forEach(shard -> outerToInners.put(shard.bounds(), new ArrayList<>()));
        return new MultiPolygon(outerToInners);
    }

    public Polygon getMaximumBounds()
    {
        return this.maximumBounds;
    }

    public Sharding getSharding()
    {
        return this.sharding;
    }

    public Consumer<Set<Shard>> getShardSetChecker()
    {
        return this.shardSetChecker;
    }

    public boolean isAggressivelyExploreRelations()
    {
        return this.aggressivelyExploreRelations;
    }

    public boolean isDeferLoading()
    {
        return this.deferLoading;
    }

    public boolean isExtendIndefinitely()
    {
        return this.extendIndefinitely;
    }

    /**
     * This switch tells the {@link DynamicAtlas} to preemptively and temporarily load the
     * neighboring shards to see if they contain the relation in the current shard and if the member
     * list is different. In which case it expands to the neighboring shards that are including
     * those members.
     *
     * @param aggressivelyExploreRelations
     *            True to aggressively explore relations
     * @return The modified policy
     */
    public DynamicAtlasPolicy withAggressivelyExploreRelations(
            final boolean aggressivelyExploreRelations)
    {
        this.aggressivelyExploreRelations = aggressivelyExploreRelations;
        return this;
    }

    /**
     * @param atlasEntitiesToConsiderForExpansion
     *            A predicate that defines what entities will be considered when deciding to expand
     *            or not to a neighboring shard.
     * @return The modified policy
     */
    public DynamicAtlasPolicy withAtlasEntitiesToConsiderForExpansion(
            final Predicate<AtlasEntity> atlasEntitiesToConsiderForExpansion)
    {
        this.atlasEntitiesToConsiderForExpansion = atlasEntitiesToConsiderForExpansion;
        return this;
    }

    /**
     * Defer loading until the load command is sent.
     *
     * @param deferLoading
     *            True to defer loading until the load command is sent.
     * @return The modified policy
     */
    public DynamicAtlasPolicy withDeferredLoading(final boolean deferLoading)
    {
        this.deferLoading = deferLoading;
        return this;
    }

    /**
     * The extension policy: if this is set to true, then the loading of shards will go as far as it
     * can within the boundary polygon. If this is set to false, then the loading will extend only
     * if the feature warranting the extension is included within or intersects the initial shard
     * boundaries.
     *
     * @param extendIndefinitely
     *            True to extend indefinitely.
     * @return The modified policy
     */
    public DynamicAtlasPolicy withExtendIndefinitely(final boolean extendIndefinitely)
    {
        this.extendIndefinitely = extendIndefinitely;
        return this;
    }

    /**
     * @param shardSetChecker
     *            A function that will inspect the shards prior to loading them in a MultiAtlas. The
     *            default version just does nothing, but this could for example throw an exception
     *            if the DynamicAtlas is loading too many neighboring shards, or send shard
     *            downloading statistics. This is up to the end user.
     * @return The modified policy
     */
    public DynamicAtlasPolicy withShardSetChecker(final Consumer<Set<Shard>> shardSetChecker)
    {
        this.shardSetChecker = shardSetChecker;
        return this;
    }
}
