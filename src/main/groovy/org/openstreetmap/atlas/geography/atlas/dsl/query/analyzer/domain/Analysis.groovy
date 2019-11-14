package org.openstreetmap.atlas.geography.atlas.dsl.query.analyzer.domain

import groovy.transform.builder.Builder
import org.openstreetmap.atlas.geography.atlas.dsl.query.Query
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.QueryOptimizationTransformer
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * Domain representing the analysis.
 *
 * @author Yazad Khambata
 */
@Builder
class Analysis<E extends AtlasEntity> {
    Query<E> originalQuery
    Query<E> optimizedQuery
    Map<Class<? extends QueryOptimizationTransformer<E>>, Query<E>> optimizationTrace

    boolean checkIfOptimized() {
        !optimizedQuery?.is(originalQuery)
    }
}
