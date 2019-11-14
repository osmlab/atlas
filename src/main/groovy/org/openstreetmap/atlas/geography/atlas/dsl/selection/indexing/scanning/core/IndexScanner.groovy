package org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.core

import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * Represents an index scanner. Index scanners are responsible for going through
 * an index and selecting data for a query.
 *
 * @author Yazad Khambata
 */
interface IndexScanner<E extends AtlasEntity, IV> {
    Iterable<E> fetch(final AtlasTable<E> atlasTable, final IV lookupValue)
}
