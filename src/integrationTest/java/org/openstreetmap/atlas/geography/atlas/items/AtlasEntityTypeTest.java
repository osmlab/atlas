package org.openstreetmap.atlas.geography.atlas.items;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasIntegrationTest;

/**
 * Tests Atlas's type retrieval
 *
 * @author mkalender
 */
public class AtlasEntityTypeTest extends AtlasIntegrationTest
{

    @Test
    public void testTypeRetrieval()
    {
        final Atlas atlas = loadCuba();
        for (final AtlasEntity entity : atlas.entities(entity -> true))
        {
            if (entity instanceof Node)
            {
                Assert.assertEquals(((Node) entity).getType(), ItemType.NODE);
            }
            else if (entity instanceof Edge)
            {
                Assert.assertEquals(((Edge) entity).getType(), ItemType.EDGE);
            }
            else if (entity instanceof Area)
            {
                Assert.assertEquals(((Area) entity).getType(), ItemType.AREA);
            }
            else if (entity instanceof Line)
            {
                Assert.assertEquals(((Line) entity).getType(), ItemType.LINE);
            }
            else if (entity instanceof Point)
            {
                Assert.assertEquals(((Point) entity).getType(), ItemType.POINT);
            }
            else if (entity instanceof Relation)
            {
                Assert.assertEquals(((Relation) entity).getType(), ItemType.RELATION);
            }
            else
            {
                Assert.fail("Unknown type");
            }
        }
    }
}
