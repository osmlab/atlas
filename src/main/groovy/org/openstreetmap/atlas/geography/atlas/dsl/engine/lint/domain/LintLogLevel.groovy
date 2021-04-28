package org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain

import groovy.transform.TupleConstructor

/**
 * Lint issue severity or level.
 *
 * @author Yazad Khambata
 */
@TupleConstructor
enum LintLogLevel {
    ERROR(true), WARN(false);

    boolean fatal
}
