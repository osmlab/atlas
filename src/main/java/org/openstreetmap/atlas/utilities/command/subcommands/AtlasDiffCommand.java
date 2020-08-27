package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.terminal.TTYAttribute;

import com.google.common.collect.Sets;

/**
 * @author lcram
 */
public class AtlasDiffCommand extends AbstractAtlasShellToolsCommand
{
    /**
     * @author matthieun
     */
    private static class AtlasDiffCommandContext
    {
        private final File beforeAtlasFile;
        private final File afterAtlasFile;
        private final boolean useGeoJson;
        private final boolean useLdGeoJson;
        private final boolean fullText;
        private final Long selectedIdentifier;
        private final ItemType selectedType;
        private final boolean recursive;

        AtlasDiffCommandContext(final File beforeAtlasFile, final File afterAtlasFile, // NOSONAR
                final boolean useGeoJson, final boolean useLdGeoJson, final boolean fullText,
                final Long selectedIdentifier, final ItemType selectedType, final boolean recursive)
        {
            this.beforeAtlasFile = beforeAtlasFile;
            this.afterAtlasFile = afterAtlasFile;
            this.useGeoJson = useGeoJson;
            this.useLdGeoJson = useLdGeoJson;
            this.fullText = fullText;
            this.selectedIdentifier = selectedIdentifier;
            this.selectedType = selectedType;
            this.recursive = recursive;
        }

        public File getAfterAtlasFile()
        {
            return this.afterAtlasFile;
        }

        public File getBeforeAtlasFile()
        {
            return this.beforeAtlasFile;
        }

        public Long getSelectedIdentifier()
        {
            return this.selectedIdentifier;
        }

        public ItemType getSelectedType()
        {
            return this.selectedType;
        }

        public boolean isFullText()
        {
            return this.fullText;
        }

        public boolean isRecursive()
        {
            return this.recursive;
        }

        public boolean isUseGeoJson()
        {
            return this.useGeoJson;
        }

        public boolean isUseLdGeoJson()
        {
            return this.useLdGeoJson;
        }
    }

    static final String NO_CHANGE = "Atlases are effectively identical";

    private static final String BEFORE_ATLAS_ARGUMENT = "before-atlas(es)";
    private static final String AFTER_ATLAS_ARGUMENT = "after-atlas(es)";

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
    private static final String FOLDER_SEARCH_RECURSIVE_OPTION_LONG = "recursive";
    private static final String FOLDER_SEARCH_RECURSIVE_OPTION_DESCRIPTION = "When comparing Atlas folders, search sub-folders too.";

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
        final File beforeAtlasFile = new File(beforeAtlasPath, this.getFileSystem());
        final File afterAtlasFile = new File(afterAtlasPath, this.getFileSystem());
        boolean useGeoJson = false;
        boolean useLdGeoJson = false;
        boolean fullText = false;
        Long selectedIdentifier = null;
        ItemType selectedType = null;
        boolean recursive = false;

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

