package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * The coordinates of the geometry.
 *
 * @param <V>
 *            - value of the coordinates.
 * @author Yazad Khambata
 */
public final class Coordinates<V>
{

    private V value;

    private Coordinates(final V value)
    {
        this.value = value;
    }

    private static Position toPosition(final List<Double> data)
    {
        Validate.notEmpty(data, "coordinates is EMPTY.");
        Validate.isTrue(data.size() == 2, "point coordinates is NOT 2: %s.", data);

        return new Position(data.get(0), data.get(1));
    }

    private static List<Position> toPositionList(final List<List<Double>> data)
    {
        Validate.notEmpty(data, "list of coordinates is EMPTY.");
        Validate.isTrue(data.size() >= 1, "multi point coordinates must be at least 1: %s.", data);

        return data.stream().map(coords -> toPosition(coords)).collect(Collectors.toList());
    }

    private static List<List<Position>> toListOfPositionList(final List<List<List<Double>>> data)
    {
        Validate.notEmpty(data, "list containing the lists of coordinates is EMPTY.");
        Validate.isTrue(data.size() >= 1, "multi point coordinates must be at least 1: %s.", data);

        return data.stream().map(listOfCoords -> toPositionList(listOfCoords))
                .collect(Collectors.toList());
    }

    public static Coordinates<Position> forPoint(final List<Double> data)
    {
        return new Coordinates<>(toPosition(data));
    }

    public static Coordinates<List<Position>> forMultiPoint(final List<List<Double>> data)
    {
        return new Coordinates<>(toPositionList(data));
    }

    public static Coordinates<List<Position>> forLineString(final List<List<Double>> data)
    {
        return forMultiPoint(data);
    }

    public static Coordinates<List<List<Position>>> forMultiLineString(
            final List<List<List<Double>>> data)
    {
        return new Coordinates<>(toListOfPositionList(data));
    }

    public static Coordinates<List<Position>> forPolygon(final List<List<Double>> data)
    {
        return forLineString(data);
    }

    public static Coordinates<List<List<Position>>> forMultiPolygon(
            final List<List<List<Double>>> data)
    {
        return forMultiLineString(data);
    }

    public V getValue()
    {
        return this.value;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public boolean equals(final Object that)
    {
        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode()
    {
        return HashCodeBuilder.reflectionHashCode(this);
    }
}
