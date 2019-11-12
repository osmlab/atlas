package org.openstreetmap.atlas.geography.atlas.dsl.field

import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity
import org.openstreetmap.atlas.geography.atlas.dsl.mutant.Mutant

/**
 * A field whose value supports "add".
 *
 * @author Yazad Khambata
 */
interface Growable<C extends CompleteEntity, AV> extends MutableProperty {
    Mutant add(value)

    void enrichAdd(final C completeEntity, final AV addValue)
}
