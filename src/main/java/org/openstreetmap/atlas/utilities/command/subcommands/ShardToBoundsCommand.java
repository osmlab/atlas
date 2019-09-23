package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.List;

import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.converters.StringToShardConverter;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.terminal.TTYAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lcram
 */
public class ShardToBoundsCommand extends AbstractAtlasShellToolsCommand
{
    private static final Logger logger = LoggerFactory.getLogger(ShardToBoundsCommand.class);

    private static final String INPUT_SHARDS = "shards";

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
        final List<String> shards = this.optionAndArgumentDelegate
                .getVariadicArgument(INPUT_SHARDS);

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
        registerArgument(INPUT_SHARDS, ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED);
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
}
