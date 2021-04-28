package org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.core

import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity
import org.openstreetmap.atlas.geography.atlas.items.ItemType

/**
 * An index scanner abstraction.
 *
 * @author Yazad Khambata
 */
abstract class AbstractIndexScanner<E extends AtlasEntity, IV> implements IndexScanner<E, IV>{

    protected Atlas toAtlas(AtlasTable<E> atlasTable) {
        Valid.notEmpty atlasTable
        Valid.notEmpty atlasTable.atlasMediator
        Valid.notEmpty atlasTable.atlasMediator.atlas

        final Atlas atlas = atlasTable.atlasMediator.atlas
        atlas
    }

    protected ItemType toItemType(AtlasTable<E> atlasTable) {
        atlasTable.tableSetting.itemType
    }
}
