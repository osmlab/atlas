package org.openstreetmap.atlas.geography.atlas.dynamic.policy;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
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
    private final Shard initialShard;
    private boolean extendIndefinitely = true;
    private boolean deferLoading = false;
    private Consumer<Set<Shard>> shardSetChecker = set ->
    {
    };

    public DynamicAtlasPolicy(final Function<Shard, Optional<Atlas>> atlasFetcher,
            final Sharding sharding, final Shard initialShard, final Polygon maximumBounds)
    {
        this.initialShard = initialShard;
        this.atlasFetcher = atlasFetcher;
        this.maximumBounds = maximumBounds;
        this.sharding = sharding;
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
            return Optional.empty();
        };
    }

    public Shard getInitialShard()
    {
        return this.initialShard;
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

    public boolean isDeferLoading()
    {
        return this.deferLoading;
    }

    public boolean isExtendIndefinitely()
    {
        return this.extendIndefinitely;
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
     *            True to extend indefinitely
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
