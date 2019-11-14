package org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.core

import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * Multi key lookup may not be suitable or desirable for all cases, for example while it makes sense for Id based
 * lookups, it is not a good idea for GeoSpatial lookups since there will be an overhead of de-duplication.
 *
 * @author Yazad Khambata
 */
interface MultiLookupIndexScanner<E extends AtlasEntity, IV> extends IndexScanner<E, IV> {
    Iterable<E> fetch(final AtlasTable<E> atlasTable, final IV... lookupValues)

    Iterable<E> fetch(final AtlasTable<E> atlasTable, final List<IV> lookupValues)
}
