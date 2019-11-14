package org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization

import org.openstreetmap.atlas.geography.atlas.dsl.query.Query
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.domain.OptimizationRequest
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * @author Yazad Khambata
 */
interface QueryOptimizationTransformer<E extends AtlasEntity> {
    boolean isApplicable(final OptimizationRequest<E> optimizationRequest)

    Query<E> applyTransformation(final OptimizationRequest<E> optimizationRequest)
}
