package org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.lintlet.impl

import org.openstreetmap.atlas.geography.Location
import org.openstreetmap.atlas.geography.atlas.Atlas
import org.openstreetmap.atlas.geography.atlas.change.exception.EmptyChangeException
import org.openstreetmap.atlas.geography.atlas.dsl.console.ConsoleWriter
import org.openstreetmap.atlas.geography.atlas.dsl.console.impl.QuietConsoleWriter
import org.openstreetmap.atlas.geography.atlas.dsl.engine.QueryExecutor
import org.openstreetmap.atlas.geography.atlas.dsl.engine.lint.domain.*
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasBuilder

/**
 * Checks if the AQL is well-formed by running the query against an virtually empty atlas.
 *
 * @author Yazad Khambata
 */
abstract class AbstractWellFormednessLintlet extends AbstractLintlet {

    private QueryExecutor queryExecutor

    protected AbstractWellFormednessLintlet(final QueryExecutor queryExecutor) {
        this.queryExecutor = queryExecutor
    }

    @Override
    final void doLint(final LintRequest lintRequest) throws LintException {
        final PackedAtlasBuilder packedAtlasBuilder = new PackedAtlasBuilder()
        packedAtlasBuilder.addNode(System.nanoTime(), Location.CENTER, new HashMap<>())
        final Atlas atlas = packedAtlasBuilder.get()

        final String query = lintRequest.getQueryAsString()
        final String signature = lintRequest.getSignature()

        try {
            queryExecutor.exec(atlas, query, signature)
        } catch (EmptyChangeException e) {
            //Ignore this - changes WILL be empty for linting.
        } catch (Exception e) {
            throw toLintException(toLintResponse(lintRequest, query, signature, e), e)
        } catch (AssertionError e) {
            throw toLintException(toLintResponse(lintRequest, query, signature, e), e)
        }
    }

    private LintException toLintException(LintResponse lintResponse, Throwable e) {
        new LintException(lintResponse, e)
    }

    private LintResponse toLintResponse(LintRequest lintRequest, String query, String signature, Throwable e) {
        final LintResponse lintResponse = LintResponse.newLintResponse(lintRequest, this.getClass(), LintResponseItem.builder()
                .category("Structural Error")
                .message("The query is broken query: [${query}]; in [${lintRequest.queryFilePath}] signature: [${signature}]. Reason: [${e.message}].")
                .lintLogLevel(LintLogLevel.ERROR)
                .build()
        )
        lintResponse
    }

    static ConsoleWriter consoleWriter() {
        QuietConsoleWriter.getInstance()
    }
}
