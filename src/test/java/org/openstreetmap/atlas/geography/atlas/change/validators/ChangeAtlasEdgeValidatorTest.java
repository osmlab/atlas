package org.openstreetmap.atlas.geography.atlas.change.validators;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedEdge;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedNode;
import org.openstreetmap.atlas.geography.atlas.change.Change;
import org.openstreetmap.atlas.geography.atlas.change.ChangeAtlas;
import org.openstreetmap.atlas.geography.atlas.change.ChangeBuilder;
import org.openstreetmap.atlas.geography.atlas.change.rule.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.rule.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

/**
 * @author matthieun
 */
public class ChangeAtlasEdgeValidatorTest
{
    private static final Location NEW_LOCATION = Location.forString("37.592796,-122.2457961");

    @Rule
    public ChangeAtlasEdgeValidatorTestRule rule = new ChangeAtlasEdgeValidatorTestRule();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testModifyEdgeAndNode()
    {
        final Atlas atlas = this.rule.getAtlasEdge();
        final ChangeBuilder changeBuilder = new ChangeBuilder();

        final Tuple<FeatureChange, FeatureChange> featureChange1 = getFeatureChangeUpdatedEdgePolyLine();
        changeBuilder.add(featureChange1.getFirst());
        changeBuilder.add(featureChange1.getSecond());

        changeBuilder.add(getFeatureChangeMovedNode());

        final Change change = changeBuilder.get();
        Assert.assertEquals("[Edge: id=39001000001, startNode=38999000000, endNode=39002000000, "
                + "polyLine=LINESTRING (-122.2457961 37.592796, -122.2450237 37.5926929, "
                + "-122.2441049 37.5930666, -122.2429584 37.5926993), "
                + "[Tags: [last_edit_user_name => myself], [last_edit_changeset => 1], "
                + "[last_edit_time => 1513719782000], [last_edit_user_id => 1], [name => primary], "
                + "[highway => primary], [last_edit_version => 1]]]",
                new ChangeAtlas(atlas, change).edge(39001000001L).toString());
    }

    @Test
    public void testModifyEdgeWithoutReverseEdge()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("have mismatching PolyLines");

        final Atlas atlas = this.rule.getAtlasEdge();
        final ChangeBuilder changeBuilder = new ChangeBuilder();

        final Edge edge = atlas.edge(39001000001L);
        final PolyLine oldPolyLine = edge.asPolyLine();
        final PolyLine newPolyLine = new PolyLine(oldPolyLine.first(), NEW_LOCATION,
                oldPolyLine.last());
        final BloatedEdge bloatedEdge = BloatedEdge.fromEdge(edge).withPolyLine(newPolyLine);
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD, bloatedEdge);
        changeBuilder.add(featureChange);

        final Change change = changeBuilder.get();
        new ChangeAtlas(atlas, change);
    }

    @Test
    public void testModifyEdgeWithoutStartNode()
    {
        this.expectedException.expect(CoreException.class);
        this.expectedException.expectMessage("does not match with its start Node");

        final Atlas atlas = this.rule.getAtlasEdge();
        final ChangeBuilder changeBuilder = new ChangeBuilder();

        final Tuple<FeatureChange, FeatureChange> featureChange1 = getFeatureChangeUpdatedEdgePolyLine();
        changeBuilder.add(featureChange1.getFirst());
        changeBuilder.add(featureChange1.getSecond());

        final Change change = changeBuilder.get();
        new ChangeAtlas(atlas, change);
    }

    /**
     * @return Feature change 2: Update the edge 39001000001L's start node: 38999000000L
     */
    private FeatureChange getFeatureChangeMovedNode()
    {
        final Atlas atlas = this.rule.getAtlasEdge();

        final Node originalNode = atlas.node(38999000000L);
        final BloatedNode bloatedNode = BloatedNode.fromNode(originalNode)
                .withLocation(NEW_LOCATION);
        return new FeatureChange(ChangeType.ADD, bloatedNode);
    }

    /**
     * @return Feature change 1: Update the first location in the edge 39001000001L's polyLine
     */
    private Tuple<FeatureChange, FeatureChange> getFeatureChangeUpdatedEdgePolyLine()
    {
        final Atlas atlas = this.rule.getAtlasEdge();

        final Edge originalEdge1 = atlas.edge(39001000001L);
        final Edge originalEdge1Reverse = atlas.edge(-39001000001L);

        // Forward:
        final PolyLine originalPolyLine1 = originalEdge1.asPolyLine();
        final PolyLine originalPolyLine1Modified = new PolyLine(originalPolyLine1
                .prepend(new PolyLine(NEW_LOCATION, originalPolyLine1.first())));
        final BloatedEdge bloatedEdge1 = BloatedEdge.fromEdge(originalEdge1)
                .withPolyLine(originalPolyLine1Modified);
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD, bloatedEdge1);

        // Backward
        final PolyLine originalPolyLine1Reverse = originalEdge1Reverse.asPolyLine();
        final PolyLine originalPolyLine1ModifiedReverse = new PolyLine(originalPolyLine1Reverse
                .append(new PolyLine(originalPolyLine1Reverse.last(), NEW_LOCATION)));
        final BloatedEdge bloatedEdge1Reverse = BloatedEdge.fromEdge(originalEdge1Reverse)
                .withPolyLine(originalPolyLine1ModifiedReverse);
        final FeatureChange featureChange1Reverse = new FeatureChange(ChangeType.ADD,
                bloatedEdge1Reverse);

        return new Tuple<>(featureChange1, featureChange1Reverse);
    }
}
