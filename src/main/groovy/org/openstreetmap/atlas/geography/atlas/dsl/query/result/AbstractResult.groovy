package org.openstreetmap.atlas.geography.atlas.dsl.query.result


import groovy.transform.PackageScope
import org.openstreetmap.atlas.geography.atlas.dsl.schema.AtlasMediator
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

import java.util.stream.Stream

/**
 * Abstraction of a query result.
 *
 * @author Yazad Khambata
 */
@PackageScope
abstract class AbstractResult<E extends AtlasEntity> implements Result<E> {

    @Override
    Stream<E> entityStream() {
        final AtlasTable<E> table = getTable()
        Valid.notEmpty table
        final List<Long> identifiers = getRelevantIdentifiers()

        final Stream<E> stream = EntityStreamHelper.<E> stream(table, identifiers)

        stream
    }

    @Override
    Stream<E> entityStream(final AtlasMediator atlasMediator) {
        EntityStreamHelper
                .<E> stream(getTable().getTableSetting().getItemType(), atlasMediator, getRelevantIdentifiers())
    }

    @Override
    Iterator<E> entityIterator() {
        EntityStreamHelper.<E> iterator(getTable(), getRelevantIdentifiers())
    }

    @Override
    Iterator<E> entityIterator(final AtlasMediator atlasMediator) {
        EntityStreamHelper
                .<E> iterator(getTable().getTableSetting().getItemType(), atlasMediator, getRelevantIdentifiers())
    }
}
