package org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl

import groovy.transform.PackageScope
import org.openstreetmap.atlas.geography.atlas.dsl.query.ConditionalConstruct
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.Explanation
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.domain.OptimizationRequest
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.ScanType
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Internal use optimization helper.
 *
 * @author Yazad Khambata
 */
@Singleton
@PackageScope
class QueryOptimizationHelper<E extends AtlasEntity> {
    private static final Logger log = LoggerFactory.getLogger(QueryOptimizationHelper.class);

    boolean considerOptimization(final OptimizationRequest<E> optimizationRequest) {
        //For optimizations 1, 2, 3
        final boolean hasUnusedBetterIndexScanOptions = optimizationRequest.hasUnusedBetterIndexScanOptions
        //For optimization 4
        final boolean hasSpatialCheck = hasSpatialCheck(optimizationRequest)

        final boolean considerOptimization = hasUnusedBetterIndexScanOptions || hasSpatialCheck

        log.info("QueryOptimizationHelper::considerOptimization -> hasUnusedBetterIndexScanOptions: ${hasUnusedBetterIndexScanOptions}; hasSpatialCheck: ${hasSpatialCheck}; considerOptimization: ${considerOptimization}.")

        considerOptimization
    }

    private boolean hasSpatialCheck(final OptimizationRequest<E> optimizationRequest) {
        optimizationRequest.query
                .conditionalConstructList?.stream()
                .filter { ConditionalConstruct<E> cc ->
                    cc.getConstraint().getBestCandidateScanType() == ScanType.SPATIAL_INDEX
                }
                .findAny()
                .isPresent()
    }
}
