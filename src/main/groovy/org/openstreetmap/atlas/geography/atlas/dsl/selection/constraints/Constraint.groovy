package org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints

import org.openstreetmap.atlas.geography.atlas.dsl.field.Constrainable
import org.openstreetmap.atlas.geography.atlas.dsl.selection.predicate.Predicatable
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * @author Yazad Khambata
 */
interface Constraint<E extends AtlasEntity> extends Predicatable<E> {
    Constrainable getField()
    BinaryOperation getOperation()
    def getValueToCheck()

    ScanType getBestCandidateScanType()

    /**
     * Creates a deep copy of the objects - not values inside the object may still be reference, so use with caution.
     * @return
     */
    Constraint<E> deepCopy()

    Constraint<E> deepCopyWithNewValueToCheck(valueToCheck)
}
