package org.openstreetmap.atlas.geography.atlas.dsl.selection.predicate

import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

import java.util.function.Predicate

/**
 * Anything that can be represented as a predicate.
 *
 * @author Yazad Khambata
 */
interface Predicatable<E extends AtlasEntity> {
    Predicate<E> toPredicate(final Class<E> entityClass)
}
