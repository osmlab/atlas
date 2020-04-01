package org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.polygon

import org.junit.Test
import org.openstreetmap.atlas.geography.GeometricSurface
import org.openstreetmap.atlas.geography.MultiPolygon
import org.openstreetmap.atlas.geography.Polygon
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.util.StreamUtil

import static org.openstreetmap.atlas.geography.atlas.dsl.TestConstants.Polygons.*

/**
 * @author Yazad Khambata
 */
class GeometricSurfaceSupportTest extends AbstractAQLTest {

    @Test
    void testMultiPolygon1() {


        final List<List<List<BigDecimal>>> listOfLocations = [
                goldenGateParkSanFransiscoCalifornia,
                guadalupeRiverParkAndGardensSanJoseCalifornia
        ]

        final GeometricSurface geometricSurface = GeometricSurfaceSupport.instance.toGeometricSurface(listOfLocations)

        assert listOfLocations.size() == 2
        assert ((MultiPolygon) geometricSurface).size() == listOfLocations.size()
    }

    @Test
    void testMultiPolygon2() {
        final List<List<List<BigDecimal>>> listOfLocations = [
                goldenGateParkSanFransiscoCalifornia,
                guadalupeRiverParkAndGardensSanJoseCalifornia,
                northernPartOfAlcatraz
        ]

        final GeometricSurface geometricSurface = GeometricSurfaceSupport.instance.toGeometricSurface(listOfLocations)

        assert listOfLocations.size() == 3
        assert ((MultiPolygon) geometricSurface).size() == listOfLocations.size()
    }

    @Test
    void testMultiPolygon3() {
        final List<List<List<BigDecimal>>> listOfLocations = [
                goldenGateParkSanFransiscoCalifornia,
                guadalupeRiverParkAndGardensSanJoseCalifornia
        ]

        final GeometricSurface geometricSurface = GeometricSurfaceSupport.instance.toGeometricSurface(listOfLocations)

        assert listOfLocations.size() == 2
        assert ((MultiPolygon) geometricSurface).size() == listOfLocations.size()

        final List<List<BigDecimal>> californiaAndNeighbouringAreaAsListOfLocations = [
                [-125.39794921875, 42.147114459220994], [-124.69482421875, 40.22082997283287], [-121.11328124999999, 34.27083595165], [-118.98193359375, 32.20350534542368], [-114.3896484375, 32.46342595776104], [-112.84057617187499, 33.660353121928814], [-119.36645507812499, 39.0533181067413], [-119.20166015625, 42.147114459220994], [-125.39794921875, 42.147114459220994]
        ]

        final Polygon californiaAndNeighbouringArea = GeometricSurfaceSupport.instance.toPolygon(californiaAndNeighbouringAreaAsListOfLocations)

        for (Polygon polygon : (MultiPolygon) geometricSurface) {
            final boolean result = californiaAndNeighbouringArea.overlaps(polygon)

            assert result
        }
    }

    @Test
    void testToGeometricSurface1() {
        final List<List<List<BigDecimal>>> list = [
                [
                        [10.1, 20.2],
                        [20.2, 30.3],
                        [10.1, 20.2],
                ],

                [
                        [50.5, 60.6],
                        [70.7, 71.71],
                        [50.5, 60.6],
                ],

                [
                        [80.8, 85.85],
                        [87.87, 89.89],
                        [80.8, 85.85],
                ]
        ]

        final GeometricSurface geometricSurface = GeometricSurfaceSupport.instance.toGeometricSurface(list)

        assert geometricSurface != null
        assert geometricSurface instanceof MultiPolygon
        assert ((MultiPolygon) geometricSurface).size() == 3
    }

    @Test
    void testToGeometricSurface2() {
        final List<List<List<BigDecimal>>> listOfLocations = [
                southernPartOfAlcatraz,
                goldenGateParkSanFransiscoCalifornia,
                provincetownMassachusetts,
                guadalupeRiverParkAndGardensSanJoseCalifornia,
                northernPartOfAlcatraz
        ]

        final List<List<BigDecimal>> californiaAndNeighbouringAreaAsListOfLocations = [
                [-125.39794921875, 42.147114459220994], [-124.69482421875, 40.22082997283287], [-121.11328124999999, 34.27083595165], [-118.98193359375, 32.20350534542368], [-114.3896484375, 32.46342595776104], [-112.84057617187499, 33.660353121928814], [-119.36645507812499, 39.0533181067413], [-119.20166015625, 42.147114459220994], [-125.39794921875, 42.147114459220994]
        ]

        final GeometricSurface californiaAndNeighbouringArea = GeometricSurfaceSupport.instance.toPolygon(californiaAndNeighbouringAreaAsListOfLocations)

        assert ((MultiPolygon) GeometricSurfaceSupport.instance.toGeometricSurface(listOfLocations)).size() == listOfLocations.size()

        final GeometricSurface filteredGeometricSurface = GeometricSurfaceSupport.instance.toGeometricSurface(listOfLocations, californiaAndNeighbouringArea).orElseThrow {
            new IllegalStateException()
        }

        assert ((MultiPolygon) filteredGeometricSurface).size() == 4
    }

    @Test
    void testJsonlFileLoad() {
        final GeometricSurface geometricSurface = GeometricSurfaceSupport.instance.fromJsonlFile("classpath:/data/polygon/jsonl/samples.jsonl")

        assert geometricSurface != null

        final MultiPolygon multiPolygon = (MultiPolygon) geometricSurface

        //seems like the MultiPolygon collapses some polygons.
        assert StreamUtil.stream((Iterable<Polygon>) multiPolygon).count() == 13
    }
}
