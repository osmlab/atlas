package org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer

import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.QueryOptimizationTransformer
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.domain.OptimizationRequest
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.domain.OptimizationResult
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * A general interface for an Optimizer - the optimizer performs ∃! optimization
 *
 * @author Yazad Khambata
 */
interface Optimizer<E extends AtlasEntity> {
    QueryOptimizationTransformer<E>[] getQueryOptimizationTransformers()

    OptimizationResult optimizeIfPossible(final OptimizationRequest optimizationRequest,
                                          final QueryOptimizationTransformer<E> queryOptimizationTransformer)
}
