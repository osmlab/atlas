package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.change.Change;
import org.openstreetmap.atlas.geography.atlas.change.diff.AtlasDiff;
import org.openstreetmap.atlas.geography.atlas.complete.PrettifyStringFormat;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;

/**
 * @author lcram
 */
public class AtlasDiffCommand extends AbstractAtlasShellToolsCommand
{
    private static final String BEFORE_ATLAS_ARGUMENT = "before-atlas";
    private static final String AFTER_ATLAS_ARGUMENT = "after-atlas";

    private static final List<String> FORMAT_TYPE_STRINGS = Arrays
            .stream(PrettifyStringFormat.values()).map(PrettifyStringFormat::toString)
            .collect(Collectors.toList());
    private static final PrettifyStringFormat DEFAULT_PRETTY_FEATURE_CHANGE_FORMAT = PrettifyStringFormat.MINIMAL_MULTI_LINE;
    private static final PrettifyStringFormat DEFAULT_PRETTY_COMPLETE_ENTITY_FORMAT = PrettifyStringFormat.MINIMAL_SINGLE_LINE;

    private static final String FEATURE_CHANGE_FORMAT_OPTION_LONG = "feature-change-format";
    private static final String COMPLETE_ENTITY_FORMAT_OPTION_LONG = "complete-entity-format";
    private static final String FEATURE_CHANGE_FORMAT_OPTION_HINT = "format";
    private static final String COMPLETE_ENTITY_FORMAT_OPTION_HINT = "format";
    private static final String FEATURE_CHANGE_FORMAT_OPTION_DESCRIPTION = "The format type for the constituent FeatureChanges. Valid settings are: "
            + new StringList(FORMAT_TYPE_STRINGS).join(", ") + ". Defaults to "
            + DEFAULT_PRETTY_FEATURE_CHANGE_FORMAT.toString() + ".";
    private static final String COMPLETE_ENTITY_FORMAT_OPTION_DESCRIPTION = "The format type for the CompleteEntities within the constituent FeatureChanges. Valid settings are: "
            + new StringList(FORMAT_TYPE_STRINGS).join(", ") + ". Defaults to "
            + DEFAULT_PRETTY_COMPLETE_ENTITY_FORMAT.toString() + ".";

    private static final String EXTENSION = ".diff";

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    public static void main(final String[] args)
    {
        new AtlasDiffCommand().runSubcommandAndExit(args);
    }

    public AtlasDiffCommand()
    {
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    public int execute()
    {
        final String beforeAtlasPath = this.optionAndArgumentDelegate
                .getUnaryArgument(BEFORE_ATLAS_ARGUMENT).orElseThrow(AtlasShellToolsException::new);
        final String afterAtlasPath = this.optionAndArgumentDelegate
                .getUnaryArgument(AFTER_ATLAS_ARGUMENT).orElseThrow(AtlasShellToolsException::new);
        final PrettifyStringFormat featureChangeFormat = this.optionAndArgumentDelegate
                .getOptionArgument(FEATURE_CHANGE_FORMAT_OPTION_LONG, PrettifyStringFormat::valueOf)
                .orElse(DEFAULT_PRETTY_FEATURE_CHANGE_FORMAT);
        final PrettifyStringFormat completeEntityFormat = this.optionAndArgumentDelegate
                .getOptionArgument(COMPLETE_ENTITY_FORMAT_OPTION_LONG,
                        PrettifyStringFormat::valueOf)
                .orElse(DEFAULT_PRETTY_COMPLETE_ENTITY_FORMAT);
        final File beforeAtlasFile = new File(beforeAtlasPath);
        final File afterAtlasFile = new File(afterAtlasPath);

        if (!beforeAtlasFile.exists())
        {
            this.outputDelegate.printlnWarnMessage("file not found: " + beforeAtlasPath);
            return 1;
        }
        if (!afterAtlasFile.exists())
        {
            this.outputDelegate.printlnWarnMessage("file not found: " + afterAtlasPath);
            return 1;
        }

        final Atlas beforeAtlas = new AtlasResourceLoader().load(beforeAtlasFile);
        final Atlas afterAtlas = new AtlasResourceLoader().load(afterAtlasFile);

        final AtlasDiff diff = new AtlasDiff(beforeAtlas, afterAtlas);
        final Optional<Change> changeOptional = diff.generateChange();

        if (changeOptional.isPresent())
        {
            final String serializedString = changeOptional.get().prettify(featureChangeFormat,
                    completeEntityFormat) + "\n";
            final String outputFile = beforeAtlasFile.getFile().getName() + "-"
                    + afterAtlasFile.getFile().getName() + EXTENSION;
            final File output = new File(outputFile);
            output.writeAndClose(serializedString);
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
        addManualPageSection("DESCRIPTION", AtlasDiffCommand.class
                .getResourceAsStream("AtlasDiffCommandDescriptionSection.txt"));
        addManualPageSection("EXAMPLES",
                AtlasDiffCommand.class.getResourceAsStream("AtlasDiffCommandExamplesSection.txt"));
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOptionWithRequiredArgument(FEATURE_CHANGE_FORMAT_OPTION_LONG,
                FEATURE_CHANGE_FORMAT_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL,
                FEATURE_CHANGE_FORMAT_OPTION_HINT);
        registerOptionWithRequiredArgument(COMPLETE_ENTITY_FORMAT_OPTION_LONG,
                COMPLETE_ENTITY_FORMAT_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL,
                COMPLETE_ENTITY_FORMAT_OPTION_HINT);
        registerArgument(BEFORE_ATLAS_ARGUMENT, ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
        registerArgument(AFTER_ATLAS_ARGUMENT, ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
        super.registerOptionsAndArguments();
    }
}
