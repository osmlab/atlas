package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
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
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.CountryBoundaryMapTemplate;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.OutputDirectoryTemplate;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.ShardingTemplate;

import com.google.gson.GsonBuilder;

/**
 * This command converts our many different file formats to a GeoJSON representation. This may be
 * useful for various visualization software.
 * 
 * @author lcram
 */
public class AnyToGeoJsonCommand extends AbstractAtlasShellToolsCommand
{
    private static final String ATLAS_OPTION_LONG = "atlas";
    private static final String ATLAS_OPTION_DESCRIPTION = "The path to an atlas file to be converted.";
    private static final String ATLAS_OPTION_HINT = "atlas-file";

    private static final String COUNTRIES_OPTION_LONG = "countries";
    private static final Character COUNTRIES_OPTION_SHORT = 'c';
    private static final String COUNTRIES_OPTION_DESCRIPTION = "A comma separated list of allowlist country codes to exclusively include. Defaults to all.";
    private static final String COUNTRIES_OPTION_HINT = "included-countries";

    private static final String COUNTRIES_DENY_LIST_OPTION_LONG = "countries-denylist";
    private static final Character COUNTRIES_DENY_LIST_OPTION_SHORT = 'C';
    private static final String COUNTRIES_DENY_LIST_OPTION_DESCRIPTION = "A comma separated denylist of country codes to explicitly exclude. Defaults to none.";
    private static final String COUNTRIES_DENY_LIST_OPTION_HINT = "excluded-countries";

    private static final String POLYGONS_OPTION_LONG = "use-polygons";
    private static final Character POLYGONS_OPTION_SHORT = 'p';
    private static final String POLYGONS_OPTION_DESCRIPTION = "Use polygons instead of linestrings for the boundary GeoJSON. This may be better for certain visualization software.";

    private static final Integer ATLAS_CONTEXT = 3;
    private static final Integer SHARDING_CONTEXT = 4;
    private static final Integer BOUNDARY_CONTEXT = 5;

    private static final String OUTPUT_FILE = "output";
    private static final String ATLAS_FILE = OUTPUT_FILE + "-" + ATLAS_OPTION_LONG
            + FileSuffix.GEO_JSON;
    private static final String SHARDING_FILE = OUTPUT_FILE + "-sharding" + FileSuffix.GEO_JSON;
    private static final String BOUNDARY_FILE = OUTPUT_FILE + "-country-boundary"
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
        registerManualPageSectionsFromTemplate(new ShardingTemplate());
        registerManualPageSectionsFromTemplate(new CountryBoundaryMapTemplate());
        registerManualPageSectionsFromTemplate(new OutputDirectoryTemplate());
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOptionWithRequiredArgument(ATLAS_OPTION_LONG, ATLAS_OPTION_DESCRIPTION,
                OptionOptionality.REQUIRED, ATLAS_OPTION_HINT, ATLAS_CONTEXT);
        registerOptionWithRequiredArgument(COUNTRIES_OPTION_LONG, COUNTRIES_OPTION_SHORT,
                COUNTRIES_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL, COUNTRIES_OPTION_HINT,
                BOUNDARY_CONTEXT);
        registerOptionWithRequiredArgument(COUNTRIES_DENY_LIST_OPTION_LONG,
                COUNTRIES_DENY_LIST_OPTION_SHORT, COUNTRIES_DENY_LIST_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, COUNTRIES_DENY_LIST_OPTION_HINT, BOUNDARY_CONTEXT);
        registerOption(POLYGONS_OPTION_LONG, POLYGONS_OPTION_SHORT, POLYGONS_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, BOUNDARY_CONTEXT);

        registerOptionsAndArgumentsFromTemplate(new ShardingTemplate(SHARDING_CONTEXT));
        registerOptionsAndArgumentsFromTemplate(new CountryBoundaryMapTemplate(BOUNDARY_CONTEXT));
        registerOptionsAndArgumentsFromTemplate(
                new OutputDirectoryTemplate(ATLAS_CONTEXT, SHARDING_CONTEXT, BOUNDARY_CONTEXT));

