package org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl

import groovy.transform.PackageScope
import org.openstreetmap.atlas.geography.atlas.dsl.query.Query
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.Explanation
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.domain.OptimizationRequest

/**
 * @author Yazad Khambata
 */
@PackageScope
@Singleton
class OptimizationTestHelper {
    AbstractQueryOptimizationTransform abstractQueryOptimizationTransform() {
        new AbstractQueryOptimizationTransform() {
            @Override
            boolean areAdditionalChecksMet(final OptimizationRequest optimizationRequest) {
                true
            }

            @Override
            Query applyTransformation(final OptimizationRequest optimizationRequest) {
                optimizationRequest.query
            }
        }
    }
}
