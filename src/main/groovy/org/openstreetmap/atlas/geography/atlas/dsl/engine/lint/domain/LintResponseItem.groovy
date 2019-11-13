package org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain

import groovy.transform.builder.Builder

/**
 * A specific lint response item.
 *
 * @author Yazad Khambata
 */
@Builder
class LintResponseItem {
    long id
    String category
    String message
    LintLogLevel lintLogLevel

    long newId() {
        System.nanoTime()
    }
}
