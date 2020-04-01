package org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.strategy

import groovy.transform.builder.Builder
import org.apache.commons.lang3.Validate
import org.openstreetmap.atlas.geography.atlas.dsl.query.ConditionalConstructList
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.IndexNonUseReason
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.Constraint
import org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.core.IndexSetting
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * Domain representing the strategy to scan data.
 *
 * @author Yazad Khambata
 */
@Builder
class ScanStrategy<E extends AtlasEntity> {
    IndexUsageInfo<E> indexUsageInfo
    ConditionalConstructList<E> conditionalConstructListExcludingIndexedConstraintIfAny

    boolean canUseIndex() {
        this.indexUsageInfo.isIndexUsed()
    }

    static class IndexUsageInfo<E extends AtlasEntity> {
        IndexSetting indexSetting
        Constraint<E> constraint
        IndexNonUseReason indexNonUseReason

        IndexUsageInfo(IndexSetting indexSetting, Constraint<E> constraint) {
            this.indexSetting = indexSetting
            this.constraint = constraint

            Validate.notNull(indexSetting)
            Validate.notNull(constraint)
        }

        IndexUsageInfo(IndexNonUseReason indexNonUseReason) {
            this.indexNonUseReason = indexNonUseReason
            Validate.notNull(indexNonUseReason)
        }

        boolean isIndexUsed() {
            indexNonUseReason == null
        }

        String toPrettyString() {
            """
Potential Index              : ${indexSetting}.
Index Actually Used?         : ${isIndexUsed()};
Reason for not using index?  : ${indexNonUseReason}
"""
        }
    }
}
