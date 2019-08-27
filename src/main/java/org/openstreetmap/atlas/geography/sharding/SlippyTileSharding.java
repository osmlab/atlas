package org.openstreetmap.atlas.geography.sharding;

import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * Atlas Sharding with {@link SlippyTile}s.
 *
 * @author matthieun
 */
public class SlippyTileSharding implements Sharding
{
    private static final long serialVersionUID = -6727830583309410676L;

    private final int zoom;

    public SlippyTileSharding(final int zoom)
    {
        this.zoom = zoom;
    }

    @Override
    public Iterable<Shard> neighbors(final Shard shard)
    {
        if (shard instanceof SlippyTile)
        {
            return ((SlippyTile) shard).neighbors().stream().map(neighbor -> (Shard) neighbor)
                    .collect(Collectors.toSet());
        }
        else
        {
            throw new CoreException("Cannot have neighbors from another type of shard.");
        }
    }

    @Override
    public Iterable<Shard> shards(final GeometricSurface surface)
    {
        return Iterables.stream(SlippyTile.allTiles(this.zoom, surface.bounds()))
                .filter(slippyTile -> surface.overlaps(slippyTile.bounds()))
                .map(shard -> (Shard) shard);
    }

    @Override
    public Iterable<Shard> shardsCovering(final Location location)
    {
        return Iterables.stream(SlippyTile.allTiles(this.zoom, location.bounds()))
                .filter(slippyTile -> slippyTile.bounds().fullyGeometricallyEncloses(location))
                .map(shard -> (Shard) shard);
    }

    @Override
    public Iterable<Shard> shardsIntersecting(final PolyLine polyLine)
    {
        return Iterables.stream(SlippyTile.allTiles(this.zoom, polyLine.bounds()))
                .filter(slippyTile -> polyLine.intersects(slippyTile.bounds())
                        || slippyTile.bounds().fullyGeometricallyEncloses(polyLine))
                .map(shard -> (Shard) shard);
    }

    @Override
    public String toString()
    {
        return "slippy:" + this.zoom;
    }
}
