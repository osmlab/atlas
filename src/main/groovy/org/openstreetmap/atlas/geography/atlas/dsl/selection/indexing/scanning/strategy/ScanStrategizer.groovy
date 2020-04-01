package org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.strategy

import groovy.transform.TupleConstructor
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.IndexNonUseReason
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.Constraint
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.NotConstraint
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.ScanType
import org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.core.IndexSetting
import org.openstreetmap.atlas.geography.atlas.dsl.query.ConditionalConstruct
import org.openstreetmap.atlas.geography.atlas.dsl.query.ConditionalConstructList
import org.openstreetmap.atlas.geography.atlas.dsl.query.Statement
import org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.strategy.ScanStrategy.IndexUsageInfo
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * Indexing can be used IFF,
 * <ul>
 *     <li>The FIRST constraint can fetch from the optionalIndexInfo<li/>
 *     <li>one of the 2 conditions are met,
 *          <ol>
 *              <li>There is only ONE constraint.</li>
 *              <li>The constrains are conjugated by AND</li>
 *          </ol>
 *     </li>
 * </ul>
 *
 * @author Yazad Khambata
 */
@Singleton
class ScanStrategizer<E extends AtlasEntity> {

    ScanStrategy<E> strategize(final ConditionalConstructList<E> conditionalConstructList) {
        /*
         * Split the conditionalConstructList - to first vs rest if an optionalIndexInfo can be used.
         */
        final ScanInfo scanInfo = strategizeInternal(conditionalConstructList)
        final ScanType scanType = scanInfo.scanType
        final IndexNonUseReason indexNonUseReason = scanInfo.indexNonUseReason

        final Optional<IndexSetting> optionalIndexSetting = IndexSetting.from(scanType)

        if (optionalIndexSetting.isPresent()) {
            final ScanStrategy<E> scanStrategy = ScanStrategy.builder()
                    .indexUsageInfo(toIndexUsageInfo(optionalIndexSetting, conditionalConstructList, indexNonUseReason))
                    .conditionalConstructListExcludingIndexedConstraintIfAny(new ConditionalConstructList(conditionalConstructList.getExcludingFirst()))
                    .build()

            return scanStrategy
        }

        final ScanStrategy<E> indexingStrategy = ScanStrategy.builder()
                .indexUsageInfo(toIndexUsageInfo(optionalIndexSetting, conditionalConstructList, indexNonUseReason))
                .conditionalConstructListExcludingIndexedConstraintIfAny(conditionalConstructList)
                .build()

        return indexingStrategy
    }

    private IndexUsageInfo<E> toIndexUsageInfo(final Optional<IndexSetting> optionalIndexSetting,
                                            final ConditionalConstructList<E> conditionalConstructList,
                                            final IndexNonUseReason indexNonUseReason) {
        if (optionalIndexSetting.isPresent()) {
            final IndexSetting indexSetting = optionalIndexSetting.get()
            final Constraint<E> constraint = conditionalConstructList.getFirst().get().constraint

            return new IndexUsageInfo<>(indexSetting, constraint)
        }

        return new IndexUsageInfo<>(indexNonUseReason)
    }

    private ScanInfo strategizeInternal(final ConditionalConstructList<E> conditionalConstructList) {
        final Optional<ConditionalConstruct<E>> optionalFirst = conditionalConstructList.getFirst()

        /*
         * There are no constraints, hence needs a full scan.
         */
        if (!optionalFirst.isPresent()) {
            return new ScanInfo(ScanType.FULL, IndexNonUseReason.NO_WHERE_CLAUSE)
        }

        final ConditionalConstruct<E> firstConditionalConstruct = optionalFirst.get()
        final ScanType firstBestCandidateScanType = firstBestCandidateScanType(firstConditionalConstruct)

        /*
         * The firstBestCandidateScanStrategy IS FULL, hence use FULL.
         */
        if (firstBestCandidateScanType == ScanType.FULL) {
            final IndexNonUseReason indexNonUseReason = firstConditionalConstruct.constraint instanceof NotConstraint ? IndexNonUseReason.FIRST_WHERE_CLAUSE_USES_NOT_OPERATOR : IndexNonUseReason.FIRST_WHERE_CLAUSE_NEEDS_FULL_SCAN

            return new ScanInfo(ScanType.FULL, indexNonUseReason)
        }

        final boolean orConditionUsed = conditionalConstructList.stream()
                .map { conditionalConstruct -> conditionalConstruct.clause }
                .filter { clause -> clause == Statement.Clause.OR }
                .findFirst()
                .isPresent()

        /*
         * No optionalIndexInfo can be used if an OR condition is used, hence use FULL.
         */
        if (orConditionUsed) {
            return new ScanInfo(ScanType.FULL, IndexNonUseReason.WHERE_HAS_OR_USED)
        }

        /*
         * Index that can be actually used.
         */
        return new ScanInfo(firstBestCandidateScanType, null)
    }

    private ScanType firstBestCandidateScanType(ConditionalConstruct<E> firstConditionalConstruct) {
        Valid.isTrue firstConditionalConstruct.clause == Statement.Clause.WHERE

        final Constraint<E> firstConstraint = firstConditionalConstruct.constraint
        final ScanType firstBestCandidateScanType = firstConstraint.bestCandidateScanType

        firstBestCandidateScanType
    }

    @TupleConstructor
    private static class ScanInfo {
        ScanType scanType
        IndexNonUseReason indexNonUseReason
    }
}
