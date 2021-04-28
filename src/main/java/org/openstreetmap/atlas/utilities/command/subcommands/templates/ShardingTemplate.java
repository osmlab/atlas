package org.openstreetmap.atlas.utilities.command.subcommands.templates;

import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AtlasShellToolsCommandTemplate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;

/**
 * An {@link AtlasShellToolsCommandTemplate} for commands that want to read an input
 * {@link Sharding}.
 * 
 * @author lcram
 */
public class ShardingTemplate implements AtlasShellToolsCommandTemplate
{
    private static final String SHARDING_OPTION_LONG = "sharding";

    private final Integer[] contexts;

    /**
     * Get a {@link Sharding} object from the user's input option.
     * 
     * @param parentCommand
     *            the parent command that controls this template
     * @return the {@link Sharding} object specified by the user
     */
    public static Sharding getSharding(final AbstractAtlasShellToolsCommand parentCommand)
    {
        return Sharding.forString(parentCommand.getOptionAndArgumentDelegate()
                .getOptionArgument(SHARDING_OPTION_LONG).orElseThrow(AtlasShellToolsException::new),
                parentCommand.getFileSystem());
    }

    /**
     * This constructor allows callers to specify under which contexts they want the options
     * provided by this template to appear. If left blank, this template will only be applied to the
     * default context.
     *
     * @param contexts
     *            the parse contexts under which you want the options provided by this template to
     *            appear
     */
    public ShardingTemplate(final Integer... contexts)
    {
        this.contexts = contexts;
    }

    @Override
    public void registerManualPageSections(final AbstractAtlasShellToolsCommand parentCommand)
    {
        parentCommand.addManualPageSection("INPUT SHARDING",
                ShardingTemplate.class.getResourceAsStream("ShardingTemplateSection.txt"));
    }

    @Override
    public void registerOptionsAndArguments(final AbstractAtlasShellToolsCommand parentCommand)
    {
        parentCommand.registerOptionWithRequiredArgument(SHARDING_OPTION_LONG,
                "The sharding to use, e.g. slippy@9, dynamic@/Users/foo/my-tree.txt, geohash@4, etc.",
                OptionOptionality.REQUIRED, "type@parameter", this.contexts);
    }
}
