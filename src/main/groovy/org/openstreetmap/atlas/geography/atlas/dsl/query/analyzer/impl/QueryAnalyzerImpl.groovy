package org.openstreetmap.atlas.geography.atlas.dsl.query.analyzer.impl

import org.apache.commons.lang3.tuple.Pair
import org.openstreetmap.atlas.geography.atlas.dsl.query.Query
import org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.query.analyzer.QueryAnalyzer
import org.openstreetmap.atlas.geography.atlas.dsl.query.analyzer.domain.Analysis
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.Explainer
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.Explanation
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.Optimizer
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.domain.OptimizationRequest
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.domain.OptimizationResult
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.QueryOptimizationTransformer
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

import java.util.stream.Collectors

/**
 * A general purpose Query Analyzer implementation.
 *
 * @author Yazad Khambata
 */
class QueryAnalyzerImpl<E extends AtlasEntity> implements QueryAnalyzer<E> {

    Explainer<E> explainer

    Optimizer<E> optimizer

    QueryAnalyzerImpl(final Explainer<E> explainer, final Optimizer<E> optimizer) {
        this.explainer = explainer
        this.optimizer = optimizer
    }

    @Override
    Analysis<E> analyze(final Query<E> originalQuery) {
        Query<E> knownMostOptimalQuery = originalQuery
        Explanation<E> explanation

        final List<OptimizationResult> optimizationResultList = []

        for (QueryOptimizationTransformer<E> queryOptimizationTransformer : getOptimizer().getQueryOptimizationTransformers()) {
            explanation = getExplainer().explain(knownMostOptimalQuery)
            final OptimizationRequest<E> optimizationRequest = QueryAnalyzerImpl.from(explanation)

            final OptimizationResult optimizationResult = getOptimizer().optimizeIfPossible(optimizationRequest, queryOptimizationTransformer)

            if (optimizationResult.checkIfOptimized()) {
                knownMostOptimalQuery = optimizationResult.optimizedQuery
                optimizationResultList.add(optimizationResult)
            }
        }

        final Map<Class<QueryOptimizationTransformer<E>>, Query<E>> optimizationTrace = QueryAnalyzerImpl.from(optimizationResultList)

        return createAnalysis(originalQuery, knownMostOptimalQuery, optimizationTrace)
    }

    @Override
    Analysis<E> analyze(final QueryBuilder queryBuilder) {
        analyze(queryBuilder.buildQuery())
    }

    private Analysis createAnalysis(Query<E> originalQuery, Query<E> knownMostOptimalQuery, Map<Class<QueryOptimizationTransformer<E>>, Query<E>> optimizationTrace) {
        Analysis.builder()
                .originalQuery(originalQuery)
                .optimizedQuery(knownMostOptimalQuery)
                .optimizationTrace(optimizationTrace)
                .build()
    }

    static <E extends AtlasEntity> Map<Class<QueryOptimizationTransformer<E>>, Query<E>> from(List<OptimizationResult> optimizationResultList) {
        optimizationResultList.stream()
                .map { optimizationResult ->
                    final Class<QueryOptimizationTransformer<E>> queryOptimizationTransformerClass = optimizationResult.queryOptimizationTransformer.getClass()
                    final Query<E> optimizedQuery = optimizationResult.getOptimizedQuery()

                    Pair.of(queryOptimizationTransformerClass, optimizedQuery)
                }
                .collect(
                        Collectors.toMap(
                                { final Pair<Class<QueryOptimizationTransformer<E>>, Query<E>> pair -> pair.getKey() },
                                { final Pair<Class<QueryOptimizationTransformer<E>>, Query<E>> pair -> pair.getValue() }
                        )
                )
    }

    static <E extends AtlasEntity> OptimizationRequest<E> from(final Explanation<E> explanation) {
        Valid.notEmpty explanation

        OptimizationRequest.builder()
                .query(explanation.query)
                .table(explanation.table)
                .scanStrategy(explanation.scanStrategy)
                .statement(explanation.statement)
                .hasUnusedBetterIndexScanOptions(explanation.hasUnusedBetterIndexScanOptions())
                .build()
    }
}
