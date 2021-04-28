package org.openstreetmap.atlas.geography.atlas.dsl.selection.predicate

import java.util.function.Predicate

/**
 * The Java {@link Predicate}s in general do not have any contextual information of the operations being performed on
 * them. The BaseContextualPredicate attempts to provide structure to Predicate and allow an introspection of
 * operations being performed.
 *
 * Specific extensions store context as appropriate along with the Predicate.
 *
 * @author Yazad Khambata
 */
class BaseContextualPredicate<T> implements Predicate<T> {

    Predicate<T> predicate

    @Override
    boolean test(final T t) {
        predicate.test(t)
    }

    @Override
    Predicate<T> and(final Predicate<? super T> other) {
        predicate.and(other)
    }

    @Override
    Predicate<T> negate() {
        predicate.negate()
    }

    @Override
    Predicate<T> or(final Predicate<? super T> other) {
        predicate.or(other)
    }

    @Override
    String toString() {
        predicate.toString()
    }
}
