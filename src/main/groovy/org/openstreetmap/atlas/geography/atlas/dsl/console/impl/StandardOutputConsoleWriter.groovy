package org.openstreetmap.atlas.geography.atlas.dsl.console.impl

import org.openstreetmap.atlas.geography.atlas.dsl.util.Valid

/**
 * Works against standard output.
 *
 * @author Yazad Khambata
 */
class StandardOutputConsoleWriter extends BaseConsoleWriter {

    private static final StandardOutputConsoleWriter instance = new StandardOutputConsoleWriter(System.out)

    private StandardOutputConsoleWriter(final PrintStream printStream) {
        super(toEchoer(printStream))
    }

    private static Closure toEchoer(final PrintStream printStream) {
        Valid.notEmpty printStream

        { text -> printStream.println(text) }
    }

    static StandardOutputConsoleWriter getInstance() {
        instance
    }
}
