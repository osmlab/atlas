package org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.lintlet

import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain.LintRequest

/**
 * Analogous to a Servlet that runs inside a server, a "Lintlet" runs inside a Linter.
 *
 * @author Yazad Khambata
 */
interface Lintlet {
    void lint(LintRequest lintRequest)
}
