package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.boundary.converters.CountryBoundaryMapGeoJsonConverter;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.MultipleOutputCommand;

import com.google.gson.GsonBuilder;

/**
 * This command converts our many different file formats to a GeoJSON representation. This may be
 * useful for various visualization software.
 * 
 * @author lcram
 */
public class AnyToGeoJsonCommand extends MultipleOutputCommand
{
    private static final String ATLAS_OPTION_LONG = "atlas";
    private static final String ATLAS_OPTION_DESCRIPTION = "The path to an atlas file to be converted.";
    private static final String ATLAS_OPTION_HINT = "atlas-file";

    private static final String SHARDING_OPTION_LONG = "sharding";
    private static final String SHARDING_OPTION_DESCRIPTION = "The sharding to convert, e.g. dynamic@/Users/foo/my-tree.txt";
    private static final String SHARDING_OPTION_HINT = "type@parameter";

    private static final String BOUNDARY_OPTION_LONG = "boundary";
    private static final String BOUNDARY_OPTION_DESCRIPTION = "The path to a boundary file to be converted.";
    private static final String BOUNDARY_OPTION_HINT = "boundary-file";

    private static final String COUNTRIES_OPTION_LONG = "countries";
    private static final Character COUNTRIES_OPTION_SHORT = 'c';
    private static final String COUNTRIES_OPTION_DESCRIPTION = "A comma separated list of whitelist country codes to exclusively include. Defaults to all.";
    private static final String COUNTRIES_OPTION_HINT = "included-countries";

    private static final String COUNTRIES_BLACKLIST_OPTION_LONG = "countries-blacklist";
    private static final Character COUNTRIES_BLACKLIST_OPTION_SHORT = 'C';
    private static final String COUNTRIES_BLACKLIST_OPTION_DESCRIPTION = "A comma separated blacklist of country codes to explicitly exclude. Defaults to none.";
    private static final String COUNTRIES_BLACKLIST_OPTION_HINT = "excluded-countries";

    private static final String POLYGONS_OPTION_LONG = "use-polygons";
    private static final Character POLYGONS_OPTION_SHORT = 'p';
    private static final String POLYGONS_OPTION_DESCRIPTION = "Use polygons instead of linestrings for the boundary GeoJSON. This may be better for certain visualization software.";

    private static final Integer ATLAS_CONTEXT = 3;
    private static final Integer SHARDING_CONTEXT = 4;
    private static final Integer BOUNDARY_CONTEXT = 5;

    private static final String OUTPUT_FILE = "output";
    private static final String ATLAS_FILE = OUTPUT_FILE + "-" + ATLAS_OPTION_LONG
            + FileSuffix.GEO_JSON;
    private static final String SHARDING_FILE = OUTPUT_FILE + "-" + SHARDING_OPTION_LONG
            + FileSuffix.GEO_JSON;
    private static final String BOUNDARY_FILE = OUTPUT_FILE + "-" + BOUNDARY_OPTION_LONG
            + FileSuffix.GEO_JSON;

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    public static void main(final String[] args)
    {
        new AnyToGeoJsonCommand().runSubcommandAndExit(args);
    }

    public AnyToGeoJsonCommand()
    {
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    public int execute()
    {
        super.execute();

        if (this.optionAndArgumentDelegate.getParserContext() == ATLAS_CONTEXT)
        {
            return executeAtlasContext();
        }
        else if (this.optionAndArgumentDelegate.getParserContext() == SHARDING_CONTEXT)
        {
            return executeShardingContext();
        }
        else if (this.optionAndArgumentDelegate.getParserContext() == BOUNDARY_CONTEXT)
        {
            return executeBoundaryContext();
        }
        else
        {
            throw new AtlasShellToolsException();
        }
    }

    @Override
    public String getCommandName()
    {
        return "any2geojson";
    }

    @Override
    public String getSimpleDescription()
    {
        return "convert a custom file format to GeoJSON";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION", AtlasSearchCommand.class
                .getResourceAsStream("AnyToGeoJsonCommandDescriptionSection.txt"));
        addManualPageSection("EXAMPLES", AtlasSearchCommand.class
                .getResourceAsStream("AnyToGeoJsonCommandExamplesSection.txt"));
        super.registerManualPageSections();
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOptionWithRequiredArgument(ATLAS_OPTION_LONG, ATLAS_OPTION_DESCRIPTION,
                OptionOptionality.REQUIRED, ATLAS_OPTION_HINT, ATLAS_CONTEXT);
        registerOptionWithRequiredArgument(SHARDING_OPTION_LONG, SHARDING_OPTION_DESCRIPTION,
                OptionOptionality.REQUIRED, SHARDING_OPTION_HINT, SHARDING_CONTEXT);
        registerOptionWithRequiredArgument(BOUNDARY_OPTION_LONG, BOUNDARY_OPTION_DESCRIPTION,
                OptionOptionality.REQUIRED, BOUNDARY_OPTION_HINT, BOUNDARY_CONTEXT);
        registerOptionWithRequiredArgument(COUNTRIES_OPTION_LONG, COUNTRIES_OPTION_SHORT,
                COUNTRIES_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL, COUNTRIES_OPTION_HINT,
                BOUNDARY_CONTEXT);
        registerOptionWithRequiredArgument(COUNTRIES_BLACKLIST_OPTION_LONG,
                COUNTRIES_BLACKLIST_OPTION_SHORT, COUNTRIES_BLACKLIST_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, COUNTRIES_BLACKLIST_OPTION_HINT, BOUNDARY_CONTEXT);
        registerOption(POLYGONS_OPTION_LONG, POLYGONS_OPTION_SHORT, POLYGONS_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, BOUNDARY_CONTEXT);
        super.registerOptionsAndArguments();
    }

