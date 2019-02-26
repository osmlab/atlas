package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.PrecisionModel;
import org.openstreetmap.atlas.geography.atlas.items.Point;

/**
 * {@link CoordinateToNewPointMapping} is responsible for storing a mapping between a JTS
 * {@link Coordinate} and the Atlas identifier for all new {@link Point}s created during
 * country-slicing. Normally, we use 7-digit precision for tracking all Atlas entity locations.
 * However, JTS often has precision problems when slicing that causes us to create duplicate Points.
 * <p>
 * For example, we'll have a slice that has a Coordinate of (-8.3261734, 6.8663838) and another one
 * that is really close, but differs at the 7th digit of significance: (-8.3261735, 6.8663838) -
 * note the 4 and 5 for the latitude value. This will cause us to create two separate Points at what
 * should be a single Location. To prevent this from happening, we're storing all points at 6-digit
 * precision
 *
 * @author mgostintsev
 * @author samuelgass
 */
public class CoordinateToNewPointMapping
{
    // See JTS Coordinate Javadoc for scale usage
    private static final Integer SIX_DIGIT_PRECISION_SCALE = 1_000_000;

    private final Map<Coordinate, Long> coordinateToPointIdentifierMap;
    private final PrecisionModel precisionModel;

    public CoordinateToNewPointMapping()
    {
        this.coordinateToPointIdentifierMap = new ConcurrentHashMap<>();
        this.precisionModel = new PrecisionModel(SIX_DIGIT_PRECISION_SCALE);
    }

    /**
     * Checks if this coordinate exists in the map or not
     *
     * @param coordinate
     *            The coordinate to check the cache for
     * @return True if the coordinate exists, false if not
     */

    public boolean containsCoordinate(final Coordinate coordinate)
    {
        return this.coordinateToPointIdentifierMap.containsKey(coordinate);
    }

    /**
     * Gets the TemporaryPoint identifier for this coordinate
     *
     * @param coordinate
     *            The coordinate to get the mapped TemporaryPoint identifier for
     * @return The TemporaryPoint identifier for the coordinate
     */
    public Long getPointForCoordinate(final Coordinate coordinate)
    {
        return this.coordinateToPointIdentifierMap.get(coordinate);
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
     * Takes a coordinate, and a TemporaryPoint id and puts them in the cache.
     *
     * @param coordinate
     *            Coordinate used to create the point
     * @param point
     *            The identifier for the TemporaryPoint made with the coordinate
     */
    public void storeMapping(final Coordinate coordinate, final Long point)
    {
        this.coordinateToPointIdentifierMap.put(coordinate, point);
    }
}
