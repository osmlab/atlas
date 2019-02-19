package org.openstreetmap.atlas.geography.atlas;

import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.delta.AtlasDelta;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;
import org.openstreetmap.atlas.geography.atlas.items.Node;
import org.openstreetmap.atlas.geography.atlas.items.Relation;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.atlas.sub.AtlasCutType;
import org.openstreetmap.atlas.tags.HighwayTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;

/**
 * @author matthieun
 * @author mgostintsev
 */
public class SubAtlasTest
{
    @Rule
    public final SubAtlasRule rule = new SubAtlasRule();

    @Test
    public void testFilteredOutSubRelation()
    {
        final Atlas source = this.rule.getFilteredOutMemberRelationAtlas();
        final Predicate<AtlasEntity> filteredOutPredicate = entity -> !"excluded"
                .equals(entity.getTag("type").orElse(""));
        final Atlas filteredOutSubRelationAtlas = source
                .subAtlas(filteredOutPredicate, AtlasCutType.SOFT_CUT)
                .orElseThrow(() -> new CoreException("SubAtlas was not present."));

        Assert.assertNull(filteredOutSubRelationAtlas.node(5));
        Assert.assertNotNull(filteredOutSubRelationAtlas.node(3));

        Assert.assertNotNull(filteredOutSubRelationAtlas.node(4));
        Assert.assertNotNull(filteredOutSubRelationAtlas.relation(8));
    }

    @Test
    public void testSubAtlasHardCutRelationsWithPolygon()
    {
        final Atlas source = this.rule.getAtlas();
        // This Rectangle covers only the Node 1, Node 4, Edge 2, Point 0, and Relation 6 made up of
        // Edge 4 and Node 2
        final Atlas sub = source
                .subAtlas(
                        Rectangle.forCorners(Location.forString("37.780400, -122.473149"),
                                Location.forString("37.780785, -122.472631")),
                        AtlasCutType.HARD_CUT_RELATIONS_ONLY)
                .orElseThrow(() -> new CoreException("SubAtlas was not present."));

        // The sub-atlas should be a combination of results from a soft-cut for all AtlasItems and a
        // hard-cut for all Relations

        // Nodes
        Assert.assertNotNull(source.node(1));
        Assert.assertNotNull(sub.node(1));
        Assert.assertNotNull(source.node(2));
        Assert.assertNotNull(sub.node(2));
        Assert.assertNotNull(source.node(3));
        Assert.assertNull(sub.node(3));

        // Edges
        Assert.assertNotNull(source.edge(0));
        Assert.assertNotNull(sub.edge(0));
        Assert.assertNotNull(source.edge(1));
        Assert.assertNull(sub.edge(1));

        // Areas
        Assert.assertNotNull(source.area(0));
        Assert.assertNotNull(sub.area(0));
        Assert.assertNotNull(source.area(1));
        Assert.assertNull(sub.area(1));

        // Lines
        Assert.assertNotNull(source.line(0));
        Assert.assertNotNull(sub.line(0));
        Assert.assertNotNull(source.line(1));
        Assert.assertNull(sub.line(1));

        // Points
        Assert.assertNotNull(source.point(0));
        Assert.assertNotNull(sub.point(0));
        Assert.assertNotNull(source.point(1));
        Assert.assertNull(sub.point(1));
        Assert.assertNotNull(source.point(2));
        Assert.assertNull(sub.point(2));
        Assert.assertNotNull(source.point(3));
        Assert.assertNull(sub.point(3));

        // Relations
        Assert.assertNotNull(source.relation(1));
        Assert.assertNull(sub.relation(1));
        Assert.assertNotNull(source.relation(2));
        Assert.assertNull(sub.relation(2));
        Assert.assertNotNull(source.relation(3));
        Assert.assertNull(sub.relation(3));
        Assert.assertNotNull(source.relation(4));
        Assert.assertNull(sub.relation(4));
        Assert.assertNotNull(source.relation(5));
        Assert.assertNull(sub.relation(5));
        Assert.assertNotNull(source.relation(6));
        Assert.assertEquals(2, source.relation(6).members().size());
        Assert.assertNotNull(sub.relation(6));
        Assert.assertEquals(2, sub.relation(6).members().size());
        Assert.assertNotNull(source.relation(7));
        Assert.assertEquals(2, source.relation(7).members().size());
        Assert.assertNotNull(sub.relation(7));
        Assert.assertEquals(1, sub.relation(7).members().size());
    }

