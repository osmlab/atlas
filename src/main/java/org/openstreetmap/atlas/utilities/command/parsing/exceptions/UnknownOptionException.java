package org.openstreetmap.atlas.utilities.command.parsing.exceptions;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.utilities.command.Levenshtein;
import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser.SimpleOption;

/**
 * @author lcram
 */
public class UnknownOptionException extends Exception
{
    private static final long serialVersionUID = 8506034533362610699L;

    private static Optional<String> closestMatchMessage(final String option,
            final Set<SimpleOption> validOptions)
    {
        final Set<String> optionNames = validOptions.stream().map(SimpleOption::getLongForm)
                .collect(Collectors.toSet());

        String closestOption = null;
        int minimumDistance = Integer.MAX_VALUE;
        for (final String optionName : optionNames)
        {
            final int distance = Levenshtein.levenshtein(option, optionName);
            if (distance < minimumDistance)
            {
                closestOption = optionName;
                minimumDistance = distance;
            }
        }

        if (closestOption == null)
        {
            return Optional.empty();
        }
        return Optional.of(", did you mean \'" + closestOption + "\'?");
    }

    public UnknownOptionException(final Character option)
    {
        super("unknown short option \'" + option + "\'");
    }

    public UnknownOptionException(final String option)
    {
        super("unknown long option \'" + option + "\'");
    }

    public UnknownOptionException(final String option, final Set<SimpleOption> validOptions)
    {
        super("unknown long option \'" + option + "\'"
                + closestMatchMessage(option, validOptions).orElse(""));
    }
}
