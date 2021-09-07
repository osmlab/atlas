package org.openstreetmap.atlas.geography.converters.jts;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryCollection;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.openstreetmap.atlas.geography.atlas.change.ChangeRelation;
import org.openstreetmap.atlas.utilities.conversion.TwoWayConverter;

/**
 * this is a somewhat specific use case converter used by {@link ChangeRelation}
 *
 * @author samuelgass
 */
public class JtsMultiPolygonToMultiLineStringConverter
        implements TwoWayConverter<MultiPolygon, GeometryCollection>
{

    @Override
    public MultiPolygon backwardConvert(final GeometryCollection object)
    {
        final Polygon[] polygons = new Polygon[object.getNumGeometries()];
        for (int i = 0; i < polygons.length; i++)
        {
            polygons[i] = (Polygon) object.getGeometryN(i);
        }
        return new MultiPolygon(polygons, JtsPrecisionManager.getGeometryFactory());
    }

    @Override
    public GeometryCollection convert(final MultiPolygon object)
    {
        final List<LineString> linestrings = new ArrayList<>();
        for (int i = 0; i < object.getNumGeometries(); i++)
        {
            final Polygon part = (Polygon) object.getGeometryN(i);
            linestrings.add(part.getExteriorRing());
            for (int j = 0; j < part.getNumInteriorRing(); j++)
            {
                linestrings.add(part.getInteriorRingN(j));
            }
        }
        Geometry[] geometries = new Geometry[linestrings.size()];
        geometries = linestrings.toArray(geometries);
        return new GeometryCollection(geometries, JtsPrecisionManager.getGeometryFactory());
    }
}
