package org.openstreetmap.atlas.geography.atlas.dsl.query

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.TestConstants
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.Result
import org.openstreetmap.atlas.geography.atlas.items.Node

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.getExec
import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.getSelect
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.getNode

/**
 * @author Yazad Khambata
 */
class SelectQueryMultiPolygonTest extends AbstractAQLTest {

    @Test
    void test() {
        def atlas = usingAlcatraz()

        def polygonNorth = [TestConstants.Polygons.northernPartOfAlcatraz]
        def polygonCentral = [TestConstants.Polygons.centralPartOfAlcatraz]
        def polygonSouth = [TestConstants.Polygons.southernPartOfAlcatraz]

        def allPolygon =[
                TestConstants.Polygons.northernPartOfAlcatraz,
                TestConstants.Polygons.centralPartOfAlcatraz,
                TestConstants.Polygons.southernPartOfAlcatraz
        ]

        final QueryBuilder select1 = select node.id, node.osmId, node.lastUserName, node.bounds, node.tags from atlas.node where node.isWithin(polygonNorth)
        final QueryBuilder select2 = select node.id, node.osmId, node.lastUserName, node.bounds, node.tags from atlas.node where node.isWithin(polygonCentral)
        final QueryBuilder select3 = select node.id, node.osmId, node.lastUserName, node.bounds, node.tags from atlas.node where node.isWithin(polygonSouth)

        final Result<Node> result1 = exec select1
        final Result<Node> result2 = exec select2
        final Result<Node> result3 = exec select3

        def count1 = 17
        def count2 = 48
        def count3 = 6

        assert result1.relevantIdentifiers.size() == count1
        assert result2.relevantIdentifiers.size() == count2
        assert result3.relevantIdentifiers.size() == count3

        assert result1.relevantIdentifiers.intersect(result2.relevantIdentifiers).size() != 0
        assert result2.relevantIdentifiers.intersect(result3.relevantIdentifiers).size() != 0
        assert result3.relevantIdentifiers.intersect(result1.relevantIdentifiers).size() == 0

        def select4 = select node.id, node.osmId, node.lastUserName, node.bounds, node.tags from atlas.node where node.isWithin(polygonNorth) or node.isWithin(polygonCentral) or node.isWithin(polygonSouth)
        def select4Combined = select node.id, node.osmId, node.lastUserName, node.bounds, node.tags from atlas.node where node.isWithin(allPolygon)

        final Result<Node> result4 = exec select4
        final Result<Node> result4Combined = exec select4Combined

        assert result4.relevantIdentifiers.size() == result4Combined.relevantIdentifiers.size()
        assert result4.relevantIdentifiers.sort() == result4Combined.relevantIdentifiers.sort()
    }
}
