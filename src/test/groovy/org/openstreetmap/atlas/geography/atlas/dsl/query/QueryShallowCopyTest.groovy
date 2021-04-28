package org.openstreetmap.atlas.geography.atlas.dsl.query

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasSchema
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.*
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.getNode

/**
 * @author Yazad Khambata
 */
class QueryShallowCopyTest extends AbstractAQLTest {
    @Test
    void testSelectShallowCopy() {
        final AtlasSchema atlas = usingAlcatraz()

        final QueryBuilder queryBuilder1 = select node.id, node.osmId, node.tags from atlas.node where node.hasId(100) and node.hasOsmId(1) and node.hasTag(abc: /xyz/)
        final QueryBuilder queryBuilder2 = select node.id, node.osmId, node.tags from atlas.node where node.hasId(100) and node.hasOsmId(1) and node.hasTag(abc: /xyz/)

        verifyShallowCopy(queryBuilder1, queryBuilder2)
    }

    @Test
    void testUpdateShallowCopy() {
        final AtlasSchema atlas = usingAlcatraz()

        final QueryBuilder queryBuilder1 = update atlas.node set node.addTag(abc: 'xyz'), node.deleteTag('pqr') where node.hasId(100) and node.hasOsmId(1) and node.hasTag(abc: /xyz/)
        final QueryBuilder queryBuilder2 = update atlas.node set node.addTag(abc: 'xyz'), node.deleteTag('pqr') where node.hasId(100) and node.hasOsmId(1) and node.hasTag(abc: /xyz/)

        verifyShallowCopy(queryBuilder1, queryBuilder2)
    }

    @Test
    void testDeleteShallowCopy() {
        final AtlasSchema atlas = usingAlcatraz()

        final QueryBuilder queryBuilder1 = delete atlas.node where node.hasId(100) and node.hasOsmId(1) and node.hasTag(abc: /xyz/)
        final QueryBuilder queryBuilder2 = delete atlas.node where node.hasId(100) and node.hasOsmId(1) and node.hasTag(abc: /xyz/)

        verifyShallowCopy(queryBuilder1, queryBuilder2)
    }

    private void verifyShallowCopy(QueryBuilder queryBuilder1, QueryBuilder queryBuilder2) {
        final Query<AtlasEntity> query1 = queryBuilder1.buildQuery()
        final Query<AtlasEntity> query2 = queryBuilder2.buildQuery()

        final Query<AtlasEntity> query3 = query1.shallowCopy()
        final Query<AtlasEntity> query4 = query2.shallowCopy()

        ensureEquals(query1, query4, query3, query2)
    }

    void ensureEquals(Query<AtlasEntity>...queries) {
        assert queries
        assert queries.length > 1

        assert (queries as Set).size() == 1
    }
}
