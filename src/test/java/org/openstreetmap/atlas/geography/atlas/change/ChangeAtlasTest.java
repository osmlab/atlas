package org.openstreetmap.atlas.geography.atlas.change;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedEdge;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedNode;
import org.openstreetmap.atlas.geography.atlas.change.rule.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.rule.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * @author matthieun
 */
public class ChangeAtlasTest
{
    private static final Location NEW_NODE_LOCATION = Location.forString("37.592796,-122.2457961");

    @Rule
    public ChangeAtlasTestRule rule = new ChangeAtlasTestRule();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testModifyEdgeAndNode()
    {
        final Atlas atlas = this.rule.getAtlasEdge();
        final ChangeBuilder changeBuilder = new ChangeBuilder();

        final Tuple<FeatureChange, FeatureChange> featureChange1 = getFeatureChange1();
        changeBuilder.add(featureChange1.getFirst());
        changeBuilder.add(featureChange1.getSecond());

        changeBuilder.add(getFeatureChange2());

        // Feature change 2: Update the

        final Change change = changeBuilder.get();
        System.out.println(new ChangeAtlas(atlas, change).edge(39001000001L));
    }

    @Test
    public void testModifyEdgeWithoutStartNode()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("does not match with its start Node");

        final Atlas atlas = this.rule.getAtlasEdge();
        final ChangeBuilder changeBuilder = new ChangeBuilder();

        final Tuple<FeatureChange, FeatureChange> featureChange1 = getFeatureChange1();
        changeBuilder.add(featureChange1.getFirst());
        changeBuilder.add(featureChange1.getSecond());

        final Change change = changeBuilder.get();
        new ChangeAtlas(atlas, change);
    }

    /**
     * @return Feature change 1: Update the first location in the edge 39001000001L's polyLine
     */
    private Tuple<FeatureChange, FeatureChange> getFeatureChange1()
    {
        final Atlas atlas = this.rule.getAtlasEdge();

        final Edge originalEdge1 = atlas.edge(39001000001L);
        final Edge originalEdge1Reverse = atlas.edge(-39001000001L);

        // Forward:
        final PolyLine originalPolyLine1 = originalEdge1.asPolyLine();
        final PolyLine originalPolyLine1Modified = new PolyLine(originalPolyLine1
                .prepend(new PolyLine(NEW_NODE_LOCATION, originalPolyLine1.first())));
        final BloatedEdge bloatedEdge1 = BloatedEdge.fromEdge(originalEdge1)
                .withPolyLine(originalPolyLine1Modified);
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD, bloatedEdge1);

        // Backward
        final PolyLine originalPolyLine1Reverse = originalEdge1Reverse.asPolyLine();
        final PolyLine originalPolyLine1ModifiedReverse = new PolyLine(originalPolyLine1Reverse
                .append(new PolyLine(originalPolyLine1Reverse.last(), NEW_NODE_LOCATION)));
        final BloatedEdge bloatedEdge1Reverse = BloatedEdge.fromEdge(originalEdge1Reverse)
                .withPolyLine(originalPolyLine1ModifiedReverse);
        final FeatureChange featureChange1Reverse = new FeatureChange(ChangeType.ADD,
                bloatedEdge1Reverse);

        return new Tuple<>(featureChange1, featureChange1Reverse);
    }

    /**
     * @return Feature change 2: Update the edge 39001000001L's start node: 38999000000L
     */
    private FeatureChange getFeatureChange2()
    {
        final Atlas atlas = this.rule.getAtlasEdge();

        final Node originalNode = atlas.node(38999000000L);
        final BloatedNode bloatedNode = BloatedNode.fromNode(originalNode)
                .withLocation(NEW_NODE_LOCATION);
        return new FeatureChange(ChangeType.ADD, bloatedNode);
    }
}
