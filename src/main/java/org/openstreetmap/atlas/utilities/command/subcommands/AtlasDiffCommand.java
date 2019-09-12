package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.change.Change;
import org.openstreetmap.atlas.geography.atlas.change.ChangeBuilder;
import org.openstreetmap.atlas.geography.atlas.change.FeatureChange;
import org.openstreetmap.atlas.geography.atlas.change.diff.AtlasDiff;
import org.openstreetmap.atlas.geography.atlas.change.serializer.ChangeGeoJsonSerializer;
import org.openstreetmap.atlas.geography.atlas.complete.PrettifyStringFormat;
import org.openstreetmap.atlas.geography.atlas.items.ItemType;
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

    private static final String LDGEOJSON_OPTION_LONG = "ldgeojson";
    private static final String LDGEOJSON_OPTION_DESCRIPTION = "Use the line-delimited geoJSON format for output.";
    private static final String GEOJSON_OPTION_LONG = "geojson";
    private static final String GEOJSON_OPTION_DESCRIPTION = "Use the pretty geoJSON format for output.";
    private static final String FULL_OPTION_LONG = "full";
    private static final String FULL_OPTION_DESCRIPTION = "Show the full FeatureChange instead of just the ChangeDescription.";

    private static final List<String> ITEM_TYPE_STRINGS = Arrays.stream(ItemType.values())
            .map(ItemType::toString).collect(Collectors.toList());
    private static final String TYPE_OPTION_LONG = "type";
    private static final String TYPE_OPTION_DESCRIPTION = "The ItemType of the desired feature. Valid types are: "
            + new StringList(ITEM_TYPE_STRINGS).join(", ") + ".";
    private static final String TYPE_OPTION_HINT = "type";

    private static final String ID_OPTION_LONG = "id";
    private static final String ID_OPTION_DESCRIPTION = "The identifier of the desired feature.";
    private static final String ID_OPTION_HINT = "id";

    private static final Integer LDGEOJSON_CONTEXT = 4;
    private static final Integer GEOJSON_CONTEXT = 5;
    private static final Integer FULL_CONTEXT = 6;

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
    public int execute() // NOSONAR
    {
        final String beforeAtlasPath = this.optionAndArgumentDelegate
                .getUnaryArgument(BEFORE_ATLAS_ARGUMENT).orElseThrow(AtlasShellToolsException::new);
        final String afterAtlasPath = this.optionAndArgumentDelegate
                .getUnaryArgument(AFTER_ATLAS_ARGUMENT).orElseThrow(AtlasShellToolsException::new);
        final File beforeAtlasFile = new File(beforeAtlasPath);
        final File afterAtlasFile = new File(afterAtlasPath);
        boolean useGeoJson = false;
        boolean useLdGeoJson = false;
        boolean fullText = false;
        Long selectedIdentifier = null;
        ItemType selectedType = null;

        if (this.optionAndArgumentDelegate.hasOption(
                ID_OPTION_LONG) != this.optionAndArgumentDelegate.hasOption(TYPE_OPTION_LONG))
        {
            this.outputDelegate.printlnErrorMessage("options --" + ID_OPTION_LONG + " and --"
                    + TYPE_OPTION_LONG + " must be supplied together or not at all");
            return 2;
        }
        if (this.optionAndArgumentDelegate.hasOption(ID_OPTION_LONG))
        {
            final String idString = this.optionAndArgumentDelegate.getOptionArgument(ID_OPTION_LONG)
                    .orElseThrow(AtlasShellToolsException::new);
            try
            {
                selectedIdentifier = Long.parseLong(idString);
            }
            catch (final Exception exception)
            {
                this.outputDelegate.printlnErrorMessage("could not parse id " + idString);
                return 2;
            }
        }
        if (this.optionAndArgumentDelegate.hasOption(TYPE_OPTION_LONG))
        {
            final String typeString = this.optionAndArgumentDelegate
                    .getOptionArgument(TYPE_OPTION_LONG).orElseThrow(AtlasShellToolsException::new)
                    .toUpperCase();
            try
            {
                selectedType = ItemType.valueOf(typeString);
            }
            catch (final Exception exception)
            {
                this.outputDelegate.printlnErrorMessage("could not parse id " + typeString);
                return 2;
            }
        }

        if (this.optionAndArgumentDelegate.getParserContext() == GEOJSON_CONTEXT)
        {
            useGeoJson = true;
        }

        if (this.optionAndArgumentDelegate.getParserContext() == LDGEOJSON_CONTEXT)
        {
            useLdGeoJson = true;
        }

        if (this.optionAndArgumentDelegate.getParserContext() == FULL_CONTEXT)
        {
            fullText = true;
        }

        if (!beforeAtlasFile.exists())
        {
            this.outputDelegate.printlnWarnMessage("file not found: " + beforeAtlasPath);
            return 2;
        }
        if (!afterAtlasFile.exists())
        {
            this.outputDelegate.printlnWarnMessage("file not found: " + afterAtlasPath);
            return 2;
        }

        final Atlas beforeAtlas = new AtlasResourceLoader().load(beforeAtlasFile);
        final Atlas afterAtlas = new AtlasResourceLoader().load(afterAtlasFile);

        final AtlasDiff diff = new AtlasDiff(beforeAtlas, afterAtlas).saveAllGeometries(false);
        final Optional<Change> changeOptional = diff.generateChange();

        if (changeOptional.isPresent())
        {
            Change change = changeOptional.get();
            if (this.optionAndArgumentDelegate.hasOption(ID_OPTION_LONG)
                    && this.optionAndArgumentDelegate.hasOption(TYPE_OPTION_LONG))
            {
                final Optional<FeatureChange> featureChangeOptional = change.changeFor(selectedType,
                        selectedIdentifier);
                if (featureChangeOptional.isPresent())
                {
                    change = new ChangeBuilder().add(featureChangeOptional.get()).get();
                }
                else
                {
                    this.outputDelegate.printlnWarnMessage(
                            "No change found for " + selectedType + " " + selectedIdentifier);
                    return 0;
                }
            }
            final String serializedString;
            if (useGeoJson)
            {
                serializedString = new ChangeGeoJsonSerializer().convert(change);
            }
            else if (useLdGeoJson)
            {
                serializedString = change.toLineDelimitedFeatureChanges();
            }
            else if (fullText)
            {
                final PrettifyStringFormat featureChangeFormat = this.optionAndArgumentDelegate
                        .getOptionArgument(FEATURE_CHANGE_FORMAT_OPTION_LONG,
                                PrettifyStringFormat::valueOf)
                        .orElse(DEFAULT_PRETTY_FEATURE_CHANGE_FORMAT);
                final PrettifyStringFormat completeEntityFormat = this.optionAndArgumentDelegate
                        .getOptionArgument(COMPLETE_ENTITY_FORMAT_OPTION_LONG,
                                PrettifyStringFormat::valueOf)
                        .orElse(DEFAULT_PRETTY_COMPLETE_ENTITY_FORMAT);
                serializedString = change.prettify(featureChangeFormat, completeEntityFormat, false)
                        + "\n";
            }
            else
            {
                final StringBuilder builder = new StringBuilder();
                change.changes().forEach(featureChange ->
                {
                    builder.append(featureChange.explain());
                    builder.append("\n");
                });
                serializedString = builder.toString();
            }
            this.outputDelegate.printlnStdout(serializedString);
            return 1;
        }
        else
        {
            this.outputDelegate.printlnWarnMessage("atlases are effectively identical");
            return 0;
        }
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
        registerOptionWithRequiredArgument(TYPE_OPTION_LONG, TYPE_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, TYPE_OPTION_HINT, DEFAULT_CONTEXT, GEOJSON_CONTEXT,
                LDGEOJSON_CONTEXT, FULL_CONTEXT);
        registerOptionWithRequiredArgument(ID_OPTION_LONG, ID_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL, ID_OPTION_HINT, DEFAULT_CONTEXT, GEOJSON_CONTEXT,
                LDGEOJSON_CONTEXT, FULL_CONTEXT);
        registerOptionWithRequiredArgument(FEATURE_CHANGE_FORMAT_OPTION_LONG,
                FEATURE_CHANGE_FORMAT_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL,
                FEATURE_CHANGE_FORMAT_OPTION_HINT, FULL_CONTEXT);
        registerOptionWithRequiredArgument(COMPLETE_ENTITY_FORMAT_OPTION_LONG,
                COMPLETE_ENTITY_FORMAT_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL,
                COMPLETE_ENTITY_FORMAT_OPTION_HINT, FULL_CONTEXT);
        registerOption(LDGEOJSON_OPTION_LONG, LDGEOJSON_OPTION_DESCRIPTION,
                OptionOptionality.REQUIRED, LDGEOJSON_CONTEXT);
        registerOption(GEOJSON_OPTION_LONG, GEOJSON_OPTION_DESCRIPTION, OptionOptionality.REQUIRED,
                GEOJSON_CONTEXT);
        registerOption(FULL_OPTION_LONG, FULL_OPTION_DESCRIPTION, OptionOptionality.REQUIRED,
                FULL_CONTEXT);
        registerArgument(BEFORE_ATLAS_ARGUMENT, ArgumentArity.UNARY, ArgumentOptionality.REQUIRED,
                DEFAULT_CONTEXT, LDGEOJSON_CONTEXT, GEOJSON_CONTEXT, FULL_CONTEXT);
        registerArgument(AFTER_ATLAS_ARGUMENT, ArgumentArity.UNARY, ArgumentOptionality.REQUIRED,
                DEFAULT_CONTEXT, LDGEOJSON_CONTEXT, GEOJSON_CONTEXT, FULL_CONTEXT);
        super.registerOptionsAndArguments();
    }
}
