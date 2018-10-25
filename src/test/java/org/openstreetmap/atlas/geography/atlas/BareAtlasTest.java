package org.openstreetmap.atlas.geography.atlas;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.items.Area;
import org.openstreetmap.atlas.geography.atlas.items.Edge;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
import org.openstreetmap.atlas.geography.atlas.items.Line;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Point;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.utilities.collections.Iterables;

/**
 * @author matthieun
 */
public class BareAtlasTest
{
    @Rule
    public final BareAtlasTestRule rule = new BareAtlasTestRule();

    @Rule
    public ExpectedException testGetEntitiesWithWrongTypeSpecifiedException = ExpectedException
            .none();

    @Test
    public void testGetEntitiesWithTypeSpecified()
    {
        final Atlas atlas = this.rule.getAtlas();

        final Set<Node> nodes = Iterables.stream(atlas.nodes()).collectToSet();
        final Set<Edge> edges = Iterables.stream(atlas.edges()).collectToSet();
        final Set<Area> areas = Iterables.stream(atlas.areas()).collectToSet();
        final Set<Line> lines = Iterables.stream(atlas.lines()).collectToSet();
        final Set<Point> points = Iterables.stream(atlas.points()).collectToSet();
        final Set<Relation> relations = Iterables.stream(atlas.relations()).collectToSet();

        final Set<Node> nodes2 = Iterables.stream(atlas.entities(ItemType.NODE, Node.class))
                .collectToSet();
        final Set<Edge> edges2 = Iterables.stream(atlas.entities(ItemType.EDGE, Edge.class))
                .collectToSet();
        final Set<Area> areas2 = Iterables.stream(atlas.entities(ItemType.AREA, Area.class))
                .collectToSet();
        final Set<Line> lines2 = Iterables.stream(atlas.entities(ItemType.LINE, Line.class))
                .collectToSet();
        final Set<Point> points2 = Iterables.stream(atlas.entities(ItemType.POINT, Point.class))
                .collectToSet();
        final Set<Relation> relations2 = Iterables
                .stream(atlas.entities(ItemType.RELATION, Relation.class)).collectToSet();

        Assert.assertEquals(nodes, nodes2);
        Assert.assertEquals(edges, edges2);
        Assert.assertEquals(areas, areas2);
        Assert.assertEquals(lines, lines2);
        Assert.assertEquals(points, points2);
        Assert.assertEquals(relations, relations2);
    }

    @Test
    public void testGetEntitiesWithWrongTypeSpecified()
    {
        this.testGetEntitiesWithWrongTypeSpecifiedException.expect(CoreException.class);
        this.testGetEntitiesWithWrongTypeSpecifiedException.expectMessage("do not match!");
        final Atlas atlas = this.rule.getAtlas();
        atlas.entities(ItemType.NODE, Relation.class);
    }

    @Test
    public void testRelationsOrder()
    {
        final Atlas atlas = this.rule.getAtlas();
        final List<Long> expectedRelationIdentifiers = new ArrayList<>();
        expectedRelationIdentifiers.add(1L);
        expectedRelationIdentifiers.add(2L);
        expectedRelationIdentifiers.add(3L);
        expectedRelationIdentifiers.add(5L);
        expectedRelationIdentifiers.add(4L);
        Assert.assertEquals(expectedRelationIdentifiers,
                Iterables.stream(atlas.relationsLowerOrderFirst())
                        .map(relation -> relation.getIdentifier()).collectToList());
    }
}
