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

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

public class WKTShardCommand extends AbstractAtlasShellToolsCommand
{
    private static final String DESCRIPTION_SECTION = "WKTShardCommandDescriptionSection.txt";
    private static final String EXAMPLES_SECTION = "WKTShardCommandExamplesSection.txt";

    private static final String SHARDING_OPTION_LONG = "sharding";
    private static final String SHARDING_OPTION_DESCRIPTION = "The sharding tree definition. E.g. dynamic@/Users/example/path/to/tree.txt";
    private static final String SHARDING_OPTION_HINT = "tree";
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
        if (!this.fetcher.hasOption(SHARDING_OPTION_LONG))
        {
            this.output.printlnErrorMessage(
                    "missing sharding tree: --" + SHARDING_OPTION_LONG + " is required");
            return 1;
        }

        final List<String> inputWKT = this.fetcher.getVariadicArgument(INPUT_WKT);
        final Sharding sharding = Sharding
                .forString(this.fetcher.getOptionArgument(SHARDING_OPTION_LONG)
                        .orElseThrow(AtlasShellToolsException::new));

        for (final String wkt : inputWKT)
        {
            final WKTReader reader = new WKTReader();
            Geometry geometry;
            try
            {
                geometry = reader.read(wkt);
            }
            catch (final ParseException exception)
            {
                exception.printStackTrace();
                continue;
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
            else
            {
                this.output.printlnErrorMessage("unknown geometry type");
            }
            this.output.printlnStdout("");
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
        registerArgument(INPUT_WKT, ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED);
        registerOptionWithRequiredArgument(SHARDING_OPTION_LONG, SHARDING_OPTION_DESCRIPTION,
                SHARDING_OPTION_HINT);
    }
}
