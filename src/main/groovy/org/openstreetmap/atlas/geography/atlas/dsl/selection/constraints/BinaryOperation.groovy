package org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints

import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

/**
 * Implementations must NOT contain mutable states.
 *
 * @author Yazad Khambata
 */
interface BinaryOperation {

    String[] getTokens()

    def <E extends AtlasEntity, VAL_ACTUAL, VAL_CHECK> boolean perform(VAL_ACTUAL actualValue, VAL_CHECK valueToCheck, final Class<E> entityClass)
}
