package org.openstreetmap.atlas.geography.atlas;

import org.junit.Assert;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.sub.AtlasCutType;

/**
 * @author matthieun
 */
public class SubAtlasIntegrationTest extends AtlasIntegrationTest
{
    @Test
    public void testSubCuba()
    {
        final Atlas cuba = loadCuba();
        final Atlas sub = cuba
                .subAtlas(Rectangle.forCorners(Location.forString("20.049468, -74.368043"),
                        Location.forString("20.382402, -74.077814")), AtlasCutType.SOFT_CUT)
                .orElseThrow(() -> new CoreException("SubAtlas was not present."));
        Assert.assertEquals(523, sub.metaData().getSize().getNodeNumber());
        Assert.assertEquals(1344, sub.metaData().getSize().getEdgeNumber());
        Assert.assertEquals(223, sub.metaData().getSize().getAreaNumber());
        Assert.assertEquals(18, sub.metaData().getSize().getLineNumber());
        Assert.assertEquals(28, sub.metaData().getSize().getPointNumber());
        Assert.assertEquals(2, sub.metaData().getSize().getRelationNumber());
    }
}
