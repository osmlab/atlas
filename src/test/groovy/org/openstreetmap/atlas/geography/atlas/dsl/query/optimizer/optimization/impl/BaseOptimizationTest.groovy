package org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.impl

import org.openstreetmap.atlas.geography.atlas.dsl.AbstractAQLTest
import org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.query.analyzer.QueryAnalyzer
import org.openstreetmap.atlas.geography.atlas.dsl.query.analyzer.domain.Analysis
import org.openstreetmap.atlas.geography.atlas.dsl.query.analyzer.impl.QueryAnalyzerImpl
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.ExplainerImpl
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.Explanation
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.RuleBasedOptimizerImpl
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.domain.OptimizationRequest
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.optimization.QueryOptimizationTransformer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Yazad Khambata
 */
abstract class BaseOptimizationTest extends AbstractAQLTest {
    private static final Logger log = LoggerFactory.getLogger(BaseOptimizationTest.class)

    protected void assertNotApplicable(select1) {
        assert !isApplicable(select1)
    }

    protected void assertApplicable(select1) {
        def applicable = isApplicable(select1)

        log.info("applicable: ${applicable}")

        assert applicable
    }

    protected boolean isApplicableAtBaseLevel(select1) {
        final QueryOptimizationTransformer optimizationTransform = OptimizationTestHelper.instance.abstractQueryOptimizationTransform()
        final Explanation explanation = ExplainerImpl.instance.explain(select1)
        final OptimizationRequest optimizationRequest = QueryAnalyzerImpl.from(explanation)

        optimizationTransform.isApplicable(optimizationRequest)
    }

    protected QueryAnalyzer createQueryAnalyzer(QueryOptimizationTransformer queryOptimizationTransformer) {
        final QueryAnalyzer queryAnalyzer = new QueryAnalyzerImpl<>(ExplainerImpl.instance, new RuleBasedOptimizerImpl(queryOptimizationTransformer))
        queryAnalyzer
    }

    protected boolean isApplicable(QueryBuilder select1) {
        final QueryAnalyzer queryAnalyzer = createQueryAnalyzer(associatedOptimization())
        final Analysis analysis = queryAnalyzer.analyze(select1)

        analysis.checkIfOptimized()
    }

    abstract QueryOptimizationTransformer associatedOptimization()
}
