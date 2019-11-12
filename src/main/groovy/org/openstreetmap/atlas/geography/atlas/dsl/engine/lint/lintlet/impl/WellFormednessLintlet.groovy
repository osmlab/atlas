package org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.lintlet.impl

import org.openstreetmap.atlas.geography.atlas.dsl.engine.impl.SecureQueryExecutorImpl

/**
 * Checks if the AQL is well-formed by running the query against an EmptyAtlas.
 *
 * @author Yazad Khambata
 */
class WellFormednessLintlet extends AbstractWellFormednessLintlet {

    WellFormednessLintlet() {
        super(new SecureQueryExecutorImpl(consoleWriter()))
    }
}
