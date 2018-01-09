package org.openstreetmap.atlas.geography.atlas.pbf;

import static org.junit.Assert.assertEquals;

import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.geography.Latitude;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Longitude;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * Tests OSM Node to Atlas {@link Node} and {@link Point} conversion given various use cases.
 *
 * @author mgostintsev
 */
public class OsmPbfNodeToAtlasItemTest
{
    private static final Location OSM_NODE_LOCATION = new Location(Latitude.degrees(7.014003),
            Longitude.degrees(-10.5466545));

    @Rule
    public final OsmPbfNodeToAtlasItemTestRule setup = new OsmPbfNodeToAtlasItemTestRule();

    @Test
    public void testOsmNodeNotInRelationNoTagsAtIntersection()
    {
        // Two OSM Ways intersecting at an OSM Node, that has no tags. We expect two Atlas Nodes
        // created at the end points of each way and an Atlas Node at the intersection.
        final Atlas atlas = this.setup.getNoRelationNoTagsAtIntersectionAtlas();
        final Iterable<Node> nodes = atlas.nodesAt(OSM_NODE_LOCATION);
        final Iterable<Point> points = atlas.pointsAt(OSM_NODE_LOCATION);

        // Verify a Node was created at the middle location
        assertEquals(1, Iterables.size(nodes));
        assertEquals(0, Iterables.size(points));

        // Verify nodes created at both end points and middle
        assertEquals(5, Iterables.size(atlas.nodes()));
    }

    @Test
    public void testOsmNodeNotInRelationNoTagsNotAtIntersection()
    {
        // Single OSM Way with an OSM Node in the middle without tags. We expect two Atlas Nodes
        // created at the end points with a shape point in the middle.
        final Atlas atlas = this.setup.getNoRelationNoTagsNoIntersectionAtlas();
        final Iterable<Node> nodes = atlas.nodesAt(OSM_NODE_LOCATION);
        final Iterable<Point> points = atlas.pointsAt(OSM_NODE_LOCATION);

        // Verify nothing was created at the middle location
        assertEquals(0, Iterables.size(nodes));
        assertEquals(0, Iterables.size(points));

        // Verify nodes created at end points
        assertEquals(2, Iterables.size(atlas.nodes()));
    }

    @Test
    public void testOsmNodeNotInRelationWithTagsAtIntersection()
    {
        // Two OSM Ways intersecting at an OSM node, that has tags. We expect two Atlas Nodes
        // created at the end points of each line and both an Atlas Node and an Atlas Point at the
        // intersection.
        final Atlas atlas = this.setup.getNoRelationWithTagsAtIntersectionAtlas();
        final Iterable<Node> nodes = atlas.nodesAt(OSM_NODE_LOCATION);
        final Iterable<Point> points = atlas.pointsAt(OSM_NODE_LOCATION);

        // Verify a Node was created at the middle location
        assertEquals(1, Iterables.size(nodes));
        assertEquals(1, Iterables.size(points));

        // Verify nodes created at both end points and middle
        assertEquals(5, Iterables.size(atlas.nodes()));
    }

    @Test
    public void testOsmNodeNotInRelationWithTagsNotAtIntersection()
    {
        // Single OSM Way with an OSM Node in the middle with tags. We expect two Atlas Nodes
        // created at the end points and an Atlas Point in the middle.
        final Atlas atlas = this.setup.getNoRelationWithTagsNoIntersectionAtlas();
        final Iterable<Node> nodes = atlas.nodesAt(OSM_NODE_LOCATION);
        final Iterable<Point> points = atlas.pointsAt(OSM_NODE_LOCATION);

        // Verify a Point was created at the middle location
        assertEquals(0, Iterables.size(nodes));
        assertEquals(1, Iterables.size(points));

        // Verify nodes created at end points
        assertEquals(2, Iterables.size(atlas.nodes()));
    }

    @Test
    public void testOsmNodePartOfRelationNoTagsAtIntersection()
    {
        // Two OSM Ways meeting at an OSM node with no tags. The two ways are part of a no u-turn
        // restriction with the OSM Node as the Via role. We expect two Atlas Nodes
        // created at the end points and a Node in the middle.
        final Atlas atlas = this.setup.getPartOfRelationNoTagsAtIntersectionAtlas();
        final Iterable<Node> nodes = atlas.nodesAt(OSM_NODE_LOCATION);
        final Iterable<Point> points = atlas.pointsAt(OSM_NODE_LOCATION);

        // Verify a Node was created at the middle location
        assertEquals(1, Iterables.size(nodes));
        assertEquals(0, Iterables.size(points));

        // Verify nodes created at end points and middle
        assertEquals(3, Iterables.size(atlas.nodes()));
    }

    @Test
    public void testOsmNodePartOfRelationNoTagsNotAtIntersection()
    {
        // Three OSM Nodes are part of a Relation. Two of them have tagging, and the middle one
        // doesn't. We expect all 3 to show up as Atlas Points.
        final Atlas atlas = this.setup.getPartOfRelationNoTagsNoIntersectionAtlas();
        final Iterable<Node> nodes = atlas.nodesAt(OSM_NODE_LOCATION);
        final Iterable<Point> points = atlas.pointsAt(OSM_NODE_LOCATION);

        // Verify a single Point was created at the untagged location
        assertEquals(0, Iterables.size(nodes));
        assertEquals(1, Iterables.size(points));

        // Verify only Points were created
        assertEquals(3, Iterables.size(atlas.points()));
        assertEquals(0, Iterables.size(atlas.nodes()));
    }

    @Test
    public void testOsmNodePartOfRelationWithTagsAtIntersection()
    {
        // Two OSM Ways meeting at an OSM node with barrier tags. The two ways are part of a no
        // u-turn restriction with the OSM Node as the Via role. We expect two Atlas Nodes
        // created at the end points and an Atlas Node in the middle.
        final Atlas atlas = this.setup.getPartOfRelationWithTagsAtIntersectionAtlas();
        final Iterable<Node> nodes = atlas.nodesAt(OSM_NODE_LOCATION);
        final Iterable<Point> points = atlas.pointsAt(OSM_NODE_LOCATION);

        // Verify a Node was created at the middle location
        assertEquals(1, Iterables.size(nodes));
        assertEquals(1, Iterables.size(points));

        // Verify nodes created at end points and middle
        assertEquals(3, Iterables.size(atlas.nodes()));
    }

    @Test
    public void testOsmNodePartOfRelationWithTagsNotAtIntersection()
    {
        // A single OSM Way and a single OSM Node (with tags), with a relation associating the Node
        // as a house to the Way as the Street. We expect two Atlas Nodes created at the end points
        // of the Way and an Atlas Point to represent the house.
        final Atlas atlas = this.setup.getPartOfRelationWithTagsNoIntersectionAtlas();
        final Iterable<Node> nodes = atlas.nodesAt(OSM_NODE_LOCATION);
        final Iterable<Point> points = atlas.pointsAt(OSM_NODE_LOCATION);

        // Verify a Point was created at the house location
        assertEquals(0, Iterables.size(nodes));
        assertEquals(1, Iterables.size(points));

        // Verify nodes created at end points and middle
        assertEquals(2, Iterables.size(atlas.nodes()));
    }
}
