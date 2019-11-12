package org.openstreetmap.atlas.geography.atlas.dsl.query.analyzer

import org.openstreetmap.atlas.geography.atlas.dsl.query.Query
import org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.query.analyzer.domain.Analysis
import org.openstreetmap.atlas.geography.atlas.dsl.query.explain.Explainer
import org.openstreetmap.atlas.geography.atlas.dsl.query.optimizer.Optimizer
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * A query analyzer is an overarching wrapper over an Explainer and an Optimizer and is responsible to iteratively
 * optimizing a query.
 *
 * @author Yazad Khambata
 */
interface QueryAnalyzer<E extends AtlasEntity> {
    Explainer<E> getExplainer()

    Optimizer<E> getOptimizer()

    Analysis<E> analyze(final Query<E> query)

    Analysis<E> analyze(final QueryBuilder queryBuilder)
}
