package org.openstreetmap.atlas.geography.atlas.dsl.query

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.TestConstants
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.ExplainerImpl
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.Explanation
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.Result
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.ScanType
import org.openstreetmap.atlas.geography.atlas.items.Edge

import java.util.stream.Collectors

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.*
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.*

/**
 * @author Yazad Khambata
 */
class SelectQueryTest extends AbstractAQLTest {

    @Test
    void testSelectFunctionFields() {
        def atlas = usingAlcatraz()

        def selectEdge = select edge.id, edge.startId, edge.endId, edge.tags, edge.start, edge.end from atlas.edge where edge.hasTag(source: 'yahoo') and edge.hasTag('name')
        final Result<Edge> resultEdge = exec selectEdge

        assert resultEdge.getTable() instanceof AtlasTable

        final List<Long> listStartEndNodeIds = resultEdge.entityStream().flatMap { Edge e -> [e.start().getIdentifier(), e.end().getIdentifier()].stream() }.distinct().collect(Collectors.toList())
        final Long[] startEndNodeIds = listStartEndNodeIds.stream().toArray { new Long[listStartEndNodeIds.size()] }

        def selectNode = select node.id, node.inEdgeIds, node.inEdges, node.outEdges, node.outEdgeIds from atlas.node where node.hasIds(startEndNodeIds)
        final Result resultNode = exec selectNode

        assert listStartEndNodeIds.sort() == resultNode.relevantIdentifiers.sort()
    }

    @Test
    void testSelectStar() {
        def atlas = usingAlcatraz()

        def select1 = select line._ from atlas.line limit 5

        final Explanation explanation = ExplainerImpl.instance.explain(select1)
        assert !explanation.scanStrategy.indexUsageInfo.isIndexUsed()

        final Result result = exec select1
        assert result.relevantIdentifiers.size() == 5
    }

    @Test
    void testExecWithRegex() {
        def atlas = usingAlcatraz()

        def polygon = [
                TestConstants.Polygons.northernPartOfAlcatraz,
                TestConstants.Polygons.centralPartOfAlcatraz,
                TestConstants.Polygons.southernPartOfAlcatraz,
        ]

        def select1 = select point.id, point.osmId, point.lastUserName, point.bounds, point.tags from atlas.point where point.hasTagLike(amenity: /e/) and point.hasTagLike(/toilets/) and point.isWithin(polygon) and point.hasLastUserNameLike(/^[a-zA-Z]+$/)

        final Explanation explanation = ExplainerImpl.instance.explain(select1)
        assert !explanation.scanStrategy.indexUsageInfo.isIndexUsed()

        final Result result = exec select1
        final List<Long> identifiers = result.relevantIdentifiers

        assert identifiers.size() >= 1
    }

    @Test
    void testExecWithWhereAndLimit() {
        def atlas = usingAlcatraz()

        def polygon = [
                TestConstants.Polygons.northernPartOfAlcatraz,
                TestConstants.Polygons.centralPartOfAlcatraz,
                TestConstants.Polygons.southernPartOfAlcatraz
        ]

        def select1 = select line.id, line.osmId, line.tags, line.osmTags from atlas.line where line.hasTag("barrier") and line.isWithin(polygon) limit 100

        final Explanation explanation = ExplainerImpl.instance.explain(select1)
        assert !explanation.scanStrategy.indexUsageInfo.isIndexUsed()

        final Result result = exec select1
        final List<Long> identifiers = result.relevantIdentifiers
        assert identifiers.size() >= 1 && identifiers.size() <= 100

        identifiers.stream().forEach({ identifier ->
            final Atlas theAtlas = atlas.atlasMediator.atlas
            assert theAtlas.line(identifier).getTag("barrier").isPresent()
        })
    }

    @Test
    void testExecWithWhere() {
        def atlas = usingAlcatraz()

        def polygon = [
                TestConstants.Polygons.northernPartOfAlcatraz,
                TestConstants.Polygons.centralPartOfAlcatraz,
                TestConstants.Polygons.southernPartOfAlcatraz
        ]

        def select1 = select edge.id, edge.osmId, edge.lastUserName, edge.isWaySectioned, edge.start, edge.end, edge.isClosed from atlas.edge where edge.isWithin(polygon) and edge.hasOsmId(27998971L) or edge.hasOsmId(27999864L) or edge.hasId(-629121014000000)

        final Explanation explanation = ExplainerImpl.instance.explain(select1)
        assert !explanation.scanStrategy.indexUsageInfo.isIndexUsed()

        final Result result = exec select1
        final List<Long> identifiers = result.relevantIdentifiers
        assert identifiers.size() >= 3
    }

