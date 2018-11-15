package org.openstreetmap.atlas.utilities.command.output;

/**
 * Easy mnemonics for TTY display attributes (ANSI control codes) and their Unicode encodings.
 *
 * @see "https://en.wikipedia.org/wiki/ANSI_escape_code#SGR_(Select_Graphic_Rendition)_parameters"
 * @author lcram
 */
public enum TTYAttribute
{
    BOLD("\u001B[1m"),
    FAINT("\u001B[2m"),
    ITALIC("\u001B[3m"),
    UNDERLINE("\u001B[4m"),
    BLINK("\u001B[5m"),
    RAPID_BLINK("\u001B[6m"),
    REVERSE_VIDEO("\u001B[7m"),
    BLACK("\u001B[30m"),
    RED("\u001B[31m"),
    GREEN("\u001B[32m"),
    YELLOW("\u001B[33m"),
    BLUE("\u001B[34m"),
    MAGENTA("\u001B[35m"),
    CYAN("\u001B[36m"),
    WHITE("\u001B[037m"),
    BACKGROUND_BLACK("\u001B[40m"),
    BACKGROUND_RED("\u001B[41m"),
    BACKGROUND_GREEN("\u001B[42m"),
    BACKGROUND_YELLOW("\u001B[43m"),
    BACKGROUND_BLUE("\u001B[44m"),
    BACKGROUND_MAGENTA("\u001B[45m"),
    BACKGROUND_CYAN("\u001B[46m"),
    BACKGROUND_WHITE("\u001B[047m"),
    RESET("\u001B[0m");

    private final String ansiSequence;

    TTYAttribute(final String ansiSequence)
    {
        this.ansiSequence = ansiSequence;
    }

    public String getANSISequence()
    {
        return this.ansiSequence;
    }
}
