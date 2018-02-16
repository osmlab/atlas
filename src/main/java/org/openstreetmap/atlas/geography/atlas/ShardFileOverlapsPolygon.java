package org.openstreetmap.atlas.geography.atlas;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.sharding.DynamicTileSharding;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Predicate that uses a sharding tree to determine whether a given atlas shard file overlaps a
 * given Polygon. By default it depends on shard files following the naming convention of
 * [name]_[zoom]-[x]-[y].atlas.gz, where the .gz extension is optional. For example,
 * XYZ_9-272-162.atlas.gz and XYZ_9-272-162.atlas are valid name formats. The shard filename pattern
 * can be overridden to work with other naming conventions as long as the [zoom]-[x]-[y] portion of
 * the name still exists as the first group in the pattern.
 *
 * @author rmegraw
 */
public class ShardFileOverlapsPolygon implements Predicate<Resource>
{
    private static final Logger logger = LoggerFactory.getLogger(ShardFileOverlapsPolygon.class);

    /**
     * Matches shard filenames such as XYZ_9-272-162.atlas.gz and XYZ_9-272-162.atlas
     */
    public static final String DEFAULT_SHARD_FILE_REGEX = "^.+_(\\d{1,2}-\\d+-\\d+)\\.atlas(\\.gz)?$";

    private final Pattern shardFilePattern;

    private final Set<String> shardsOverlappingPolygon;

    /**
     * @param shardingTree
     *            Sharding tree
     * @param bounds
     *            Polygon over which shard file overlap is tested
     */
    public ShardFileOverlapsPolygon(final DynamicTileSharding shardingTree, final Polygon bounds)
    {
        this(shardingTree, bounds, DEFAULT_SHARD_FILE_REGEX);
    }

    /**
     * @param shardingTree
     *            Sharding tree
     * @param bounds
     *            Polygon over which shard file overlap is tested
     * @param shardFileRegex
     *            Regex which must extract [zoom]-[x]-[y] portion of shard filename as the first
     *            group (see default regex for example)
     */
    public ShardFileOverlapsPolygon(final DynamicTileSharding shardingTree, final Polygon bounds,
            final String shardFileRegex)
    {
        this.shardFilePattern = Pattern.compile(shardFileRegex);
        this.shardsOverlappingPolygon = new HashSet<>();
        shardingTree.shards(bounds)
                .forEach(shard -> this.shardsOverlappingPolygon.add(shard.getName()));
    }

    @Override
    public boolean test(final Resource resource)
    {
        boolean result = false;
        final String resourceName = resource.getName();
        if (resourceName != null)
        {
            final Matcher matcher = this.shardFilePattern.matcher(resourceName);
            if (matcher.find())
            {
                final String shardName = matcher.group(1);
                if (this.shardsOverlappingPolygon.contains(shardName))
                {
                    logger.debug("Resource {} overlaps polygon.", resourceName);
                    result = true;
                }
                else
                {
                    logger.debug("Resource {} does not overlap polygon.", resourceName);
                }
            }
            else
            {
                logger.debug("Resource {} does not match shard filename pattern.", resourceName);
            }
        }
        else
        {
            logger.debug("Resource {} name is null.", resource.toString());
        }

        return result;
    }
}
