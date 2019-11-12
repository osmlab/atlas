package org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.lintlet.impl

import org.junit.BeforeClass
import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.engine.impl.AbstractQueryExecutorImpl
import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain.LintException
import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain.LintRequest
import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.lintlet.Lintlet

/**
 * @author Yazad Khambata
 */
class WellFormednessLintletTest {
    private static final String QUERY = """select node.id, node.osmId, node.tags from atlas.node limit 10"""
    private static final String QUERY_SIGNATURE = "mCw1ob4SX8a4vMrMvEqI7iGovs/aPKq3hbk/rZ2vUFNasF7pjEJjwFOp496xAwEp32bgiIuW5YxJASVDqQ5oEw=="

    @BeforeClass
    static void setup() {
        System.setProperty(AbstractQueryExecutorImpl.SYSTEM_PARAM_KEY, "DUMMY_SECRET")
    }

    @Test
    void sanity() {
        Lintlet lintlet = new WellFormednessLintlet()

        lintlet.doLint(new LintRequest(QUERY, QUERY_SIGNATURE))
    }

    @Test
    void badSignature() {
        Lintlet lintlet = new WellFormednessLintlet()

        try {
            lintlet.doLint(new LintRequest(QUERY, "bad signature"))
        } catch (LintException e) {
            assert e.getMessage().contains("Signature Mismatch")
            return
        }

        assert false
    }
}
