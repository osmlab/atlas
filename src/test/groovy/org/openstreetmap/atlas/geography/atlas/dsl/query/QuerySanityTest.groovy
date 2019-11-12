package org.openstreetmap.atlas.geography.atlas.dsl.query

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.Result
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasSchema
import org.openstreetmap.atlas.geography.atlas.items.Node
import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.*
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.*

/**
 * @author Yazad Khambata
 */
class QuerySanityTest extends AbstractAQLTest {
    @Test
    void testSameSelectExecMultipleTimes() {
        def atlas = usingButterflyPark()

        def q = select node.id from atlas.node limit 2

        (1..3).forEach {
            final Result result = exec q
            assert result.relevantIdentifiers.size() == 2
        }
    }

    @Test
    void testDifferentSelectSameTableExecMultipleTimes() {
        def atlas = usingButterflyPark()

        def q1 = select node.id, node.osmId, node.tags from atlas.node where node.hasTag([foot: "no"]) or node.hasLastUserNameLike(/[A-Za-z]{5,12}/) limit 2
        def q2 = select node.id, node.osmId, node.tags from atlas.node where node.hasTag([foot: "no"]) or node.hasLastUserNameLike(/[A-Za-z]{5,12}/) limit 2
        def q3 = select node.id, node.osmId, node.tags from atlas.node where node.hasTag([foot: "no"]) or node.hasLastUserNameLike(/[A-Za-z]{5,12}/) limit 2

        final Result result1 = exec q1
        final Result result2 = exec q2
        final Result result3 = exec q3

        assert result1.relevantIdentifiers == result2.relevantIdentifiers
        assert result2.relevantIdentifiers == result3.relevantIdentifiers
    }

    @Test
    void testDifferentSelectSameTableSameSchemasExecMultipleTimes() {
        def atlas1 = usingButterflyPark()
        def atlas2 = usingButterflyPark()

        def q1 = select node.id, node.osmId, node.tags from atlas1.node where node.hasTag([foot: "no"]) or node.hasLastUserNameLike(/[A-Za-z]{5,12}/) limit 2
        def q2 = select node.id, node.osmId, node.tags from atlas2.node where node.hasTag([foot: "no"]) or node.hasLastUserNameLike(/[A-Za-z]{5,12}/) limit 2
        def q3 = select node.id, node.osmId, node.tags from atlas1.node where node.hasTag([foot: "no"]) or node.hasLastUserNameLike(/[A-Za-z]{5,12}/) limit 2
        def q4 = select node.id, node.osmId, node.tags from atlas2.node where node.hasTag([foot: "no"]) or node.hasLastUserNameLike(/[A-Za-z]{5,12}/) limit 2

        final Result result1 = exec q1
        final Result result2 = exec q2
        final Result result3 = exec q3
        final Result result4 = exec q4

        assert result1.relevantIdentifiers == result2.relevantIdentifiers
        assert result2.relevantIdentifiers == result3.relevantIdentifiers
        assert result3.relevantIdentifiers == result4.relevantIdentifiers
    }

    @Test
    void testDifferentSelectSameTableDifferentSchemasExecMultipleTimes() {
        def atlas1 = usingButterflyPark()
        def atlas2 = usingAlcatraz()

        def q1 = select node.identifier, node.osmIdentifier, node.tags from atlas1.node where node.hasTag([foot: "no"]) or node.hasLastUserNameLike(/[A-Za-z]{5,12}/) limit 2
        def q2 = select node.identifier, node.osmIdentifier, node.tags from atlas2.node where node.hasTag([foot: "no"]) or node.hasLastUserNameLike(/[A-Za-z]{5,12}/) limit 2

        final Result result1 = exec q1
        final Result result2 = exec q2

        assert result1.relevantIdentifiers.size() == 2
        assert result2.relevantIdentifiers.size() == 2
    }

    @Test
    void testAtlasSanity() {
        final AtlasSchema atlasSchema1 = usingButterflyPark()
        final AtlasSchema atlasSchema2 = usingAlcatraz()

        final Iterable<Node> nodes1 = atlasSchema1.atlasMediator.atlas.nodes()
        final Iterable<Node> nodes2 = atlasSchema2.atlasMediator.atlas.nodes()

        assert nodes1.first()
        assert nodes2.first()
    }

    @Test
    void testAllTables() {
        final String fieldName = "id"

        final long noOfRecs = 5

        final List<String> atlasSchemas = [usingAlcatraz(), usingButterflyPark()]

        for (AtlasSchema atlasSchema : atlasSchemas) {
            (1..2).forEach { dbCtr ->
                (1..2).forEach { tableCtr ->
                    for (String tableName : atlasSchema.allTableNames) {
                        def query = select atlasSchema[tableName][fieldName] from atlasSchema["node"] limit noOfRecs

                        final Result result = exec query
                        assert result.relevantIdentifiers.size() == noOfRecs
                    }
                }
            }
        }
    }
}
