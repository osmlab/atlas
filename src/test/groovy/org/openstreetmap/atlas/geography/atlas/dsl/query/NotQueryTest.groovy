package org.openstreetmap.atlas.geography.atlas.dsl.query

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.TestConstants
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.ExplainerImpl
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.ScanType
import org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.core.IndexSetting
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.*
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.*

/**
 * @author Yazad Khambata
 */
class NotQueryTest extends AbstractAQLTest {
    private static final Logger log = LoggerFactory.getLogger(NotQueryTest.class)

    @Test
    void testId() {
        def atlasSchema = usingAlcatraz()

        final long id = 307459622000000

        def selectSuperSet = select node.id, node.osmId, node.tags from atlasSchema.node
        def selectPositiveSubset = select node.id, node.osmId, node.tags from atlasSchema.node where node.hasId(id)
        def selectNegativeSubset = select node.id, node.osmId, node.tags from atlasSchema.node where not(node.hasId(id))

        verify(selectSuperSet, selectPositiveSubset, selectNegativeSubset, ScanType.ID_UNIQUE_INDEX)
    }

    @Test
    void testIds() {
        def atlasSchema = usingAlcatraz()

        final Long[] ids = [307351652000000, 307459464000000, 307446864000000]

        def selectSuperSet = select node.id, node.osmId, node.tags from atlasSchema.node
        def selectPositiveSubset = select node.id, node.osmId, node.tags from atlasSchema.node where node.hasIds(ids)
        def selectNegativeSubset = select node.id, node.osmId, node.tags from atlasSchema.node where not(node.hasIds(ids))

        verify(selectSuperSet, selectPositiveSubset, selectNegativeSubset, ScanType.ID_UNIQUE_INDEX)
    }

    @Test
    void testOsmId() {
        def atlasSchema = usingAlcatraz()

        final long id = 1417681452

        def selectSuperSet = select node.id, node.osmId, node.tags from atlasSchema.node
        def selectPositiveSubset = select node.id, node.osmId, node.tags from atlasSchema.node where node.hasOsmId(id)
        def selectNegativeSubset = select node.id, node.osmId, node.tags from atlasSchema.node where not(node.hasOsmId(id))

        verify(selectSuperSet, selectPositiveSubset, selectNegativeSubset, ScanType.FULL)
    }

    @Test
    void testOsmIds() {
        def atlasSchema = usingAlcatraz()

        final Long[] ids = [307459464, 3202364308, 307446838]

        def selectSuperSet = select node.id, node.osmId, node.tags from atlasSchema.node
        def selectPositiveSubset = select node.id, node.osmId, node.tags from atlasSchema.node where node.hasOsmIds(ids)
        def selectNegativeSubset = select node.id, node.osmId, node.tags from atlasSchema.node where not(node.hasOsmIds(ids))

        verify(selectSuperSet, selectPositiveSubset, selectNegativeSubset, ScanType.FULL)
    }


    @Test
    void testTag() {
        def atlasSchema = usingButterflyPark()

        def selectSuperSet = select node.id, node.osmId, node.tags from atlasSchema.node where node.hasTag('highway')
        def selectPositiveSubset = select node.id, node.osmId, node.tags from atlasSchema.node where node.hasTag('highway') and node.hasTag(highway: 'bus_stop')
        def selectNegativeSubset = select node.id, node.osmId, node.tags from atlasSchema.node where node.hasTag('highway') and not(node.hasTag(highway: 'bus_stop'))

        verify(selectSuperSet, selectPositiveSubset, selectNegativeSubset, ScanType.FULL)
    }

    @Test
    void testInBounds() {
        def atlasSchema = usingAlcatraz()

        def selectSuperSet = select node.id, node.osmId, node.tags from atlasSchema.node
        def northAlcatrazPolygon = [TestConstants.Polygons.northernPartOfAlcatraz]
        def selectPositiveSubset = select node.id, node.osmId, node.tags from atlasSchema.node where node.isWithin(northAlcatrazPolygon)
        def selectNegativeSubset = select node.id, node.osmId, node.tags from atlasSchema.node where not(node.isWithin(northAlcatrazPolygon))

        verify(selectSuperSet, selectPositiveSubset, selectNegativeSubset, ScanType.SPATIAL_INDEX)
    }

