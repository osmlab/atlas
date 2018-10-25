package org.openstreetmap.atlas.geography.atlas;

import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity;

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
        final Atlas filteredOutSubRelationAtlas = source.subAtlas(filteredOutPredicate)
                .orElseThrow(() -> new CoreException("SubAtlas was not present."));

        Assert.assertNull(filteredOutSubRelationAtlas.node(5));
        Assert.assertNotNull(filteredOutSubRelationAtlas.node(3));

        Assert.assertNotNull(filteredOutSubRelationAtlas.node(4));
        Assert.assertNotNull(filteredOutSubRelationAtlas.relation(8));
    }

    @Test
    public void testSubAtlasWithNodeNestedWithinRelationCase()
    {
        final Atlas source = this.rule.getNodeNestedWithinRelationAtlas();

        final Predicate<AtlasEntity> entitiesWithIdentifierZero = entity -> entity
                .getIdentifier() == 0;

        final Atlas subAtlasWithZeroBasedIdentifiers = source.subAtlas(entitiesWithIdentifierZero)
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
    public void testSubAtlasWithPolygon()
    {
        final Atlas source = this.rule.getAtlas();
        // This Rectangle covers only the Node 1, Edge 0, Area 0, Line 0 and Point 0.
        final Atlas sub = source
                .subAtlas(Rectangle.forCorners(Location.forString("37.780400, -122.473149"),
                        Location.forString("37.780785, -122.472631")))
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
    public void testSubAtlasWithPolygonAndEdgeAtBoundary()
    {
        final Atlas source = this.rule.getAtlasWithEdgeAlongBoundary();
        final Polygon boundary = Polygon
                .wkt("POLYGON ((-121.7540269 37.0463639, -121.75403 37.04635, "
                        + "-121.75408 37.0462, -121.75408 37.04611, -121.75406 37.04606, "
                        + "-121.75399 37.04599, -121.75344 37.04557, -121.75338 37.0455, "
                        + "-121.7533422 37.0454102, -121.7544982 37.0454102, "
                        + "-121.7544982 37.0463639, -121.7540269 37.0463639))");
        final Atlas result = source.subAtlas(boundary).get();
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

    @Test
    public void testSubAtlasWithPredicate()
    {
        final Atlas source = this.rule.getAtlas();

        // Should return back all entities in this atlas
        final Predicate<AtlasEntity> allEntities = (entity) -> entity.getIdentifier() > 0
                || entity.getIdentifier() < 0 || entity.getIdentifier() == 0;

        // Should return back only Entities with identifier 0
        final Predicate<AtlasEntity> entitiesWithIdentifierZero = (
                entity) -> entity.getIdentifier() == 0;

        final Atlas identicalSubAtlas = source.subAtlas(allEntities)
                .orElseThrow(() -> new CoreException("SubAtlas was not present."));

        final Atlas subAtlasWithZeroBasedIdentifiers = source.subAtlas(entitiesWithIdentifierZero)
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
}
