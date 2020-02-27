package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import java.util.List;
import java.util.Map;

import org.openstreetmap.atlas.geography.geojson.parser.domain.bbox.Bbox;
import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.ForeignFields;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Coordinates;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Position;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Positions;

/**
 * @author Yazad Khambata
 */
@SuppressWarnings("squid:S2160")
public class Polygon extends
        AbstractGeometryWithCoordinateSupport<List<List<Position>>, org.openstreetmap.atlas.geography.Polygon>
{
    private MultiLineString value;

    public Polygon(final Map<String, Object> map)
    {
        super(map, null);
        this.value = new MultiLineString(map);
    }

    @Override
    public Bbox getBbox()
    {
        return this.value.getBbox();
    }

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
    public org.openstreetmap.atlas.geography.Polygon toAtlasGeometry()
    {
        return Positions.toAtlasPolygonFromMultiLineString(this.value);
    }
}
