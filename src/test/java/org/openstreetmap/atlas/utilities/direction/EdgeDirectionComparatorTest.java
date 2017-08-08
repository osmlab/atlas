package org.openstreetmap.atlas.utilities.direction;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.items.Edge;

/**
 * @author Sid
 */
public class EdgeDirectionComparatorTest
{
    @Rule
    public EdgeDirectionComparatorTestCaseRule setup = new EdgeDirectionComparatorTestCaseRule();

    @Test
    public void testOppositeDirection()
    {
        final EdgeDirectionComparator edgeDirectionComparator = new EdgeDirectionComparator();
        final Map<Integer, Integer> oppositeTurnMap = new HashMap<>();
        oppositeTurnMap.put(1314, 1415);
        oppositeTurnMap.put(56, 78);
        oppositeTurnMap.put(56, 65);
        oppositeTurnMap.forEach((from, too) ->
        {
            final String entry = from + " -> " + too;
            final Edge fromOppositeTurn = this.setup.getAtlas().edge(from);
            final Edge toOppositeTurn = this.setup.getAtlas().edge(too);
            Assert.assertTrue("Should be a U Turn : " + entry,
                    edgeDirectionComparator.isOppositeDirection(fromOppositeTurn.asPolyLine(),
                            toOppositeTurn.asPolyLine(), false));
            Assert.assertFalse("Should be only U Turn : " + entry, edgeDirectionComparator
                    .isSameDirection(fromOppositeTurn, toOppositeTurn, false));
        });
    }

    @Test
    public void testSameDirection()
    {
        final EdgeDirectionComparator edgeDirectionComparator = new EdgeDirectionComparator();
        final Map<Integer, Integer> sameDirectionMap = new HashMap<>();
        sameDirectionMap.put(12, 12);
        sameDirectionMap.put(1617, 1718);
        sameDirectionMap.forEach((from, too) ->
        {
            final String entry = from + " -> " + too;
            final Edge fromSameDirection = this.setup.getAtlas().edge(from);
            final Edge toSameDirection = this.setup.getAtlas().edge(too);
            Assert.assertTrue("Should be Same Direction : " + entry, edgeDirectionComparator
                    .isSameDirection(fromSameDirection, toSameDirection, false));
            Assert.assertFalse("Should be only Same Direction : " + entry,
                    edgeDirectionComparator.isOppositeDirection(fromSameDirection.asPolyLine(),
                            toSameDirection.asPolyLine(), false));
        });

        // Segment Vs Overall Heading
        final Map<Integer, Integer> sameDirectionEdgeCasesMap = new HashMap<>();
        sameDirectionEdgeCasesMap.put(1920, 202122);
        sameDirectionEdgeCasesMap.forEach((from, too) ->
        {
            final String entry = from + " -> " + too;
            final Edge fromSameDirection = this.setup.getAtlas().edge(from);
            final Edge toSameDirection = this.setup.getAtlas().edge(too);
            Assert.assertTrue("Should be Same Direction if we use segment Heading: " + entry,
                    edgeDirectionComparator.isSameDirection(fromSameDirection, toSameDirection,
                            false));
            Assert.assertFalse("Should NOT be in Same Direction if we use overallHeading: " + entry,
                    edgeDirectionComparator.isSameDirection(fromSameDirection, toSameDirection,
                            true));
            Assert.assertFalse("Should be only Same Direction : " + entry,
                    edgeDirectionComparator.isOppositeDirection(fromSameDirection.asPolyLine(),
                            toSameDirection.asPolyLine(), false));
        });
    }
}
