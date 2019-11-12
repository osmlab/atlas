package org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.domain

import groovy.transform.builder.Builder
import org.openstreetmap.atlas.geography.atlas.dsl.query.Query
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.QueryOptimizationTransformer
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * @author Yazad Khambata
 */
@Builder
class OptimizationResult<E extends AtlasEntity> {
    Query<E> originalQuery
    Query<E> optimizedQuery

    QueryOptimizationTransformer<E> queryOptimizationTransformer

    boolean checkIfOptimized() {
        Valid.notEmpty originalQuery
        Valid.notEmpty optimizedQuery

        !originalQuery.is(optimizedQuery)
    }
}
