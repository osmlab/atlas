package org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.lintlet.impl

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain.LintException
import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain.LintLogLevel
import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain.LintRequest
import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.lintlet.Lintlet
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * @author Yazad Khambata
 */
class InsecureWellFormednessLintletTest {

    private static final Logger log = LoggerFactory.getLogger(InsecureWellFormednessLintletTest)

    private String QUERY_1 = """select node.id, node.osmId, node.tags from atlas.node where node.hasLastUserName("granger") limit 10"""

    @Test
    void sanity() {
        Lintlet lintlet = new InsecureWellFormednessLintlet()
        lintlet.doLint(new LintRequest(QUERY_1, null))
        assert true
    }

    @Test
    void bad1() {
        final String query = """select node.BAD_COL_NAME from atlas.node where node.hasLastUserName("lestrange") limit 10"""
        verifyQuery(query)
    }

    @Test
    void bad2() {
        final String query = """Not a query"""
        verifyQuery(query)
    }

    @Test
    void bad3() {
        final String query = """select bad_table._ from atlas.node"""
        verifyQuery(query)
    }

    @Test
    void bad4() {
        final String query = """select node._ from atlas.bad_table1"""
        verifyQuery(query)
    }

    @Test
    void bad5() {
        final String query = """select node._ from missingAtlas.node"""
        verifyQuery(query)
    }

    private void verifyQuery(String query) {
        Lintlet lintlet = new InsecureWellFormednessLintlet()

        final LintRequest lintRequest = new LintRequest(query, null)

        try {
            lintlet.doLint(lintRequest)
        } catch (LintException e) {
            validException(e, lintRequest)
            return
        }

        assert false
    }

    private void validException(LintException e, LintRequest lintRequest) {
        log.error("", e)

        assert e.lintResponses
        assert e.lintResponses.get(0).getLintResponseItems()
        assert e.lintResponses.get(0).getLintRequest() == lintRequest
        assert e.lintResponses.get(0).getLintletClass() == InsecureWellFormednessLintlet
        assert e.lintResponses.get(0).getLintResponseItems().get(0).lintLogLevel == LintLogLevel.ERROR
        assert e.lintResponses.get(0).getLintResponseItems().get(0).category == "Structural Error"
        final String message = e.lintResponses.get(0).getLintResponseItems().get(0).message
        assert message =~ /The query is broken query: /
        assert message.contains(e.cause.message)
        assert message.contains(lintRequest.queryAsString)
    }
}
