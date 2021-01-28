package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.AtlasLoaderCommand;
import org.openstreetmap.atlas.utilities.command.terminal.TTYAttribute;

/**
 * @author lcram
 */
public class AtlasMetaDataReaderCommand extends AtlasLoaderCommand
{
    private static final String SIZE_OPTION_LONG = "size";
    private static final String SIZE_OPTION_DESCRIPTION = "Show feature array sizes.";

    private static final String ORIGINAL_OPTION_LONG = "original";
    private static final String ORIGINAL_OPTION_DESCRIPTION = "Show value of 'original' field.";

    private static final String CODE_VERSION_OPTION_LONG = "code-version";
    private static final String CODE_VERSION_OPTION_DESCRIPTION = "Show the code version.";

    private static final String DATA_VERSION_OPTION_LONG = "data-version";
    private static final String DATA_VERSION_OPTION_DESCRIPTION = "Show the data version.";

    private static final String COUNTRY_OPTION_LONG = "country";
    private static final String COUNTRY_OPTION_DESCRIPTION = "Show country(s).";

    private static final String SHARD_OPTION_LONG = "shard";
    private static final String SHARD_OPTION_DESCRIPTION = "Show shard(s).";

    private static final String TAGS_OPTION_LONG = "tags";
    private static final String TAGS_OPTION_DESCRIPTION = "Show metadata tags.";

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    public static void main(final String[] args)
    {
        new AtlasMetaDataReaderCommand().runSubcommandAndExit(args);
    }

    public AtlasMetaDataReaderCommand()
    {
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    public String getCommandName()
    {
        return "atlas-metadata-reader";
    }

    @Override
    public String getSimpleDescription()
    {
        return "read selected fields from the atlas metadata";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION", AtlasMetaDataReaderCommand.class
                .getResourceAsStream("AtlasMetaDataReaderCommandDescriptionSection.txt"));
        addManualPageSection("EXAMPLES", AtlasMetaDataReaderCommand.class
                .getResourceAsStream("AtlasMetaDataReaderCommandExamplesSection.txt"));
        super.registerManualPageSections();
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOption(SIZE_OPTION_LONG, SIZE_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL);
        registerOption(ORIGINAL_OPTION_LONG, ORIGINAL_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL);
        registerOption(CODE_VERSION_OPTION_LONG, CODE_VERSION_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL);
        registerOption(DATA_VERSION_OPTION_LONG, DATA_VERSION_OPTION_DESCRIPTION,
                OptionOptionality.OPTIONAL);
        registerOption(COUNTRY_OPTION_LONG, COUNTRY_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL);
        registerOption(SHARD_OPTION_LONG, SHARD_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL);
        registerOption(TAGS_OPTION_LONG, TAGS_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL);
        super.registerOptionsAndArguments();
    }

    @Override
    protected void processAtlas(final Atlas atlas, final String atlasFileName,
            final File atlasResource)
    {
        this.outputDelegate.printlnStdout(atlasResource.getPathString() + " metadata:",
                TTYAttribute.BOLD);
        if (this.optionAndArgumentDelegate.hasOption(SIZE_OPTION_LONG))
        {
            final StringBuilder builder = new StringBuilder();
            builder.append("Size: ");
            builder.append("\n\tNodes: ");
            builder.append(atlas.metaData().getSize().getNodeNumber());
            builder.append("\n\tEdges: ");
            builder.append(atlas.metaData().getSize().getEdgeNumber());
            builder.append("\n\tAreas: ");
            builder.append(atlas.metaData().getSize().getAreaNumber());
            builder.append("\n\tLines: ");
            builder.append(atlas.metaData().getSize().getLineNumber());
            builder.append("\n\tPoints: ");
            builder.append(atlas.metaData().getSize().getPointNumber());
            builder.append("\n\tRelations: ");
            builder.append(atlas.metaData().getSize().getRelationNumber());
            this.outputDelegate.printlnStdout(builder.toString(), TTYAttribute.GREEN);
        }
        if (this.optionAndArgumentDelegate.hasOption(ORIGINAL_OPTION_LONG))
        {
            this.outputDelegate.printlnStdout("Original: " + atlas.metaData().isOriginal(),
                    TTYAttribute.GREEN);
        }
        if (this.optionAndArgumentDelegate.hasOption(CODE_VERSION_OPTION_LONG))
        {
            this.outputDelegate.printlnStdout(
                    "Code Version: " + atlas.metaData().getCodeVersion().orElse("null"),
                    TTYAttribute.GREEN);
        }
        if (this.optionAndArgumentDelegate.hasOption(DATA_VERSION_OPTION_LONG))
        {
            this.outputDelegate.printlnStdout(
                    "Data Version: " + atlas.metaData().getDataVersion().orElse("null"),
                    TTYAttribute.GREEN);
        }
        if (this.optionAndArgumentDelegate.hasOption(COUNTRY_OPTION_LONG))
        {
            this.outputDelegate.printlnStdout(
                    "Country: " + atlas.metaData().getCountry().orElse("null"), TTYAttribute.GREEN);
        }
        if (this.optionAndArgumentDelegate.hasOption(SHARD_OPTION_LONG))
        {
            this.outputDelegate.printlnStdout(
                    "Shard: " + atlas.metaData().getShardName().orElse("null"), TTYAttribute.GREEN);
        }
        if (this.optionAndArgumentDelegate.hasOption(TAGS_OPTION_LONG))
        {
            final StringBuilder builder = new StringBuilder();
            final SortedSet<String> sortedTags = atlas.metaData().getTags().entrySet().stream()
                    .map(entry -> entry.getKey() + " -> " + entry.getValue())
                    .collect(Collectors.toCollection(TreeSet::new));
            builder.append(new StringList(sortedTags).join("\n\t"));
            this.outputDelegate.printlnStdout("Tags:\n\t" + builder.toString(), TTYAttribute.GREEN);
        }
        // If none of the specific options are supplied, print everything
        if (!this.optionAndArgumentDelegate.hasOption(SIZE_OPTION_LONG)
                && !this.optionAndArgumentDelegate.hasOption(ORIGINAL_OPTION_LONG)
                && !this.optionAndArgumentDelegate.hasOption(CODE_VERSION_OPTION_LONG)
                && !this.optionAndArgumentDelegate.hasOption(DATA_VERSION_OPTION_LONG)
                && !this.optionAndArgumentDelegate.hasOption(COUNTRY_OPTION_LONG)
                && !this.optionAndArgumentDelegate.hasOption(SHARD_OPTION_LONG)
                && !this.optionAndArgumentDelegate.hasOption(TAGS_OPTION_LONG))
        {
            this.outputDelegate.printlnStdout(atlas.metaData().toReadableString(),
                    TTYAttribute.GREEN);
        }
        else
        {
            this.outputDelegate.printlnStdout("");
        }
    }
}
