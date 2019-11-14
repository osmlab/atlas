package org.openstreetmap.atlas.geography.atlas.dsl.selection.predicate.chained

import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.openstreetmap.atlas.geography.atlas.dsl.selection.predicate.BaseContextualPredicate

import java.util.function.Predicate

/**
 * Constructed by ChainedConditionalConstruct#toChainedConditionalConstructPredicate.
 *
 * @author Yazad Khambata
 */
@ToString(includeSuperProperties = true, includePackage = false)
@Builder(includeSuperProperties = true)
class ChainedConditionalConstructPredicate<T> extends BaseContextualPredicate<T> {
    Predicate<T> predicateA

    BinaryLogicalOperator op

    ConditionalConstructPredicate<T> predicateB
}
