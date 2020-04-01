package org.openstreetmap.atlas.geography.atlas.dsl.query

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasSchema

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.getSelect
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.getNode

/**
 * @author Yazad Khambata
 */
class InnerSelectWrapperTest extends AbstractAQLTest {

    @Test
    void testSelectWithInnerQueryEqualsAndHashCode() {
        final AtlasSchema atlasSchema = usingAlcatraz()

        final QueryBuilder innerQueryBuilder1 = select node.id, node.osmId, node.tags from atlasSchema.node where node.hasId(100) and node.hasOsmId(1) and node.hasTag(abc: /xyz/)
        final QueryBuilder innerQueryBuilder2 = select node.id, node.osmId, node.tags from atlasSchema.node where node.hasId(100) and node.hasOsmId(1) and node.hasTag(abc: /xyz/)

        final Select<Node> innerSelect1 = innerQueryBuilder1.buildQuery()
        final Select<Node> innerSelect2 = innerQueryBuilder2.buildQuery()

        assert innerSelect1 == innerSelect2
        assert innerSelect1.hashCode() == innerSelect2.hashCode()

        final InnerSelectWrapper<Node> innerSelectWrapper1 = new InnerSelectWrapper<>(innerSelect1)
        final InnerSelectWrapper<Node> innerSelectWrapper2 = new InnerSelectWrapper<>(innerSelect2)

        assert innerSelectWrapper1 == innerSelectWrapper2
        assert innerSelectWrapper1.hashCode() == innerSelectWrapper2.hashCode()
    }
}
