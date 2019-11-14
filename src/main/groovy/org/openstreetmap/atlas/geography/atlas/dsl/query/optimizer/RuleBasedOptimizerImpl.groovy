package org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer

import org.openstreetmap.atlas.geography.atlas.dsl.query.Query
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.domain.OptimizationRequest
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.domain.OptimizationResult
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.QueryOptimizationTransformer
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl.GeometricSurfacesOverlapOptimization
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl.GeometricSurfacesWithinOptimization
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl.IdsInOptimization
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl.ReorderingOptimization
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * Analogous to a Rule based optimization in a query engine.
 *
 * @author Yazad Khambata
 */
class RuleBasedOptimizerImpl<E extends AtlasEntity> implements Optimizer<E> {
    QueryOptimizationTransformer<E>[] queryOptimizationTransformers

    RuleBasedOptimizerImpl(final QueryOptimizationTransformer<E>... queryOptimizationTransformers) {
        this.queryOptimizationTransformers = queryOptimizationTransformers
    }

    /**
     * @param optimizationRequest
     * @return
     */
    OptimizationResult<E> optimizeIfPossible(final OptimizationRequest optimizationRequest, final QueryOptimizationTransformer<E> queryOptimizationTransformer) {
        if (queryOptimizationTransformer.isApplicable(optimizationRequest)) {
            final Query<E> optimizedQuery = queryOptimizationTransformer.applyTransformation(optimizationRequest)

            return optimized(optimizationRequest, optimizedQuery, queryOptimizationTransformer)
        }

        return notOptimized(optimizationRequest, queryOptimizationTransformer)
    }

    private static <E extends AtlasEntity> OptimizationResult<E> optimized(OptimizationRequest<E> optimizationRequest, Query<E> optimizedQuery, QueryOptimizationTransformer<E> queryOptimizationTransformer) {
        OptimizationResult.builder()
                .originalQuery(optimizationRequest.getQuery())
                .optimizedQuery(optimizedQuery)
                .queryOptimizationTransformer(queryOptimizationTransformer)
                .build()
    }

    private static <E extends AtlasEntity> OptimizationResult notOptimized(OptimizationRequest<E> optimizationRequest, QueryOptimizationTransformer<E> queryOptimizationTransformer) {
        OptimizationResult.builder()
                .originalQuery(optimizationRequest.getQuery())
                .optimizedQuery(optimizationRequest.getQuery())
                .queryOptimizationTransformer(queryOptimizationTransformer)
                .build()
    }

    /**
     * Order is important here, since we allow multiple optimizations to be applied at once.
     * When spatial index conditions are involved it is possible that more than 2 are applied
     * (within and overlap optimizations)
     *
     * @return - An Optimizer with configured QueryOptimizationTransformer.
     */
    static <E extends AtlasEntity> RuleBasedOptimizerImpl<E> defaultOptimizer() {
        new RuleBasedOptimizerImpl(
                ReorderingOptimization.instance,
                IdsInOptimization.instance,
                GeometricSurfacesWithinOptimization.instance,
                GeometricSurfacesOverlapOptimization.instance
        )
    }
}
