package org.openstreetmap.atlas.geography.atlas.dsl.query

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.TestConstants
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.ExplainerImpl
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.Explanation
import org.openstreetmap.atlas.geography.atlas.dsl.query.result.Result
import org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.core.IndexSetting

import static org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilderFactory.*
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.getRelation
import static org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasDB.node

/**
 * @author Yazad Khambata
 */
class ExplainTest extends AbstractAQLTest {

    def polygon = [
            TestConstants.Polygons.northernPartOfAlcatraz
    ]

    def ids = [1641119524000000, 307446838000000, 3202364309000000, 3202364308000000, 1417681468000000] as Long[]

    @Test
    void testNoWhereClause() {
        def atlasSchema = usingAlcatraz()

        def select1 = select relation._ from atlasSchema.relation

        def explanation = ExplainerImpl.instance.explain(select1)

        assert !explanation.scanStrategy.canUseIndex()
        assert !explanation.scanStrategy.indexUsageInfo.isIndexUsed()
        assert !explanation.hasUnusedBetterIndexScanOptions()
        assert explanation.scanStrategy.conditionalConstructListExcludingIndexedConstraintIfAny.size() == 0
    }

    @Test
    void testWhereClauseFullScan() {
        def atlasSchema = usingAlcatraz()

        def select1 = select node._ from atlasSchema.node where node.hasLastUserNameLike(/\w/) and node.hasIds(ids) and node.isWithin(polygon)

        def explanationSelect = ExplainerImpl.instance.explain select1

        assert !explanationSelect.scanStrategy.canUseIndex()
        assert !explanationSelect.scanStrategy.indexUsageInfo.isIndexUsed()
        assert explanationSelect.hasUnusedBetterIndexScanOptions()
        assert explanationSelect.scanStrategy.conditionalConstructListExcludingIndexedConstraintIfAny.size() == 3

        ensureResultsAreConsistent(select1)

        def update1 = update atlasSchema.node set node.addTag("test": "test") where node.hasLastUserNameLike(/\w/) and node.hasIds(ids) and node.isWithin(polygon)

        validateCorrespondingUpdate(update1, explanationSelect)
    }


    @Test
    void testWhereClauseIdUniqueScan() {
        def atlasSchema = usingAlcatraz()

        def select1 = select node._ from atlasSchema.node where node.hasIds(ids) and node.hasLastUserNameLike(/\w/) and node.isWithin(polygon)

        def explanationSelect = ExplainerImpl.instance.explain select1

        assert explanationSelect.scanStrategy.canUseIndex()
        assert explanationSelect.scanStrategy.indexUsageInfo.isIndexUsed()
        assert explanationSelect.scanStrategy.indexUsageInfo.indexSetting == IndexSetting.ID_UNIQUE_INDEX
        assert explanationSelect.scanStrategy.indexUsageInfo.constraint.field == relation.id
        assert !explanationSelect.hasUnusedBetterIndexScanOptions()
        assert explanationSelect.scanStrategy.conditionalConstructListExcludingIndexedConstraintIfAny.size() == 2

        ensureResultsAreConsistent(select1)

        def update1 = update atlasSchema.node set node.addTag("test": "test") where node.hasIds(ids) and node.hasLastUserNameLike(/\w/) and node.isWithin(polygon)

        validateCorrespondingUpdate(update1, explanationSelect)
    }

    @Test
    void testWhereClauseSpatialScan() {
        def atlasSchema = usingAlcatraz()

        def select1 = select node._ from atlasSchema.node where node.isWithin(polygon) and node.hasIds(ids) and node.hasLastUserNameLike(/\w/)

        def explanationSelect = ExplainerImpl.instance.explain select1

        assert explanationSelect.scanStrategy.canUseIndex()
        assert explanationSelect.scanStrategy.indexUsageInfo.isIndexUsed()
        assert explanationSelect.scanStrategy.indexUsageInfo.indexSetting == IndexSetting.SPATIAL_INDEX
        assert explanationSelect.scanStrategy.indexUsageInfo.constraint.field == relation._
        assert explanationSelect.hasUnusedBetterIndexScanOptions()
        assert explanationSelect.scanStrategy.conditionalConstructListExcludingIndexedConstraintIfAny.size() == 2

        ensureResultsAreConsistent(select1)

        def update1 = update atlasSchema.node set node.addTag("test": "test") where node.isWithin(polygon) and node.hasIds(ids) and node.hasLastUserNameLike(/\w/)

        validateCorrespondingUpdate(update1, explanationSelect)
    }

    private void ensureResultsAreConsistent(QueryBuilder anyQueryBuilder) {
        exec anyQueryBuilder

        final Result result = exec anyQueryBuilder
        assert result
        assert result.relevantIdentifiers.sort() as Set == ids as Set
    }

    private void validateCorrespondingUpdate(QueryBuilder updateQueryBuilder, Explanation explanationSelect) {
        validateExplanationForCorrespondingUpdate(updateQueryBuilder, explanationSelect)

        ensureResultsAreConsistent(updateQueryBuilder)
    }

    private void validateExplanationForCorrespondingUpdate(QueryBuilder update1, Explanation explanationSelect) {
        def explanationUpdate = ExplainerImpl.instance.explain update1

        assert explanationSelect.scanStrategy.canUseIndex() == explanationUpdate.scanStrategy.canUseIndex()
        assert explanationSelect.scanStrategy.indexUsageInfo.isIndexUsed() == explanationUpdate.scanStrategy.indexUsageInfo.isIndexUsed()
        assert explanationSelect.scanStrategy.conditionalConstructListExcludingIndexedConstraintIfAny.size() == explanationUpdate.scanStrategy.conditionalConstructListExcludingIndexedConstraintIfAny.size()
        assert explanationSelect.hasUnusedBetterIndexScanOptions() == explanationUpdate.hasUnusedBetterIndexScanOptions()
    }
}
