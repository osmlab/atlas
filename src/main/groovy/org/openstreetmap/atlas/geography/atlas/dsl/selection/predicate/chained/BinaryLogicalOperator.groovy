package org.openstreetmap.atlas.geography.atlas.dsl.selection.predicate.chained

import org.openstreetmap.atlas.geography.atlas.dsl.query.Statement

import java.util.function.Predicate

/**
 * The Binary Logical Operators that can actually perform logical operations on Predicates.
 *
 * @author Yazad Khambata
 */
enum BinaryLogicalOperator {
    AND(Statement.Clause.AND){
        @Override
        <T> Predicate<T> chain(final Predicate<T> predicateA, final ConditionalConstructPredicate<T> predicateB) {
            predicateA.and(predicateB)
        }
    },

    OR(Statement.Clause.OR){
        @Override
        <T> Predicate<T> chain(final Predicate<T> predicateA, final ConditionalConstructPredicate<T> predicateB) {
            predicateA.or(predicateB)
        }
    };

    Statement.Clause clause

    BinaryLogicalOperator(Statement.Clause clause) {
        this.clause = clause
    }

    static BinaryLogicalOperator from(Statement.Clause clause) {
        Arrays.stream(BinaryLogicalOperator.values())
                .filter { op -> op.clause == clause }
                .findFirst()
                .orElseThrow { new IllegalArgumentException("clause ${clause} is unsupported.") }
    }

    abstract <T> Predicate<T> chain(Predicate<T> predicateA, ConditionalConstructPredicate<T> predicateB)
}
