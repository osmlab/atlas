package org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain

import java.util.stream.Collectors

/**
 * Txt based lint report generator for the console.
 *
 * @author Yazad Khambata
 */
@Singleton
class LintReport {
    String generateReport(final List<LintResponse> issues) {
        final String report = issues.stream().map { LintResponse lintResponse ->
            final LintRequest lintRequest = lintResponse.lintRequest
            final String queryFilePath = lintRequest.queryFilePath
            final String query = lintRequest.queryAsString
            final String signature = lintRequest.signature

            final String lintClassName = lintResponse.lintletClass.name

            final String responseMessages = lintResponse.lintResponseItems.stream()
                    .map { LintResponseItem lintResponseItem ->
                        final Long id = lintResponseItem.id
                        final String category = lintResponseItem.category
                        final String logLevel = lintResponseItem.lintLogLevel?.toString()
                        final String message = lintResponseItem.message

                        "[${logLevel}][$category][${id}][${message}]"
                    }
                    .collect(Collectors.joining(";"))
            """
File                      : [${queryFilePath}].
Query                     : [${query}].
Signature                 : [${signature}]
    -> Lint Class         : [${lintClassName}]
        => Issues         : {
            ${responseMessages}
        }
"""
        }.collect(Collectors.joining("\n"))

        report
    }
}
