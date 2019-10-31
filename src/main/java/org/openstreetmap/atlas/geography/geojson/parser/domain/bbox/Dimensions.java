package org.openstreetmap.atlas.geography.geojson.parser.domain.bbox;

import java.util.Arrays;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.ConstructorUtils;

/**
 * @author Yazad Khambata
 */
public enum Dimensions
{
    TWO_DIMENSIONAL(2, Bbox2D.class),

    THREE_DIMENSIONAL(3, Bbox3D.class);

    private int numberOfDimensions;

    private Class<? extends Bbox> bboxClass;

    public static Bbox toBbox(final Double... coordinates)
    {
        Validate.notEmpty(coordinates);
        final int length = coordinates.length;
        final int minCoordinates = 4;
        Validate.isTrue(length >= minCoordinates, "length: %s.", length);

        final Dimensions dimensions = Arrays.stream(Dimensions.values())
                .filter(theseDimensions -> theseDimensions.getNumberOfCoordinates() == length)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(Arrays.toString(coordinates)));

        try
        {
            return ConstructorUtils.invokeConstructor(dimensions.getBboxClass(), coordinates);
        }
        catch (final Exception e)
        {
            throw new IllegalStateException(e);
        }
    }

    Dimensions(final int numberOfDimensions, final Class<? extends Bbox> bboxClass)
    {
        this.numberOfDimensions = numberOfDimensions;
        this.bboxClass = bboxClass;
    }

    public Class<? extends Bbox> getBboxClass()
    {
        return this.bboxClass;
    }

    /**
     * Per https://tools.ietf.org/html/rfc7946#section-5
     *
     * <pre>
     *     The value of the bbox member MUST be an array of
     *    length 2*n where n is the number of dimensions represented in the
     *    contained geometries, with all axes of the most southwesterly point
     *    followed by all axes of the more northeasterly point.
     * </pre>
     *
     * @return the number of coordinates.
     */
    public int getNumberOfCoordinates()
    {
        final int multiply = 2;
        return this.getNumberOfDimensions() * multiply;
    }

    public int getNumberOfDimensions()
    {
        return this.numberOfDimensions;
    }

    public void validate(final Double... coordinates)
    {
        Validate.notEmpty(coordinates, "coordinates is EMPTY for %s.", this);
        final int actual = coordinates.length;
        final int expected = getNumberOfCoordinates();
        Validate.isTrue(actual == expected, "coordinates.length actual {}; expected: {}.", actual,
                expected);
    }
}
