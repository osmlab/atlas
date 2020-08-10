package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.boundary.CountryBoundary;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.converters.jts.JtsPointConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolyLineConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.geography.sharding.converters.StringToShardConverter;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.terminal.TTYAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class WKTShardCommand extends AbstractAtlasShellToolsCommand
{
    private static final Logger logger = LoggerFactory.getLogger(WKTShardCommand.class);

    private static final String SHARDING_OPTION_LONG = "sharding";
    private static final String SHARDING_OPTION_DESCRIPTION = "The sharding to use, e.g. dynamic@/Users/foo/my-tree.txt";
    private static final String SHARDING_OPTION_HINT = "type@parameter";

    private static final String INPUT_FILE_OPTION_LONG = "input";
    private static final String INPUT_FILE_OPTION_DESCRIPTION = "An input file from which to source the WKT entities. See DESCRIPTION section for details.";
    private static final String INPUT_FILE_OPTION_HINT = "file";

    private static final String COUNTRY_BOUNDARY_OPTION_LONG = "country-boundary";
    private static final String COUNTRY_BOUNDARY_OPTION_DESCRIPTION = "A boundary file to use for intersection checks. See DESCRIPTION section for details.";
    private static final String COUNTRY_BOUNDARY_OPTION_HINT = "boundary-file";

    private static final Integer SHARDING_CONTEXT = 3;
    private static final Integer COUNTRY_BOUNDARY_CONTEXT = 4;

    private static final String INPUT_WKT_SHARD = "wkt|shard";

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    public static void main(final String[] args)
    {
        new WKTShardCommand().runSubcommandAndExit(args);
    }

    public WKTShardCommand()
    {
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    public int execute()
    {
        final List<String> inputWktOrShard = new ArrayList<>();
        if (this.optionAndArgumentDelegate.hasOption(INPUT_FILE_OPTION_LONG))
        {
            inputWktOrShard.addAll(readInputsFromFile(this.optionAndArgumentDelegate
                    .getOptionArgument(INPUT_FILE_OPTION_LONG).orElse(null)));
        }
        inputWktOrShard.addAll(this.optionAndArgumentDelegate.getVariadicArgument(INPUT_WKT_SHARD));

        if (inputWktOrShard.isEmpty())
        {
            this.outputDelegate.printlnWarnMessage("no input WKTs were found");
            return 0;
        }

        Sharding sharding = null;
        CountryBoundaryMap countryBoundaryMap = null;
        if (this.optionAndArgumentDelegate.getParserContext() == SHARDING_CONTEXT)
        {
            sharding = Sharding.forString(
                    this.optionAndArgumentDelegate.getOptionArgument(SHARDING_OPTION_LONG)
                            .orElseThrow(AtlasShellToolsException::new));
        }
        else if (this.optionAndArgumentDelegate.getParserContext() == COUNTRY_BOUNDARY_CONTEXT)
        {
            final Optional<CountryBoundaryMap> mapOptional = loadCountryBoundaryMap();
            if (!mapOptional.isPresent())
            {
                this.outputDelegate.printlnErrorMessage("failed to load country boundary");
                return 1;
            }
            countryBoundaryMap = mapOptional.get();
        }
        else
        {
            throw new AtlasShellToolsException();
        }

        for (int i = 0; i < inputWktOrShard.size(); i++)
        {
            final String wktOrShard = inputWktOrShard.get(i);
            parseWktOrShardAndPrintOutput(wktOrShard, sharding, countryBoundaryMap);

            // Only print a separating newline if there were multiple entries
            if (i < inputWktOrShard.size() - 1)
            {
                this.outputDelegate.printlnStdout("");
            }
        }

        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "wkt-shard";
    }

    @Override
    public String getSimpleDescription()
    {
        return "perform various intersection lookups";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION",
                WKTShardCommand.class.getResourceAsStream("WKTShardCommandDescriptionSection.txt"));
        addManualPageSection("EXAMPLES",
                WKTShardCommand.class.getResourceAsStream("WKTShardCommandExamplesSection.txt"));
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerArgument(INPUT_WKT_SHARD, ArgumentArity.VARIADIC, ArgumentOptionality.OPTIONAL,
                SHARDING_CONTEXT, COUNTRY_BOUNDARY_CONTEXT);
        registerOptionWithRequiredArgument(INPUT_FILE_OPTION_LONG, INPUT_FILE_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, INPUT_FILE_OPTION_HINT, SHARDING_CONTEXT,
                COUNTRY_BOUNDARY_CONTEXT);
        registerOptionWithRequiredArgument(SHARDING_OPTION_LONG, SHARDING_OPTION_DESCRIPTION,
                OptionOptionality.REQUIRED, SHARDING_OPTION_HINT, SHARDING_CONTEXT);
        registerOptionWithRequiredArgument(COUNTRY_BOUNDARY_OPTION_LONG,
                COUNTRY_BOUNDARY_OPTION_DESCRIPTION, OptionOptionality.REQUIRED,
                COUNTRY_BOUNDARY_OPTION_HINT, COUNTRY_BOUNDARY_CONTEXT);
        super.registerOptionsAndArguments();
    }

    private Optional<CountryBoundaryMap> loadCountryBoundaryMap()
    {
        final Optional<CountryBoundaryMap> countryBoundaryMap;
        final File boundaryMapFile = new File(
                this.optionAndArgumentDelegate.getOptionArgument(COUNTRY_BOUNDARY_OPTION_LONG)
                        .orElseThrow(AtlasShellToolsException::new));
        if (!boundaryMapFile.exists())
        {
            this.outputDelegate.printlnErrorMessage(
                    "boundary file " + boundaryMapFile.getAbsolutePathString() + " does not exist");
            return Optional.empty();
        }
        if (this.optionAndArgumentDelegate.hasVerboseOption())
        {
            this.outputDelegate.printlnCommandMessage("loading country boundary map...");
        }
        countryBoundaryMap = Optional.of(CountryBoundaryMap.fromPlainText(boundaryMapFile));
        if (this.optionAndArgumentDelegate.hasVerboseOption())
        {
            this.outputDelegate.printlnCommandMessage("loaded boundary map");
        }
        return countryBoundaryMap;
    }

    private void parseWktOrShardAndPrintOutput(final String wktOrShard, final Sharding sharding,
            final CountryBoundaryMap countryBoundaryMap)
    {
        final Optional<Geometry> geometryOptional = parseWktOrShardString(wktOrShard);

        if (geometryOptional.isEmpty())
        {
            this.outputDelegate.printlnErrorMessage(
                    "unable to parse " + wktOrShard + " as WKT or shard string");
            return;
        }
        final Geometry geometry = geometryOptional.get();

        if (geometry instanceof Point)
        {
            printPointOutput(wktOrShard, geometry, sharding, countryBoundaryMap);
        }
        else if (geometry instanceof LineString)
        {
            printLineStringOutput(wktOrShard, geometry, sharding, countryBoundaryMap);
        }
        else if (geometry instanceof Polygon)
        {
            printPolygonOutput(wktOrShard, geometry, sharding, countryBoundaryMap);
        }
        /*
         * TODO handle more geometry types? e.g. MultiPoint, MultiLineString, and MultiPolygon?
         */
        else
        {
            this.outputDelegate.printlnErrorMessage("unsupported geometry type " + wktOrShard);
        }
    }

    private Optional<Geometry> parseWktOrShardString(final String wktOrShard)
    {
        final WKTReader reader = new WKTReader();
        try
        {
            return Optional.of(reader.read(wktOrShard));
        }
        catch (final ParseException exception)
        {
            logger.warn("unable to parse {} as wkt", wktOrShard, exception);
            // input String was not a WKT, so try parsing it as a shard string
            try
            {
                final StringToShardConverter converter = new StringToShardConverter();
                final Shard shard = converter.convert(wktOrShard);
                return Optional.of(new WKTReader().read(shard.toWkt()));
            }
            catch (final Exception exception2)
            {
                logger.warn("unable to parse {} as shard", wktOrShard, exception2);
            }
        }
        return Optional.empty();
    }

    private void printLineStringOutput(final String wktOrShard, final Geometry geometry,
            final Sharding sharding, final CountryBoundaryMap countryBoundaryMap)
    {
        this.outputDelegate.printlnStdout(wktOrShard + " intersects:", TTYAttribute.BOLD);
        final PolyLine polyline = new JtsPolyLineConverter().backwardConvert((LineString) geometry);
        if (sharding != null)
        {
            final Iterable<? extends Shard> shards = sharding.shardsIntersecting(polyline);
            for (final Shard shard : shards)
            {
                this.outputDelegate.printlnStdout(shard.toString(), TTYAttribute.GREEN);
            }
        }

        if (countryBoundaryMap != null)
        {
            final List<CountryBoundary> boundaries = countryBoundaryMap.boundaries(polyline);
            for (final CountryBoundary boundary : boundaries)
            {
                this.outputDelegate.printlnStdout(boundary.getCountryName(), TTYAttribute.GREEN);
            }
        }
    }

    private void printPointOutput(final String wktOrShard, final Geometry geometry,
            final Sharding sharding, final CountryBoundaryMap countryBoundaryMap)
    {
        this.outputDelegate.printlnStdout(wktOrShard + " covered by:", TTYAttribute.BOLD);
        final Location location = new JtsPointConverter().backwardConvert((Point) geometry);
        if (sharding != null)
        {
            final Iterable<? extends Shard> shards = sharding.shardsCovering(location);
            for (final Shard shard : shards)
            {
                this.outputDelegate.printlnStdout(shard.toString(), TTYAttribute.GREEN);
            }
        }
        if (countryBoundaryMap != null)
        {
            final List<CountryBoundary> boundaries = countryBoundaryMap.boundaries(location);
            for (final CountryBoundary boundary : boundaries)
            {
                this.outputDelegate.printlnStdout(boundary.getCountryName(), TTYAttribute.GREEN);
            }
        }
    }

    private void printPolygonOutput(final String wktOrShard, final Geometry geometry,
            final Sharding sharding, final CountryBoundaryMap countryBoundaryMap)
    {
        this.outputDelegate.printlnStdout(wktOrShard + " contains or intersects:",
                TTYAttribute.BOLD);
        final org.openstreetmap.atlas.geography.Polygon polygon = new JtsPolygonConverter()
                .backwardConvert((Polygon) geometry);
        if (sharding != null)
        {
            final Iterable<? extends Shard> shards = sharding.shards(polygon);
            for (final Shard shard : shards)
            {
                this.outputDelegate.printlnStdout(shard.toString(), TTYAttribute.GREEN);
            }
        }

        if (countryBoundaryMap != null)
        {
            /*
             * This is handled a little differently here than in the other printXOutput methods.
             * This is because the CountryBoundaryMap#boundaries method does not handle certain
             * cases the way we want it to, e.g. like when a shard boundary or other large polygon
             * completely encloses a country boundary (think large ocean shard totally containing a
             * small island country). We still want to report those enclosed boundaries in this
             * case. In the future, we may want to fix CountryBoundaryMap to handle this case in
             * some way.
             */
            final List<org.locationtech.jts.geom.Polygon> polygons = countryBoundaryMap
                    .query(geometry.getEnvelopeInternal()).stream().distinct()
                    .collect(Collectors.toList());
            final Set<String> countries = new HashSet<>();
            polygons.forEach(polygon2 -> countries
                    .add(CountryBoundaryMap.getGeometryProperty(polygon2, ISOCountryTag.KEY)));
            for (final String country : countries)
            {
                this.outputDelegate.printlnStdout(country, TTYAttribute.GREEN);
            }
        }
    }

    private List<String> readInputsFromFile(final String path)
    {
        if (path == null)
        {
            throw new AtlasShellToolsException();
        }
        final Path inputPath = Paths.get(path);
        if (inputPath.toString().startsWith("~"))
        {
            this.outputDelegate.printlnWarnMessage("the '~' was not expanded by your shell");
        }
        if (!inputPath.toAbsolutePath().toFile().canRead()
                || !inputPath.toAbsolutePath().toFile().isFile())
        {
            this.outputDelegate.printlnErrorMessage(
                    inputPath.toAbsolutePath().toString() + " is not a readable file");
            return new ArrayList<>();
        }
        final List<String> wktOrShardList = new ArrayList<>();
        final StringResource resource = new StringResource();
        resource.copyFrom(new File(inputPath.toAbsolutePath().toString()));
        final String rawText = resource.all();

        final String[] split = rawText.split(System.getProperty("line.separator"));
        for (final String line : split)
        {
            if (!line.isEmpty())
            {
                wktOrShardList.add(line);
            }
        }

        return wktOrShardList;
    }
}
