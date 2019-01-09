package org.openstreetmap.atlas.utilities.command.parsing.exceptions;

import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.utilities.command.parsing.SimpleOptionAndArgumentParser.SimpleOption;

/**
 * @author lcram
 */
public class UnknownOptionException extends Exception
{
    private static final long serialVersionUID = 8506034533362610699L;

    private static String closestMatch(final String option, final Set<SimpleOption> validOptions)
    {
        final Set<String> optionNames = validOptions.stream().map(SimpleOption::getLongForm)
                .collect(Collectors.toSet());

        String closestOption = null;
        int minimumDistance = Integer.MAX_VALUE;
        for (final String optionName : optionNames)
        {
            final int distance = levenshtein(option, optionName);
            if (distance < minimumDistance)
            {
                closestOption = optionName;
                minimumDistance = distance;
            }
        }

        return closestOption;
    }

    private static int levenshtein(final String string1, final String string2)
    {
        final char[] letters1 = string1.toCharArray();
        final char[] letters2 = string2.toCharArray();

        final int[][] distance = new int[letters1.length + 1][letters2.length + 1];
        for (int i = 0; i < letters1.length + 1; i++)
        {
            distance[i][0] = i;
        }
        for (int i = 0; i < letters2.length + 1; i++)
        {
            distance[0][i] = i;
        }

        for (int i = 1; i <= letters1.length; i++)
        {
            for (int j = 1; j <= letters2.length; j++)
            {
                final int cost = letters1[i - 1] == letters2[j - 1] ? 0 : 1;
                distance[i][j] = Math.min(distance[i - 1][j] + 1,
                        Math.min(distance[i][j - 1] + 1, distance[i - 1][j - 1] + cost));
            }
        }

        return distance[letters1.length - 1][letters2.length - 1];
    }

    public UnknownOptionException(final Character option)
    {
        super("unknown short option \'" + option + "\'");
    }

    public UnknownOptionException(final String option, final Set<SimpleOption> validOptions)
    {
        super("unknown long option \'" + option + "\', did you mean "
                + closestMatch(option, validOptions) + "?");
    }
}
