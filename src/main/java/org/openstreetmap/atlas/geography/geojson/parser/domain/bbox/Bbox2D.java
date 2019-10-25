package org.openstreetmap.atlas.geography.geojson.parser.domain.bbox;

import java.util.Arrays;
import java.util.List;

/**
 * @author Yazad Khambata
 */
public class Bbox2D extends AbstractBbox
{
    private final Double coordinate1;
    private final Double coordinate2;
    private final Double coordinate3;
    private final Double coordinate4;

    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int THREE = 3;

    public Bbox2D(final Double... coordinates)
    {
        this(Dimensions.TWO_DIMENSIONAL, coordinates);
    }

    Bbox2D(final Dimensions dimensions, final Double... coordinates)
    {
        super(dimensions);

        dimensions.validate(coordinates);
        this.coordinate1 = coordinates[ZERO];
        this.coordinate2 = coordinates[ONE];
        this.coordinate3 = coordinates[TWO];
        this.coordinate4 = coordinates[THREE];
    }

    public Double getCoordinate1()
    {
        return this.coordinate1;
    }

    public Double getCoordinate2()
    {
        return this.coordinate2;
    }

    public Double getCoordinate3()
    {
        return this.coordinate3;
    }

    public Double getCoordinate4()
    {
        return this.coordinate4;
    }

    @Override
    public List<Double> toList()
    {
        return Arrays.asList(this.coordinate1, this.coordinate2, this.coordinate3,
                this.coordinate4);
    }
}
