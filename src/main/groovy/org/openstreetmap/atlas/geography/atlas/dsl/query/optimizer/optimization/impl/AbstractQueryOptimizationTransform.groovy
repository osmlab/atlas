package org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl

import org.openstreetmap.atlas.geography.atlas.dsl.query.ConditionalConstruct
import org.openstreetmap.atlas.geography.atlas.dsl.query.Statement
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.domain.OptimizationRequest
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.QueryOptimizationTransformer
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.ScanType
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.stream.Collectors
import java.util.stream.Stream

/**
 * An abstraction of an optimization transformation process.
 *
 * @author Yazad Khambata
 */
abstract class AbstractQueryOptimizationTransform<E extends AtlasEntity> implements QueryOptimizationTransformer<E> {
    private static final Logger log = LoggerFactory.getLogger(AbstractQueryOptimizationTransform.class)

    @Override
    final boolean isApplicable(final OptimizationRequest<E> optimizationRequest) {
        if (QueryOptimizationHelper.instance.considerOptimization(optimizationRequest)) {
            final boolean additionalChecksMet = areAdditionalChecksMet(optimizationRequest)

            log.info("AbstractQueryOptimizationTransform::isApplicable -> additionalChecksMet: ${additionalChecksMet}.")

            return additionalChecksMet
        }

        log.info("AbstractQueryOptimizationTransform::isApplicable -> Basic checks NOT met!")
        return false
    }

    protected Set<Statement.Clause> andOrClausesInUse(final OptimizationRequest<E> optimizationRequest) {
        conditionalConstructListStream(optimizationRequest)
                .map { it.clause }
                .filter { it != Statement.Clause.WHERE }
                .distinct()
                .collect(Collectors.toSet())
    }

    private Stream<ConditionalConstruct<E>> conditionalConstructListStream(final OptimizationRequest<E> optimizationRequest) {
        optimizationRequest.query.conditionalConstructList.stream()
    }

    protected Set<ScanType> scanTypeAvailable(final OptimizationRequest<E> optimizationRequest) {
        conditionalConstructListStream(optimizationRequest)
                .map { conditionalConstruct -> conditionalConstruct.constraint.bestCandidateScanType }
                .distinct()
                .collect(Collectors.toSet())
    }

    abstract boolean areAdditionalChecksMet(final OptimizationRequest<E> optimizationRequest)
}
