package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.List;

import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.converters.WktPolygonConverter;
import org.openstreetmap.atlas.geography.sharding.GeoHashSharding;
import org.openstreetmap.atlas.geography.sharding.GeoHashTile;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.geography.sharding.SlippyTileSharding;
import org.openstreetmap.atlas.geography.sharding.converters.StringToShardConverter;
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
public class ShardToBoundsCommand extends AbstractAtlasShellToolsCommand
{
    private static final Logger logger = LoggerFactory.getLogger(ShardToBoundsCommand.class);

    private static final String REVERSE_OPTION_LONG = "reverse";
    private static final String REVERSE_OPTION_DESCRIPTION = "Convert given WKT bound(s) to SlippyTile/GeoHashTile shard(s) if possible. Supports up to slippy zoom level "
            + Sharding.SLIPPY_ZOOM_MAXIMUM + " and geohash precision "
            + GeoHashTile.MAXIMUM_PRECISION + ".";

    private static final String INPUT = "input";

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    public static void main(final String[] args)
    {
        new ShardToBoundsCommand().runSubcommandAndExit(args);
    }

    public ShardToBoundsCommand()
    {
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    public int execute()
    {
        if (this.optionAndArgumentDelegate.hasOption(REVERSE_OPTION_LONG))
        {
            final List<String> wkts = this.optionAndArgumentDelegate.getVariadicArgument(INPUT);

            for (int i = 0; i < wkts.size(); i++)
            {
                final String wkt = wkts.get(i);
                parseWktAndPrintOutput(wkt);

                // Only print a separating newline if there were multiple entries
                if (i < wkts.size() - 1)
                {
                    this.outputDelegate.printlnStdout("");
                }
            }
        }
        else
        {
            final List<String> shards = this.optionAndArgumentDelegate.getVariadicArgument(INPUT);

            for (int i = 0; i < shards.size(); i++)
            {
                final String shard = shards.get(i);
                parseShardAndPrintOutput(shard);

                // Only print a separating newline if there were multiple entries
                if (i < shards.size() - 1)
                {
                    this.outputDelegate.printlnStdout("");
                }
            }
        }

        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "shard-bounds";
    }

    @Override
    public String getSimpleDescription()
    {
        return "get the WKT bounds of given shard(s)";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION", ShardToBoundsCommand.class
                .getResourceAsStream("ShardToBoundsCommandDescriptionSection.txt"));
        addManualPageSection("EXAMPLES", ShardToBoundsCommand.class
                .getResourceAsStream("ShardToBoundsCommandExamplesSection.txt"));
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOption(REVERSE_OPTION_LONG, REVERSE_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL);
        registerArgument(INPUT, ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED);
        super.registerOptionsAndArguments();
    }

    private void parseShardAndPrintOutput(final String shardName)
    {
        this.outputDelegate.printlnStdout(shardName + " bounds: ", TTYAttribute.BOLD);
        final Shard shard;
        try
        {
            shard = new StringToShardConverter().convert(shardName);
        }
        catch (final Exception exception)
        {
            logger.error("unable to parse {}", shardName, exception);
            return;
        }
        this.outputDelegate.printlnStdout(shard.bounds().toWkt(), TTYAttribute.GREEN);
    }

    private void parseWktAndPrintOutput(final String wkt)
    {
        final Polygon polygon;
        try
        {
            polygon = new WktPolygonConverter().backwardConvert(wkt);
        }
        catch (final Exception exception)
        {
            logger.error("unable to parse WKT polygon {}", wkt, exception);
            return;
        }
        for (int zoom = 1; zoom <= Sharding.SLIPPY_ZOOM_MAXIMUM; zoom++)
        {
            final SlippyTileSharding sharding = new SlippyTileSharding(zoom);
            for (final Shard shard : sharding.shardsIntersecting(polygon))
            {
                if (shard.toWkt().equals(wkt))
                {
                    this.outputDelegate.printlnStdout(wkt + " exactly matched shard:",
                            TTYAttribute.BOLD);
                    this.outputDelegate.printlnStdout(shard.toString(), TTYAttribute.GREEN);
                    return;
                }
            }
        }
        for (int precision = 1; precision <= GeoHashTile.MAXIMUM_PRECISION; precision++)
        {
            final GeoHashSharding sharding = new GeoHashSharding(precision);
            for (final Shard shard : sharding.shardsIntersecting(polygon))
            {
                if (shard.toWkt().equals(wkt))
                {
                    this.outputDelegate.printlnStdout(wkt + " exactly matched shard:",
                            TTYAttribute.BOLD);
                    this.outputDelegate.printlnStdout(shard.toString(), TTYAttribute.GREEN);
                    return;
                }
            }
        }
    }
}
