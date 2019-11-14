package org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints

import groovy.transform.builder.Builder
import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.field.Constrainable
import org.openstreetmap.atlas.geography.atlas.dsl.selection.predicate.ConstraintPredicate
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

import java.util.function.Predicate

/**
 * @author Yazad Khambata
 */
@Builder
class BasicConstraint<E extends AtlasEntity> implements Constraint<E> {
    Constrainable field
    BinaryOperation operation
    def valueToCheck

    ScanType bestCandidateScanType

    Class<E> atlasEntityClass

    /**
     * Generates a predicate ignoring the bestCandidateScanType.
     * @param entityClass
     * @return
     */
    @Override
    Predicate<E> toPredicate(final Class<E> entityClass) {
        final Predicate<E> predicate = { atlasEntity ->
            final Object actualValue = field.read(atlasEntity)

            operation.perform(actualValue, valueToCheck, entityClass)
        }

        ConstraintPredicate.builder().constraint(this).predicate(predicate).build()
    }

    @Override
    Constraint<E> deepCopy() {
        this.deepCopyWithNewValueToCheck(this.valueToCheck)
    }

    @Override
    Constraint<E> deepCopyWithNewValueToCheck(final Object valueToCheck) {
        BasicConstraint.builder()
                .field(this.field) //less risk since not backed by schema here.
                .operation(this.operation)
                .valueToCheck(valueToCheck) //risky since cloning is not universal in the JVM, and using a reference here.
                .bestCandidateScanType(bestCandidateScanType)
                .atlasEntityClass(atlasEntityClass)
                .build()
    }

    @Override
    String toString() {
        "Constraint(${field} ${operation} ${valueToCheck}; Potential Index: ${bestCandidateScanType})"
    }

    @Override
    boolean equals(final Object that) {
        EqualsBuilder.reflectionEquals(this, that)
    }

    @Override
    int hashCode() {
        HashCodeBuilder.reflectionHashCode(this)
    }
}
