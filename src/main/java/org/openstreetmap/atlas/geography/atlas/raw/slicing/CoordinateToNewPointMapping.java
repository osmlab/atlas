package org.openstreetmap.atlas.geography.atlas.raw.slicing;

import java.util.HashMap;
import java.util.Map;

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
 * should be a single Location. To prevent this from happening, we're storing all Coordinates with 6
 * digits of precision. This class takes care of doing any conversions and lookup for the caller.
 *
 * @author mgostintsev
 */
public class CoordinateToNewPointMapping
{
    // See JTS Coordinate Javadoc for scale usage
    private static final Integer SIX_DIGIT_PRECISION_SCALE = 1_000_000;

    private final Map<Coordinate, Long> coordinateToPointIdentifierMap;
    private final PrecisionModel precisionModel;

    public CoordinateToNewPointMapping()
    {
        this.coordinateToPointIdentifierMap = new HashMap<>();
        this.precisionModel = new PrecisionModel(SIX_DIGIT_PRECISION_SCALE);
    }

    public boolean containsCoordinate(final Coordinate coordinate)
    {
        final Coordinate scaled = getScaledCoordinate(coordinate);
        return this.coordinateToPointIdentifierMap.containsKey(scaled);
    }

    public Long getPointForCoordinate(final Coordinate coordinate)
    {
        final Coordinate precise = getScaledCoordinate(coordinate);
        return this.coordinateToPointIdentifierMap.get(precise);
    }

    public void storeMapping(final Coordinate coordinate, final Long value)
    {
        final Coordinate scaled = getScaledCoordinate(coordinate);
        this.coordinateToPointIdentifierMap.put(scaled, value);
    }

    private Coordinate getScaledCoordinate(final Coordinate target)
    {
        // Clone to avoid updating actual coordinate
        final Coordinate cloned = (Coordinate) target.clone();
        this.precisionModel.makePrecise(cloned);
        return cloned;
    }
}
