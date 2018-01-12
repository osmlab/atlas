package org.openstreetmap.atlas.utilities.matching;

import java.io.Serializable;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;

/**
 * A {@code Predicate<String>} utility to find matching names
 *
 * @author brian_l_davis
 */
public class NameMatcher implements Serializable, Predicate<String>
{
    private static final long serialVersionUID = 5446378702031541204L;

    private static final int DEFAULT_LEVENSHTEIN_DISTANCE_THRESHOLD = 1;

    private final String sourceName;

    // Uses Levenstein Distance to compare strings when true
    private boolean fuzzyMatch = false;
    // The minimum number of single-character differences allow to still match (see
    // https://en.wikipedia.org/wiki/Levenshtein_distance)
    private int lavenshteinDistanceThreshold = DEFAULT_LEVENSHTEIN_DISTANCE_THRESHOLD;
    // Null values are considered a match when true
    private boolean matchNull = false;
    // Requires strings have the same characters and casing
    private boolean exactMatch = false;

    public NameMatcher(final String sourceName)
    {
        this.sourceName = sourceName;
    }

    public NameMatcher matchExactly()
    {
        this.exactMatch = true;
        this.fuzzyMatch = false;
        return this;
    }

    public NameMatcher matchNulls()
    {
        this.matchNull = true;
        return this;
    }

    public NameMatcher matchSimilar()
    {
        this.exactMatch = false;
        this.fuzzyMatch = true;
        return this;
    }

    public NameMatcher matchSimilar(final int lavenshteinDistanceThreshold)
    {
        this.exactMatch = false;
        this.fuzzyMatch = true;
        this.lavenshteinDistanceThreshold = lavenshteinDistanceThreshold;
        return this;
    }

    @Override
    public boolean test(final String candidateName)
    {
        if (candidateName == null)
        {
            return matchNull;
        }
        if (exactMatch)
        {
            return candidateName.equals(sourceName);
        }
        if (fuzzyMatch)
        {
            return nameFuzzyMatch(sourceName, candidateName);
        }
        return candidateName.equalsIgnoreCase(sourceName);
    }

    private boolean nameFuzzyMatch(final String nameA, final String nameB)
    {
        return nameA.equalsIgnoreCase(nameB) || StringUtils.getLevenshteinDistance(nameA, nameB,
                lavenshteinDistanceThreshold) != -1;
    }
}
