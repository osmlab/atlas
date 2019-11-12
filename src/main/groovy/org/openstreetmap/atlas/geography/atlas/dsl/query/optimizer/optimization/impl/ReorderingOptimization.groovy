package org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl

import org.openstreetmap.atlas.geography.atlas.dsl.query.ConditionalConstruct
import org.openstreetmap.atlas.geography.atlas.dsl.query.ConditionalConstructList
import org.openstreetmap.atlas.geography.atlas.dsl.query.Query
import org.openstreetmap.atlas.geography.atlas.dsl.query.Statement
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.Explanation
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.domain.OptimizationRequest
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.ScanType
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.stream.Collectors

/**
 * Also called Optimization-1
 *
 * An optimization that can kick in,
 *
 * IFF,
 *  0. Passes AbstractQueryOptimizationTransform#isApplicable(Explain)
 *  1. only "and" clauses are used in the "where" clause (i.e. no "or" clauses in "where").
 *  2. The conditions use different scan types.
 *  3. A condition using an inferior scan type is placed before a condition with a superior scan type.
 *
 * @author Yazad Khambata
 */
@Singleton
class ReorderingOptimization<E extends AtlasEntity> extends AbstractQueryOptimizationTransform<E> {
    private static final Logger log = LoggerFactory.getLogger(ReorderingOptimization.class);

    @Override
    boolean areAdditionalChecksMet(final OptimizationRequest<E> optimizationRequest) {
        final boolean usesDiverseScanTypes = usesDiverseScanTypes(optimizationRequest)
        final boolean noOrsUsed = this.noOrsUsed(optimizationRequest)

        log.info("usesDiverseScanTypes: ${usesDiverseScanTypes}; noOrsUsed: ${noOrsUsed}.")

        usesDiverseScanTypes && noOrsUsed
    }

    private boolean usesDiverseScanTypes(final OptimizationRequest<E> optimizationRequest) {
        final Set<ScanType> scanTypesAvailable = scanTypeAvailable(optimizationRequest)
        scanTypesAvailable.size() > 1
    }

    private boolean noOrsUsed(final OptimizationRequest<E> optimizationRequest) {
        final Set<Statement.Clause> andOrClausesInUse = andOrClausesInUse(optimizationRequest)
        andOrClausesInUse.size() == 1 && andOrClausesInUse.contains(Statement.Clause.AND)
    }

    @Override
    Query<E> applyTransformation(final OptimizationRequest<E> optimizationRequest) {
        final Query<E> query = optimizationRequest.getQuery()
        final ConditionalConstructList<E> copyOfConditionalConstructList = query.conditionalConstructList.deepCopy().stream()
                .sorted(Comparator.comparing { ConditionalConstruct<E> conditionalConstruct -> conditionalConstruct.constraint.bestCandidateScanType.preferntialRank })
                .map { conditionalConstruct -> conditionalConstruct.clause = Statement.Clause.AND; conditionalConstruct }
                .collect(Collectors.toList())

        if (copyOfConditionalConstructList.size() > 1) {
            copyOfConditionalConstructList.get(0).clause = Statement.Clause.WHERE
        }
        final Query<E> shallowCopyOfQuery = query.shallowCopyWithConditionalConstructList(copyOfConditionalConstructList)

        shallowCopyOfQuery
    }
}
