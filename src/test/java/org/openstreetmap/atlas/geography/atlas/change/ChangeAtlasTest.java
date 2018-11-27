package org.openstreetmap.atlas.geography.atlas.change;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Heading;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedEdge;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedNode;
import org.openstreetmap.atlas.geography.atlas.bloated.BloatedPoint;
import org.openstreetmap.atlas.geography.atlas.change.rule.ChangeType;
import org.openstreetmap.atlas.geography.atlas.change.rule.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.utilities.scalars.Distance;

/**
 * @author matthieun
 */
public class ChangeAtlasTest
{
    @Rule
    public ChangeAtlasTestRule rule = new ChangeAtlasTestRule();

    @Test
    public void testBounds()
    {
        final Atlas atlas = this.rule.getAtlas();
        Assert.assertEquals("POLYGON ((-122.2450237 37.5920679, -122.2450237 37.5938783, "
                + "-122.2412753 37.5938783, -122.2412753 37.5920679, -122.2450237 37.5920679))",
                atlas.bounds().toWkt());

        final ChangeBuilder changeBuilder = new ChangeBuilder();
        // One-way motorway
        final Edge source = atlas.edge(39004000002L);
        final PolyLine newPolyLine = source.asPolyLine().shiftLastAlongGreatCircle(Heading.NORTH,
                Distance.ONE_METER);
        final Location end = newPolyLine.last();
        final FeatureChange featureChange = new FeatureChange(ChangeType.ADD,
                BloatedEdge.shallowFromEdge(source).withPolyLine(newPolyLine));
        changeBuilder.add(featureChange);
        changeBuilder.add(new FeatureChange(ChangeType.ADD,
                BloatedNode.shallowFromNode(atlas.node(38990000000L)).withLocation(end)));
        final Change change = changeBuilder.get();

        final Atlas changeAtlas = new ChangeAtlas(atlas, change);
        Assert.assertEquals("POLYGON ((-122.2450237 37.5920679, -122.2450237 37.5938873, "
                + "-122.2412753 37.5938873, -122.2412753 37.5920679, -122.2450237 37.5920679))",
                changeAtlas.bounds().toWkt());
    }

    @Test
    public void testPoint()
    {
        final Atlas atlas = this.rule.getAtlas();
        final ChangeBuilder changeBuilder = new ChangeBuilder();
        final Point source = atlas.point(41822000000L);
        final Location newLocation = source.getLocation().shiftAlongGreatCircle(Heading.NORTH,
                Distance.ONE_METER);
        changeBuilder.add(new FeatureChange(ChangeType.ADD,
                BloatedPoint.shallowFromPoint(source).withLocation(newLocation)));
        final Change change = changeBuilder.get();

        final Atlas changeAtlas = new ChangeAtlas(atlas, change);
        Assert.assertEquals(newLocation, changeAtlas.point(41822000000L).getLocation());
    }
}
