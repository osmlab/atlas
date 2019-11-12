package org.openstreetmap.atlas.geography.atlas.dsl.query

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasSchema

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.*
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.*

/**
 * @author Yazad Khambata
 */
class QueryEqualsAndHasCodeTest extends AbstractAQLTest {
    @Test
    void testSelectEqualsAndHashCode() {
        final AtlasSchema atlas = usingAlcatraz()

        final QueryBuilder queryBuilder1 = select node.id, node.osmId, node.tags from atlas.node where node.hasId(100) and node.hasOsmId(1) and node.hasTag(abc: /xyz/)
        final QueryBuilder queryBuilder2 = select node.id, node.osmId, node.tags from atlas.node where node.hasId(100) and node.hasOsmId(1) and node.hasTag(abc: /xyz/)

        final Select<Node> queryA1 = queryBuilder1.buildQuery()
        final Select<Node> queryB1 = queryBuilder2.buildQuery()
        assert queryA1.fieldsToSelect == queryB1.fieldsToSelect
        assert queryA1.table == queryB1.table
        assert queryA1.limit == queryB1.limit
        assert queryA1.conditionalConstructList == queryB1.conditionalConstructList
        assert queryA1 == queryB1
        assert queryA1.hashCode() == queryB1.hashCode()
    }

    @Test
    void testUpdateEqualsAndHashCode() {
        final AtlasSchema atlas = usingAlcatraz()

        final QueryBuilder queryBuilder1 = update atlas.node set node.addTag(abc: 'xyz'), node.deleteTag('pqr') where node.hasId(100) and node.hasOsmId(1) and node.hasTag(abc: /xyz/)
        final QueryBuilder queryBuilder2 = update atlas.node set node.addTag(abc: 'xyz'), node.deleteTag('pqr') where node.hasId(100) and node.hasOsmId(1) and node.hasTag(abc: /xyz/)

        final Update<Node> queryA1 = queryBuilder1.buildQuery()
        final Update<Node> queryB1 = queryBuilder2.buildQuery()
        assert queryA1.table == queryB1.table
        assert queryA1.mutants == queryB1.mutants
        assert queryA1.conditionalConstructList == queryB1.conditionalConstructList
        assert queryA1 == queryB1
        assert queryA1.hashCode() == queryB1.hashCode()
    }

    @Test
    void testDeleteEqualsAndHashCode() {
        final AtlasSchema atlas = usingAlcatraz()

        final QueryBuilder queryBuilder1 = delete atlas.node where node.hasId(100) and node.hasOsmId(1) and node.hasTag(abc: /xyz/)
        final QueryBuilder queryBuilder2 = delete atlas.node where node.hasId(100) and node.hasOsmId(1) and node.hasTag(abc: /xyz/)

        final Delete<Node> queryA1 = queryBuilder1.buildQuery()
        final Delete<Node> queryB1 = queryBuilder2.buildQuery()
        assert queryA1.table == queryB1.table
        assert queryA1.conditionalConstructList == queryB1.conditionalConstructList
        assert queryA1 == queryB1
        assert queryA1.hashCode() == queryB1.hashCode()
    }

    @Test
    void testSelectWithInnerQueryEqualsAndHashCode() {
        final AtlasSchema atlas = usingAlcatraz()

        final QueryBuilder innerQuery1 = select node.id, node.osmId, node.tags from atlas.node where node.hasId(100) and node.hasOsmId(1) and node.hasTag(abc: /xyz/)
        final QueryBuilder innerQuery2 = select node.id, node.osmId, node.tags from atlas.node where node.hasId(100) and node.hasOsmId(1) and node.hasTag(abc: /xyz/)

        final QueryBuilder queryBuilder1 = select node.id, node.osmId, node.tags from atlas.node where node.hasIds(innerQuery1) and node.hasTag(pqr: /123/)
        final QueryBuilder queryBuilder2 = select node.id, node.osmId, node.tags from atlas.node where node.hasIds(innerQuery2) and node.hasTag(pqr: /123/)

        final Select<Node> queryA1 = queryBuilder1.buildQuery()
        final Select<Node> queryB1 = queryBuilder2.buildQuery()
        assert queryA1.fieldsToSelect == queryB1.fieldsToSelect
        assert queryA1.table == queryB1.table
        assert queryA1.limit == queryB1.limit
        assert queryA1.conditionalConstructList == queryB1.conditionalConstructList
        assert queryA1 == queryB1
        assert queryA1.hashCode() == queryB1.hashCode()
    }
}
