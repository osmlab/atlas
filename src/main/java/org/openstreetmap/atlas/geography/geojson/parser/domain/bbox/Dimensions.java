package org.openstreetmap.atlas.geography.geojson.parser.domain.bbox;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.ConstructorUtils;

import java.util.Arrays;

/**
 * @author Yazad Khambata
 */
public enum Dimensions {
    TWO_DIMENSIONAL(2, Bbox2D.class),

    THREE_DIMENSIONAL(3, Bbox3D.class);

    private int numberOfDimensions;

    private Class<? extends Bbox> bboxClass;

    Dimensions(final int numberOfDimensions, final Class<? extends Bbox> bboxClass) {
        this.numberOfDimensions = numberOfDimensions;
        this.bboxClass = bboxClass;
    }

    public int getNumberOfDimensions() {
        return numberOfDimensions;
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
     * @return
     */
    public int getNumberOfCoordinates() {
        return this.getNumberOfDimensions() * 2;
    }

    public Class<? extends Bbox> getBboxClass() {
        return bboxClass;
    }

    public void validate(Double...coordinates) {
        Validate.notEmpty(coordinates, "coordinates is EMPTY for %s.", this);
        final int actual = coordinates.length;
        int expected = getNumberOfCoordinates();
        Validate.isTrue(actual == expected, "coordinates.length actual {}; expected: {}.", actual, expected);
    }

    public static Bbox toBbox(Double...coordinates) {
        Validate.notEmpty(coordinates);
        final int length = coordinates.length;
        Validate.isTrue(length >= 4, "length: %s.", length);

        final Dimensions dimensions = Arrays.stream(Dimensions.values())
                .filter(theseDimensions -> theseDimensions.getNumberOfCoordinates() == coordinates.length)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(Arrays.toString(coordinates)));

        try {
            return ConstructorUtils.invokeConstructor(dimensions.getBboxClass(), coordinates); //TODO: confirm if works.
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