    @Test
    public void testSubAtlasHardCutWithPolygon()
    {
        final Atlas source = this.rule.getAtlas();
        // This Rectangle covers only the Node 1, Node 4, Edge 2, Point 0, and Relation 6 made up of
        // Edge 4 and Node 2
        final Atlas sub = source
                .subAtlas(
                        Rectangle.forCorners(Location.forString("37.780400, -122.473149"),
                                Location.forString("37.780785, -122.472631")),
                        AtlasCutType.HARD_CUT_ALL)
                .orElseThrow(() -> new CoreException("SubAtlas was not present."));

        // Nodes
        Assert.assertNotNull(source.node(1));
        Assert.assertNotNull(sub.node(1));
        Assert.assertNotNull(source.node(2));
        Assert.assertNull(sub.node(2));
        Assert.assertNotNull(source.node(3));
        Assert.assertNull(sub.node(3));
        Assert.assertNotNull(source.node(4));
        Assert.assertNotNull(sub.node(4));

        // Edges
        Assert.assertNotNull(source.edge(0));
        Assert.assertNull(sub.edge(0));
        Assert.assertNotNull(source.edge(1));
        Assert.assertNull(sub.edge(1));
        Assert.assertNotNull(source.edge(2));
        Assert.assertNotNull(sub.edge(2));

        // Areas
        Assert.assertNotNull(source.area(0));
        Assert.assertNull(sub.area(0));
        Assert.assertNotNull(source.area(1));
        Assert.assertNull(sub.area(1));

        // Lines
        Assert.assertNotNull(source.line(0));
        Assert.assertNull(sub.line(0));
        Assert.assertNotNull(source.line(1));
        Assert.assertNull(sub.line(1));

        // Points
        Assert.assertNotNull(source.point(0));
        Assert.assertNotNull(sub.point(0));
        Assert.assertNotNull(source.point(1));
        Assert.assertNull(sub.point(1));
        Assert.assertNotNull(source.point(2));
        Assert.assertNull(sub.point(2));
        Assert.assertNotNull(source.point(3));
        Assert.assertNull(sub.point(3));

        // Relations
        Assert.assertNotNull(source.relation(1));
        Assert.assertNull(sub.relation(1));
        Assert.assertNotNull(source.relation(2));
        Assert.assertNull(sub.relation(2));
        Assert.assertNotNull(source.relation(3));
        Assert.assertNull(sub.relation(3));
        Assert.assertNotNull(source.relation(4));
        Assert.assertNull(sub.relation(4));
        Assert.assertNotNull(source.relation(5));
        Assert.assertNull(sub.relation(5));
        Assert.assertNotNull(source.relation(6));
        Assert.assertEquals(2, source.relation(6).members().size());
        Assert.assertNotNull(sub.relation(6));
        Assert.assertEquals(2, sub.relation(6).members().size());
        Assert.assertNotNull(source.relation(7));
        Assert.assertEquals(2, source.relation(7).members().size());
        Assert.assertNotNull(sub.relation(7));
        Assert.assertEquals(1, sub.relation(7).members().size());
    }

