package org.openstreetmap.atlas.geography.atlas.items;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.tags.MaxSpeedTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 * @author mgostintsev
 */
public class EdgeTest
{
    private static final Logger logger = LoggerFactory.getLogger(EdgeTest.class);

    @Rule
    public final EdgeTestRule rule = new EdgeTestRule();

    @Test
    public void testDirectionalizedTags()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Edge edge6 = atlas.edge(6L).directionalized();
        final Edge edge7 = atlas.edge(7L).directionalized();
        final Edge edge8 = atlas.edge(8L).directionalized();
        final Edge edge9 = atlas.edge(9L).directionalized();

        logger.info("Edge 6: {}", edge6);
        Assert.assertEquals("60", edge6.tag(MaxSpeedTag.KEY));
        logger.info("Edge -6: {}", edge6.reversed().get());
        Assert.assertNull(edge6.reversed().get().tag(MaxSpeedTag.KEY));

        logger.info("Edge 7: {}", edge7);
        Assert.assertNull(edge7.tag(MaxSpeedTag.KEY));
        logger.info("Edge -7: {}", edge7.reversed().get());
        Assert.assertEquals("70", edge7.reversed().get().tag(MaxSpeedTag.KEY));

        logger.info("Edge 8: {}", edge8);
        Assert.assertEquals("80", edge8.tag(MaxSpeedTag.KEY));
        logger.info("Edge -8: {}", edge8.reversed().get());
        Assert.assertEquals("80", edge8.reversed().get().tag(MaxSpeedTag.KEY));

        logger.info("Edge 9: {}", edge9);
        Assert.assertEquals("90", edge9.tag(MaxSpeedTag.KEY));
        logger.info("Edge -9: {}", edge9.reversed().get());
        Assert.assertEquals("10", edge9.reversed().get().tag(MaxSpeedTag.KEY));
    }

    @Test
    public void testGetMasterEdge()
    {
        final Atlas atlas = this.rule.getAtlas();

        // Given the reverse edge, should return the master
        final Edge reverseEdge = atlas.edge(-293669785000001L);
        Assert.assertEquals(atlas.edge(293669785000001L), reverseEdge.getMasterEdge());

        // The master edge should just be itself
        final Edge masterEdge = atlas.edge(293669785000001L);
        Assert.assertEquals(masterEdge, masterEdge.getMasterEdge());

        // Now, let's try with a one-way edge
        final Edge masterEdge2 = atlas.edge(293669786000001L);
        Assert.assertEquals(masterEdge2, masterEdge2.getMasterEdge());
    }

    @Test
    public void testIsWaySectioned()
    {
        final Atlas atlas = this.rule.getAtlas();

        final Edge nonWaySectioned = atlas.edge(293669785000000L);
        Assert.assertFalse(nonWaySectioned.isWaySectioned());

        final Edge reverseNonWaySectioned = atlas.edge(-293669785000000L);
        Assert.assertFalse(reverseNonWaySectioned.isWaySectioned());

        final Edge waySectioned = atlas.edge(293669785000001L);
        Assert.assertTrue(waySectioned.isWaySectioned());

        final Edge reverseWaySectioned = atlas.edge(-293669785000001L);
        Assert.assertTrue(reverseWaySectioned.isWaySectioned());
    }

    @Test
    public void testSize()
    {
        final Atlas atlas = this.rule.getAtlas();
        final Edge edge = atlas.edge(293669785000000L);
        Assert.assertTrue(edge.numberOfShapePoints() == 2);
    }

}
