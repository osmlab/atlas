package org.openstreetmap.atlas.geography.geojson.parser.domain.bbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Yazad Khambata
 */
public class Bbox3D extends Bbox2D {
    private Double coordinate5;
    private Double coordinate6;

    public Bbox3D(Double... coordinates) {
        super(Dimensions.THREE_DIMENSIONAL, coordinates);

        this.coordinate5 = coordinates[4];
        this.coordinate6 = coordinates[5];
    }

    public Double getCoordinate5() {
        return coordinate5;
    }

    public Double getCoordinate6() {
        return coordinate6;
    }

    @Override
    public List<Double> toList() {
        final List<Double> list = new ArrayList<>(super.toList());
        list.addAll(Arrays.asList(coordinate5, coordinate6));

        return list;
    }
}