    @Test
    public void testSubAtlasPredicateHardCut()
    {
        final Atlas source = this.rule.getHardCutPredicateAtlas();
        final Predicate<AtlasEntity> filteredOutPredicate = entity -> entity instanceof Relation
                || entity instanceof Node
                || Validators.isOfType(entity, HighwayTag.class, HighwayTag.RESIDENTIAL);
        final Atlas filtered = source.subAtlas(filteredOutPredicate, AtlasCutType.HARD_CUT_ALL)
                .orElseThrow(() -> new CoreException("SubAtlas was not present."));

        // Verify counts
        Assert.assertEquals("Two connected edges got filtered out, so there should be 3 less nodes",
                source.numberOfNodes() - 3, filtered.numberOfNodes());
        Assert.assertEquals("Two non-residential edges got filtered out",
                source.numberOfEdges() - 2, filtered.numberOfEdges());
        Assert.assertEquals("There should not be any areas left", 0, filtered.numberOfAreas());
        Assert.assertEquals("There should not be any points left", 0, filtered.numberOfPoints());
        Assert.assertEquals("One relation should have gotten removed due to empty members", 1,
                filtered.numberOfRelations());

        // Verify filtered entities
        Assert.assertNull(filtered.edge(2));
        Assert.assertNull(filtered.edge(3));
        Assert.assertNull(filtered.node(4));
        Assert.assertNull(filtered.node(5));
        Assert.assertNull(filtered.node(6));
        Assert.assertNull(filtered.relation(2));
        Assert.assertNotNull(filtered.relation(1));
    }

    @Test
    public void testSubAtlasPredicateHardCutRelationsOnly()
    {
        final Atlas source = this.rule.getHardCutPredicateAtlas();
        final Predicate<AtlasEntity> filteredOutPredicate = entity -> entity instanceof Relation
                || entity instanceof Node
                || Validators.isOfType(entity, HighwayTag.class, HighwayTag.RESIDENTIAL);
        final Atlas filtered = source
                .subAtlas(filteredOutPredicate, AtlasCutType.HARD_CUT_RELATIONS_ONLY)
                .orElseThrow(() -> new CoreException("SubAtlas was not present."));

        // Verify counts
        Assert.assertEquals("No nodes should be filtered out", source.numberOfNodes(),
                filtered.numberOfNodes());
        Assert.assertEquals("Two non-residential edges got filtered out",
                source.numberOfEdges() - 2, filtered.numberOfEdges());
        Assert.assertEquals("There should not be any areas left", 0, filtered.numberOfAreas());
        Assert.assertEquals("There should not be any points left", 0, filtered.numberOfPoints());
        Assert.assertEquals("One relation should have gotten removed due to empty members", 1,
                filtered.numberOfRelations());

        // Verify filtered entities
        Assert.assertNull(filtered.edge(2));
        Assert.assertNull(filtered.edge(3));
        Assert.assertNull(filtered.relation(2));
        Assert.assertNotNull(filtered.relation(1));
    }