        if (this.optionAndArgumentDelegate.hasOption(FOLDER_SEARCH_RECURSIVE_OPTION_LONG))
        {
            recursive = true;
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

        final AtlasDiffCommandContext context = new AtlasDiffCommandContext(beforeAtlasFile,
                afterAtlasFile, useGeoJson, useLdGeoJson, fullText, selectedIdentifier,
                selectedType, recursive);

        if (beforeAtlasFile.isDirectory() && afterAtlasFile.isDirectory())
        {
            final int result = this.compute(context, beforeAtlasFile, afterAtlasFile);
            return result > 0 ? 1 : 0;
        }
        else if (!beforeAtlasFile.isDirectory() && !afterAtlasFile.isDirectory())
        {
            final Atlas beforeAtlas = load(beforeAtlasFile);
            final Atlas afterAtlas = load(afterAtlasFile);

            final int result = this.compute(context, beforeAtlas, afterAtlas);
            return result > 0 ? 1 : 0;
        }
        else
        {
            this.outputDelegate.printlnErrorMessage("Cannot compare a file and a directory.");
            return 1;
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
        registerOption(FOLDER_SEARCH_RECURSIVE_OPTION_LONG,
                FOLDER_SEARCH_RECURSIVE_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL,
                DEFAULT_CONTEXT, FULL_CONTEXT, GEOJSON_CONTEXT, LDGEOJSON_CONTEXT);
        registerArgument(BEFORE_ATLAS_ARGUMENT, ArgumentArity.UNARY, ArgumentOptionality.REQUIRED,
                DEFAULT_CONTEXT, LDGEOJSON_CONTEXT, GEOJSON_CONTEXT, FULL_CONTEXT);
        registerArgument(AFTER_ATLAS_ARGUMENT, ArgumentArity.UNARY, ArgumentOptionality.REQUIRED,
                DEFAULT_CONTEXT, LDGEOJSON_CONTEXT, GEOJSON_CONTEXT, FULL_CONTEXT);
        super.registerOptionsAndArguments();
    }

    int compute(final AtlasDiffCommandContext context, final File beforeAtlasFile,
            final File afterAtlasFile)
    {
        int result = 0;
        final Map<String, File> beforeNamesToFiles = new HashMap<>();
        final Map<String, File> afterNamesToFiles = new HashMap<>();
        final List<File> beforeFilesToConsider = context.isRecursive()
                ? beforeAtlasFile.listFilesRecursively(false)
                : beforeAtlasFile.listFiles(false);
        final List<File> afterFilesToConsider = context.isRecursive()
                ? afterAtlasFile.listFilesRecursively(false)
                : afterAtlasFile.listFiles(false);
        beforeFilesToConsider.stream().filter(FileSuffix.ATLAS::matches).forEach(
                file -> beforeNamesToFiles.put(getRelativeFileName(beforeAtlasFile, file), file));
        afterFilesToConsider.stream().filter(FileSuffix.ATLAS::matches).forEach(
                file -> afterNamesToFiles.put(getRelativeFileName(afterAtlasFile, file), file));
        final Set<String> filesOnlyInBefore = Sets.difference(beforeNamesToFiles.keySet(),
                afterNamesToFiles.keySet());
        final Set<String> filesOnlyInAfter = Sets.difference(afterNamesToFiles.keySet(),
                beforeNamesToFiles.keySet());
        final Set<String> filesInBoth = Sets.intersection(beforeNamesToFiles.keySet(),
                afterNamesToFiles.keySet());
        if (!filesOnlyInBefore.isEmpty())
        {
            final String warnMessage = "Files only in Before Atlas folder:";
            this.outputDelegate.printlnWarnMessage(warnMessage);
            filesOnlyInBefore.stream().sorted().forEach(this.outputDelegate::printlnWarnMessage);
            result += filesOnlyInBefore.size();
        }
        if (!filesOnlyInAfter.isEmpty())
        {
            final String warnMessage = "Files only in After Atlas folder:";
            this.outputDelegate.printlnWarnMessage(warnMessage);
            filesOnlyInAfter.stream().sorted().forEach(this.outputDelegate::printlnWarnMessage);
            result += filesOnlyInAfter.size();
        }
        for (final String name : filesInBoth.stream().sorted().collect(Collectors.toList()))
        {
            final Atlas beforeAtlas = load(beforeNamesToFiles.get(name));
            final Atlas afterAtlas = load(afterNamesToFiles.get(name));
            this.outputDelegate.printlnStdout(name, TTYAttribute.BOLD, TTYAttribute.GREEN);
            result += compute(context, beforeAtlas, afterAtlas);
        }
        return result;
    }

    /**
     * @param context
     *            The context to apply
     * @param beforeAtlas
     *            The before Atlas to compare
     * @param afterAtlas
     *            The after Atlas to compare
     * @return The number of differences
     */
    int compute(final AtlasDiffCommandContext context, final Atlas beforeAtlas,
            final Atlas afterAtlas)
    {
        final AtlasDiff diff = new AtlasDiff(beforeAtlas, afterAtlas).saveAllGeometries(false);
        final Optional<Change> changeOptional = diff.generateChange();

        if (changeOptional.isPresent())
        {
            final Optional<Change> trimmedChangeOption = trimChange(context, changeOptional.get());
            final Change change;
            if (trimmedChangeOption.isPresent())
            {
                change = trimmedChangeOption.get();
            }
            else
            {
                return 0;
            }

            final String serializedString;
            if (context.isUseGeoJson())
            {
                serializedString = new ChangeGeoJsonSerializer().convert(change);
            }
            else if (context.isUseLdGeoJson())
            {
                serializedString = change.toLineDelimitedFeatureChanges(true);
            }
            else if (context.isFullText())
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
            return change.changeCount();
        }
        else
        {
            this.outputDelegate.printlnStdout(NO_CHANGE);
            return 0;
        }
    }

    private String getRelativeFileName(final File parent, final File file)
    {
        return file.getAbsolutePathString().substring(parent.getAbsolutePathString().length() + 1);
    }

    private Atlas load(final File file)
    {
        return new AtlasResourceLoader()
                .load(new InputStreamResource(file::read).withName(file.getAbsolutePathString()));
    }

    private Optional<Change> trimChange(final AtlasDiffCommandContext context, final Change change)
    {
        if (this.optionAndArgumentDelegate.hasOption(ID_OPTION_LONG)
                && this.optionAndArgumentDelegate.hasOption(TYPE_OPTION_LONG))
        {
            final Optional<FeatureChange> featureChangeOptional = change
                    .changeFor(context.getSelectedType(), context.getSelectedIdentifier());
            if (featureChangeOptional.isPresent())
            {
                return Optional.of(new ChangeBuilder().add(featureChangeOptional.get()).get());
            }
            else
            {
                final String stdoutMessage = "No change found for " + context.getSelectedType()
                        + " " + context.getSelectedIdentifier();
                this.outputDelegate.printlnWarnMessage(stdoutMessage);
                return Optional.empty();
            }
        }
        return Optional.of(change);
    }
}
