package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Coordinates;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Position;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Positions;

/**
 * @author Yazad Khambata
 */
@SuppressWarnings("squid:S2160") public class Point extends AbstractGeometryWithCoordinateSupport<Position, Location>
{
    private Coordinates<Position> coordinates;

    public Point(final Map<String, Object> map)
    {
        super(map);
        this.coordinates = Coordinates.forPoint((List<Double>) extractRawCoordinates(map));
    }

    @Override
    public Coordinates<Position> getCoordinates()
    {
        return this.coordinates;
    }

    @Override
    public Location toAtlasGeometry()
    {
        return Positions.toLocation(this.coordinates.getValue());
    }
}
