package org.openstreetmap.atlas.geography.atlas.dsl.selection


import org.openstreetmap.atlas.geography.atlas.dsl.query.ConditionalConstructList
import org.openstreetmap.atlas.geography.atlas.dsl.query.Statement
import org.openstreetmap.atlas.geography.atlas.dsl.schema.table.AtlasTable
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.Constraint
import org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.core.IndexScanner
import org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.core.IndexSetting
import org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.strategy.ScanStrategizer
import org.openstreetmap.atlas.geography.atlas.dsl.selection.indexing.scanning.strategy.ScanStrategy
import org.openstreetmap.atlas.geography.atlas.dsl.selection.predicate.StatementPredicate
import org.openstreetmap.atlas.geography.atlas.dsl.selection.predicate.chained.BinaryLogicalOperator
import org.openstreetmap.atlas.geography.atlas.dsl.selection.predicate.chained.ChainedConditionalConstruct
import org.openstreetmap.atlas.geography.atlas.dsl.selection.predicate.chained.ConditionalConstructPredicate
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid
import org.openstreetmap.atlas.geography.atlas.items.AtlasEntity

import java.util.function.Predicate
import java.util.stream.StreamSupport

/**
 * Deals with selection (σ) in terms of relational algebra and SQL.
 *
 * @author Yazad Khambata
 */
class Selector<E extends AtlasEntity> {

    final AtlasTable<E> table

    final ScanStrategy<E> scanStrategy

    final Statement statement

    private Selector(final AtlasTable<E> table, final ScanStrategy<E> scanStrategy, final Statement statement) {
        this.table = table
        this.scanStrategy = scanStrategy
        this.statement = statement
    }

    static class SelectorBuilder<E extends AtlasEntity> {
        AtlasTable<E> table

        ConditionalConstructList<E> conditionalConstructList

        Statement statement

        SelectorBuilder with(final AtlasTable<E> table) {
            this.table = table
            this
        }

        SelectorBuilder with(final ConditionalConstructList<E> conditionalConstructList) {
            this.conditionalConstructList = conditionalConstructList
            this
        }

        SelectorBuilder with(final Statement statement) {
            this.statement = statement
            this
        }

        Selector<E> build() {
            final ScanStrategy<E> scanStrategy = ScanStrategizer.<E> getInstance().strategize(conditionalConstructList)
            final Selector<E> selector = new Selector<>(this.table, scanStrategy, this.statement)
            selector
        }
    }

    static <E extends AtlasEntity> SelectorBuilder<E> builder() {
        new SelectorBuilder<>()
    }

    Iterable<E> fetchMatchingEntities() {
        if (scanStrategy.canUseIndex()) {
            //1. When Index info is present.
            final IndexSetting indexSetting = scanStrategy.indexUsageInfo.indexSetting
            final Constraint<E> indexConstraint = scanStrategy.indexUsageInfo.constraint

            final IndexScanner<E, ?> indexScanner = indexSetting.indexScanner

            final Object valueToCheck = indexConstraint.valueToCheck
            final Iterable<E> iterableOfRecordsFromIndex = indexScanner.fetch(table, valueToCheck)

            if (sizeOfNonIndexedConstraints() >= 1) {
                //1.1 Has additional constraints.
                final Iterable<E> iterable = {
                    StreamSupport.stream(iterableOfRecordsFromIndex.spliterator(), false).filter(toPredicate()).iterator()
                }

                return iterable
            } else {
                //1.2 No additional constraints.
                return iterableOfRecordsFromIndex
            }
        } else {
            //2. No index
            if (sizeOfNonIndexedConstraints() >= 1) {
                //2.1 Has constraints
                return table.getAllMatching(toPredicate())
            } else {
                //2.2 No constraints
                return table.getAll()
            }
        }
    }


    private Predicate<E> toPredicate() {
        toPredicate(table.tableSetting.memberClass, statement)
    }

    private <E extends AtlasEntity> Predicate<E> toPredicate(final Class<E> entityClass, final Statement statement) {
        final ConditionalConstructList<E> conditionalConstructList = scanStrategy.conditionalConstructListExcludingIndexedConstraintIfAny

        Valid.isTrue conditionalConstructList.size() > 0

        if (!scanStrategy.canUseIndex()) {
            Valid.isTrue conditionalConstructList.get(0).clause == Statement.Clause.WHERE
        }

        final Predicate<E> aggregatePredicate =
                conditionalConstructList.stream()
                        .map { conditionalConstruct -> new Tuple2<>(conditionalConstruct.clause, conditionalConstruct.toPredicate(entityClass)) }
                        .reduce { t2A, t2B ->
                            final Predicate<E> predicateA = t2A.second

                            final Statement.Clause clauseB = t2B.first

                            final Predicate<E> predicateB = t2B.second

                            /*
                            Conditional Construct Predicate expected for B
                            (A my not be conditional construct as it is being aggregated).
                            */
                            Valid.isTrue predicateB instanceof ConditionalConstructPredicate

                            final BinaryLogicalOperator operator = BinaryLogicalOperator.from(clauseB)

                            final ChainedConditionalConstruct chainedConditionalConstruct = ChainedConditionalConstruct
                                    .builder()
                                    .predicateA(predicateA)
                                    .op(operator)
                                    .predicateB(predicateB)
                                    .build()

                            final Predicate<E> subAggregate = chainedConditionalConstruct.toChainedConditionalConstructPredicate(entityClass)

                            new Tuple2<Statement.Clause, Predicate<E>>(null, subAggregate)
                        }
                        .get()
                        .second

        StatementPredicate.builder().statement(statement).predicate(aggregatePredicate).build()
    }

    private int sizeOfNonIndexedConstraints() {
        (scanStrategy.conditionalConstructListExcludingIndexedConstraintIfAny ?: []).size()
    }
}
