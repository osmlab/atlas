package org.openstreetmap.atlas.geography.converters.jts;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

/**
 * JTS Precision utility class.
 *
 * @author Yiqing Jin
 */
public final class JtsPrecisionManager
{
    private static final int PRECISION_SCALE = 100_000_000;
    private static PrecisionModel precisionModel;
    private static GeometryFactory geometryFactory;

    static
    {
        precisionModel = new PrecisionModel(PRECISION_SCALE);
        geometryFactory = new GeometryFactory();
    }

    public static GeometryFactory getGeometryFactory()
    {
        return geometryFactory;
    }

    public static PrecisionModel getPrecisionModel()
    {
        return precisionModel;
    }

    private JtsPrecisionManager()
    {
    }
}
