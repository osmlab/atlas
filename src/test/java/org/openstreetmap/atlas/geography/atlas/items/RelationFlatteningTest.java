package org.openstreetmap.atlas.geography.atlas.items;

import static org.junit.Assert.assertEquals;

import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author samuelgass
 */
public class RelationFlatteningTest
{
    private static final Logger logger = LoggerFactory.getLogger(RelationFlatteningTest.class);

    @Rule
    public final RelationFlatteningRule rule = new RelationFlatteningRule();

    @Test
    public void testComplexRelation()
    {
        final Relation complexRelation = this.rule.getAtlas1().relation(7);
        logger.info("Relation 7: {}", complexRelation);
        final Set<AtlasObject> flattened = complexRelation.flatten();
        logger.info("Flattened: {}", flattened);
        assertEquals(2, flattened.size());
    }

    @Test
    public void testRecursiveRelation()
    {
        final Relation recursiveRelation = this.rule.getAtlas1().relation(8);
        logger.info("Relation 8: {}", recursiveRelation);
        final Set<AtlasObject> flattened = recursiveRelation.flatten();
        logger.info("Flattened: {}", flattened);
        assertEquals(2, flattened.size());
    }

    @Test
    public void testSimpleRelation()
    {
        final Relation simpleRelation = this.rule.getAtlas1().relation(6);
        logger.info("Relation 6: {}", simpleRelation);
        final Set<AtlasObject> flattened = simpleRelation.flatten();
        logger.info("Flattened: {}", flattened);
        assertEquals(1, flattened.size());
        final AtlasObject memberNode = flattened.stream().findFirst().get();
        if (memberNode instanceof Node)
        {
            assertEquals(1, ((Node) memberNode).getIdentifier());
        }
        else
        {
            Assert.fail("Member was not the expected type of 'Node'!");
        }
    }
}
