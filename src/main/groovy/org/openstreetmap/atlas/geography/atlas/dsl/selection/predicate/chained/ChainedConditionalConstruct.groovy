package org.openstreetmap.atlas.geography.atlas.dsl.selection.predicate.chained

import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.openstreetmap.atlas.geography.atlas.dsl.selection.predicate.Predicatable
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

import java.util.function.Predicate

/**
 * Chains a Predicate to a ConditionalConstructPredicate.
 *
 * @author Yazad Khambata
 */
@Builder
@ToString
class ChainedConditionalConstruct<T, E extends AtlasEntity> implements Predicatable<E> {

    Predicate<T> predicateA

    BinaryLogicalOperator op

    ConditionalConstructPredicate<T> predicateB

    @Override
    Predicate<E> toPredicate(final Class<E> entityClass) {
        op.chain(predicateA, predicateB)
    }

    Predicate<E> toChainedConditionalConstructPredicate(final Class<E> entityClass) {
        final ChainedConditionalConstructPredicate<E> chainedConditionalConstructPredicate =
                ChainedConditionalConstructPredicate.builder()
                        .predicateA(predicateA)
                        .op(op)
                        .predicateB(predicateB)
                        .predicate(toPredicate(entityClass))
                        .build()

        chainedConditionalConstructPredicate
    }
}
