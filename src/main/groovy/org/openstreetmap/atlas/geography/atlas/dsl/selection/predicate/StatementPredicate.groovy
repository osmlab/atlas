package org.openstreetmap.atlas.geography.atlas.dsl.selection.predicate

import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.openstreetmap.atlas.geography.atlas.dsl.query.Statement

/**
 * Store the statement alongside the Predicate.
 *
 * @author Yazad Khambata
 */
@ToString(includeSuperProperties = true)
@Builder(includeSuperProperties = true)
class StatementPredicate<T> extends BaseContextualPredicate<T> {
    Statement statement
}
