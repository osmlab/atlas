package org.openstreetmap.atlas.geography.atlas.dsl.query.explain

import groovy.transform.builder.Builder
import org.openstreetmap.atlas.geography.atlas.dsl.query.Query
import org.openstreetmap.atlas.geography.atlas.dsl.query.Statement
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.ScanType
import org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.core.IndexSetting
import org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.strategy.ScanStrategy
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * Domain representing the explanation of the explain command.
 *
 * @author Yazad Khambata
 */
@Builder
class Explanation<E extends AtlasEntity> {
    Query<E> query
    AtlasTable<E> table
    ScanStrategy<E> scanStrategy
    Statement statement

    boolean hasUnusedBetterIndexScanOptions() {
        final IndexSetting indexSettingForFirstClause = scanStrategy.indexUsageInfo.indexSetting
        final int rankOfScanTypeInUse = indexSettingForFirstClause == null?ScanType.FULL.preferntialRank:indexSettingForFirstClause.scanType.preferntialRank

        final int bestOfTheRest = scanStrategy.conditionalConstructListExcludingIndexedConstraintIfAny.stream()
                .map { conditionalConstruct -> conditionalConstruct.constraint }
                .map { constraint -> constraint.bestCandidateScanType }
                .mapToInt { it.preferntialRank }
                .min()
                .orElse(ScanType.FULL.preferntialRank)

        //return true when ID index is used but the for the second condition we are performing a full scan for another id.
        //therefore >= and not just >.
        final boolean bestOptionUsed = (rankOfScanTypeInUse < ScanType.FULL.preferntialRank)?rankOfScanTypeInUse >= bestOfTheRest:rankOfScanTypeInUse > bestOfTheRest

        bestOptionUsed
    }
}
