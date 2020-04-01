package org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.id

import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.dsl.query.InnerSelectWrapper
import org.openstreetmap.atlas.geography.atlas.dsl.query.QueryBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.core.AbstractIndexScanner
import org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.core.InnerQueryLookupIndexScanner
import org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.core.MultiLookupIndexScanner
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity
import org.openstreetmap.atlas.geography.atlas.items.ItemType

/**
 * Analogous to clustered index scanner on a primary key of a db table.
 *
 * @author Yazad Khambata
 */
@Singleton
class IdIndexScanner<E extends AtlasEntity> extends AbstractIndexScanner<E, Long> implements MultiLookupIndexScanner<E, Long>, InnerQueryLookupIndexScanner<E, Long> {
    @Override
    Iterable<E> fetch(final AtlasTable<E> atlasTable, final Long lookupValue) {
        this.fetchInternal(atlasTable, [lookupValue] as Set)
    }

    @Override
    Iterable<E> fetch(final AtlasTable<E> atlasTable, final Long... lookupValues) {
        this.fetchInternal(atlasTable, lookupValues as Set)
    }

    @Override
    Iterable<E> fetch(final AtlasTable<E> atlasTable, final List<Long> lookupValues) {
        this.fetchInternal(atlasTable, lookupValues as Set)
    }

    @Override
    Iterable<E> fetch(final AtlasTable<E> atlasTable, final QueryBuilder selectInnerQueryBuilder) {
        final InnerSelectWrapper innerSelectWrapper = new InnerSelectWrapper(selectInnerQueryBuilder)
        this.fetch(atlasTable, innerSelectWrapper)
    }

    @Override
    Iterable<E> fetch(final AtlasTable<E> atlasTable, final InnerSelectWrapper<E> innerSelectWrapper) {
        this.fetch(atlasTable, innerSelectWrapper.identifiers)
    }

    /**
     * Ensures de-duplication is NOT needed on the results.
     *
     * @param atlasTable
     * @param lookupValues
     * @return
     */
    private Iterable<E> fetchInternal(final AtlasTable<E> atlasTable, final Set<Long> lookupValues) {
        final Atlas atlas = toAtlas(atlasTable)

        final ItemType itemType = toItemType(atlasTable)

        itemType.entitiesForIdentifiers(atlas, lookupValues as Long[])
    }
}
