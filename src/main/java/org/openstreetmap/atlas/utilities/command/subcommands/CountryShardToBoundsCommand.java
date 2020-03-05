package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.List;

import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.boundary.CountryBoundary;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.converters.WktPolygonConverter;
import org.openstreetmap.atlas.geography.sharding.GeoHashSharding;
import org.openstreetmap.atlas.geography.sharding.GeoHashTile;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.geography.sharding.SlippyTileSharding;
import org.openstreetmap.atlas.geography.sharding.converters.StringToShardConverter;
import org.openstreetmap.atlas.streaming.resource.File;
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
public class CountryShardToBoundsCommand extends AbstractAtlasShellToolsCommand
{
    private static final Logger logger = LoggerFactory.getLogger(CountryShardToBoundsCommand.class);

    private static final String REVERSE_OPTION_LONG = "reverse";
    private static final String REVERSE_OPTION_DESCRIPTION = "Convert given WKT bound(s) to SlippyTile/GeoHashTile shard(s) if possible. Supports up to slippy zoom level "
            + Sharding.SLIPPY_ZOOM_MAXIMUM + " and geohash precision "
            + GeoHashTile.MAXIMUM_PRECISION + ".";

    private static final String COUNTRY_BOUNDARY_OPTION_LONG = "country-boundary";
    private static final String COUNTRY_BOUNDARY_OPTION_DESCRIPTION = "A boundary file to use as a source. See DESCRIPTION section for details.";
    private static final String COUNTRY_BOUNDARY_OPTION_HINT = "boundary-file";

    private static final String SHARD = "shard";
    private static final String COUNTRY = "ISO3-country-code";

    private static final Integer SHARD_CONTEXT = 3;
    private static final Integer COUNTRY_CONTEXT = 4;

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    public static void main(final String[] args)
    {
        new CountryShardToBoundsCommand().runSubcommandAndExit(args);
    }

    public CountryShardToBoundsCommand()
    {
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    public int execute()
    {
        if (this.optionAndArgumentDelegate.getParserContext() == SHARD_CONTEXT)
        {
            return executeShardContext();
        }
        else if (this.optionAndArgumentDelegate.getParserContext() == COUNTRY_CONTEXT)
        {
            return executeCountryContext();
        }
        else
        {
            throw new AtlasShellToolsException();
        }
    }

    @Override
    public String getCommandName()
    {
        return "country-shard-bounds";
    }

    @Override
    public String getSimpleDescription()
    {
        return "get the WKT bounds of given shards or countries";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION", CountryShardToBoundsCommand.class
                .getResourceAsStream("CountryShardToBoundsCommandDescriptionSection.txt"));
        addManualPageSection("EXAMPLES", CountryShardToBoundsCommand.class
                .getResourceAsStream("CountryShardToBoundsCommandExamplesSection.txt"));
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOption(REVERSE_OPTION_LONG, REVERSE_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL);
        registerOptionWithRequiredArgument(COUNTRY_BOUNDARY_OPTION_LONG,
                COUNTRY_BOUNDARY_OPTION_DESCRIPTION, OptionOptionality.REQUIRED,
                COUNTRY_BOUNDARY_OPTION_HINT, COUNTRY_CONTEXT);
        registerArgument(SHARD, ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED,
                SHARD_CONTEXT);
        registerArgument(COUNTRY, ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED,
                COUNTRY_CONTEXT);
        super.registerOptionsAndArguments();
    }

    private int executeCountryContext()
    {
        final CountryBoundaryMap countryBoundaryMap;
        final File boundaryMapFile = new File(
                this.optionAndArgumentDelegate.getOptionArgument(COUNTRY_BOUNDARY_OPTION_LONG)
                        .orElseThrow(AtlasShellToolsException::new));
        if (!boundaryMapFile.exists())
        {
            this.outputDelegate.printlnErrorMessage(
                    "boundary file " + boundaryMapFile.getAbsolutePath() + " does not exist");
            return 1;
        }
        if (this.optionAndArgumentDelegate.hasVerboseOption())
        {
            this.outputDelegate.printlnCommandMessage("loading country boundary map...");
        }
        countryBoundaryMap = CountryBoundaryMap.fromPlainText(boundaryMapFile);
        if (this.optionAndArgumentDelegate.hasVerboseOption())
        {
            this.outputDelegate.printlnCommandMessage("loaded boundary map");
        }

        final List<String> countryCodes = this.optionAndArgumentDelegate
                .getVariadicArgument(COUNTRY);

        for (int i = 0; i < countryCodes.size(); i++)
        {
            final String countryCode = countryCodes.get(i).toUpperCase();
            this.outputDelegate.printlnStdout(countryCode + " boundary:", TTYAttribute.BOLD);
            final List<CountryBoundary> boundaries = countryBoundaryMap
                    .countryBoundary(countryCode);
            if (boundaries == null || boundaries.isEmpty())
            {
                this.outputDelegate.printlnWarnMessage("no boundaries found for " + countryCode);
            }
            else
            {
                for (final CountryBoundary boundary : boundaries)
                {
                    this.outputDelegate.printlnStdout(boundary.getBoundary().toWkt(),
                            TTYAttribute.GREEN);
                }
            }

            if (i < countryCodes.size() - 1)
            {
                this.outputDelegate.printlnStdout("");
            }
        }

        return 0;
    }

    private int executeShardContext()
    {
        if (this.optionAndArgumentDelegate.hasOption(REVERSE_OPTION_LONG))
        {
            final List<String> wkts = this.optionAndArgumentDelegate.getVariadicArgument(SHARD);

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
            final List<String> shards = this.optionAndArgumentDelegate.getVariadicArgument(SHARD);

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
