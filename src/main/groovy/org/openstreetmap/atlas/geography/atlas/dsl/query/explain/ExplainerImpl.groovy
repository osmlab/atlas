package org.openstreetmap.atlas.geography.atlas.dsl.query.explain

import org.openstreetmap.atlas.geography.atlas.dsl.query.Query
import org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.query.Statement
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.strategy.ScanStrategizer
import org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.strategy.ScanStrategy
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * @author Yazad Khambata
 */
@Singleton
class ExplainerImpl<E extends AtlasEntity> implements Explainer<E> {
    Explanation<E> explain(final QueryBuilder queryBuilder) {
        final Query query = queryBuilder.buildQuery()

        explain(query)
    }

    Explanation<E> explain(Query query) {
        final Statement statement = query.type()
        final ScanStrategy<E> scanStrategy = ScanStrategizer.<E> getInstance().strategize(query.conditionalConstructList)
        final AtlasTable<E> table = query.table

        Explanation.<E> builder()
                .statement(statement)
                .scanStrategy(scanStrategy)
                .table(table)
                .query(query)
                .build()
    }
}
