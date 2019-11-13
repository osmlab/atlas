package org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.domain

import groovy.transform.builder.Builder
import org.openstreetmap.atlas.geography.atlas.dsl.query.Query
import org.openstreetmap.atlas.geography.atlas.dsl.query.Statement
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.strategy.ScanStrategy
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * Inputs to the optimization process.
 *
 * @author Yazad Khambata
 */
@Builder
class OptimizationRequest<E extends AtlasEntity> {
    Query<E> query
    AtlasTable<E> table
    ScanStrategy<E> scanStrategy
    Statement statement
    boolean hasUnusedBetterIndexScanOptions
}
