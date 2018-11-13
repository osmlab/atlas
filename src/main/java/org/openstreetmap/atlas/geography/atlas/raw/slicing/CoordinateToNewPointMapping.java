package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.openstreetmap.atlas.geography.atlas.items.Point;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * {@link CoordinateToNewPointMapping} is responsible for storing a mapping between a JTS
 * {@link Coordinate} and the Atlas identifier for all new {@link Point}s created during
 * country-slicing. Normally, we use 7-digit precision for tracking all Atlas entity locations.
 * However, JTS often has precision problems when slicing that causes us to create duplicate Points.
 * <p>
 * For example, we'll have a slice that has a Coordinate of (-8.3261734, 6.8663838) and another one
 * that is really close, but differs at the 7th digit of significance: (-8.3261735, 6.8663838) -
 * note the 4 and 5 for the latitude value. This will cause us to create two separate Points at what
 * should be a single Location. To prevent this from happening, we're maintaining maps for
 * TemporaryPoints that have been made made while slicing, allowing for consistent geometry per OSM
 * shape and limiting duplicate TemporaryPoints.
 *
 * @author mgostintsev
 * @author samuelgass
 */
public class CoordinateToNewPointMapping
{
    // See JTS Coordinate Javadoc for scale usage
    private static final Integer SIX_DIGIT_PRECISION_SCALE = 1_000_000;

    private final Map<Coordinate, Long> coordinateToPointIdentifierMap;
    private final Map<Long, Map<Coordinate, Long>> scaledPerIdentifierMap;
    private final PrecisionModel precisionModel;

    public CoordinateToNewPointMapping()
    {
        this.coordinateToPointIdentifierMap = new ConcurrentHashMap<>();
        this.scaledPerIdentifierMap = new ConcurrentHashMap<>();
        this.precisionModel = new PrecisionModel(SIX_DIGIT_PRECISION_SCALE);
    }

    /**
     * Takes a coordinate and an OSM identifier, and returns whether the coordinate has been mapped
     * before. The mapping check is somewhat unintuitive-- first, it checks to see if the coordinate
     * has a six digit approximation already used by this OSM shape. This enforces consistency
     * across OSM shape builds, ensuring they always use the same six-digit precision coordinate if
     * necessary. If this is false, then the higher 7-digit precision map is checked. This ensures
     * that if a TemporaryPoint with the same 7-digit precision has already been made by another OSM
     * shape, it will be used instead of a duplicate being made.
     *
     * @param coordinate
     *            The coordinate to check the cache for
     * @param osmIdentifier
     *            The OSM identifier whose shape is being sliced
     * @return True if the 6-digit scaled coordinate exists in the cache for this OSM shape, or else
     *         if the 7-digit original coordinate exists in the high precision general cache. False
     *         if neither of those caches contain relevant entries.
     */

    public boolean containsCoordinate(final Coordinate coordinate, final Long osmIdentifier)
    {
        //
        if (this.scaledPerIdentifierMap.containsKey(osmIdentifier))
        {
            final Coordinate scaled = getScaledCoordinate(coordinate);
            if (this.scaledPerIdentifierMap.get(osmIdentifier).containsKey(scaled))
            {
                return true;
            }
        }
        if (this.coordinateToPointIdentifierMap.containsKey(coordinate))
        {
            return true;
        }
        return false;
    }

    /**
     * Takes a coordinate of 7-digit precision and an OSM identifer for the shape being sliced.
     * First examines the cache mapping to this OSM idenitifier-- if the scaled 6-digit version of
     * this coordinate exists there, then returns the TemporaryPoint id associated with that.
     * Otherwise, searches the higher 7-digit precision general cache for the point. If this logic
     * is reached, then the 6-digit scaled OSM map didn't have an entry for this point, so we update
     * that mapping, then return the TemporaryPoint id associated for the point.
     *
     * @param coordinate
     *            The coordinate to get the mapped TemporaryPoint identifier for
     * @param osmIdentifier
     *            The OSM identifier for the shape being sliced
     * @return The TemporaryPoint identifier for the coordinate
     */
    public Long getPointForCoordinate(final Coordinate coordinate, final Long osmIdentifier)
    {
        final Coordinate scaled = getScaledCoordinate(coordinate);
        // if this geometry has already used a six-digit approximation, use it
        if (this.scaledPerIdentifierMap.containsKey(osmIdentifier))
        {
            if (this.scaledPerIdentifierMap.get(osmIdentifier).containsKey(scaled))
            {
                return this.scaledPerIdentifierMap.get(osmIdentifier).get(scaled);
            }
        }

        // else, there must be a seven digit precision equals value location already created -- use
        // that point instead of making a new one
        if (this.coordinateToPointIdentifierMap.containsKey(coordinate))
        {
            final Long cachedPoint = this.coordinateToPointIdentifierMap.get(coordinate);
            storeMapping(coordinate, osmIdentifier, cachedPoint);
            return cachedPoint;
        }
        return null;
    }

    /**
     * Takes a coordinate and returns a copy that is scaled to 6-digits of precision.
     *
     * @param target
     *            The coordinate to scale
     * @return The coordinate scaled to 6-digits
     */
    public Coordinate getScaledCoordinate(final Coordinate target)
    {
        // Clone to avoid updating actual coordinate
        final Coordinate cloned = (Coordinate) target.clone();
        this.precisionModel.makePrecise(cloned);
        return cloned;
    }

    /**
     * Takes a coordinate, an OSM identifier, and a TemporaryPoint id and puts them in the cache.
     * Since there are two mappings, first inserts the Point into the scaled per-OSM-shape cache,
     * then inserts into the general 7-digit precision cache.
     *
     * @param coordinate
     *            Coordinate used to create the point
     * @param osmIdentifier
     *            The OSM identifier for the shape being sliced
     * @param point
     *            The identifier for the TemporaryPoint made with the coordinate
     */
    public void storeMapping(final Coordinate coordinate, final Long osmIdentifier,
            final Long point)
    {
        final Coordinate scaled = getScaledCoordinate(coordinate);
        if (this.scaledPerIdentifierMap.containsKey(osmIdentifier))
        {
            this.scaledPerIdentifierMap.get(osmIdentifier).put(scaled, point);
            if (!this.coordinateToPointIdentifierMap.containsKey(coordinate))
            {
                this.coordinateToPointIdentifierMap.put(coordinate, point);
            }
        }
        else
        {
            final Map<Coordinate, Long> newCachedMap = new ConcurrentHashMap<>();
            newCachedMap.put(scaled, point);
            this.scaledPerIdentifierMap.put(osmIdentifier, newCachedMap);
            if (!this.coordinateToPointIdentifierMap.containsKey(coordinate))
            {
                this.coordinateToPointIdentifierMap.put(coordinate, point);
            }
        }
    }
}
