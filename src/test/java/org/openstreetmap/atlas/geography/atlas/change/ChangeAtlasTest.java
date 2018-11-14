package org.openstreetmap.atlas.geography.atlas.change;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedEdge;
import org.openstreetmap.atlas.geography.atlas.change.rule.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.rule.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.items.Edge;

/**
 * @author matthieun
 */
public class ChangeAtlasTest
{
    @Rule
    public ChangeAtlasTestRule rule = new ChangeAtlasTestRule();

    @Test
    public void testEdge()
    {
        final Atlas atlas = this.rule.getAtlasEdge();
        final ChangeBuilder changeBuilder = new ChangeBuilder();

        // Feature change 1: Update the first node in the polyLine
        final Edge originalEdge1 = atlas.edge(39001000001L);
        final Edge originalEdge1Reverse = atlas.edge(-39001000001L);

        // Forward:
        final PolyLine originalPolyLine1 = originalEdge1.asPolyLine();
        final Location prependLocation = Location.forString("37.592796,-122.2457961");
        final PolyLine originalPolyLine1Modified = new PolyLine(originalPolyLine1
                .prepend(new PolyLine(prependLocation, originalPolyLine1.first())));
        final BloatedEdge bloatedEdge1 = BloatedEdge.fromEdge(originalEdge1)
                .withPolyLine(originalPolyLine1Modified);
        final FeatureChange featureChange1 = new FeatureChange(ChangeType.ADD, bloatedEdge1);
        changeBuilder.add(featureChange1);

        // Backward
        final PolyLine originalPolyLine1Reverse = originalEdge1Reverse.asPolyLine();
        final PolyLine originalPolyLine1ModifiedReverse = new PolyLine(originalPolyLine1Reverse
                .append(new PolyLine(originalPolyLine1Reverse.last(), prependLocation)));
        final BloatedEdge bloatedEdge1Reverse = BloatedEdge.fromEdge(originalEdge1Reverse)
                .withPolyLine(originalPolyLine1ModifiedReverse);
        final FeatureChange featureChange1Reverse = new FeatureChange(ChangeType.ADD,
                bloatedEdge1Reverse);
        changeBuilder.add(featureChange1Reverse);

        final Change change = changeBuilder.get();
        System.out.println(new ChangeAtlas(atlas, change).edge(39001000001L));
    }
}
