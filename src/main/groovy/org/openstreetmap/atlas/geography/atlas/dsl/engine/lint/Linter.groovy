package org.openstreetmap.atlas.geography.atlas.dsl.engine.lint

import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain.Source
import org.openstreetmap.atlas.geography.atlas.dsl.path.PathQueryFilePackCollection
import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain.LintException
import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain.LintRequest
import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain.LintResponse
import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.lintlet.Lintlet
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.stream.Collectors

/**
 * The main entry point for the linter process.
 *
 * @author Yazad Khambata
 */
@Singleton
class Linter {
    private static final Logger log = LoggerFactory.getLogger(Linter)

    void lint(final LintRequest lintRequest, final Class<Lintlet>... lintletClasses) {
        Valid.notEmpty lintletClasses

        lint(lintRequest, Arrays.stream(lintletClasses)
                .map { Class<Lintlet> lintletClass -> lintletInstance(lintletClass) }
                .collect(Collectors.toList()))
    }

    void lint(final LintRequest lintRequest, final List<Lintlet> lintlets) {
        final Lintlet[] lintletsArray = lintlets.stream().toArray { new Lintlet[lintlets.size()] }
        lint(lintRequest, lintletsArray)
    }

    void lint(final LintRequest lintRequest, final Lintlet... lintlets) {
        lint([lintRequest], lintlets)
    }

    void lint(final List<LintRequest> lintRequests, final Lintlet... lintlets) {


        final List<LintResponse> issues =
                lintRequests.stream()
                        .map { lintRequest ->
                            final List<LintResponse> issuesForTheRequest = Arrays.stream(lintlets)
                                    .map { lintlet ->
                                        try {
                                            lintlet.lint(lintRequest)
                                        } catch (LintException e) {
                                            log.error("Linting issue.", e)
                                            final List<LintResponse> responses = e.getLintResponses()
                                            return Optional.of(responses)
                                        }

                                        return Optional.<List<LintResponse>> empty()
                                    }.filter { optional -> optional.isPresent() }
                                    .map { optional -> optional.get() }
                                    .flatMap { list -> list.stream() }
                                    .collect(Collectors.toList())

                            issuesForTheRequest
                        }
                        .flatMap { issuesForTheRequest -> issuesForTheRequest.stream() }
                        .collect(Collectors.toList())

        final LintException lintException = new LintException(issues)
        final String message = lintException.getMessage()

        println "---------------------------------------"
        println message
        println "---------------------------------------"

        if (hasFatalIssues(issues)) {
            throw lintException
        }
    }

    void lint(final Source source, final String root, final List<Lintlet> lintlets) {
        final PathQueryFilePackCollection classpathQueryFilePackCollection = source.aqlFilesFrom(root)

        final List<LintRequest> lintRequests = classpathQueryFilePackCollection.stream()
                .map { classpathQueryFilePack -> new LintRequest(classpathQueryFilePack.getFileName(), classpathQueryFilePack.getQuery(), classpathQueryFilePack.getSignature()) }
                .collect(Collectors.toList())

        lint(lintRequests, lintlets as Lintlet[])
    }

    void lint(final Source source, final String root, final Lintlet... lintlets) {
        lint(source, root, Arrays.stream(lintlets).collect(Collectors.toList()))
    }

    void lint(final Source source, final String root, final Class<Lintlet>... lintletClassses) {
        lint(
                source,
                root,
                Arrays
                        .stream(lintletClassses)
                        .map { Class<Lintlet> lintClass -> lintletInstance(lintClass) }
                        .collect(Collectors.toList())
        )
    }

    private boolean hasFatalIssues(List<LintResponse> issues) {
        issues.stream().filter { issue -> issue.hasFatalIssues() }.findAny().isPresent()
    }

    private <L extends Lintlet> L lintletInstance(final Class lintletClass) {
        lintletClass.newInstance()
    }
}
