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
    public void testLoopingRelation()
    {
        final Relation loopingRelation = this.rule.getAtlas().relation(8);
        logger.info("Looping (self-containing) Relation: {}", loopingRelation);
        final Set<AtlasObject> flattened = loopingRelation.flatten();
        logger.info("Flattened: {}", flattened);
        assertEquals(2, flattened.size());
    }

    @Test
    public void testNestedRelation()
    {
        final Relation nestedRelation = this.rule.getAtlas().relation(10);
        logger.info("Nested Relation: {}", nestedRelation);
        final Set<AtlasObject> flattened = nestedRelation.flatten();
        logger.info("Flattened: {}", flattened);
        assertEquals(6, flattened.size());
    }

    @Test
    public void testNodesAndRelationsWithSameId()
    {
        final Relation relation = this.rule.getAtlas().relation(1);
        logger.info("Relation containing nodes and relations with the same numeric id: {}",
                relation);
        final Set<AtlasObject> flattened = relation.flatten();
        logger.info("Flattened: {}", flattened);
        assertEquals(5, flattened.size());
    }

    @Test
    public void testShallowRelation()
    {
        final Relation shallowRelation = this.rule.getAtlas().relation(6);
        logger.info("Shallow (1-node) relation: {}", shallowRelation);
        final Set<AtlasObject> flattened = shallowRelation.flatten();
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
