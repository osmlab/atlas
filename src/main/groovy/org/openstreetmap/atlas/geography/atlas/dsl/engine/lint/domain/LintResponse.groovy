package org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain

import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.lintlet.Lintlet

/**
 * Response of linting issues if any.
 *
 * @author Yazad Khambata
 */
class LintResponse {
    LintRequest lintRequest
    Class<Lintlet> lintletClass
    List<LintResponseItem> lintResponseItems = []

    LintResponse(final LintRequest lintRequest, final Class<Lintlet> lintletClass) {
        this.lintRequest = lintRequest
        this.lintletClass = lintletClass
    }

    static LintResponse newLintResponse(final LintRequest lintRequest, final Class<Lintlet> lintletClass) {
        new LintResponse(lintRequest, lintletClass)
    }

    static LintResponse newLintResponse(final LintRequest lintRequest, final Class<Lintlet> lintletClass, final LintResponseItem...lintResponseItems) {
        newLintResponse(lintRequest, lintletClass).addLintResponseItems(lintResponseItems)
    }

    LintResponse addLintResponseItems(LintResponseItem...lintResponseItems) {
        this.lintResponseItems.addAll(lintResponseItems.toList())
        this
    }

    boolean hasFatalIssues() {
        lintResponseItems?.stream()
                .filter { lintResponseItem -> lintResponseItem.getLintLogLevel().isFatal() }
                .findAny()
                .isPresent()
    }
}
