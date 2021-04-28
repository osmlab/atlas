package org.openstreetmap.atlas.geography.atlas.dsl.field

import org.openstreetmap.atlas.geography.atlas.complete.CompleteEntity
import org.openstreetmap.atlas.geography.atlas.dsl.mutant.Mutant

/**
 * A Field whose value can be replaced or overridden.
 *
 * @author Yazad Khambata
 */
interface Overridable<C extends CompleteEntity, OV> extends MutableProperty {
    Mutant to(value)

    void enrichOverride(final C completeEntity, final OV overrideValue)
}
