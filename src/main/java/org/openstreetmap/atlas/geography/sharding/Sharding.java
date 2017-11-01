package org.openstreetmap.atlas.geography.sharding;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;

/**
 * Sharding strategy
 *
 * @author matthieun
 */
public interface Sharding extends Serializable
{
    int SHARDING_STRING_SPLIT = 2;
    int SLIPPY_ZOOM_MAXIMUM = 18;

    /**
     * Parse a sharding definition string
     *
     * @param sharding
     *            The definition string
     * @return The corresponding {@link Sharding} instance.
     */
    static Sharding forString(final String sharding)
    {
        final StringList split;
        split = StringList.split(sharding, "@");
        if (split.size() != SHARDING_STRING_SPLIT)
        {
            throw new CoreException("Invalid sharding string: {}", sharding);
        }
        if ("slippy".equals(split.get(0)))
        {
            final int zoom;
            zoom = Integer.valueOf(split.get(1));
            if (zoom > SLIPPY_ZOOM_MAXIMUM)
            {
                throw new CoreException("Slippy Sharding zoom too high : {}, max is {}", zoom,
                        SLIPPY_ZOOM_MAXIMUM);
            }
            return new SlippyTileSharding(zoom);
        }
        if ("dynamic".equals(split.get(0)))
        {
            final String definition = split.get(1);
            return new DynamicTileSharding(new File(definition));
        }
        throw new CoreException("Sharding type {} is not recognized.", split.get(0));
    }

    /**
     * Get the neighboring shards for a given shard.
     *
     * @param shard
     *            The shard for which to get neighbors
     * @return The shards {@link Iterable}, neighboring the supplied shard
     */
    Iterable<? extends Shard> neighbors(Shard shard);

    /**
     * Generate shards for the whole planet. This needs to be deterministic!
     *
     * @return The shards {@link Iterable}, covering the whole planet.
     */
    default Iterable<? extends Shard> shards()
    {
        return shards(Rectangle.MAXIMUM);
    }

    /**
     * Generate shards. This needs to be deterministic!
     *
     * @param multiPolygon
     *            The bounds to limit the shards.
     * @return The shards {@link Iterable}.
     */
    default Iterable<? extends Shard> shards(final MultiPolygon multiPolygon)
    {
        final Set<Shard> result = new HashSet<>();
        for (final Polygon polygon : multiPolygon.outers())
        {
            final List<Polygon> inners = multiPolygon.innersOf(polygon);
            Iterables.stream(this.shards(polygon)).filter(shard ->
            {
                for (final Polygon inner : inners)
                {
                    if (inner.fullyGeometricallyEncloses(shard.bounds()))
                    {
                        return false;
                    }
                }
                return true;
            }).forEach(result::add);
        }
        return result;
    }

    /**
     * Generate shards. This needs to be deterministic!
     *
     * @param polygon
     *            The bounds to limit the shards.
     * @return The shards {@link Iterable}.
     */
    Iterable<? extends Shard> shards(Polygon polygon);

    /**
     * Generate shards. This needs to be deterministic!
     *
     * @param location
     *            The location to find
     * @return The shards {@link Iterable} (In case the location falls right at the boundary between
     *         shards)
     */
    Iterable<? extends Shard> shardsCovering(Location location);

    /**
     * Generate shards. This needs to be deterministic!
     *
     * @param polyLine
     *            The line intersecting the shards
     * @return The shards {@link Iterable}.
     */
    Iterable<? extends Shard> shardsIntersecting(PolyLine polyLine);
}
