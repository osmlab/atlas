package org.openstreetmap.atlas.geography.atlas;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

import com.google.common.collect.Iterables;

/**
 * Any command that wants to load Atlas files based on certain criteria (ISO country codes, etc...)
 * can subclass this command and receive that behavior for free. Currently we only support the ISO
 * country codes with the -include-only flag
 *
 * @author cstaylor
 */
public abstract class AtlasLoadingCommand extends Command
{
    /**
     * Lets us filter both Paths and Strings based on the ISO country names to be included or
     * excluded
     *
     * @author cstaylor
     */
    public static class AcceptableInputFileFilter implements Predicate<Resource>
    {
        /**
         * These are for extracting the ISO3 country code from the filename
         */
        private static final int START_ISO3_NAME_INDEX = 0;
        private static final int END_ISO3_NAME_INDEX = 3;

        private final Set<String> acceptableISOCodes = new HashSet<>();
        private final Set<String> excludedISOCodes = new HashSet<>();

        public AcceptableInputFileFilter exclude(final Iterable<String> exclude)
        {
            if (exclude != null)
            {
                Iterables.addAll(this.excludedISOCodes, exclude);
            }
            return this;
        }

        public AcceptableInputFileFilter include(final Iterable<String> include)
        {
            if (include != null)
            {
                Iterables.addAll(this.acceptableISOCodes, include);
            }
            return this;
        }

        @Override
        public boolean test(final Resource fileName)
        {
            final String isoCode = fileName.getName().substring(START_ISO3_NAME_INDEX,
                    END_ISO3_NAME_INDEX);
            if (this.acceptableISOCodes.size() > 0)
            {
                if (!this.acceptableISOCodes.contains(isoCode))
                {
                    return false;
                }
            }
            return this.excludedISOCodes.size() == 0 || !this.excludedISOCodes.contains(isoCode);
        }
    }

    protected static final Switch<Set<String>> INCLUDE_ONLY_THESE_COUNTRIES_PARAMETER = new Switch<>(
            "include-only",
            "list of comma-delimited ISO country codes that we'll include when searching folders for atlas files",
            possiblyMultipleISOs -> StringList.split(possiblyMultipleISOs, ",").stream()
                    .collect(Collectors.toSet()),
            Optionality.OPTIONAL);

    protected static final Switch<File> INPUT_FOLDER = new Switch<>("inputFolder",
            "Path of folder which contains Atlas files", File::new, Command.Optionality.OPTIONAL);

    protected static final Switch<File> INPUT = new Switch<>("input", "Path of Atlas file",
            File::new, Command.Optionality.OPTIONAL);

    protected static final Switch<Set<String>> EXCLUDE_THESE_COUNTRIES_PARAMETER = new Switch<>(
            "exclude",
            "list of comma-delimited ISO country codes that we'll exclude when searching folders for atlas files",
            possiblyMultipleISOs -> StringList.split(possiblyMultipleISOs, ",").stream()
                    .collect(Collectors.toSet()),
            Optionality.OPTIONAL);

    /**
     * Helper method for loading a MultiAtlas based on the criteria passed in through the command
     * line.
     *
     * @param commandMap
     *            the list of command line arguments
     * @return a MultiAtlas containing the data from files who matched our search criteria
     */
    protected Atlas loadAtlas(final CommandMap commandMap)
    {
        return loadAtlas(INPUT_FOLDER, commandMap);
    }

    /**
     * Helper method for loading a MultiAtlas based on the criteria passed in through the command
     * line.
     *
     * @param parameter
     *            the command line parameter to use for loading the atlas files
     * @param commandMap
     *            the list of command line arguments
     * @return a MultiAtlas containing the data from files who matched our search criteria
     */
    @SuppressWarnings("unchecked")
    protected Atlas loadAtlas(final Switch<File> parameter, final CommandMap commandMap)
    {
        final AcceptableInputFileFilter filter = new AcceptableInputFileFilter()
                .include((Set<String>) commandMap.get(INCLUDE_ONLY_THESE_COUNTRIES_PARAMETER))
                .exclude((Set<String>) commandMap.get(EXCLUDE_THESE_COUNTRIES_PARAMETER));
        final File input = (File) commandMap.get(INPUT);
        if (input != null)
        {
            return new AtlasResourceLoader().load(input);
        }
        final File inputFolder = (File) commandMap.get(parameter);
        if (inputFolder == null)
        {
            throw new CoreException("Switch missing: input file or input folder");
        }
        return new AtlasResourceLoader().withResourceFilter(filter).load(inputFolder);
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(INPUT, INPUT_FOLDER, INCLUDE_ONLY_THESE_COUNTRIES_PARAMETER,
                EXCLUDE_THESE_COUNTRIES_PARAMETER);
    }
}
