package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import java.util.List;
import java.util.Map;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.geojson.parser.domain.bbox.Bbox;
import org.openstreetmap.atlas.geography.geojson.parser.domain.foreign.ForeignFields;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Coordinates;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Position;

/**
 * @author Yazad Khambata
 */
public class LineString extends AbstractGeometryWithCoordinateSupport<List<Position>, PolyLine>
{
    private MultiPoint value;
    
    public LineString(final Map<String, Object> map)
    {
        super(map, null);
        this.value = new MultiPoint(map);
    }
    
    @Override
    public Bbox getBbox()
    {
        return this.value.getBbox();
    }
    
    @Override
    public Coordinates<List<Position>> getCoordinates()
    {
        return this.value.getCoordinates();
    }
    
    @Override
    public ForeignFields getForeignFields()
    {
        return this.value.getForeignFields();
    }
    
    @Override
    public PolyLine toAtlasGeometry()
    {
        final List<Location> locations = this.value.toAtlasGeometry();
        final PolyLine polyLine = new PolyLine(locations);
        return polyLine;
    }
}
