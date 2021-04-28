package org.openstreetmap.atlas.geography.atlas.dsl.field

import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity
import org.openstreetmap.atlas.geography.atlas.dsl.mutant.Mutant

/**
 * A field whose value supports remove.
 *
 * @author Yazad Khambata
 */
interface Shrinkable<C extends CompleteEntity, RV> extends MutableProperty {
    Mutant remove(Object key)

    void enrichRemove(final C completeEntity, final RV removeValue)
}
