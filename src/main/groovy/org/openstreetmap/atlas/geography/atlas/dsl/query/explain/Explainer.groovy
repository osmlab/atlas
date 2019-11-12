package org.openstreetmap.atlas.geography.atlas.dsl.query.explain

import org.openstreetmap.atlas.geography.atlas.dsl.query.Query
import org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilder
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * Provides an explanation of why an index is or is not used. Note that the explanation does not suggest
 * or perform optimizations directly.
 *
 * @author Yazad Khambata
 */
interface Explainer<E extends AtlasEntity> {
    Explanation<E> explain(final QueryBuilder queryBuilder)

    Explanation<E> explain(Query query)
}
