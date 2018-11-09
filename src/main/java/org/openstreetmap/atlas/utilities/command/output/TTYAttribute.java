package org.openstreetmap.atlas.utilities.command.output;

/**
 * @author lcram
 */
public enum TTYAttribute
{
    BOLD("\033[1m"),
    RESET("\033[0m");

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
