package org.openstreetmap.atlas.utilities.command;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A very simple option parser. Designed specifically to impose constraints on the format of the
 * options.
 *
 * @author lcram
 */
public class SimpleOptionParser
{
    /*
     * TODO: An alternate approach to explore would be: extend the Apache Commons CLI library to
     * impose the necessary constraints without having to reinvent the wheel.
     */

    private static final String LONG_ARGUMENT_PREFIX = "--";
    private static final String SHORT_ARGUMENT_PREFIX = "-";
    private static final String LONG_ARGUMENT_EQUALS = "=";

    private final Map<String, String> registeredLongOptions;
    private final Map<String, String> registeredShortOptions;

    private final Map<String, String> parsedLongOptions;
    private final Set<String> parsedShortOptions;

    public SimpleOptionParser()
    {
        this.registeredLongOptions = new HashMap<>();
        this.registeredShortOptions = new HashMap<>();
        this.parsedLongOptions = new HashMap<>();
        this.parsedShortOptions = new HashSet<>();
    }
}
