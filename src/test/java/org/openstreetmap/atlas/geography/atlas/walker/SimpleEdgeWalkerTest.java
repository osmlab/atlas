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
    private static final long FIRST_EDGE_IDENTIFIER = 1000000001L;
    @Rule
    public SimpleEdgeWalkerTestRule setup = new SimpleEdgeWalkerTestRule();

    @Test
    public void collectAllTest()
    {
        Assert.assertEquals(3,
                new SimpleEdgeWalker(
                        this.setup.motorwayPrimaryTriangleAtlas().edge(FIRST_EDGE_IDENTIFIER),
                        edge -> edge.outEdges().stream()).collectEdges().size());
    }

    @Test
    public void collectPrimaryQueueAllTest()
    {
        Assert.assertEquals(2,
                new SimpleEdgeWalker(
                        this.setup.motorwayPrimaryTriangleAtlas().edge(FIRST_EDGE_IDENTIFIER),
                        connectedEdge -> Validators.isOfType(connectedEdge, HighwayTag.class,
                                HighwayTag.PRIMARY),
                        edge -> edge.outEdges().stream()).collectEdges().size());
    }
}
