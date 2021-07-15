package org.openstreetmap.atlas.geography.atlas.lightweight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * Test class for {@link LightEdge}
 *
 * @author Taylor Smock
 */
public class LightEdgeTest
{
    @Rule
    public LightweightTestAtlasRule rule = new LightweightTestAtlasRule();

    @Test
    public void testNewEdgeFromEdge()
    {
        final LightEdge lightEdge = LightEdge.from(this.rule.getAtlas().edge(6000000));
        assertEquals(6000000, lightEdge.getIdentifier());
        assertEquals(1, lightEdge.relations().size());
        assertEquals(1, lightEdge.getRelationIdentifiers().length);
        assertEquals(8000000, lightEdge.getRelationIdentifiers()[0]);
        assertEquals(2, Iterables.asList(lightEdge.asPolyLine()).size());
        assertEquals(2, Iterables.asList(lightEdge.getRawGeometry()).size());
        assertEquals(Location.TEST_3, lightEdge.start().getLocation());
        assertEquals(Location.TEST_4, lightEdge.end().getLocation());
        assertEquals(3000000, lightEdge.start().getIdentifier());
        assertEquals(4000000, lightEdge.end().getIdentifier());
        assertEquals(lightEdge.hashCode(), lightEdge.hashCode());

        final LightEdge otherEdge = new LightEdge(1000000, Location.TEST_1, Location.TEST_2);
        assertNotEquals(lightEdge.hashCode(), otherEdge.hashCode());
        assertNotEquals(lightEdge, otherEdge);
    }

    @Test
    public void testNewEdgeIdentifier()
    {
        final LightEdge lightEdge = new LightEdge(1000000);
        assertEquals(1000000, lightEdge.getIdentifier());
        assertEquals(0, lightEdge.relations().size());
        assertEquals(0, lightEdge.getRelationIdentifiers().length);
        assertNull(lightEdge.asPolyLine());
        assertNull(lightEdge.getRawGeometry());
        assertNull(lightEdge.getGeometry());
        assertNull(lightEdge.end());
        assertNull(lightEdge.start());
        assertEquals(lightEdge.hashCode(), lightEdge.hashCode());

        final LightEdge otherEdge = new LightEdge(2000000);
        assertNotEquals(lightEdge.hashCode(), otherEdge.hashCode());
        assertNotEquals(lightEdge, otherEdge);
    }

    @Test
    public void testNewEdgeIdentifierLocations()
    {
        final LightEdge lightEdge = new LightEdge(1000000, Location.TEST_3, Location.TEST_4);
        assertEquals(1000000, lightEdge.getIdentifier());
        assertEquals(0, lightEdge.relations().size());
        assertEquals(0, lightEdge.getRelationIdentifiers().length);
        assertEquals(2, Iterables.asList(lightEdge.asPolyLine()).size());
        assertEquals(2, Iterables.asList(lightEdge.getRawGeometry()).size());
        assertEquals(Location.TEST_4, lightEdge.end().getLocation());
        assertEquals(Location.TEST_3, lightEdge.start().getLocation());
        assertEquals(lightEdge.hashCode(), lightEdge.hashCode());

        final LightEdge otherEdge = new LightEdge(1000000, Location.TEST_1, Location.TEST_2);
        assertNotEquals(lightEdge.hashCode(), otherEdge.hashCode());
        assertNotEquals(lightEdge, otherEdge);
    }
}
