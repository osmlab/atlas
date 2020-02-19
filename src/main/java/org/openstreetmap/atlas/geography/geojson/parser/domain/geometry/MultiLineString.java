package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Coordinates;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Position;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Positions;

/**
 * @author Yazad Khambata
 */
public class MultiLineString
        extends AbstractGeometryWithCoordinateSupport<List<List<Position>>, List<PolyLine>>
{
    private Coordinates<List<List<Position>>> coordinates;
    
    public MultiLineString(final Map<String, Object> map)
    {
        super(map, null);
        this.coordinates = Coordinates.forMultiLineString(
                (List<List<List<Double>>>) extractRawCoordinates(map));
    }
    
    @Override
    public Coordinates<List<List<Position>>> getCoordinates()
    {
        return this.coordinates;
    }
    
    @Override
    public List<PolyLine> toAtlasGeometry()
    {
        final List<List<Location>> listsOfLocations = Positions.toCollectionsOfLocations(
                this.coordinates.getValue());
        final List<PolyLine> polyLines = listsOfLocations.stream().map(PolyLine::new).collect(
                Collectors.toList());
        return polyLines;
    }
}
