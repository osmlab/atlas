package org.openstreetmap.atlas.geography.sharding;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.geojson.GeoJsonType;
import org.openstreetmap.atlas.geography.sharding.converters.SlippyTileConverter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.scalars.Distance;

import com.google.gson.JsonObject;

/**
 * OSM Slippy tile
 *
 * @see "http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames"
 * @author matthieun
 * @author mgostintsev
 */
public class SlippyTile implements Shard, Comparable<SlippyTile>
{
    public static final SlippyTile ROOT = new SlippyTile(0, 0, 0);
    public static final int MAX_ZOOM = 30;
    public static final String COORDINATE_SEPARATOR = "-";

    private static final long serialVersionUID = -3752920878013084039L;
    private static final SlippyTileConverter CONVERTER = new SlippyTileConverter();
    private static final double CIRCULAR_MULTIPLIER = 2.0;
    private static final double ZOOM_LEVEL_POWER = 2.0;
    private static final int BIT_SHIFT = 2;
    private static final double FULL_ROTATION_DEGREES = 360.0;
    private static final double HALF_ROTATION_DEGREES = 180.0;
    private static final double LONGITUDE_BOUNDARY = 180;
    private static final double NEIGHBOR_EXPANSION_SCALE = 0.10;

    private Rectangle bounds;
    private final int xAxis;
    private final int yAxis;
    private final int zoom;

    /**
     * All tiles at some zoom level
     *
     * @param zoom
     *            The zoom to consider
     * @return All tiles at some zoom level
     */
    public static Iterable<SlippyTile> allTiles(final int zoom)
    {
        return allTiles(zoom, Rectangle.MAXIMUM);
    }

    /**
     * All tiles within some bounds
     *
     * @param zoom
     *            The zoom to consider
     * @param bounds
     *            The bounds to consider
     * @return All tiles within some bounds
     */
    public static Iterable<SlippyTile> allTiles(final int zoom, final Rectangle bounds)
    {
        if (zoom > MAX_ZOOM)
        {
            throw new CoreException("Zoom too large.");
        }
        final Iterable<SlippyTile> result = () -> allTilesIterator(zoom, bounds);
        final List<SlippyTile> list = Iterables.asList(result);
        if (list.isEmpty())
        {
            throw new CoreException("List cannot be empty");
        }
        return list;
    }

    /**
     * Iterator for all tiles within some bounds
     *
     * @param zoom
     *            The zoom to consider
     * @param bounds
     *            The bounds to consider
     * @return Iterator for all tiles within some bounds
     */
    public static Iterator<SlippyTile> allTilesIterator(final int zoom, final Rectangle bounds)
    {
        if (zoom > MAX_ZOOM)
        {
            throw new CoreException("Zoom too large.");
        }
        final SlippyTile lowerLeft = new SlippyTile(bounds.lowerLeft(), zoom);
        final SlippyTile upperRight = new SlippyTile(bounds.upperRight(), zoom);
        final int minX = lowerLeft.getX();
        final int maxX = upperRight.getX();
        final int minY = upperRight.getY();
        final int maxY = lowerLeft.getY();
        return new Iterator<SlippyTile>()
        {
            private int xAxis = minX;
            private int yAxis = minY;

            @Override
            public boolean hasNext()
            {
                return this.yAxis <= maxY && this.xAxis <= maxX;
            }

            @Override
            public SlippyTile next()
            {
                if (!hasNext())
                {
                    throw new NoSuchElementException();
                }
                final SlippyTile result = new SlippyTile(this.xAxis, this.yAxis, zoom);
                this.xAxis++;
                if (this.xAxis > maxX)
                {
                    this.yAxis++;
                    this.xAxis = minX;
                }
                return result;
            }
        };
    }

    /**
     * Expansion distance will be the smaller of the height/width scaled by 1/4.
     *
     * @param bounds
     *            The bounds
     * @return The smaller of the height/width scaled by 1/4
     */
    public static Distance calculateExpansionDistance(final Rectangle bounds)
    {
        final Distance shorterSide = bounds.width().onEarth().isLessThanOrEqualTo(
                bounds.height().onEarth()) ? bounds.width().onEarth() : bounds.height().onEarth();
        return shorterSide.scaleBy(NEIGHBOR_EXPANSION_SCALE);
    }

    public static SlippyTile forName(final String name)
    {
        return CONVERTER.backwardConvert(name);
    }

    /**
     * Construct
     *
     * @param xAxis
     *            The x index (along north-south)
     * @param yAxis
     *            The y index (along west-east)
     * @param zoom
     *            The zoom level
     */
    public SlippyTile(final int xAxis, final int yAxis, final int zoom)
    {
        if (zoom > MAX_ZOOM)
        {
            throw new CoreException("Zoom {} is too large.", zoom);
        }
        this.zoom = zoom;
        this.xAxis = xAxis;
        this.yAxis = yAxis;
    }

