package org.openstreetmap.atlas.geography.geojson.parser.domain.bbox;

import java.util.Arrays;
import java.util.List;

/**
 * @author Yazad Khambata
 */
public class Bbox2D extends AbstractBbox {
    private Double coordinate1;
    private Double coordinate2;
    private Double coordinate3;
    private Double coordinate4;

    public Bbox2D(Double... coordinates) {
        this(Dimensions.TWO_DIMENSIONAL, coordinates);
    }

    Bbox2D(Dimensions dimensions, Double... coordinates) {
        super(dimensions);

        dimensions.validate(coordinates);
        this.coordinate1 = coordinates[0];
        this.coordinate2 = coordinates[1];
        this.coordinate3 = coordinates[2];
        this.coordinate4 = coordinates[3];
    }

    public Double getCoordinate1() {
        return coordinate1;
    }

    public Double getCoordinate2() {
        return coordinate2;
    }

    public Double getCoordinate3() {
        return coordinate3;
    }

    public Double getCoordinate4() {
        return coordinate4;
    }

    @Override
    public List<Double> toList() {
        return Arrays.asList(coordinate1, coordinate2, coordinate3, coordinate4);
    }
}
