package org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.core

import org.openstreetmap.atlas.geography.atlas.dsl.query.InnerSelectWrapper
import org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * Index scanning based on inner queries.
 *
 * @author Yazad Khambata
 */
interface InnerQueryLookupIndexScanner<E extends AtlasEntity, IV> extends IndexScanner<E, IV> {
    Iterable<E> fetch(final AtlasTable<E> atlasTable, final QueryBuilder selectInnerQueryBuilder)

    Iterable<E> fetch(final AtlasTable<E> atlasTable, final InnerSelectWrapper<E> innerSelectWrapper)
}
