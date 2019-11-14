package org.openstreetmap.atlas.geography.atlas.dsl.console.impl

import org.openstreetmap.atlas.geography.atlas.dsl.console.ConsoleWriter
import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid

import java.util.function.Consumer

/**
 * Base class of console writers.
 *
 * @author Yazad Khambata
 */
abstract class BaseConsoleWriter implements ConsoleWriter {
    private boolean turnedOff

    private Consumer<String> echoer

    protected BaseConsoleWriter(final Consumer<String> echoer) {
        this.echoer = echoer
        this.turnedOff = (echoer == null)
    }

    @Override
    boolean isTurnedOff() {
        turnedOff
    }

    @Override
    void echo(final String text) {
        if (isTurnedOff()) {
            return
        }

        echoer.accept(text)
    }

    @Override
    void echo(final Object object) {
        echo(((String)(object?.toString())))
    }
}
