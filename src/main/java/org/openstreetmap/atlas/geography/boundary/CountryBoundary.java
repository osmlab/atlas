package org.openstreetmap.atlas.geography.boundary;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.Located;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * This {@link CountryBoundary} holds country name and country boundary, and will be stored in
 * spatial index directly for best query performance
 *
 * @author tony
 */
public class CountryBoundary implements Located, Serializable
{
    private static final long serialVersionUID = 4728303272397434187L;

    private final String countryName;
    private final MultiPolygon boundary;

    public CountryBoundary(final String name, final MultiPolygon boundary)
    {
        this.countryName = name;
        this.boundary = boundary;
    }

    @Override
    public Rectangle bounds()
    {
        return this.boundary.bounds();
    }

    public boolean covers(final Rectangle bound)
    {
        boolean covers = false;
        for (final Polygon outer : this.boundary.outers())
        {
            if (outer.fullyGeometricallyEncloses(bound))
            {
                covers = true;
                break;
            }
            if (outer.overlaps(bound))
            {
                covers = true;
                break;
            }
        }
        return covers;
    }

    public MultiPolygon getBoundary()
    {
        return this.boundary;
    }

    public String getCountryName()
    {
        return this.countryName;
    }

    /**
     * Iterate through outers of country boundary to avoid unnecessary overlap checks
     *
     * @param zoom
     *            The zoom level of slippy tiles
     * @return A set of slippy tiles
     */
    public Set<SlippyTile> tiles(final int zoom)
    {
        final Set<SlippyTile> validTiles = new HashSet<>();
        for (final Polygon subBoundary : this.boundary.outers())
        {
            final List<SlippyTile> tiles = Iterables
                    .asList(SlippyTile.allTiles(zoom, subBoundary.bounds()));
            validTiles.addAll(tiles.stream().filter(tile -> subBoundary.overlaps(tile.bounds()))
                    .collect(Collectors.toList()));
        }
        return validTiles;
    }
}
