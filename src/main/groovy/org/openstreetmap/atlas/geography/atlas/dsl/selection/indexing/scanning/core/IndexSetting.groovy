package org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.core

import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.ScanType
import org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.id.IdIndexScanner
import org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.spatial.SpatialIndexScanner

/**
 * Enum managing settings related to the supported indexing strategies.
 *
 * @author Yazad Khambata
 */
enum IndexSetting {
    /**
     * The Unique Index here allows fetching an Entity by Atlas Id in O(1) time.
     */
    ID_UNIQUE_INDEX(IdIndexScanner.instance, ScanType.ID_UNIQUE_INDEX),

    /**
     * The Spatial Index based search.
     */
    SPATIAL_INDEX(SpatialIndexScanner.instance, ScanType.SPATIAL_INDEX);

    IndexScanner indexScanner

    ScanType scanType

    IndexSetting(final IndexScanner indexScanner, final ScanType scanType) {
        this.indexScanner = indexScanner
        this.scanType = scanType
    }

    static Optional<IndexSetting> from(final ScanType scanStrategy) {
        final Optional<IndexSetting> indexSetting = Arrays.stream(IndexSetting.values())
                .filter { indexSetting -> indexSetting.scanType == scanStrategy }
                .findFirst()

        indexSetting
    }
}
