package org.openstreetmap.atlas.geography.atlas.dsl.engine.impl

import org.openstreetmap.atlas.geography.atlas.dsl.authentication.Authenticator
import org.openstreetmap.atlas.geography.atlas.dsl.authentication.impl.SHA512HMACAuthenticatorImpl
import org.openstreetmap.atlas.geography.atlas.dsl.console.ConsoleWriter
import org.openstreetmap.atlas.geography.atlas.dsl.console.impl.StandardOutputConsoleWriter
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid

/**
 * @author Yazad Khambata
 */
class SecureQueryExecutorImpl extends AbstractQueryExecutorImpl {
    private Authenticator authenticator

    SecureQueryExecutorImpl() {
        this(StandardOutputConsoleWriter.getInstance())
    }

    SecureQueryExecutorImpl(final ConsoleWriter consoleWriter) {
        this(getKeyFromSystemParam(), consoleWriter)
    }

    SecureQueryExecutorImpl(final String key) {
        this(key, StandardOutputConsoleWriter.newInstance())
    }

    SecureQueryExecutorImpl(final String key, ConsoleWriter consoleWriter) {
        super(key, consoleWriter)

        Valid.notEmpty key

        this.authenticator = new SHA512HMACAuthenticatorImpl(key)
    }

    @Override
    void validateSignature(final String queryAsString, final String signature) {
        this.authenticator.verify(queryAsString, signature)
    }
}
