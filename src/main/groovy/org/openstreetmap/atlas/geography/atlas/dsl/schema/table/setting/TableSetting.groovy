package org.openstreetmap.atlas.geography.atlas.dsl.schema.table.setting

import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity
import org.openstreetmap.atlas.geography.atlas.complete.CompleteItemType
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity
import org.openstreetmap.atlas.geography.atlas.items.ItemType

import java.util.function.Predicate

/**
 * The built-in tables supported in AQL.
 *
 * @author Yazad Khambata
 */
enum TableSetting {

    NODE,

    POINT,

    LINE,

    EDGE,

    RELATION,

    AREA;


    private String atlasGetAllMethodName() {
        "${toLowerCase()}s"
    }

    private String atlasGetByIdMethodName() {
        toLowerCase()
    }

    private String toLowerCase() {
        this.name().toLowerCase()
    }

    String tableName() {
        toLowerCase()
    }

    ItemType getItemType() {
        this.name() as ItemType
    }

    def <E extends AtlasEntity> Class<E> getMemberClass() {
        this.itemType.memberClass
    }

    CompleteItemType getCompleteItemType() {
        CompleteItemType.from(itemType)
    }

    def <C extends CompleteEntity> Class<C> getCompleteEntityClass() {
        completeItemType.completeEntityClass
    }

    Iterable<? extends AtlasEntity> getAll(final Atlas atlas) {
        atlas."${atlasGetAllMethodName()}"()
    }

    Iterable<? extends AtlasEntity> getAll(final Atlas atlas, final Predicate<? extends AtlasEntity> predicate) {
        atlas."${atlasGetAllMethodName()}"(predicate)
    }

    AtlasEntity getById(final Atlas atlas, final long id) {
        atlas."${atlasGetByIdMethodName()}"(id)
    }
}

