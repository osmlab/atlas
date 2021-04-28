package org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints

import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * @author Yazad Khambata
 */
@Singleton
class Constraints {
    def <E extends AtlasEntity> Constraint<E> alwaysTrue(AtlasTable<E> table) {

    }
}
