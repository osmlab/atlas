package org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain

import org.apache.commons.io.IOUtils

/**
 * Request to lint a specified AQL file or content.
 *
 * @author Yazad Khambata
 */
class LintRequest {
    String queryFilePath
    String queryAsString
    String signature

    LintRequest(String queryAsString, final String signature) {
        this(null, queryAsString, signature)
    }

    LintRequest(final Reader queryAsReader, final String signature) {
        this(IOUtils.toString(queryAsReader), signature)
    }

    LintRequest(final String queryFilePath, final String queryAsString, final String signature) {
        this.queryAsString = queryAsString
        this.signature = signature
        this.queryFilePath = queryFilePath
    }
}
