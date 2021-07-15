package org.openstreetmap.atlas.geography.atlas.lightweight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * Test class for {@link LightLine}
 *
 * @author Taylor Smock
 */
public class LightLineTest
{
    @Rule
    public LightweightTestAtlasRule rule = new LightweightTestAtlasRule();

    @Test
    public void testNewLineIdentifier()
    {
        final LightLine lightLine = new LightLine(1000000);
        assertEquals(1000000, lightLine.getIdentifier());
        assertEquals(0, lightLine.getRelationIdentifiers().length);
        assertEquals(0, lightLine.relations().size());
        assertNull(lightLine.asPolyLine());
        assertNull(lightLine.getGeometry());
        assertNull(lightLine.getRawGeometry());
        assertEquals(new LightLine(1000000), lightLine);
        assertEquals(lightLine.hashCode(), lightLine.hashCode());
    }

    @Test
    public void testNewLineIdentifierLocations()
    {
        final LightLine lightLine = new LightLine(1000000, Location.TEST_1, Location.TEST_2);
        assertEquals(1000000, lightLine.getIdentifier());
        assertEquals(0, lightLine.getRelationIdentifiers().length);
        assertEquals(0, lightLine.relations().size());
        assertEquals(2, Iterables.asList(lightLine.asPolyLine()).size());
        assertEquals(2, Iterables.asList(lightLine.getRawGeometry()).size());
        assertNotEquals(new LightLine(1000000), lightLine);
        assertNotEquals(new LightLine(1000000, Location.TEST_1), lightLine);
        assertEquals(new LightLine(1000000, Location.TEST_1, Location.TEST_2), lightLine);
        assertEquals(lightLine.hashCode(), lightLine.hashCode());
    }

    @Test
    public void testNewLineLine()
    {
        final LightLine lightLine = LightLine.from(this.rule.getAtlas().line(5000000));
        assertEquals(5000000, lightLine.getIdentifier());
        assertEquals(1, lightLine.getRelationIdentifiers().length);
        assertEquals(1, lightLine.relations().size());
        assertEquals(2, Iterables.asList(lightLine.asPolyLine()).size());
        assertEquals(2, Iterables.asList(lightLine.getRawGeometry()).size());
        assertNotEquals(new LightLine(5000000), lightLine);
        assertNotEquals(new LightLine(5000000, Location.TEST_1), lightLine);
        assertNotEquals(new LightLine(5000000, Location.TEST_1, Location.TEST_2), lightLine);
        assertEquals(lightLine.hashCode(), lightLine.hashCode());
        assertEquals(lightLine, new LightLine(lightLine));
    }
}
