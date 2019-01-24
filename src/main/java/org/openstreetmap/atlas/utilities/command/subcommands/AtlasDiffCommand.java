package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.Optional;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.change.Change;
import org.openstreetmap.atlas.geography.atlas.change.diff.AtlasDiff;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;

public class AtlasDiffCommand extends AbstractAtlasShellToolsCommand
{
    private static final String AFTER_ATLAS_ARGUMENT = "after-atlas";
    private static final String BEFORE_ATLAS_ARGUMENT = "before-atlas";
    private static final String DIFF_FILE = "diff.geojson";

    private final OptionAndArgumentDelegate optArgDelegate;
    private final CommandOutputDelegate outputDelegate;

    public static void main(final String[] args)
    {
        new AtlasDiffCommand().runSubcommandAndExit(args);
    }

    public AtlasDiffCommand()
    {
        this.optArgDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    public int execute()
    {
        final String beforeAtlasPath = this.optArgDelegate.getUnaryArgument(BEFORE_ATLAS_ARGUMENT)
                .orElseThrow(AtlasShellToolsException::new);
        final String afterAtlasPath = this.optArgDelegate.getUnaryArgument(AFTER_ATLAS_ARGUMENT)
                .orElseThrow(AtlasShellToolsException::new);
        final File beforeAtlasFile = new File(beforeAtlasPath);
        final File afterAtlasFile = new File(afterAtlasPath);

        if (!beforeAtlasFile.exists())
        {
            this.outputDelegate.printlnWarnMessage("file not found: " + beforeAtlasPath);
        }
        if (!afterAtlasFile.exists())
        {
            this.outputDelegate.printlnWarnMessage("file not found: " + afterAtlasPath);
        }

        final Atlas beforeAtlas = new AtlasResourceLoader().load(beforeAtlasFile);
        final Atlas afterAtlas = new AtlasResourceLoader().load(afterAtlasFile);

        final AtlasDiff diff = new AtlasDiff(beforeAtlas, afterAtlas);
        final Optional<Change> changeOptional = diff.generateChange();

        if (changeOptional.isPresent())
        {
            final String changeJSON = changeOptional.get().toJson();
            final File output = new File(DIFF_FILE);
            output.writeAndClose(changeJSON);
        }
        else
        {
            this.outputDelegate.printlnWarnMessage("atlases are effectively identical");
        }

        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "atlas-diff";
    }

    @Override
    public String getSimpleDescription()
    {
        return "compare two atlas files";
    }

    @Override
    public void registerManualPageSections()
    {

    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerArgument(BEFORE_ATLAS_ARGUMENT, ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
        registerArgument(AFTER_ATLAS_ARGUMENT, ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
        super.registerOptionsAndArguments();
    }
}
