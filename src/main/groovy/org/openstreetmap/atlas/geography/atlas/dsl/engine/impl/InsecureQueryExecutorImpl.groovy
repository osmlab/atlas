package org.openstreetmap.atlas.geography.atlas.dsl.engine.impl

import org.openstreetmap.atlas.geography.atlas.dsl.console.ConsoleWriter
import org.openstreetmap.atlas.geography.atlas.dsl.console.impl.StandardOutputConsoleWriter
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This class should only be used while experimenting with the integration. Once Integration is completed switch to
 * SecureQueryExecutorImpl.
 *
 * This class skips validating the authenticity of the source of the query.
 *
 * use SecureQueryExecutorImpl instead.
 *
 * @author Yazad Khambata
 */
class InsecureQueryExecutorImpl extends AbstractQueryExecutorImpl {
    //Note: Deprecation is removed from this class to allow easy integration.

    private static final Logger log = LoggerFactory.getLogger(InsecureQueryExecutorImpl.class)

    InsecureQueryExecutorImpl() {
        this(StandardOutputConsoleWriter.getInstance())
    }

    InsecureQueryExecutorImpl(final ConsoleWriter consoleWriter) {
        super("NOT_USED", consoleWriter)
    }

    @Override
    void validateSignature(final String queryAsString, final String signature) {
        //NOP
        log.warn("Stop using InsecureQueryExecutorImpl and switch to SecureQueryExecutorImpl.")
    }
}
