package org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints

import org.apache.commons.lang3.builder.EqualsBuilder
import org.apache.commons.lang3.builder.HashCodeBuilder
import org.openstreetmap.atlas.geography.atlas.dsl.field.Constrainable
import org.openstreetmap.atlas.geography.atlas.dsl.query.Statement
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

import java.util.function.Predicate

/**
 * @author Yazad Khambata
 */
class NotConstraint<E extends AtlasEntity> implements Constraint<E> {
    Constraint constraint
    private final Statement.Clause additionalClause = Statement.Clause.NOT
    private final ScanType bestCandidateScanType = ScanType.FULL

    private NotConstraint(final Constraint constraint) {
        super()
        this.constraint = constraint
    }

    static <E extends AtlasEntity> NotConstraint<E> from(Constraint<E> constraint) {
        new NotConstraint<E>(constraint)
    }

    @Override
    Predicate<E> toPredicate(final Class<E> entityClass) {
        //Just negate the predicate from the composed Constraint.
        return constraint.toPredicate(entityClass).negate()
    }

    @Override
    Constrainable getField() {
        constraint.field
    }

    Statement.Clause getAdditionalClause() {
        additionalClause
    }

    @Override
    BinaryOperation getOperation() {
        constraint.operation
    }

    @Override
    def getValueToCheck() {
        constraint.valueToCheck
    }

    @Override
    ScanType getBestCandidateScanType() {
        bestCandidateScanType
    }

    @Override
    Constraint<E> deepCopy() {
        this.deepCopyWithNewValueToCheck(this.constraint.valueToCheck)
    }

    @Override
    Constraint<E> deepCopyWithNewValueToCheck(final Object valueToCheck) {
        from(this.constraint.deepCopyWithNewValueToCheck(valueToCheck))
    }

    @Override
    String toString() {
        "Constraint(${field} ${additionalClause} ${operation} ${valueToCheck}; Potential Index: ${bestCandidateScanType})."
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
