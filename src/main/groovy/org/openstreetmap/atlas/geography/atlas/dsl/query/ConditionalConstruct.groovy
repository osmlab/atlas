package org.openstreetmap.atlas.geography.atlas.dsl.query

import groovy.transform.builder.Builder
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.Constraint
import org.openstreetmap.atlas.geography.atlas.dsl.selection.predicate.Predicatable
import org.openstreetmap.atlas.geography.atlas.dsl.selection.predicate.chained.ConditionalConstructPredicate
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

import java.util.function.Predicate

/**
 * A constraint packed with the clause (where, and, or).
 *
 * @author Yazad Khambata
 */
@Builder
class ConditionalConstruct<E extends AtlasEntity> implements Predicatable<E> {
    Statement.Clause clause
    Constraint<E> constraint

    @Override
    Predicate<E> toPredicate(final Class<E> entityClass) {
        final Predicate<E> predicate = constraint.toPredicate(entityClass)

        ConditionalConstructPredicate
                .builder()
                .conditionalConstruct(this)
                .predicate(predicate)
                .build()
    }

    @Override
    String toString() {
        "ConditionalConstruct: [${clause} ${constraint}]"
    }

    @Override
    boolean equals(final Object that) {
        EqualsBuilder.reflectionEquals(this, that)
    }

    @Override
    int hashCode() {
        HashCodeBuilder.reflectionHashCode(this)
    }

    ConditionalConstruct<E> deepCopy() {
        shallowCopyWithConstraintOverride(constraint.deepCopy())
    }

    ConditionalConstruct<E> shallowCopyWithConstraintOverride(final Constraint<E> constraint) {
        ConditionalConstruct.builder()
                .clause(this.clause)
                .constraint(constraint) //Not deep-copied - deep copy explicitly if needed.
                .build()
    }
}