    @Test
    public void testSubAtlasPredicateSoftCut()
    {
        final Atlas source = this.rule.getAtlas();

        // Should return back all entities in this atlas
        final Predicate<AtlasEntity> allEntities = (entity) -> entity.getIdentifier() > 0
                || entity.getIdentifier() < 0 || entity.getIdentifier() == 0;

        // Should return back only Entities with identifier 0
        final Predicate<AtlasEntity> entitiesWithIdentifierZero = (
                entity) -> entity.getIdentifier() == 0;

        final Atlas identicalSubAtlas = source.subAtlas(allEntities, AtlasCutType.SOFT_CUT)
                .orElseThrow(() -> new CoreException("SubAtlas was not present."));

        final Atlas subAtlasWithZeroBasedIdentifiers = source
                .subAtlas(entitiesWithIdentifierZero, AtlasCutType.SOFT_CUT)
                .orElseThrow(() -> new CoreException("SubAtlas was not present."));

        // Nodes
        Assert.assertNotNull(source.node(1));
        Assert.assertNotNull(identicalSubAtlas.node(1));
        Assert.assertNotNull(source.node(2));
        Assert.assertNotNull(identicalSubAtlas.node(2));
        Assert.assertNotNull(source.node(3));
        Assert.assertNotNull(identicalSubAtlas.node(3));

        // Edges
        Assert.assertNotNull(source.edge(0));
        Assert.assertNotNull(identicalSubAtlas.edge(0));
        Assert.assertNotNull(source.edge(1));
        Assert.assertNotNull(identicalSubAtlas.edge(1));

        // Areas
        Assert.assertNotNull(source.area(0));
        Assert.assertNotNull(identicalSubAtlas.area(0));
        Assert.assertNotNull(source.area(1));
        Assert.assertNotNull(identicalSubAtlas.area(1));

        // Lines
        Assert.assertNotNull(source.line(0));
        Assert.assertNotNull(identicalSubAtlas.line(0));
        Assert.assertNotNull(source.line(1));
        Assert.assertNotNull(identicalSubAtlas.line(1));

        // Points
        Assert.assertNotNull(source.point(0));
        Assert.assertNotNull(identicalSubAtlas.point(0));
        Assert.assertNotNull(source.point(1));
        Assert.assertNotNull(identicalSubAtlas.point(1));
        Assert.assertNotNull(source.point(2));
        Assert.assertNotNull(identicalSubAtlas.point(2));
        Assert.assertNotNull(source.point(3));
        Assert.assertNotNull(identicalSubAtlas.point(3));

        // Relations
        Assert.assertNotNull(source.relation(1));
        Assert.assertNotNull(identicalSubAtlas.relation(1));
        Assert.assertNotNull(source.relation(2));
        Assert.assertEquals(2, source.relation(2).members().size());
        Assert.assertNotNull(identicalSubAtlas.relation(2));
        Assert.assertEquals(2, identicalSubAtlas.relation(2).members().size());
        Assert.assertNotNull(source.relation(3));
        Assert.assertNotNull(identicalSubAtlas.relation(3));
        Assert.assertNotNull(source.relation(4));
        Assert.assertEquals(2, source.relation(4).members().size());
        Assert.assertNotNull(identicalSubAtlas);
        Assert.assertEquals(2, identicalSubAtlas.relation(4).members().size());
        Assert.assertNotNull(source.relation(5));
        Assert.assertEquals(1, source.relation(5).members().size());
        Assert.assertNotNull(identicalSubAtlas.relation(5));
        Assert.assertEquals(1, identicalSubAtlas.relation(5).members().size());

        // Nodes
        Assert.assertNotNull(source.node(1));
        // Node 1 gets pulled in by Edge 0
        Assert.assertNotNull(subAtlasWithZeroBasedIdentifiers.node(1));
        Assert.assertNotNull(source.node(2));
        // Node 2 gets pulled in by Edge 0
        Assert.assertNotNull(subAtlasWithZeroBasedIdentifiers.node(2));
        Assert.assertNotNull(source.node(3));
        Assert.assertNull(subAtlasWithZeroBasedIdentifiers.node(3));

        // Edges
        Assert.assertNotNull(source.edge(0));
        Assert.assertNotNull(subAtlasWithZeroBasedIdentifiers.edge(0));
        Assert.assertNotNull(source.edge(1));
        Assert.assertNull(subAtlasWithZeroBasedIdentifiers.edge(1));

        // Areas
        Assert.assertNotNull(source.area(0));
        Assert.assertNotNull(subAtlasWithZeroBasedIdentifiers.area(0));
        Assert.assertNotNull(source.area(1));
        Assert.assertNull(subAtlasWithZeroBasedIdentifiers.area(1));

        // Lines
        Assert.assertNotNull(source.line(0));
        Assert.assertNotNull(subAtlasWithZeroBasedIdentifiers.line(0));
        Assert.assertNotNull(source.line(1));
        Assert.assertNull(subAtlasWithZeroBasedIdentifiers.line(1));

        // Points
        Assert.assertNotNull(source.point(0));
        Assert.assertNotNull(subAtlasWithZeroBasedIdentifiers.point(0));
        Assert.assertNotNull(source.point(1));
        Assert.assertNull(subAtlasWithZeroBasedIdentifiers.point(1));
        Assert.assertNotNull(source.point(2));
        Assert.assertNull(subAtlasWithZeroBasedIdentifiers.point(2));
        Assert.assertNotNull(source.point(3));
        Assert.assertNull(subAtlasWithZeroBasedIdentifiers.point(3));

        // Relations
        Assert.assertNotNull(source.relation(1));
        Assert.assertNull(subAtlasWithZeroBasedIdentifiers.relation(1));
        Assert.assertNotNull(source.relation(2));
        Assert.assertNull(subAtlasWithZeroBasedIdentifiers.relation(2));
        Assert.assertNotNull(source.relation(3));
        Assert.assertNull(subAtlasWithZeroBasedIdentifiers.relation(3));
        Assert.assertNotNull(source.relation(4));
        Assert.assertNull(subAtlasWithZeroBasedIdentifiers.relation(4));
        Assert.assertNotNull(source.relation(5));
        Assert.assertNull(subAtlasWithZeroBasedIdentifiers.relation(5));
    }

