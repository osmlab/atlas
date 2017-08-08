package org.openstreetmap.atlas.geography.atlas.items;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 */
public class LoopingRelationTest
{
    private static final Logger logger = LoggerFactory.getLogger(LoopingRelationTest.class);

    @Rule
    public final LoopingRelationTestRule rule = new LoopingRelationTestRule();

    private Atlas combined;

    @Before
    public void init()
    {
        this.combined = new MultiAtlas(this.rule.getAtlas1(), this.rule.getAtlas2());
    }

    @Test
    public void testBounds()
    {
        final Rectangle bounds3 = this.combined.relation(3).bounds();
        logger.info("Relation 3: {}", bounds3);
        final Rectangle bounds4 = this.combined.relation(4).bounds();
        logger.info("Relation 4: {}", bounds4);
    }

    @Test
    public void testIntersects()
    {
        final boolean intersects3 = this.combined.relation(3).intersects(Polygon.SILICON_VALLEY);
        logger.info("Relation 3: {}", intersects3);
        final boolean intersects4 = this.combined.relation(4).intersects(Polygon.SILICON_VALLEY);
        logger.info("Relation 4: {}", intersects4);
    }
}
