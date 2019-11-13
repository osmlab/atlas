package org.openstreetmap.atlas.geography.atlas.dsl.field

import org.openstreetmap.atlas.geography.atlas.dsl.mutant.EntityUpdateType

/**
 * A field that is mutable in any way.
 *
 * @author Yazad Khambata
 */
interface MutableProperty {
    EntityUpdateType getEntityUpdateType()
}