    @Test
    public void testSubAtlasSilkCutWithPolygon()
    {
        final Atlas source = this.rule.getAtlas();
        // This Rectangle covers only the Node 1, Edge 0, Area 0, Line 0 and Point 0.
        final Atlas sub = source
                .subAtlas(
                        Rectangle.forCorners(Location.forString("37.780400, -122.473149"),
                                Location.forString("37.780785, -122.472631")),
                        AtlasCutType.SILK_CUT)
                .orElseThrow(() -> new CoreException("SubAtlas was not present."));
        // Nodes
        Assert.assertNotNull(source.node(1));
        Assert.assertNotNull(sub.node(1));
        Assert.assertNotNull(source.node(2));
        Assert.assertNotNull(sub.node(2));
        Assert.assertNotNull(source.node(3));
        Assert.assertNull(sub.node(3));

        // Edges
        Assert.assertNotNull(source.edge(0));
        Assert.assertNotNull(sub.edge(0));
        Assert.assertNotNull(source.edge(1));
        Assert.assertNull(sub.edge(1));

        // Areas
        Assert.assertNotNull(source.area(0));
        Assert.assertNotNull(sub.area(0));
        Assert.assertNotNull(source.area(1));
        Assert.assertNull(sub.area(1));

        // Lines
        Assert.assertNotNull(source.line(0));
        Assert.assertNotNull(sub.line(0));
        Assert.assertNotNull(source.line(1));
        Assert.assertNull(sub.line(1));

        // Check that points for line coordinates were preserved
        sub.lines().forEach(line ->
        {
            line.asPolyLine().forEach(location ->
            {
                source.pointsAt(location).forEach(point ->
                {
                    Assert.assertTrue(sub.point(point.getIdentifier()) != null);
                });
            });
        });

        // Points
        Assert.assertNotNull(source.point(0));
        Assert.assertNotNull(sub.point(0));
        Assert.assertNotNull(source.point(1));
        Assert.assertNotNull(sub.point(1));
        Assert.assertNotNull(source.point(2));
        Assert.assertNull(sub.point(2));
        Assert.assertNotNull(source.point(3));
        Assert.assertNull(sub.point(3));

        // Relations
        Assert.assertNotNull(source.relation(1));
        Assert.assertNotNull(sub.relation(1));
        Assert.assertNotNull(source.relation(2));
        Assert.assertEquals(2, source.relation(2).members().size());
        Assert.assertNotNull(sub.relation(2));
        Assert.assertEquals(1, sub.relation(2).members().size());
        Assert.assertNotNull(source.relation(3));
        Assert.assertNull(sub.relation(3));
        Assert.assertNotNull(source.relation(4));
        Assert.assertEquals(2, source.relation(4).members().size());
        Assert.assertNotNull(sub.relation(4));
        Assert.assertEquals(1, sub.relation(4).members().size());
        Assert.assertNotNull(source.relation(5));
        Assert.assertEquals(1, source.relation(5).members().size());
        Assert.assertNotNull(sub.relation(5));
        Assert.assertEquals(1, sub.relation(5).members().size());
    }

