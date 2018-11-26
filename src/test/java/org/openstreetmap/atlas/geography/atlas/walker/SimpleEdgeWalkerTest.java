package org.openstreetmap.atlas.geography.atlas.walker;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * Unit tests for {@link SimpleEdgeWalker}.
 *
 * @author bbreithaupt
 */
public class SimpleEdgeWalkerTest
{
    @Rule
    public SimpleEdgeWalkerTestRule setup = new SimpleEdgeWalkerTestRule();
    private static final long FIRST_EDGE_IDENTIFIER = 1000000001L;

    @Test
    public void collectAllTest()
    {
        Assert.assertEquals(3,
                new SimpleEdgeWalker(
                        setup.motorwayPrimaryTriangleAtlas().edge(FIRST_EDGE_IDENTIFIER),
                        edge -> edge.outEdges().stream()).collectEdges().size());
    }

    @Test
    public void collectPrimaryQueueAllTest()
    {
        Assert.assertEquals(2,
                new SimpleEdgeWalker(
                        setup.motorwayPrimaryTriangleAtlas().edge(FIRST_EDGE_IDENTIFIER),
                        connectedEdge -> Validators.isOfType(connectedEdge, HighwayTag.class,
                                HighwayTag.PRIMARY),
                        edge -> edge.outEdges().stream()).collectEdges().size());
    }
}
