package org.openstreetmap.atlas.geography.atlas.lightweight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;

/**
 * Test class for {@link LightNode}
 *
 * @author Taylor Smock
 */
public class LightNodeTest
{
    @Rule
    public LightweightTestAtlasRule rule = new LightweightTestAtlasRule();

    @Test
    public void testNewNodeFromNode()
    {
        final LightNode lightNode = new LightNode(this.rule.getAtlas().node(3000000));
        assertEquals(lightNode, LightNode.from(this.rule.getAtlas().node(3000000)));
        assertNotEquals(lightNode, LightNode.from(this.rule.getAtlas().node(4000000)));
        assertEquals(Location.TEST_3, lightNode.getLocation());

        assertEquals(1, lightNode.getOutEdgeIdentifiers().length);
        lightNode.getOutEdgeIdentifiers()[0] = 5L;
        assertNotEquals(5L, lightNode.getOutEdgeIdentifiers()[0]);

        final LightNode lightNode4 = LightNode.from(this.rule.getAtlas().node(4000000));
        lightNode4.getInEdgeIdentifiers()[0] = 5L;
        assertEquals(1, lightNode4.getInEdgeIdentifiers().length);
        assertNotEquals(5L, lightNode4.getInEdgeIdentifiers()[0]);

        assertEquals(1, lightNode.outEdges().size());
        assertEquals(0, lightNode.inEdges().size());
        assertEquals(0, lightNode4.outEdges().size());
        assertEquals(1, lightNode4.inEdges().size());
        // This is checking that the equals method handles some edge cases.
        assertTrue(lightNode.equals(lightNode));
        assertFalse(lightNode.equals(null));
        assertEquals(3000000, lightNode.getIdentifier());
    }

    @Test
    public void testNewNodeIdentifier()
    {
        final LightNode lightNode = new LightNode(1000000);
        assertNull(lightNode.getLocation());
        assertEquals(0, lightNode.getInEdgeIdentifiers().length);
        assertEquals(0, lightNode.getOutEdgeIdentifiers().length);
        assertEquals(0, lightNode.outEdges().size());
        assertEquals(0, lightNode.inEdges().size());
        assertEquals(0, lightNode.getRelationIdentifiers().length);
        assertEquals(0, lightNode.relations().size());
        assertEquals(lightNode.hashCode(), lightNode.hashCode());
        assertNotEquals(lightNode.hashCode(), new LightNode(2000000).hashCode());
        assertEquals(lightNode, new LightNode(1000000));
        assertNotEquals(lightNode, new LightNode(2000000));
        assertEquals(1000000, lightNode.getIdentifier());
    }

    @Test
    public void testNewNodeIdentifierLocation()
    {
        final LightNode lightNode = new LightNode(1000000, Location.CENTER);
        assertEquals(0, lightNode.getInEdgeIdentifiers().length);
        assertEquals(0, lightNode.getOutEdgeIdentifiers().length);
        assertEquals(0, lightNode.outEdges().size());
        assertEquals(0, lightNode.inEdges().size());
        assertEquals(Location.CENTER, lightNode.getLocation());
        assertEquals(0, lightNode.getRelationIdentifiers().length);
        assertEquals(0, lightNode.relations().size());
        assertEquals(lightNode.hashCode(), lightNode.hashCode());
        assertNotEquals(lightNode, new LightNode(1000000));
        assertNotEquals(lightNode, new LightNode(1000000, Location.TEST_1));
        assertEquals(lightNode, new LightNode(1000000, Location.CENTER));
        assertEquals(1000000, lightNode.getIdentifier());
    }
}