    @Test
    public void testSubAtlasSoftCutWithPolygon()
    {
        final Atlas source = this.rule.getAtlas();
        // This Rectangle covers only the Node 1, Edge 0, Area 0, Line 0 and Point 0.
        final Atlas sub = source
                .subAtlas(
                        Rectangle.forCorners(Location.forString("37.780400, -122.473149"),
                                Location.forString("37.780785, -122.472631")),
                        AtlasCutType.SOFT_CUT)
                .orElseThrow(() -> new CoreException("SubAtlas was not present."));
        // Nodes
        Assert.assertNotNull(source.node(1));
        Assert.assertNotNull(sub.node(1));
        Assert.assertNotNull(source.node(2));
        Assert.assertNotNull(sub.node(2));
        Assert.assertNotNull(source.node(3));
        Assert.assertNull(sub.node(3));

        // Edges
        Assert.assertNotNull(source.edge(0));
        Assert.assertNotNull(sub.edge(0));
        Assert.assertNotNull(source.edge(1));
        Assert.assertNull(sub.edge(1));

        // Areas
        Assert.assertNotNull(source.area(0));
        Assert.assertNotNull(sub.area(0));
        Assert.assertNotNull(source.area(1));
        Assert.assertNull(sub.area(1));

        // Lines
        Assert.assertNotNull(source.line(0));
        Assert.assertNotNull(sub.line(0));
        Assert.assertNotNull(source.line(1));
        Assert.assertNull(sub.line(1));

        // Points
        Assert.assertNotNull(source.point(0));
        Assert.assertNotNull(sub.point(0));
        Assert.assertNotNull(source.point(1));
        Assert.assertNull(sub.point(1));
        Assert.assertNotNull(source.point(2));
        Assert.assertNull(sub.point(2));
        Assert.assertNotNull(source.point(3));
        Assert.assertNull(sub.point(3));

        // Relations
        Assert.assertNotNull(source.relation(1));
        Assert.assertNotNull(sub.relation(1));
        Assert.assertNotNull(source.relation(2));
        Assert.assertEquals(2, source.relation(2).members().size());
        Assert.assertNotNull(sub.relation(2));
        Assert.assertEquals(1, sub.relation(2).members().size());
        Assert.assertNotNull(source.relation(3));
        Assert.assertNull(sub.relation(3));
        Assert.assertNotNull(source.relation(4));
        Assert.assertEquals(2, source.relation(4).members().size());
        Assert.assertNotNull(sub.relation(4));
        Assert.assertEquals(1, sub.relation(4).members().size());
        Assert.assertNotNull(source.relation(5));
        Assert.assertEquals(1, source.relation(5).members().size());
        Assert.assertNotNull(sub.relation(5));
        Assert.assertEquals(1, sub.relation(5).members().size());
    }