    /**
     * Construct
     *
     * @param location
     *            The location to get the overlapping {@link SlippyTile}
     * @param zoom
     *            The zoom level
     */
    public SlippyTile(final Location location, final int zoom)
    {
        if (zoom > MAX_ZOOM)
        {
            throw new CoreException("Zoom {} is too large.", zoom);
        }
        final List<Integer> tileNumbers = this.getTileNumbers(location, zoom);
        this.zoom = zoom;
        this.xAxis = tileNumbers.get(0);
        this.yAxis = tileNumbers.get(1);
    }

    @Override
    public JsonObject asGeoJson()
    {
        return bounds().asGeoJsonGeometry();
    }

    @Override
    public Rectangle bounds()
    {
        if (this.bounds == null)
        {
            this.bounds = tile2boundingBox(this.xAxis, this.yAxis, this.zoom);
        }
        return this.bounds;
    }

    @Override
    public int compareTo(final SlippyTile other)
    {
        // Order by z-level, x-value and then y-value
        final int zoomLevelDelta = this.getZoom() - other.getZoom();
        if (zoomLevelDelta > 0)
        {
            return 1;
        }
        else if (zoomLevelDelta < 0)
        {
            return -1;
        }
        else
        {
            final int xDelta = this.getX() - other.getX();
            if (xDelta > 0)
            {
                return 1;
            }
            else if (xDelta < 0)
            {
                return -1;
            }
            else
            {
                final int yDelta = this.getY() - other.getY();
                if (yDelta > 0)
                {
                    return 1;
                }
                else if (yDelta < 0)
                {
                    return -1;
                }
                else
                {
                    return 0;
                }
            }
        }
    }

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof SlippyTile)
        {
            final SlippyTile that = (SlippyTile) other;
            return this.getZoom() == that.getZoom() && this.getX() == that.getX()
                    && this.getY() == that.getY();
        }
        return false;
    }

    @Override
    public GeoJsonType getGeoJsonType()
    {
        return GeoJsonType.POLYGON;
    }

    @Override
    public String getName()
    {
        return CONVERTER.convert(this);
    }

    /**
     * @return The tile's X index.
     */
    public int getX()
    {
        return this.xAxis;
    }

    /**
     * @return The tile's Y index.
     */
    public int getY()
    {
        return this.yAxis;
    }

    /**
     * @return The tile's zoom.
     */
    public int getZoom()
    {
        return this.zoom;
    }

    @Override
    public int hashCode()
    {
        final long result = (long) Math.pow(getMaxXorY(this.zoom - 1), 2)
                + (long) Math.pow(this.xAxis, 2) + this.yAxis;
        return (int) result % Integer.MAX_VALUE;
    }

    /**
     * @return All neighbors for the current tile, including diagonal tiles that may share a single
     *         vertex on the corner they meet.
     */
    public Set<SlippyTile> neighbors()
    {
        return Iterables
                .stream(new SlippyTileSharding(this.zoom)
                        .shards(bounds().expand(calculateExpansionDistance(bounds()))))
                .map(shard -> (SlippyTile) shard).filter(tile -> !this.equals(tile)).collectToSet();
    }

    /**
     * @return The {@link SlippyTile} that has one less zoom level, and that covers that
     *         {@link SlippyTile}. If the zoom is already 0, return itself.
     */
    public SlippyTile parent()
    {
        if (this.zoom == 0)
        {
            return this;
        }
        final int parentZoom = this.zoom - 1;
        final int parentX = this.xAxis / 2;
        final int parentY = this.yAxis / 2;
        return new SlippyTile(parentX, parentY, parentZoom);
    }

    /**
     * @return The 4 {@link SlippyTile} that represent the current {@link SlippyTile} at one more
     *         zoom level.
     */
    public List<SlippyTile> split()
    {
        if (this.zoom == MAX_ZOOM)
        {
            throw new CoreException("Cannot split further than zoom {}", MAX_ZOOM);
        }
        final List<SlippyTile> result = new ArrayList<>();
        for (int i = 2 * this.xAxis; i <= 2 * this.xAxis + 1; i++)
        {
            for (int j = 2 * this.yAxis; j <= 2 * this.yAxis + 1; j++)
            {
                result.add(new SlippyTile(i, j, this.zoom + 1));
            }
        }
        return result;
    }

    /**
     * Split a {@link SlippyTile} up to the given zoom level. If the zoom is smaller than the
     * current tile's, then the method will return the only parent tile at the specified zoom in the
     * result list.
     *
     * @param newZoom
     *            The zoom to split to
     * @return the split tiles
     */
    public List<SlippyTile> split(final int newZoom)
    {
        if (newZoom < 0 || newZoom > MAX_ZOOM)
        {
            throw new CoreException(
                    "Cannot split to a zoom {} which is not between 0 and {} included.", newZoom,
                    MAX_ZOOM);
        }
        List<SlippyTile> result = new ArrayList<>();
        if (newZoom == this.zoom)
        {
            result.add(this);
        }
        if (newZoom > this.zoom)
        {
            List<SlippyTile> temporary = new ArrayList<>();
            temporary.add(this);
            int temporaryZoom = this.zoom + 1;
            while (temporaryZoom <= newZoom)
            {
                final List<SlippyTile> newTemporary = new ArrayList<>();
                for (final SlippyTile temporaryTile : temporary)
                {
                    newTemporary.addAll(temporaryTile.split());
                }
                temporary = newTemporary;
                temporaryZoom++;
            }
            result = temporary;
        }
        if (newZoom < this.zoom)
        {
            SlippyTile temporary = this;
            int temporaryZoom = temporary.getZoom() - 1;
            while (temporaryZoom >= newZoom)
            {
                temporary = temporary.parent();
                temporaryZoom--;
            }
            result.add(temporary);
        }
        return result;
    }

    @Override
    public String toString()
    {
        return "[SlippyTile: zoom = " + this.zoom + ", x = " + this.xAxis + ", y = " + this.yAxis
                + "]";
    }

    @Override
    public byte[] toWkb()
    {
        return bounds().toWkb();
    }

    @Override
    public String toWkt()
    {
        return bounds().toWkt();
    }

    /**
     * Add the siblings and parent.
     *
     * @param candidates
     *            The candidate tiles
     * @param visitedTiles
     *            The tiles already visited
     * @param targetTile
     *            The target tile
     */
    protected void getNeighborsForAllZoomLevels(final Queue<SlippyTile> candidates,
            final Set<String> visitedTiles, final SlippyTile targetTile)
    {
        final SlippyTile parent = targetTile.parent();
        for (final SlippyTile child : parent.split())
        {
            if (!visitedTiles.contains(child.getName()) && !child.equals(targetTile))
            {
                candidates.add(child);
            }
        }
        candidates.add(parent);
    }

    /**
     * @param zoom
     *            The provided zoom level
     * @return The maximum slippy tile index (x or y) for that zoom level
     */
    private int getMaxXorY(final int zoom)
    {
        if (zoom <= 0)
        {
            return 1;
        }
        final int result = getTileNumbers(new Location(Latitude.MINIMUM, Longitude.ZERO), zoom)
                .get(0);
        return result;
    }

    /**
     * @param location
     *            The location to consider
     * @param zoom
     *            The zoom level
     * @return The list made of the corresponding x and y indices.
     */
    private List<Integer> getTileNumbers(final Location location, final int zoom)
    {
        final double latitude = location.getLatitude().asDegrees();
        final double longitude = location.getLongitude().asDegrees();
        int xAxis = (int) Math
                .floor((longitude + HALF_ROTATION_DEGREES) / FULL_ROTATION_DEGREES * (1 << zoom));
        int yAxis = (int) Math.floor((1 - Math
                .log(Math.tan(Math.toRadians(latitude)) + 1 / Math.cos(Math.toRadians(latitude)))
                / Math.PI) / BIT_SHIFT * (1 << zoom));
        if (xAxis < 0)
        {
            xAxis = 0;
        }
        if (xAxis >= 1 << zoom)
        {
            xAxis = (1 << zoom) - 1;
        }
        if (yAxis < 0)
        {
            yAxis = 0;
        }
        if (yAxis >= 1 << zoom)
        {
            yAxis = (1 << zoom) - 1;
        }
        final List<Integer> result = new ArrayList<>();
        result.add(xAxis);
        result.add(yAxis);
        return result;
    }

    /**
     * @param xAxis
     *            The x index
     * @param yAxis
     *            The y index
     * @param zoom
     *            The zoom level
     * @return The corresponding bounding box {@link Rectangle}
     */
    private Rectangle tile2boundingBox(final int xAxis, final int yAxis, final int zoom)
    {
        final Latitude minLat = tile2lat(yAxis + 1, zoom);
        final Latitude maxLat = tile2lat(yAxis, zoom);
        final Longitude minLon = tile2lon(xAxis, zoom);
        final Longitude maxLon = tile2lon(xAxis + 1, zoom);
        return Rectangle.forCorners(new Location(minLat, minLon), new Location(maxLat, maxLon));
    }

    /**
     * @param yAxis
     *            The tile's y index
     * @param zoom
     *            The zoom level
     * @return The corresponding latitude
     */
    private Latitude tile2lat(final int yAxis, final int zoom)
    {
        final double pivot = Math.PI
                - CIRCULAR_MULTIPLIER * Math.PI * yAxis / Math.pow(ZOOM_LEVEL_POWER, zoom);
        return Latitude.degrees(Math.toDegrees(Math.atan(Math.sinh(pivot))));
    }

    /**
     * @param xAxis
     *            The tile's x index
     * @param zoom
     *            The zoom level
     * @return The corresponding longitude
     */
    private Longitude tile2lon(final int xAxis, final int zoom)
    {
        final double longitude = xAxis / Math.pow(ZOOM_LEVEL_POWER, zoom) * FULL_ROTATION_DEGREES
                - HALF_ROTATION_DEGREES;
        return longitude >= LONGITUDE_BOUNDARY ? Longitude.MAXIMUM
                : longitude < -LONGITUDE_BOUNDARY ? Longitude.MINIMUM
                        : Longitude.degrees(longitude);
    }
}
