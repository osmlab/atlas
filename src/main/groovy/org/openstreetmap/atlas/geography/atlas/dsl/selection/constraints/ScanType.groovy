package org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints

import groovy.transform.TupleConstructor

/**
 * @author Yazad Khambata
 */
@TupleConstructor
enum ScanType {
    FULL(99),

    ID_UNIQUE_INDEX(1),

    SPATIAL_INDEX(2);

    /**
     * Lower number is better, used to indicate if an option is superior to another.
     *
     * Examples
     * 1. ID_UNIQUE_INDEX scan is superior to a SPATIAL_INDEX scan.
     * 2. SPATIAL_INDEX scan is superior to FULL.
     */
    int preferntialRank

    static ScanType preferredOption(ScanType scanType1, ScanType scanType2) {
        scanType1.preferntialRank < scanType2?scanType1:scanType2
    }
}
