package org.openstreetmap.atlas.geography.atlas.dsl.schema.table

import org.openstreetmap.atlas.geography.atlas.dsl.field.Field
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasMediator
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.setting.TableSetting
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

import java.util.function.Predicate

/**
 * The Atlas Table contract.
 *
 * @author Yazad Khambata
 */
interface AtlasTable<E extends AtlasEntity> {

    Iterable<E> getAll()

    Iterable<E> getAllMatching(Predicate<E> predicate)

    E getById(final long id)

    AtlasMediator getAtlasMediator()

    Class<E> getEntityClass()

    TableSetting getTableSetting()

    Map<String, Field> getAllFields()
}
