package org.openstreetmap.atlas.utilities.command;

/**
 * @author lcram
 */
public final class Levenshtein
{
    /**
     * Compute the Levenshtein distance between two given strings.
     *
     * @see "https://en.wikipedia.org/wiki/Levenshtein_distance"
     * @param string1
     *            the first string
     * @param string2
     *            the second string
     * @return the distance
     */
    public static int levenshtein(final String string1, final String string2)
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

    private Levenshtein()
    {

    }
}
