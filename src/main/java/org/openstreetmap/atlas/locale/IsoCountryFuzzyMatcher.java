package org.openstreetmap.atlas.locale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.openstreetmap.atlas.exception.CoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends {@link IsoCountry} functionality with fuzzy matching for display names. Note that this
 * functionality is very simple, and assumes dependence on English country name representations.
 *
 * @author lcram
 */
public final class IsoCountryFuzzyMatcher
{
    private static final Logger logger = LoggerFactory.getLogger(IsoCountryFuzzyMatcher.class);

    /**
     * Provides closest IsoCountries for a country display name. If the given display name does not
     * perfectly match a valid IsoCountry, this will return the closest string match up to number of
     * matches.
     *
     * @param number
     *            the number of matches to show
     * @param displayCountry
     *            the display country name, e.g. "united stats"
     * @return an Optional containing the IsoCountry if present
     */
    public static List<IsoCountry> forDisplayCountryTopMatches(final int number,
            final String displayCountry)
    {
        if (displayCountry != null)
        {
            final List<IsoCountry> results = new ArrayList<>();
            if (IsoCountry.DISPLAY_COUNTRY_TO_ISO2.containsKey(displayCountry))
            {

                results.add(IsoCountry.ISO_COUNTRIES
                        .get(IsoCountry.DISPLAY_COUNTRY_TO_ISO2.get(displayCountry)));
            }
            else
            {
                final List<String> closestCountries = closestIsoCountries(number, displayCountry);
                if (!closestCountries.isEmpty())
                {
                    logger.debug(
                            "Exact match for {} was not found, returning closest {} matches {}",
                            displayCountry, number, closestCountries);
                    results.addAll(closestCountries.stream()
                            .map(countryString -> IsoCountry.ISO_COUNTRIES
                                    .get(IsoCountry.DISPLAY_COUNTRY_TO_ISO2.get(countryString)))
                            .collect(Collectors.toList()));
                }
            }
            return results;
        }
        return new ArrayList<>();
    }

    private static List<String> closestIsoCountries(final int number, final String displayCountry)
    {
        if (number <= 0 || number > IsoCountry.ALL_DISPLAY_COUNTRIES.size())
        {
            throw new CoreException("number " + number + " out of range (0, "
                    + IsoCountry.ALL_DISPLAY_COUNTRIES.size() + "]");
        }
        final Map<String, Integer> countryRankings = new HashMap<>();
        for (final String countryName : IsoCountry.ALL_DISPLAY_COUNTRIES)
        {
            final String countryNameLowerCase = countryName.toLowerCase();
            final String[] nameComponents = countryNameLowerCase.split("\\s+");
            if (nameComponents.length > 1)
            {
                int bestInterCountryDistance = Integer.MAX_VALUE;
                for (final String nameComponent : nameComponents)
                {
                    final int distance = LevenshteinDistance.getDefaultInstance()
                            .apply(displayCountry, nameComponent);
                    if (distance < bestInterCountryDistance)
                    {
                        bestInterCountryDistance = distance;
                    }
                    countryRankings.put(countryName, bestInterCountryDistance);
                }
            }
            else
            {
                final int distance = LevenshteinDistance.getDefaultInstance().apply(displayCountry,
                        countryNameLowerCase);
                countryRankings.put(countryName, distance);
            }
        }
        final List<Entry<String, Integer>> entries = new ArrayList<>(countryRankings.entrySet());
        entries.sort(Entry.comparingByValue());

        return entries.subList(0, number).stream().map(Entry::getKey).collect(Collectors.toList());
    }

    private IsoCountryFuzzyMatcher()
    {

    }
}
