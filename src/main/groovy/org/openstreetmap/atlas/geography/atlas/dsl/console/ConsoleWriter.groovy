package org.openstreetmap.atlas.geography.atlas.dsl.console

/**
 * Decouple using PrintStream or Logger in the statements and commends.
 *
 * @author Yazad Khambata
 */
interface ConsoleWriter {
    boolean isTurnedOff()

    void echo(String text)

    void echo(Object object)
}
