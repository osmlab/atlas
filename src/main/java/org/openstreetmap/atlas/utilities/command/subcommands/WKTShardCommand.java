package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.converters.jts.JtsPointConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolyLineConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.StringResource;
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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * @author lcram
 */
public class WKTShardCommand extends AbstractAtlasShellToolsCommand
{
    private static final Logger logger = LoggerFactory.getLogger(WKTShardCommand.class);

    private static final String TREE_OPTION_LONG = "tree";
    private static final String TREE_OPTION_DESCRIPTION = "The path to the dynamic sharding tree file. E.g. /Users/example/path/to/tree.txt";
    private static final String TREE_OPTION_HINT = "path";

    private static final String SLIPPY_OPTION_LONG = "slippy";
    private static final String SLIPPY_OPTION_DESCRIPTION = "The slippy tile zoom level for the sharding.";
    private static final String SLIPPY_OPTION_HINT = "zoom";

    private static final String INPUT_FILE_OPTION_LONG = "input";
    private static final String INPUT_FILE_OPTION_DESCRIPTION = "An input file from which to source the WKT entities. See DESCRIPTION section for details.";
    private static final String INPUT_FILE_OPTION_HINT = "file";

    private static final Integer TREE_CONTEXT = 3;
    private static final Integer SLIPPY_CONTEXT = 4;

    private static final String INPUT_WKT = "wkt";

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
        final List<String> inputWKT = new ArrayList<>();
        if (this.optionAndArgumentDelegate.hasOption(INPUT_FILE_OPTION_LONG))
        {
            inputWKT.addAll(readWKTFromFile(
                    this.optionAndArgumentDelegate.getOptionArgument(INPUT_FILE_OPTION_LONG)));
        }
        inputWKT.addAll(this.optionAndArgumentDelegate.getVariadicArgument(INPUT_WKT));

        if (inputWKT.isEmpty())
        {
            this.outputDelegate.printlnWarnMessage("no input WKTs were found");
            return 0;
        }

        final Sharding sharding;
        if (this.optionAndArgumentDelegate.getParserContext() == TREE_CONTEXT
                && this.optionAndArgumentDelegate.hasOption(TREE_OPTION_LONG))
        {
            sharding = Sharding.forString(
                    "dynamic@" + this.optionAndArgumentDelegate.getOptionArgument(TREE_OPTION_LONG)
                            .orElseThrow(AtlasShellToolsException::new));
        }
        else if (this.optionAndArgumentDelegate.getParserContext() == SLIPPY_CONTEXT
                && this.optionAndArgumentDelegate.hasOption(SLIPPY_OPTION_LONG))
        {
            sharding = Sharding.forString(
                    "slippy@" + this.optionAndArgumentDelegate.getOptionArgument(SLIPPY_OPTION_LONG)
                            .orElseThrow(AtlasShellToolsException::new));
        }
        else
        {
            throw new AtlasShellToolsException();
        }

        for (int i = 0; i < inputWKT.size(); i++)
        {
            final String wkt = inputWKT.get(i);
            parseWKTAndPrintOutput(wkt, sharding);

            // Only print a separating newline if there were multiple entries
            if (i < inputWKT.size() - 1)
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
        return "get the shards that intersect some given WKT(s)";
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
        registerArgument(INPUT_WKT, ArgumentArity.VARIADIC, ArgumentOptionality.OPTIONAL,
                TREE_CONTEXT, SLIPPY_CONTEXT);
        registerOptionWithRequiredArgument(INPUT_FILE_OPTION_LONG, INPUT_FILE_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, INPUT_FILE_OPTION_HINT, TREE_CONTEXT, SLIPPY_CONTEXT);
        registerOptionWithRequiredArgument(TREE_OPTION_LONG, TREE_OPTION_DESCRIPTION,
                OptionOptionality.REQUIRED, TREE_OPTION_HINT, TREE_CONTEXT);
        registerOptionWithRequiredArgument(SLIPPY_OPTION_LONG, SLIPPY_OPTION_DESCRIPTION,
                OptionOptionality.REQUIRED, SLIPPY_OPTION_HINT, SLIPPY_CONTEXT);
        super.registerOptionsAndArguments();
    }

    private void parseWKTAndPrintOutput(final String wkt, final Sharding sharding)
    {
        final WKTReader reader = new WKTReader();
        Geometry geometry = null;
        try
        {
            geometry = reader.read(wkt);
        }
        catch (final ParseException exception)
        {
            logger.error("unable to parse {}", wkt, exception);
        }

        if (geometry instanceof Point)
        {
            this.outputDelegate.printlnStdout(wkt + " covered by:", TTYAttribute.BOLD);
            final Location location = new JtsPointConverter().backwardConvert((Point) geometry);
            final Iterable<? extends Shard> shards = sharding.shardsCovering(location);
            for (final Shard shard : shards)
            {
                this.outputDelegate.printlnStdout(shard.toString(), TTYAttribute.GREEN);
            }
        }
        else if (geometry instanceof LineString)
        {
            this.outputDelegate.printlnStdout(wkt + " intersects:", TTYAttribute.BOLD);
            final PolyLine polyline = new JtsPolyLineConverter()
                    .backwardConvert((LineString) geometry);
            final Iterable<? extends Shard> shards = sharding.shardsIntersecting(polyline);
            for (final Shard shard : shards)
            {
                this.outputDelegate.printlnStdout(shard.toString(), TTYAttribute.GREEN);
            }
        }
        else if (geometry instanceof Polygon)
        {
            this.outputDelegate.printlnStdout(wkt + " intersects:", TTYAttribute.BOLD);
            final org.openstreetmap.atlas.geography.Polygon polygon = new JtsPolygonConverter()
                    .backwardConvert((Polygon) geometry);
            final Iterable<? extends Shard> shards = sharding.shards(polygon);
            for (final Shard shard : shards)
            {
                this.outputDelegate.printlnStdout(shard.toString(), TTYAttribute.GREEN);
            }
        }
        /*
         * TODO handle more geometry types? e.g. MultiPoint, MultiLineString, and MultiPolygon?
         */
        else
        {
            this.outputDelegate.printlnErrorMessage("unsupported geometry type " + wkt);
        }
    }

    private List<String> readWKTFromFile(final Optional<String> pathOptional)
    {
        if (!pathOptional.isPresent())
        {
            throw new AtlasShellToolsException();
        }
        final Path inputPath = Paths.get(pathOptional.get());
        if (inputPath.toString().startsWith("~"))
        {
            this.outputDelegate.printlnWarnMessage("the \'~\' was not expanded by your shell");
        }
        if (!inputPath.toAbsolutePath().toFile().canRead()
                || !inputPath.toAbsolutePath().toFile().isFile())
        {
            this.outputDelegate.printlnErrorMessage(
                    inputPath.toAbsolutePath().toString() + " is not a readable file");
            return new ArrayList<>();
        }
        final List<String> wktList = new ArrayList<>();
        final StringResource resource = new StringResource();
        resource.copyFrom(new File(inputPath.toAbsolutePath().toString()));
        final String rawText = resource.all();

        final String[] split = rawText.split(System.getProperty("line.separator"));
        for (final String line : split)
        {
            if (!line.isEmpty())
            {
                wktList.add(line);
            }
        }

        return wktList;
    }
}
