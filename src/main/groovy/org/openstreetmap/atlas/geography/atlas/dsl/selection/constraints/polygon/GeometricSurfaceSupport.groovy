package org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.polygon

import groovy.json.JsonSlurper
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.tuple.Pair
import org.openstreetmap.atlas.geography.*
import org.openstreetmap.atlas.geography.atlas.dsl.schema.uri.SchemeSupport
import org.openstreetmap.atlas.geography.atlas.dsl.util.StreamUtil
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid

import java.nio.charset.Charset
import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * @author Yazad Khambata
 */
@Singleton
class GeometricSurfaceSupport {

    Polygon toPolygon(List<List<BigDecimal>> polygonAsNextedList) {
        final Location[] locations = polygonAsNextedList.stream().map { longLatPair ->
            toLocation(longLatPair)
        }.toArray { new Location[polygonAsNextedList.size()] }

        new Polygon(locations)
    }

    private Location toLocation(BigDecimal latitude, BigDecimal longitude) {
        new Location(Latitude.degrees(latitude), Longitude.degrees(longitude))
    }

    private Location toLocation(List<BigDecimal> longLatPair) {
        final BigDecimal longitude = longLatPair.get(0)
        final BigDecimal latitude = longLatPair.get(1)

        toLocation(latitude, longitude)
    }

    /**
     * Converts a set of locations to a Polygon.
     * Example of input,
     *
     *          [
     *                 [
     *                         [103.7817053, 1.249778],
     *                         [103.8952432, 1.249778],
     *                         [103.8952432, 1.404799],
     *                         [103.7817053, 1.404799],
     *                         [103.7817053, 1.249778]
     *                 ],
     *
     *                 [
     *                         [105.7817053, 3.249778],
     *                         [105.8952432, 3.249778],
     *                         [105.8952432, 3.404799],
     *                         [105.7817053, 3.404799],
     *                         [105.7817053, 3.249778]
     *                 ],
     *
     *                 ...
     *         ]
     *
     *
     * @param surfaceLocations - see example above.
     * @param bounds - Rectangle of the bounds used for filtering. Only polygons contained in polygonsAsNextedLists that
     *                  have some overlap with bounds would be considered while generating the GeometricSurface.
     * @return - An Optional Atlas MultiPolygon
     */
    Optional<GeometricSurface> toGeometricSurface(final List<List<List<BigDecimal>>> polygonsAsNextedLists, final GeometricSurface bounds) {
        final Stream<Polygon> polygonStream = toPolygonStream(polygonsAsNextedLists)

        toGeometricSurface(polygonStream, bounds)
    }

    Optional<GeometricSurface> toGeometricSurface(final MultiPolygon multiPolygon, final GeometricSurface bounds) {
        final Stream<Polygon> polygonStream = StreamUtil.stream(multiPolygon.iterator())

        toGeometricSurface(polygonStream, bounds)
    }

    Optional<GeometricSurface> toGeometricSurface(Stream<Polygon> polygonStream, GeometricSurface bounds) {
        final MultiPolygon multiPolygon = polygonStream
                .filter { Polygon polygon ->
                    bounds != null?polygon.overlaps((PolyLine) bounds):true
                }
                .collect(
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                { listOfPolygons -> MultiPolygon.forOuters(listOfPolygons) }
                        )
                )

        Valid.isTrue multiPolygon != null

        Optional.of(multiPolygon)
    }

    private Stream<Polygon> toPolygonStream(List<List<List<BigDecimal>>> polygonsAsNextedLists) {
        polygonsAsNextedLists.stream()
                .map { List<List<BigDecimal>> polygonAsNextedList ->
                    toPolygon(polygonAsNextedList)
                }
    }

    GeometricSurface toGeometricSurface(final List<List<List<BigDecimal>>> surfaceLocations) {
        toGeometricSurface(surfaceLocations, null).get()
    }

    Optional<GeometricSurface> fromJsonlFile(final String uri, final GeometricSurface bounds) {
        final InputStream inputStream = SchemeSupport.instance.loadFile(uri)

        Valid.isTrue inputStream != null

        final List<String> lines = IOUtils.readLines(inputStream, Charset.defaultCharset())
        final JsonSlurper jsonSlurper = new JsonSlurper()
        final List<List<List<BigDecimal>>> allPolygons =
                lines.stream().map { line ->
                    final List<List<BigDecimal>> polygonAsNestedLists = jsonSlurper.parse(line.getBytes(Charset.defaultCharset()))

                    polygonAsNestedLists
                }.collect(Collectors.toList())

        toGeometricSurface(allPolygons, bounds)
    }

    GeometricSurface fromJsonlFile(final String uri) {
        fromJsonlFile(uri, null).get()
    }

    private static void isValid(final Pair<Integer, Polygon> pair) {
        Valid.notEmpty pair
        Valid.notEmpty pair.getKey()
        Valid.notEmpty pair.getValue()
    }
}