    @Test
    void testTagKeyRegex() {
        def atlasSchema = usingButterflyPark()

        def selectSuperSet = select point.id, point.tags from atlasSchema.point where point.hasTagLike(image: /http.*:.*/)
        def selectPositiveSubset = select point.id, point.tags from atlasSchema.point where point.hasTagLike(image: /http.*:.*/) and point.hasTagLike(/tour*/)
        def selectNegativeSubset = select point.id, point.tags from atlasSchema.point where point.hasTagLike(image: /http.*:.*/) and not(point.hasTagLike(/tour*/))

        verify(selectSuperSet, selectPositiveSubset, selectNegativeSubset, ScanType.FULL)
    }

    @Test
    void testTagValueRegex() {
        def atlasSchema = usingButterflyPark()

        //Note not(point.hasTagLike(highway: /cross*/)) --> will lead to rows where there is no website as well
        //besides rows with http websites. hence the first check ensures the website tag is present.

        def selectSuperSet = select point.id, point.tags from atlasSchema.point where point.hasTag('highway')
        def selectPositiveSubset = select point.id, point.tags from atlasSchema.point where point.hasTag('highway') and point.hasTagLike(highway: /cross*/)
        def selectNegativeSubset = select point.id, point.tags from atlasSchema.point where point.hasTag('highway') and not(point.hasTagLike(highway: /cross*/))

        verify(selectSuperSet, selectPositiveSubset, selectNegativeSubset, ScanType.FULL)
    }

    @Test
    void testLastUserRegex() {
        def atlasSchema = usingButterflyPark()

        def selectSuperSet = select point.id, point.tags from atlasSchema.point
        def selectPositiveSubset = select point.id, point.tags from atlasSchema.point where point.hasLastUserNameLike(/^[A-Z]+$/)
        def selectNegativeSubset = select point.id, point.tags from atlasSchema.point where not(point.hasLastUserNameLike(/^[A-Z]+$/))

        verify(selectSuperSet, selectPositiveSubset, selectNegativeSubset, ScanType.FULL)
    }

    @Test
    void testWhereAndLimit() {
        def atlasSchema = usingAlcatraz()

        def select1 = select point.id, point.tags from atlasSchema.point where not(point.hasLastUserNameLike(/^[A-Z]+$/)) limit 10

        def result1 = exec select1

        assert result1.relevantIdentifiers.size() == 10
    }

    @Test
    void testInnerQuery() {
        def atlasSchema = usingAlcatraz()

        def select1 = select edge.id, edge.osmId, edge.tags from atlasSchema.edge where edge.hasTagLike(name: /Ferry/) or edge.hasTagLike(/surfac/)
        def select2 = select edge.id, edge.osmId, edge.tags from atlasSchema.edge where edge.hasTag("highway") or edge.hasTagLike(/wheelchair/)

        def select3 = select edge.id, edge.tags from atlasSchema.edge where edge.hasIds(select1) or edge.hasIds(select2)

        def selectSuperSet = select edge.id, edge.tags, edge.osmTags from atlasSchema.edge
        def selectPositiveSubset = select edge.id, edge.tags, edge.osmTags from atlasSchema.edge where edge.hasIds(select3)
        def selectNegativeSubset = select edge.id, edge.tags, edge.osmTags from atlasSchema.edge where not(edge.hasIds(select3))

        verify(selectSuperSet, selectPositiveSubset, selectNegativeSubset, ScanType.ID_UNIQUE_INDEX)
    }

