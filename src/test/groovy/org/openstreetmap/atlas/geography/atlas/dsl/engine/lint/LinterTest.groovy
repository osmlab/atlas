package org.openstreetmap.atlas.geography.atlas.dsl.engine.lint

import org.junit.Test
import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain.Source
import org.openstreetmap.atlas.geography.atlas.dsl.path.PathUtil
import org.openstreetmap.atlas.geography.atlas.dsl.engine.impl.AbstractQueryExecutorImpl
import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain.LintException
import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain.LintLogLevel
import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain.LintRequest
import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.lintlet.impl.InsecureWellFormednessLintlet
import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.lintlet.impl.WellFormednessLintlet

import java.nio.file.Paths

/**
 * @author Yazad Khambata
 */
class LinterTest {

    public static final String CLASSPATH = "aql-files/"

    @Test
    void sanity() {
        Linter.instance.lint(new LintRequest("select node._ from atlas.node limit 10", null), InsecureWellFormednessLintlet)
    }

    @Test
    void bad() {
        try {
            Linter.instance.lint(new LintRequest("bad query", null), InsecureWellFormednessLintlet)
        } catch (LintException e) {
            assert e.lintResponses.size() == 1
            assert e.lintResponses.get(0).getLintResponseItems().size() == 1
            assert e.lintResponses.get(0).getLintResponseItems().get(0).lintLogLevel == LintLogLevel.ERROR
            assert e.lintResponses.get(0).getLintResponseItems().get(0).message =~ /The query is broken query: /
        }
    }

    @Test
    void testAll() {
        System.setProperty(AbstractQueryExecutorImpl.SYSTEM_PARAM_KEY, UUID.randomUUID().toString())
        try {
            Linter.instance.lint(Source.CLASSPATH, CLASSPATH, WellFormednessLintlet)
        } catch (LintException e) {
            assert e.getLintResponses().size() == PathUtil.instance.aqlFilesFromClasspath("aql-files").size()
            e.getLintResponses().stream().forEach { lintResponse ->
                assert lintResponse.getLintResponseItems().size() == 1
                assert lintResponse.getLintResponseItems().get(0).getMessage().contains("Signature Mismatch")
            }

            return
        }

        assert false
    }

    @Test
    void testAllInsecureClasspath() {
        Linter.instance.lint(Source.CLASSPATH, CLASSPATH, InsecureWellFormednessLintlet)

        assert true
    }

    @Test
    void testAllInsecurePath() {
        final String physicalPathAsStr = "/tmp/aql-files"
        PathUtil.instance.deleteRecursivelyQuietly(Paths.get(physicalPathAsStr))

        final URL url = this.getClass().getClassLoader().getResource(CLASSPATH)
        PathUtil.instance.copyFolder(Paths.get(url.toURI()), Paths.get(physicalPathAsStr))

        Linter.instance.lint(Source.PATH, physicalPathAsStr, InsecureWellFormednessLintlet)

        assert true
    }
}
