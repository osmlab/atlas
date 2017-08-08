package org.openstreetmap.atlas.geography.atlas.dynamic.policy;

import java.util.Optional;
import java.util.function.Function;

import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.streaming.resource.Resource;

/**
 * Policy that uses {@link Resource}s and takes care of converting them to {@link Atlas}
 *
 * @author matthieun
 */
public class DynamicAtlasResourcePolicy extends DynamicAtlasPolicy
{
    public DynamicAtlasResourcePolicy(final Function<Shard, Optional<Resource>> atlasFetcher,
            final Sharding sharding, final Shard initialShard, final Polygon maximumBounds)
    {
        super(shard ->
        {
            final Optional<Resource> resourceOption = atlasFetcher.apply(shard);
            if (resourceOption.isPresent())
            {
                return Optional.ofNullable(new AtlasResourceLoader().load(resourceOption.get()));
            }
            else
            {
                return Optional.empty();
            }
        }, sharding, initialShard, maximumBounds);
    }
}