        super.registerOptionsAndArguments();
    }

    private int executeAtlasContext()
    {
        final File atlasFile = new File(this.optionAndArgumentDelegate
                .getOptionArgument(ATLAS_OPTION_LONG).orElseThrow(AtlasShellToolsException::new),
                this.getFileSystem());
        if (!atlasFile.exists())
        {
            this.outputDelegate
                    .printlnErrorMessage("file not found: " + atlasFile.getAbsolutePathString());
            return 1;
        }
        final Atlas atlas = new AtlasResourceLoader().load(atlasFile);
        final Optional<Path> pathOptional = OutputDirectoryTemplate.getOutputPath(this);
        if (pathOptional.isEmpty())
        {
            this.outputDelegate.printlnErrorMessage("could not save atlas file");
            return 1;
        }
        final Path concatenatedPath = Paths.get(pathOptional.get().toAbsolutePath().toString(),
                ATLAS_FILE);
        final File outputFile = new File(concatenatedPath.toAbsolutePath().toString(),
                this.getFileSystem());
        if (this.optionAndArgumentDelegate.hasVerboseOption())
        {
            this.outputDelegate.printlnCommandMessage(
                    "writing the atlas geojson file to " + outputFile.toAbsolutePath().toString());
        }
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
        Set<String> countriesDenyList = new HashSet<>();
        if (this.optionAndArgumentDelegate.hasOption(COUNTRIES_DENY_LIST_OPTION_LONG))
        {
            countriesDenyList = this.optionAndArgumentDelegate
                    .getOptionArgument(COUNTRIES_DENY_LIST_OPTION_LONG,
                            this::parseCommaSeparatedCountries)
                    .orElse(new HashSet<>());
        }

        CountryBoundaryMap map = null;
        final Optional<CountryBoundaryMap> mapOptional = CountryBoundaryMapTemplate
                .getCountryBoundaryMap(this);
        if (mapOptional.isEmpty())
        {
            this.outputDelegate.printlnErrorMessage("failed to load country boundary");
            return 1;
        }
        map = mapOptional.get();

        final String boundaryJson;
        if (countries.isEmpty())
        {
            boundaryJson = new CountryBoundaryMapGeoJsonConverter().prettyPrint(true)
                    .withCountryDenyList(countriesDenyList).usePolygons(usePolygons)
                    .convertToString(map);
        }
        else
        {
            boundaryJson = new CountryBoundaryMapGeoJsonConverter().withCountryAllowList(countries)
                    .withCountryDenyList(countriesDenyList).prettyPrint(true)
                    .usePolygons(usePolygons).convertToString(map);
        }
        if (this.optionAndArgumentDelegate.hasVerboseOption())
        {
            this.outputDelegate.printlnCommandMessage("converting boundary file to GeoJSON...");
        }
        final Optional<Path> pathOptional = OutputDirectoryTemplate.getOutputPath(this);
        if (pathOptional.isEmpty())
        {
            this.outputDelegate.printlnErrorMessage("could not save boundary file");
            return 1;
        }
        final Path concatenatedPath = Paths.get(pathOptional.get().toAbsolutePath().toString(),
                BOUNDARY_FILE);
        if (this.optionAndArgumentDelegate.hasVerboseOption())
        {
            this.outputDelegate.printlnCommandMessage("writing the boundary geojson file to "
                    + concatenatedPath.toAbsolutePath().toString());
        }
        new File(concatenatedPath.toAbsolutePath().toString(), this.getFileSystem())
                .writeAndClose(boundaryJson);

        return 0;
    }

    private int executeShardingContext()
    {
        final Sharding sharding = ShardingTemplate.getSharding(this);
        final String shardingJson = new GsonBuilder().setPrettyPrinting().create()
                .toJson(sharding.asGeoJson());
        final Optional<Path> pathOptional = OutputDirectoryTemplate.getOutputPath(this);
        if (pathOptional.isEmpty())
        {
            this.outputDelegate.printlnErrorMessage("could not save sharding tree");
            return 1;
        }
        final Path concatenatedPath = Paths.get(pathOptional.get().toAbsolutePath().toString(),
                SHARDING_FILE);
        if (this.optionAndArgumentDelegate.hasVerboseOption())
        {
            this.outputDelegate.printlnCommandMessage("writing the sharding geojson file to "
                    + concatenatedPath.toAbsolutePath().toString());
        }
        new File(concatenatedPath.toAbsolutePath().toString(), this.getFileSystem())
                .writeAndClose(shardingJson);
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
