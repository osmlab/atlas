package org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.lintlet.impl

import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain.LintException
import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain.LintRequest
import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.lintlet.Lintlet
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid

/**
 * Abstraction of a Lintlet.
 *
 * @author Yazad Khambata
 */
abstract class AbstractLintlet implements Lintlet {
    @Override
    void lint(final LintRequest lintRequest) {
        Valid.notEmpty lintRequest

        doLint(lintRequest)
    }

    abstract void doLint(final LintRequest lintRequest) throws LintException
}
