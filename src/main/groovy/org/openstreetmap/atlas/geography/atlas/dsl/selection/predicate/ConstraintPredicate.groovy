package org.openstreetmap.atlas.geography.atlas.dsl.selection.predicate

import groovy.transform.ToString
import groovy.transform.builder.Builder
import org.openstreetmap.atlas.geography.atlas.dsl.selection.constraints.Constraint

/**
 * Captures the Constraint along side the Predicate. This allows one to introspect the operation being performed
 * in the Predicate (via the constraint).
 *
 * @author Yazad Khambata
 */
@ToString(includeSuperProperties = true, includePackage = false)
@Builder(includeSuperProperties = true)
class ConstraintPredicate<T> extends BaseContextualPredicate<T> {

    Constraint constraint
}
