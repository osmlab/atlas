package org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain

/**
 * Issues detected by the linter is wrapped in this exception.
 *
 * @author Yazad Khambata
 */
class LintException extends RuntimeException {
    List<LintResponse> lintResponses

    LintException(final List<LintResponse> lintResponses, final Throwable cause) {
        super(report(lintResponses), (Throwable)cause)
        this.lintResponses = lintResponses
    }

    LintException(final LintResponse lintResponse, final Throwable cause) {
        this([lintResponse], cause)
    }

    LintException(final List<LintResponse> lintResponses) {
        this(lintResponses, null)
    }

    static String report(final List<LintResponse> lintResponses) {
        LintReport.instance.generateReport(lintResponses)
    }
}