    @Test
    public void testSubAtlasSoftCutWithMultiPolygon()
    {
        final Atlas source = this.rule.getAtlas();
        final Rectangle rectangle1 = Rectangle.forCorners(
                Location.forString("37.780400, -122.473149"),
                Location.forString("37.780785, -122.472631"));
        final Rectangle rectangle2 = Rectangle.forCorners(
                Location.forString("37.780422500976194, -122.47218757867812"),
                Location.forString("37.781049995371575, -122.47145265340805"));
        final Atlas sub1 = source.subAtlas(rectangle1, AtlasCutType.SOFT_CUT)
                .orElseThrow(() -> new CoreException("SubAtlas was not present."));
        final Atlas sub2 = source.subAtlas(rectangle2, AtlasCutType.SOFT_CUT)
                .orElseThrow(() -> new CoreException("SubAtlas was not present."));

        // cut an atlas with a multipolygon of the two rectangles
        final MultiPolygon bothRectangles = MultiPolygon.forOuters(rectangle1, rectangle2);
        final Atlas subBoth = source.subAtlas(bothRectangles, AtlasCutType.SOFT_CUT)
                .orElseThrow(() -> new CoreException("SubAtlas was not present."));

        // assert no differences between subAtlas with MultiPolygon and MultiAtlas of subAtlases
        // with the Multipolygon's outer Polygons
        final AtlasDelta delta = new AtlasDelta(subBoth, new MultiAtlas(sub1, sub2));
        Assert.assertTrue(delta.getDifferences().isEmpty());
    }

    @Test
    public void testSubAtlasWithNodeNestedWithinRelationCase()
    {
        final Atlas source = this.rule.getNodeNestedWithinRelationAtlas();

        final Predicate<AtlasEntity> entitiesWithIdentifierZero = entity -> entity
                .getIdentifier() == 0;

        final Atlas subAtlasWithZeroBasedIdentifiers = source
                .subAtlas(entitiesWithIdentifierZero, AtlasCutType.SOFT_CUT)
                .orElseThrow(() -> new CoreException("SubAtlas was not present."));

        // Nodes
        Assert.assertNotNull(source.node(1));
        Assert.assertNotNull(subAtlasWithZeroBasedIdentifiers.node(1));
        Assert.assertNotNull(source.node(2));
        Assert.assertNotNull(subAtlasWithZeroBasedIdentifiers.node(2));
        Assert.assertNotNull(source.node(3));
        Assert.assertNull(subAtlasWithZeroBasedIdentifiers.node(3));
        Assert.assertNotNull(source.node(4));
        Assert.assertNotNull(subAtlasWithZeroBasedIdentifiers.node(4));

        // Edges
        Assert.assertNotNull(source.edge(0));
        Assert.assertNotNull(subAtlasWithZeroBasedIdentifiers.edge(0));
        Assert.assertNotNull(source.edge(1));
        Assert.assertNull(subAtlasWithZeroBasedIdentifiers.edge(1));

        // Relations
        Assert.assertNotNull(source.relation(0));
        Assert.assertEquals(2, source.relation(0).members().size());
        Assert.assertNotNull(subAtlasWithZeroBasedIdentifiers.relation(0));
        Assert.assertEquals(2, subAtlasWithZeroBasedIdentifiers.relation(0).members().size());
    }

    @Test
    public void testSubAtlasWithPolygonAndEdgeAtBoundary()
    {
        final Atlas source = this.rule.getAtlasWithEdgeAlongBoundary();
        final Polygon boundary = Polygon
                .wkt("POLYGON ((-121.7540269 37.0463639, -121.75403 37.04635, "
                        + "-121.75408 37.0462, -121.75408 37.04611, -121.75406 37.04606, "
                        + "-121.75399 37.04599, -121.75344 37.04557, -121.75338 37.0455, "
                        + "-121.7533422 37.0454102, -121.7544982 37.0454102, "
                        + "-121.7544982 37.0463639, -121.7540269 37.0463639))");
        final Atlas result = source.subAtlas(boundary, AtlasCutType.SOFT_CUT).get();
        Assert.assertEquals(4, result.numberOfEdges());
        // Does not clip with JTS
        Assert.assertNotNull(result.edge(67));
        // Does clip with JTS
        Assert.assertNotNull(result.edge(-67));
        // Does clip with JTS
        Assert.assertNotNull(result.edge(76));
        // Does not clip with JTS
        Assert.assertNotNull(result.edge(-76));
    }
}
