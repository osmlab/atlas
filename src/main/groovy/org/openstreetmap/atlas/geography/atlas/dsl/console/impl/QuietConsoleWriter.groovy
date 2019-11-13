package org.openstreetmap.atlas.geography.atlas.dsl.console.impl

/**
 * Permanently turned off console writer.
 *
 * @author Yazad Khambata
 */
final class QuietConsoleWriter extends BaseConsoleWriter {

    private static QuietConsoleWriter instance = new QuietConsoleWriter()

    private QuietConsoleWriter() {
        super(null)
    }

    @Override
    void echo(final String text) {
        //NOP
    }

    static QuietConsoleWriter getInstance() {
        instance
    }
}
