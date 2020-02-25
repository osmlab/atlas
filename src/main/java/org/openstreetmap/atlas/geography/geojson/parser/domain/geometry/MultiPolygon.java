package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.geojson.parser.domain.bbox.Bbox;
import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.ForeignFields;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Coordinates;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Position;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Positions;

/**
 * @author Yazad Khambata
 */
public class MultiPolygon
        extends AbstractGeometryWithCoordinateSupport<List<List<Position>>, List<Polygon>>
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

    @Override
    public List<Polygon> toAtlasGeometry()
    {
        Validate.notEmpty(Positions.toListOfAtlasPolygonsFromMultiLineString(this.value));
        return Positions.toListOfAtlasPolygonsFromMultiLineString(this.value);
    }
}
