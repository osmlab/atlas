package org.openstreetmap.atlas.geography.sharding;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.GeometricSurface;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author matthieun
 */
public class GeoHashSharding implements Sharding
{
    private static final long serialVersionUID = -7355746343440111174L;

    private final int precision;

    public GeoHashSharding(final int precision)
    {
        GeoHashTile.validatePrecision(precision);
        this.precision = precision;
    }

    public int getPrecision()
    {
        return this.precision;
    }

    @Override
    public Iterable<Shard> neighbors(final Shard shard)
    {
        if (shard instanceof GeoHashTile)
        {
            return ((GeoHashTile) shard).neighbors();
        }
        else
        {
            throw new CoreException("Shard parameter was of invalid type {}",
                    shard.getClass().getName());
        }
    }

    @Override
    public Iterable<Shard> shards(final GeometricSurface surface)
    {
        return Iterables.stream(GeoHashTile.allTiles(this.precision, surface))
                .map(tile -> (Shard) tile);
    }

    @Override
    public Iterable<Shard> shardsCovering(final Location location)
    {
        return Iterables.stream(Iterables.from(GeoHashTile.covering(location, this.precision)))
                .map(tile -> (Shard) tile);
    }

    @Override
    public Iterable<Shard> shardsIntersecting(final PolyLine polyLine)
    {
        return Iterables.stream(GeoHashTile.allTiles(this.precision, polyLine.bounds()))
                .filter(tile -> tile.bounds().intersects(polyLine)).map(tile -> (Shard) tile);
    }
}
