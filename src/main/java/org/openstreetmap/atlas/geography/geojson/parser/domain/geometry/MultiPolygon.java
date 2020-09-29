package org.openstreetmap.atlas.geography.geojson.parser.domain.geometry;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Coordinates;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Position;
import org.openstreetmap.atlas.geography.geojson.parser.domain.geometry.coordinate.Positions;
import org.openstreetmap.atlas.utilities.maps.MultiMap;

/**
 * @author Yazad Khambata
 */
@SuppressWarnings("squid:S2160")
public class MultiPolygon extends
        AbstractGeometryWithCoordinateSupport<List<List<List<Position>>>, org.openstreetmap.atlas.geography.MultiPolygon>
{
    private Coordinates<List<List<List<Position>>>> coordinates;

    public MultiPolygon(final Map<String, Object> map)
    {
        super(map, null);
        this.coordinates = Coordinates
                .forMultiPolygon((List<List<List<List<Double>>>>) extractRawCoordinates(map));
    }

    @Override
    public Coordinates<List<List<List<Position>>>> getCoordinates()
    {
        return this.coordinates;
    }

    @Override
    public org.openstreetmap.atlas.geography.MultiPolygon toAtlasGeometry()
    {
        final MultiMap<Polygon, Polygon> outersToIneers = new MultiMap<>();
        this.coordinates.getValue().stream()
                .map(geojsonPolygon -> geojsonPolygon.stream().map(
                        geojsonLinearRing -> new Polygon(Positions.toLocations(geojsonLinearRing)))
                        .collect(Collectors.toList()))
                .forEach(polygonList -> outersToIneers.put(polygonList.get(0),
                        polygonList.size() > 1 ? polygonList.subList(1, polygonList.size())
                                : Collections.emptyList()));
        return new org.openstreetmap.atlas.geography.MultiPolygon(outersToIneers);
    }
}
