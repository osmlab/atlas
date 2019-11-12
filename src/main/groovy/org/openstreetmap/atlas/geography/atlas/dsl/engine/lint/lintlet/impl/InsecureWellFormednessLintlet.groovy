package org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.lintlet.impl

import org.openstreetmap.atlas.geography.atlas.dsl.engine.impl.InsecureQueryExecutorImpl

/**
 * Checks if the AQL is well-formed by running the query against an almost empty atlas.
 *
 * Avoid using this class - use WellFormednessLintlet instead.
 *
 * @author Yazad Khambata
 */
class InsecureWellFormednessLintlet extends AbstractWellFormednessLintlet {
    InsecureWellFormednessLintlet() {
        super(new InsecureQueryExecutorImpl(consoleWriter()))
    }
}
