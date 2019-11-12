package org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.polygon


import org.junit.Test
import org.openstreetmap.atlas.geography.*
import org.openstreetmap.atlas.geography.atlas.complete.CompleteNode
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.BinaryOperations
import org.openstreetmap.atlas.geography.atlas.items.Node

/**
 * @author Yazad Khambata
 */
class PolygonSupportTest {

    @Test
    void testPolygonConversion() {
        final List<List<List<BigDecimal>>> poly = [
                [
                        [103.7817053, 1.249778],
                        [103.8952432, 1.249778],
                        [103.8952432, 1.404799],
                        [103.7817053, 1.404799],
                        [103.7817053, 1.249778]
                ]
        ]

        final List<List<BigDecimal>> locs = poly.get(0)
        final Polygon polygonOfLocations = new Polygon(
                locs.stream().map { longLatPair ->
                    final BigDecimal longitude = longLatPair.get(0)
                    final BigDecimal latitude = longLatPair.get(1)

                    return new Location(Latitude.degrees(latitude), Longitude.degrees(longitude))
                }.toArray { new Location[locs.size()] }
        )
        final Location center = polygonOfLocations.center()
        final Node node = new CompleteNode(1, center, new HashMap<>(), null, null, null)
        assert BinaryOperations.within.perform(node, poly, Node)
    }
}
