package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.List;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.converters.jts.JtsPointConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolyLineConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentFetcher;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
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

    private static final String DESCRIPTION_SECTION = "WKTShardCommandDescriptionSection.txt";
    private static final String EXAMPLES_SECTION = "WKTShardCommandExamplesSection.txt";

    private static final String TREE_OPTION_LONG = "tree";
    private static final String TREE_OPTION_DESCRIPTION = "The path to the dynamic sharding tree file. E.g. /Users/example/path/to/tree.txt";
    private static final String TREE_OPTION_HINT = "path";

    private static final String SLIPPY_OPTION_LONG = "slippy";
    private static final String SLIPPY_OPTION_DESCRIPTION = "The slippy tile zoom level for the sharding.";
    private static final String SLIPPY_OPTION_HINT = "zoom";

    private static final Integer TREE_CONTEXT = 3;
    private static final Integer SLIPPY_CONTEXT = 4;

    private static final String INPUT_WKT = "wkt";

    private final OptionAndArgumentFetcher fetcher;
    private final CommandOutputDelegate output;

    public static void main(final String[] args)
    {
        new WKTShardCommand().runSubcommandAndExit(args);
    }

    public WKTShardCommand()
    {
        this.fetcher = this.getOptionAndArgumentFetcher();
        this.output = this.getCommandOutputDelegate();
    }

    @Override
    public int execute()
    {
        final List<String> inputWKT = this.fetcher.getVariadicArgument(INPUT_WKT);

        final Sharding sharding;
        if (this.fetcher.getParserContext() == TREE_CONTEXT)
        {
            if (this.fetcher.hasOption(TREE_OPTION_LONG))
            {
                sharding = Sharding
                        .forString("dynamic@" + this.fetcher.getOptionArgument(TREE_OPTION_LONG)
                                .orElseThrow(AtlasShellToolsException::new));
            }
            else
            {
                this.output.printlnErrorMessage("either --" + TREE_OPTION_LONG + " or --"
                        + SLIPPY_OPTION_LONG + " is required");
                return 1;
            }
        }
        else if (this.fetcher.getParserContext() == SLIPPY_CONTEXT
                && this.fetcher.hasOption(SLIPPY_OPTION_LONG))
        {
            sharding = Sharding
                    .forString("slippy@" + this.fetcher.getOptionArgument(SLIPPY_OPTION_LONG)
                            .orElseThrow(AtlasShellToolsException::new));
        }
        else
        {
            throw new AtlasShellToolsException();
        }

        for (int i = 0; i < inputWKT.size(); i++)
        {
            final String wkt = inputWKT.get(i);
            parseWKTAndOutput(wkt, sharding);

            // Only print a separating newline if there were multiple entries
            if (i < inputWKT.size() - 1)
            {
                this.output.printlnStdout("");
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
        return "Get the shards that intersect some given WKT(s)";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION",
                WKTShardCommand.class.getResourceAsStream(DESCRIPTION_SECTION));
        addManualPageSection("EXAMPLES",
                WKTShardCommand.class.getResourceAsStream(EXAMPLES_SECTION));
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerArgument(INPUT_WKT, ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED,
                TREE_CONTEXT, SLIPPY_CONTEXT);
        registerOptionWithRequiredArgument(TREE_OPTION_LONG, TREE_OPTION_DESCRIPTION,
                TREE_OPTION_HINT, TREE_CONTEXT);
        registerOptionWithRequiredArgument(SLIPPY_OPTION_LONG, SLIPPY_OPTION_DESCRIPTION,
                SLIPPY_OPTION_HINT, SLIPPY_CONTEXT);
    }

    private void parseWKTAndOutput(final String wkt, final Sharding sharding)
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
            this.output.printlnStdout(wkt + " covered by:", TTYAttribute.BOLD);
            final Location location = new JtsPointConverter().backwardConvert((Point) geometry);
            final Iterable<? extends Shard> shards = sharding.shardsCovering(location);
            for (final Shard shard : shards)
            {
                this.output.printlnStdout(shard.toString(), TTYAttribute.GREEN);
            }
        }
        else if (geometry instanceof LineString)
        {
            this.output.printlnStdout(wkt + " intersects:", TTYAttribute.BOLD);
            final PolyLine polyline = new JtsPolyLineConverter()
                    .backwardConvert((LineString) geometry);
            final Iterable<? extends Shard> shards = sharding.shardsIntersecting(polyline);
            for (final Shard shard : shards)
            {
                this.output.printlnStdout(shard.toString(), TTYAttribute.GREEN);
            }
        }
        else if (geometry instanceof Polygon)
        {
            this.output.printlnStdout(wkt + " intersects:", TTYAttribute.BOLD);
            final org.openstreetmap.atlas.geography.Polygon polygon = new JtsPolygonConverter()
                    .backwardConvert((Polygon) geometry);
            final Iterable<? extends Shard> shards = sharding.shards(polygon);
            for (final Shard shard : shards)
            {
                this.output.printlnStdout(shard.toString(), TTYAttribute.GREEN);
            }
        }
        // TODO handle more geometry types
        else
        {
            this.output.printlnErrorMessage("unsupported geometry type " + wkt);
        }
    }
}
