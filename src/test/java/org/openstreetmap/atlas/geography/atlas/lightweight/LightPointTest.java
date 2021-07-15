package org.openstreetmap.atlas.geography.atlas.lightweight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;

/**
 * Test class for {@link LightPoint}
 *
 * @author Taylor Smock
 */
public class LightPointTest
{
    @Rule
    public LightweightTestAtlasRule rule = new LightweightTestAtlasRule();

    @Test
    public void testNewPointFromPoint()
    {
        final LightPoint lightPoint = new LightPoint(this.rule.getAtlas().point(1000000));
        assertEquals(lightPoint, LightPoint.from(this.rule.getAtlas().point(1000000)));
        assertNotEquals(lightPoint, LightPoint.from(this.rule.getAtlas().point(2000000)));
        // This is checking that the equals method handles some edge cases.
        assertTrue(lightPoint.equals(lightPoint));
        assertFalse(lightPoint.equals(null));
    }

    @Test
    public void testNewPointIdentifier()
    {
        final LightPoint lightPoint = new LightPoint(1000000);
        assertNull(lightPoint.getLocation());
        assertEquals(0, lightPoint.getRelationIdentifiers().length);
        assertEquals(0, lightPoint.relations().size());
        assertEquals(lightPoint.hashCode(), lightPoint.hashCode());
        assertNotEquals(lightPoint.hashCode(), new LightPoint(2000000).hashCode());
        assertEquals(lightPoint, new LightPoint(1000000));
        assertNotEquals(lightPoint, new LightPoint(2000000));
    }

    @Test
    public void testNewPointIdentifierLocation()
    {
        final LightPoint lightPoint = new LightPoint(1000000, Location.CENTER);
        assertEquals(Location.CENTER, lightPoint.getLocation());
        assertEquals(0, lightPoint.getRelationIdentifiers().length);
        assertEquals(0, lightPoint.relations().size());
        assertEquals(lightPoint.hashCode(), lightPoint.hashCode());
        assertNotEquals(lightPoint, new LightPoint(1000000));
        assertNotEquals(lightPoint, new LightPoint(1000000, Location.TEST_1));
        assertEquals(lightPoint, new LightPoint(1000000, Location.CENTER));
    }

    @Test
    public void testNewPointIdentifierLocationRelationIdentifiers()
    {
        final LightPoint lightPoint = new LightPoint(1000000L, Location.CENTER, Set.of(8000000L));
        assertEquals(Location.CENTER, lightPoint.getLocation());
        assertEquals(1, lightPoint.getRelationIdentifiers().length);
        assertEquals(1, lightPoint.relations().size());
        assertEquals(lightPoint.hashCode(), lightPoint.hashCode());
        assertNotEquals(lightPoint, new LightPoint(1000000));
        assertNotEquals(lightPoint, new LightPoint(1000000, Location.TEST_1));
        assertNotEquals(lightPoint, new LightPoint(1000000L, Location.TEST_1, Set.of(8000000L)));
        assertEquals(lightPoint, new LightPoint(1000000L, Location.CENTER, Set.of(8000000L)));
    }
}