    private int executeAtlasContext()
    {
        final File atlasFile = new File(this.optionAndArgumentDelegate
                .getOptionArgument(ATLAS_OPTION_LONG).orElseThrow(AtlasShellToolsException::new));
        if (!atlasFile.exists())
        {
            this.outputDelegate
                    .printlnErrorMessage("file not found: " + atlasFile.getAbsolutePath());
            return 1;
        }
        final Atlas atlas = new AtlasResourceLoader().load(atlasFile);
        final Path concatenatedPath = Paths.get(getOutputPath().toAbsolutePath().toString(),
                ATLAS_FILE);
        final File outputFile = new File(concatenatedPath.toAbsolutePath().toString());
        atlas.saveAsLineDelimitedGeoJsonFeatures(outputFile, (entity, json) ->
        {
            // Dummy consumer, we don't need to mutate the JSON
        });
        return 0;
    }

    private int executeBoundaryContext()
    {
        Set<String> countries = new HashSet<>();
        if (this.optionAndArgumentDelegate.hasOption(COUNTRIES_OPTION_LONG))
        {
            countries = this.optionAndArgumentDelegate
                    .getOptionArgument(COUNTRIES_OPTION_LONG, this::parseCommaSeparatedCountries)
                    .orElse(new HashSet<>());
        }
        final boolean usePolygons = this.optionAndArgumentDelegate.hasOption(POLYGONS_OPTION_LONG);
        Set<String> countriesBlacklist = new HashSet<>();
        if (this.optionAndArgumentDelegate.hasOption(COUNTRIES_BLACKLIST_OPTION_LONG))
        {
            countriesBlacklist = this.optionAndArgumentDelegate
                    .getOptionArgument(COUNTRIES_BLACKLIST_OPTION_LONG,
                            this::parseCommaSeparatedCountries)
                    .orElse(new HashSet<>());
        }

        if (this.optionAndArgumentDelegate.hasVerboseOption())
        {
            this.outputDelegate.printlnCommandMessage("reading CountryBoundaryMap from file...");
        }
        final CountryBoundaryMap map = CountryBoundaryMap.fromPlainText(
                new File(this.optionAndArgumentDelegate.getOptionArgument(BOUNDARY_OPTION_LONG)
                        .orElseThrow(AtlasShellToolsException::new)));
        final String boundaryJson;
        if (countries.isEmpty())
        {
            boundaryJson = new CountryBoundaryMapGeoJsonConverter().prettyPrint(true)
                    .withCountryBlacklist(countriesBlacklist).usePolygons(usePolygons)
                    .convertToString(map);
        }
        else
        {
            boundaryJson = new CountryBoundaryMapGeoJsonConverter().withCountryWhitelist(countries)
                    .withCountryBlacklist(countriesBlacklist).prettyPrint(true)
                    .usePolygons(usePolygons).convertToString(map);
        }
        if (this.optionAndArgumentDelegate.hasVerboseOption())
        {
            this.outputDelegate.printlnCommandMessage("converting boundary file to GeoJSON...");
        }

        if (this.optionAndArgumentDelegate.hasVerboseOption())
        {
            this.outputDelegate.printlnCommandMessage("writing the boundary file...");
        }
        final Path concatenatedPath = Paths.get(getOutputPath().toAbsolutePath().toString(),
                BOUNDARY_FILE);
        new File(concatenatedPath.toAbsolutePath().toString()).writeAndClose(boundaryJson);

        return 0;
    }

    private int executeShardingContext()
    {
        final String shardingString = this.optionAndArgumentDelegate
                .getOptionArgument(SHARDING_OPTION_LONG).orElseThrow(AtlasShellToolsException::new);
        final Sharding sharding;
        try
        {
            sharding = Sharding.forString(shardingString);
        }
        catch (final Exception exception)
        {
            this.outputDelegate.printlnErrorMessage(exception.getMessage());
            return 1;
        }

        final String shardingJson = new GsonBuilder().setPrettyPrinting().create()
                .toJson(sharding.asGeoJson());
        final Path concatenatedPath = Paths.get(getOutputPath().toAbsolutePath().toString(),
                SHARDING_FILE);
        new File(concatenatedPath.toAbsolutePath().toString()).writeAndClose(shardingJson);
        return 0;
    }

    private Set<String> parseCommaSeparatedCountries(final String countryString)
    {
        final Set<String> countrySet = new HashSet<>();

        if (countryString.isEmpty())
        {
            return countrySet;
        }

        countrySet.addAll(Arrays.stream(countryString.split(",")).collect(Collectors.toSet()));
        return countrySet;
    }
}
