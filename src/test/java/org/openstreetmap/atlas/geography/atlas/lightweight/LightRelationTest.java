package org.openstreetmap.atlas.geography.atlas.lightweight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.Rule;
import org.junit.Test;

/**
 * Test class from {@link LightRelation}
 *
 * @author Taylor Smock
 */
public class LightRelationTest
{
    @Rule
    public LightweightTestAtlasRule atlas = new LightweightTestAtlasRule();

    @Test
    public void testNewRelationFromRelation()
    {
        final LightRelation lightRelation = LightRelation
                .from(this.atlas.getAtlas().relation(8000000));
        assertEquals(5, lightRelation.members().size());
        assertEquals(5, lightRelation.allKnownOsmMembers().size());
        assertEquals(0, lightRelation.getRelationIdentifiers().length);
        assertEquals(0, lightRelation.relations().size());
        assertEquals(8000000, lightRelation.getIdentifier());
        assertEquals(lightRelation.getOsmIdentifier(), lightRelation.osmRelationIdentifier());
        assertEquals(lightRelation, new LightRelation(lightRelation));
        assertNotEquals(lightRelation, new LightRelation(1000000));
    }

    @Test
    public void testNewRelationIdentifier()
    {
        final LightRelation lightRelation = new LightRelation(8000000);
        assertEquals(0, lightRelation.members().size());
        assertEquals(0, lightRelation.allKnownOsmMembers().size());
        assertEquals(0, lightRelation.relations().size());
        assertEquals(0, lightRelation.getRelationIdentifiers().length);
        assertEquals(8000000, lightRelation.getIdentifier());
        assertEquals(lightRelation.getOsmIdentifier(), lightRelation.osmRelationIdentifier());
        assertEquals(lightRelation, new LightRelation(8000000));
        assertNotEquals(lightRelation, new LightRelation(1000000));
    }
}
