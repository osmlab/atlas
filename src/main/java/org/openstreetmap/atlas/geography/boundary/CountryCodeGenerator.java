package org.openstreetmap.atlas.geography.boundary;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * Generate complete country code list and excluded list based on the given boundary file and
 * country code list
 *
 * @author tony
 */
public class CountryCodeGenerator extends Command
{
    private static final String SEPARATOR = ",";
    private static final Switch<java.io.File> BOUNDARY = new Switch<>("boundary",
            "The country boundary file", path -> new java.io.File(path), Optionality.REQUIRED);
    private static final Switch<Set<String>> COUNTRIES_TO_EXCLUDE = new Switch<>(
            "countriesToExclude", "The comma separated country name list to exclude",
            countries -> new HashSet<>(Arrays.asList(countries.split(SEPARATOR))),
            Optionality.OPTIONAL);

    public static void main(final String[] args)
    {
        new CountryCodeGenerator().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final java.io.File boundaryFile = (java.io.File) command.get(BOUNDARY);
        final CountryBoundaryMap boundaryMap = CountryBoundaryMap.fromShapeFile(boundaryFile);
        final StringList allCountries = boundaryMap.countryCodesOverlappingWith(Rectangle.MAXIMUM);
        System.out.println("The number of all countries: " + allCountries.size());
        System.out.println(allCountries.join(SEPARATOR));

        @SuppressWarnings("unchecked")
        final Set<String> countriesToExclude = (Set<String>) command.get(COUNTRIES_TO_EXCLUDE);
        if (countriesToExclude != null)
        {
            final StringList filtered = new StringList();
            for (final String country : allCountries)
            {
                if (!countriesToExclude.contains(country))
                {
                    filtered.add(country);
                }
            }
            System.out.println("The number of all filtered countries: " + filtered.size());
            System.out.println(filtered.join(SEPARATOR));
        }
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(BOUNDARY, COUNTRIES_TO_EXCLUDE);
    }

}
