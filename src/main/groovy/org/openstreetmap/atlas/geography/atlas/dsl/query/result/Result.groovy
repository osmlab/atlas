package org.openstreetmap.atlas.geography.atlas.dsl.query.result

import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * Contract of a result of a statement.
 *
 * @author Yazad Khambata
 */
interface Result<E extends AtlasEntity> extends EntityIterable<E> {
    AtlasTable<E> getTable()

    List<Long> getRelevantIdentifiers()
}