    @Test
    void testOr() {
        def atlasSchema = usingAlcatraz()

        def selectSuperSet = select relation.id from atlasSchema.relation
        def selectNegativeSubset = select relation.id from atlasSchema.relation where relation.hasId(3087373000000) or not(relation.hasTagLike(name: /BlahBlahBlah/))

        def result1 = exec selectSuperSet
        def result2 = exec selectNegativeSubset

        assert result1.relevantIdentifiers.sort() == result2.relevantIdentifiers.sort()
    }

    @Test
    void testUpdateByIds() {
        def atlasSchema = usingAlcatraz()

        final Long[] ids = [307351652000000, 307459464000000, 307446864000000]

        def updateSuperSet = update atlasSchema.node set node.addTag(a: 'b')
        def updatePositiveSubset = update atlasSchema.node set node.addTag(a: 'b') where node.hasIds(ids)
        def updateNegativeSubset = update atlasSchema.node set node.addTag(a: 'b') where not(node.hasIds(ids))

        verify(updateSuperSet, updatePositiveSubset, updateNegativeSubset, ScanType.ID_UNIQUE_INDEX)
    }

    @Test
    void testUpdateByBounds() {
        def atlasSchema = usingAlcatraz()

        def polygon = [TestConstants.Polygons.northernPartOfAlcatraz]

        def selectSuperSet = update atlasSchema.area set area.addTag(a: 'b')
        def selectPositiveSubset = update atlasSchema.area set area.addTag(a: 'b') where area.isWithin(polygon)
        def selectNegativeSubset = update atlasSchema.area set area.addTag(a: 'b') where not(area.isWithin(polygon))

        verify(selectSuperSet, selectPositiveSubset, selectNegativeSubset, ScanType.SPATIAL_INDEX)
    }

    @Test
    void testUpdateByTag() {
        def atlasSchema = usingButterflyPark()

        def selectSuperSet = update atlasSchema.node set node.addTag(a: 'b') where node.hasTag('highway')
        def selectPositiveSubset = update atlasSchema.node set node.addTag(a: 'b') where node.hasTag('highway') and node.hasTag(highway: 'crossing')
        def selectNegativeSubset = update atlasSchema.node set node.addTag(a: 'b') where node.hasTag('highway') and not(node.hasTag(highway: 'crossing'))

        verify(selectSuperSet, selectPositiveSubset, selectNegativeSubset, ScanType.FULL)
    }

    private void verify(querySuperSet, queryPositiveSubset, queryNegativeSubset, final ScanType expectedScanStrategy) {
        verifyExplainPlan(querySuperSet, ScanType.FULL)
        verifyExplainPlan(queryPositiveSubset, expectedScanStrategy)
        verifyExplainPlan(querySuperSet, ScanType.FULL)

        def resultAll = exec querySuperSet
        log.info "---"
        def resultWithCriterion = exec queryPositiveSubset
        log.info "---"
        def resultWithCriterionNegated = exec queryNegativeSubset
        log.info "---"

        assert resultAll.relevantIdentifiers.size() >= 2
        assert resultWithCriterion.relevantIdentifiers.size() >= 1
        assert resultWithCriterionNegated.relevantIdentifiers.size() >= 1

        assert resultAll.relevantIdentifiers.sort() == (resultWithCriterion.relevantIdentifiers + resultWithCriterionNegated.relevantIdentifiers).sort()
    }

    private void verifyExplainPlan(query, expectedScanStrategy) {
        def explanation = ExplainerImpl.instance.explain query

        if (!explanation.scanStrategy.indexUsageInfo.isIndexUsed()) { //No Indexing Info (no where clause)
            assert !explanation.scanStrategy.canUseIndex()
            assert expectedScanStrategy == ScanType.FULL
        } else {
            if (expectedScanStrategy != ScanType.FULL) { //Not FULL
                assert explanation.scanStrategy.canUseIndex()
                assert explanation.scanStrategy.indexUsageInfo.indexSetting == IndexSetting.from(expectedScanStrategy).get()
            } else { //FULL
                assert !explanation.scanStrategy.canUseIndex()
            }
        }
    }
}