    @Test
    void testExecWithLimit() {
        def atlas = usingAlcatraz()

        def select1 = select node.id, node.osmId from atlas.node limit 10

        final Explanation explanation = ExplainerImpl.instance.explain(select1)
        assert !explanation.scanStrategy.indexUsageInfo.isIndexUsed()

        final Result result = exec select1
        final List<Long> identifiers = result.relevantIdentifiers
        assert identifiers.size() == 10
    }

    @Test
    void testExecRelation() {
        def atlas = usingAlcatraz()

        def polygon = [
               TestConstants.Polygons.northernPartOfAlcatraz
        ]

        def select1 = select relation.id, relation.osmId, relation.allRelationsWithSameOsmIdentifier, relation.allKnownOsmMembers, relation.osmRelationIdentifier, relation.isMultiPolygon, relation.members from atlas.relation where not(relation.isWithin(polygon))

        final Explanation explanation = ExplainerImpl.instance.explain(select1)
        assert !explanation.scanStrategy.indexUsageInfo.isIndexUsed()

        final Result result = exec select1
        assert result.relevantIdentifiers.size() == 1
    }

    @Test
    void testExecArea() {
        def atlas = usingAlcatraz()

        def polygon = [
                TestConstants.Polygons.northernPartOfAlcatraz
        ]

        def select1 = select area.id, area.osmId, area.tags, area.asPolygon, area.closedGeometry, area.rawGeometry from atlas.area where area.isWithin(polygon) and area.hasLastUserNameLike(/M/) limit 10

        final Explanation explanation = ExplainerImpl.instance.explain(select1)
        assert explanation.scanStrategy.indexUsageInfo.isIndexUsed()
        assert explanation.scanStrategy.indexUsageInfo.indexSetting.scanType == ScanType.SPATIAL_INDEX

        final Result result = exec select1
        assert result.relevantIdentifiers.size() > 1
        final List<Long> identifiers = result.relevantIdentifiers

        identifiers.stream().forEach({ identifier ->
            final Atlas theAtlas = atlas.atlasMediator.atlas
            assert theAtlas.area(identifier).lastUserName().get().contains("M")
        })
    }

    @Test
    void testExecWithInClause() {
        def atlas = usingAlcatraz()

        def select1 = select point.id, point.osmId from atlas.point where point.hasIds(5784941541000000, 4553243887000000, 307446836000000) or point.hasOsmIds(307446860, 2407548229)

        final Explanation explanation = ExplainerImpl.instance.explain(select1)
        assert !explanation.scanStrategy.indexUsageInfo.isIndexUsed()

        final Result result = exec select1
        assert result.relevantIdentifiers.size() == 5
    }

    @Test
    void testExecWithInnerQuery() {
        def atlas = usingAlcatraz()

        def select1 = select edge.id, edge.osmId, edge.tags from atlas.edge where edge.hasTagLike(highway: /foot/) or edge.hasTagLike(/payment/)
        def select2 = select edge.id, edge.osmId, edge.tags from atlas.edge where edge.hasTag("access") or edge.hasTagLike(/surface/)

        def select3 = select edge.id, edge.tags from atlas.edge where edge.hasIds(select1) or edge.hasIds(select2)

        def select4 = select edge.id, edge.tags, edge.osmTags from atlas.edge where edge.hasIds(select3)

        final Explanation explanation1 = ExplainerImpl.instance.explain( select1)
        assert !explanation1.scanStrategy.indexUsageInfo.isIndexUsed()

        final Explanation explanation2 = ExplainerImpl.instance.explain(select2)
        assert !explanation2.scanStrategy.indexUsageInfo.isIndexUsed()

        final Explanation explanation3 = ExplainerImpl.instance.explain(select3)
        assert !explanation3.scanStrategy.indexUsageInfo.isIndexUsed()

        final Explanation explanation4 = ExplainerImpl.instance.explain(select4)
        assert explanation4.scanStrategy.indexUsageInfo.isIndexUsed()
        assert explanation4.scanStrategy.indexUsageInfo.indexSetting.scanType == ScanType.ID_UNIQUE_INDEX

        final Result result = exec select4
        assert result.relevantIdentifiers.size() > 1
    }
}
