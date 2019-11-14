package org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.domain

import org.openstreetmap.atlas.geography.atlas.dsl.query.Query
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.QueryOptimizationTransformer
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * Domain representing the results of an optimization.
 *
 * @author Yazad Khambata
 */
final class OptimizationInfo<E extends AtlasEntity, O extends QueryOptimizationTransformer<E>> {
    private Query<E> originalQuery
    private Query<E> optimizedQuery

    private Map<Class<O>, Query<E>> optimizationTrace

    OptimizationInfo(final Query<E> originalQuery) {
        this.originalQuery = this.optimizedQuery = originalQuery
    }

    OptimizationInfo(final Query<E> originalQuery, final Query<E> optimizedQuery, final Map<Class<O>, Query<E>> optimizationTrace) {
        this.originalQuery = originalQuery
        this.optimizedQuery = optimizedQuery
        this.optimizationTrace = optimizationTrace
    }

    boolean isOptimized() {
        !optimizedQuery?.is(originalQuery)
    }

    static <E extends AtlasEntity> OptimizationInfo<E, QueryOptimizationTransformer<E>> notOptimized(final Query<E> originalQuery) {
        Valid.notEmpty originalQuery
        return new OptimizationInfo<>(originalQuery)
    }

    static <E extends AtlasEntity, O extends QueryOptimizationTransformer<E>> OptimizationInfo<E, O> optimized(final Query<E> originalQuery, final Query<E> optimizedQuery, final Map<Class<O>, Query<E>> optimizationTrace) {
        Valid.notEmpty originalQuery
        Valid.notEmpty optimizedQuery
        Valid.notEmpty optimizationTrace
        return new OptimizationInfo<>(originalQuery, optimizedQuery, optimizationTrace)
    }

    @Override
    String toString() {
        "isOptimized: ${isOptimized()}; originalQuery: ${originalQuery}; optimizedQuery: ${optimizedQuery}; optimizationTrace: ${optimizationTrace}."
    }
}
