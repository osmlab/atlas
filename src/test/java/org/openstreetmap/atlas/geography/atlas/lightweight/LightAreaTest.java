package org.openstreetmap.atlas.geography.atlas.lightweight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * Test class for {@link LightArea}
 *
 * @author Taylor Smock
 */
public class LightAreaTest
{
    @Rule
    public LightweightTestAtlasRule rule = new LightweightTestAtlasRule();

    @Test
    public void testLightAreaFromArea()
    {
        final LightArea lightArea = LightArea.from(this.rule.getAtlas().area(7000000));
        assertEquals(7000000, lightArea.getIdentifier());
        assertEquals(1, lightArea.relations().size());
        assertEquals(1, lightArea.getRelationIdentifiers().length);
        assertEquals(8000000, lightArea.getRelationIdentifiers()[0]);
        assertEquals(4, Iterables.asList(lightArea.asPolyLine()).size());
        assertEquals(4, Iterables.asList(lightArea.getRawGeometry()).size());
        assertEquals(lightArea.hashCode(), lightArea.hashCode());

        final LightArea otherArea = new LightArea(7000000, Location.TEST_1, Location.TEST_2,
                Location.TEST_3, Location.TEST_4);
        assertNotEquals(lightArea.hashCode(), otherArea.hashCode());
        assertNotEquals(lightArea, otherArea);
    }

    @Test
    public void testLightAreaIdentifier()
    {
        final LightArea lightArea = new LightArea(1000000);
        assertEquals(1000000, lightArea.getIdentifier());
        assertEquals(0, lightArea.relations().size());
        assertEquals(0, lightArea.getRelationIdentifiers().length);
        assertNull(lightArea.asPolyLine());
        assertNull(lightArea.getRawGeometry());
        assertNull(lightArea.getGeometry());
        assertEquals(lightArea.hashCode(), lightArea.hashCode());

        final LightArea otherArea = new LightArea(2000000);
        assertNotEquals(lightArea.hashCode(), otherArea.hashCode());
        assertNotEquals(lightArea, otherArea);
    }

    @Test
    public void testLightAreaIdentifierLocations()
    {
        final LightArea lightArea = new LightArea(1000000, Location.TEST_1, Location.TEST_2);
        assertEquals(1000000, lightArea.getIdentifier());
        assertEquals(0, lightArea.relations().size());
        assertEquals(0, lightArea.getRelationIdentifiers().length);
        assertEquals(2, Iterables.asList(lightArea.asPolyLine()).size());
        assertEquals(2, Iterables.asList(lightArea.getRawGeometry()).size());
        assertEquals(lightArea.hashCode(), lightArea.hashCode());

        final LightArea otherArea = new LightArea(1000000, Location.TEST_3, Location.TEST_4);
        assertNotEquals(lightArea.hashCode(), otherArea.hashCode());
        assertNotEquals(lightArea, otherArea);
    }
}
