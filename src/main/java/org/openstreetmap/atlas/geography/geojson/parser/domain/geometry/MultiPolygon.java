package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import java.util.List;
import java.util.Map;

import org.openstreetmap.atlas.geography.geojson.parser.domain.bbox.Bbox;
import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.ForeignFields;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Coordinates;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Position;

/**
 * @author Yazad Khambata
 */
public class MultiPolygon extends AbstractGeometryWithCoordinateSupport<List<List<Position>>>
{
    private MultiLineString value;

    public MultiPolygon(final Map<String, Object> map)
    {
        super(map, null);
        this.value = new MultiLineString(map);
    }

    @Override
    public Bbox getBbox()
    {
        return this.value.getBbox();
    }

    @Override
    public Coordinates<List<List<Position>>> getCoordinates()
    {
        return this.value.getCoordinates();
    }

    @Override
    public ForeignFields getForeignFields()
    {
        return this.value.getForeignFields();
    }
}
