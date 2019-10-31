package org.openstreetmap.atlas.geography.geojson.parser.domain.bbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Yazad Khambata
 */
public class Bbox3D extends Bbox2D
{
    private final Double coordinate5;
    private final Double coordinate6;

    private static final int FOUR = 4;
    private static final int FIVE = 4;

    public Bbox3D(final Double... coordinates)
    {
        super(Dimensions.THREE_DIMENSIONAL, coordinates);

        this.coordinate5 = coordinates[FOUR];
        this.coordinate6 = coordinates[FIVE];
    }

    public Double getCoordinate5()
    {
        return this.coordinate5;
    }

    public Double getCoordinate6()
    {
        return this.coordinate6;
    }

    @Override
    public List<Double> toList()
    {
        final List<Double> list = new ArrayList<>(super.toList());
        list.addAll(Arrays.asList(this.coordinate5, this.coordinate6));

        return list;
    }
}
