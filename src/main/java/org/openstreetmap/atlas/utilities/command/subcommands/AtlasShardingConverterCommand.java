package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.sub.AtlasCutType;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.geography.sharding.Sharding;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;

/**
 * This command provides an easy way to change the sharding in which a folder of atlas files is
 * described.
 * 
 * @author matthieun
 */
public class AtlasShardingConverterCommand extends AbstractAtlasShellToolsCommand
{
    private static final String INPUT = "input";
    private static final String INPUT_DESCRIPTION = "The input folder containing XXX_<old_shard_name>.atlas files";
    private static final String OUTPUT = "output";
    private static final String OUTPUT_DESCRIPTION = "The output folder where XXX_<new_shard_name>.atlas files will be saved";
    private static final String INPUT_SHARDING = "inputSharding";
    private static final String INPUT_SHARDING_DESCRIPTION = "The input sharding";
    private static final String OUTPUT_SHARDING = "outputSharding";
    private static final String OUTPUT_SHARDING_DESCRIPTION = "The output sharding";

    private static final Pattern FILE_MATCHER = Pattern
            .compile("^[A-Za-z0-9]+_{1}([A-Za-z0-9]|-)+\\.atlas$");
    private static final String EXCEPTION_MESSAGE = "{} needs to be specified.";

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    public static void main(final String[] args)
    {
        new AtlasShardingConverterCommand().runSubcommandAndExit(args);
    }

    public AtlasShardingConverterCommand()
    {
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    public int execute()
    {
        final File inputFolder = new File(this.optionAndArgumentDelegate.getOptionArgument(INPUT)
                .orElseThrow(() -> new CoreException(EXCEPTION_MESSAGE, INPUT)));
        final File outputFolder = new File(this.optionAndArgumentDelegate.getOptionArgument(OUTPUT)
                .orElseThrow(() -> new CoreException(EXCEPTION_MESSAGE, OUTPUT)));
        if (!inputFolder.exists())
        {
            throw new CoreException("{} does not exist.", inputFolder);
        }
        if (outputFolder.exists())
        {
            throw new CoreException("{} already exists.", outputFolder);
        }
        final Sharding inputSharding = Sharding
                .forString(this.optionAndArgumentDelegate.getOptionArgument(INPUT_SHARDING)
                        .orElseThrow(() -> new CoreException(EXCEPTION_MESSAGE, INPUT_SHARDING)));
        final Sharding outputSharding = Sharding
                .forString(this.optionAndArgumentDelegate.getOptionArgument(OUTPUT_SHARDING)
                        .orElseThrow(() -> new CoreException(EXCEPTION_MESSAGE, OUTPUT_SHARDING)));
        final List<File> inputFiles = inputFolder.listFilesRecursively().stream()
                .filter(file -> FILE_MATCHER.matcher(file.getName()).matches())
                .collect(Collectors.toList());
        this.outputDelegate.printlnCommandMessage("Found input files: " + inputFiles);
        final Map<Shard, File> inputShardToAtlas = new HashMap<>();
        final Set<String> countries = new HashSet<>();
        final Set<Shard> inputShards = inputFiles.stream().map(file ->
        {
            final StringList split = StringList.split(file.getName(), "_");
            String shardName = split.get(1);
            shardName = shardName.substring(0, shardName.indexOf(FileSuffix.ATLAS.toString()));
            final Shard inputShard = inputSharding.shardForName(shardName);
            inputShardToAtlas.put(inputShard, file);
            countries.add(split.get(0));
            return inputShard;
        }).collect(Collectors.toSet());
        if (countries.size() > 1)
        {
            throw new CoreException("Found more than one country in the folder: {}", countries);
        }
        this.outputDelegate.printlnCommandMessage(
                "Found " + inputShards.size() + " input shards: " + inputShards);
        this.outputDelegate.printlnCommandMessage("Found country: " + countries.iterator().next());
        final Set<Shard> outputShards = inputShards.stream().flatMap(
                inputShard -> Iterables.asList(outputSharding.shards(inputShard.bounds())).stream())
                .collect(Collectors.toSet());
        if (outputShards.isEmpty())
        {
            throw new CoreException("There are no resulting output shards.");
        }
        else
        {
            outputFolder.mkdirs();
        }
        this.outputDelegate.printlnCommandMessage(
                "Found " + outputShards.size() + " output shards: " + outputShards);
        for (final Shard outputShard : outputShards)
        {
            this.outputDelegate.printlnCommandMessage("Processing output shard " + outputShard);
            final List<File> inputAtlases = new ArrayList<>();
            Iterables.stream(inputSharding.shards(outputShard.bounds()))
                    .filter(inputShardToAtlas::containsKey)
                    .forEach(inputShard -> inputAtlases.add(inputShardToAtlas.get(inputShard)));
            this.outputDelegate.printlnCommandMessage("Loading Atlas with " + inputAtlases);
            final Atlas combined = new AtlasResourceLoader().load(inputAtlases);
            final Optional<Atlas> result = combined.subAtlas(outputShard.bounds(),
                    AtlasCutType.SOFT_CUT);
            final File outputFile = outputFolder.child(
                    countries.iterator().next() + "_" + outputShard.getName() + FileSuffix.ATLAS);
            this.outputDelegate.printlnCommandMessage("Saving Atlas to " + outputFile);
            result.ifPresent(atlas -> atlas.save(outputFile));
        }
        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "sharding-converter";
    }

    @Override
    public String getSimpleDescription()
    {
        return "Translate Atlas files from one Sharding to another";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION", AtlasShardingConverterCommand.class
                .getResourceAsStream("AtlasShardingConverterCommandDescriptionSection.txt"));
        addManualPageSection("EXAMPLES", AtlasShardingConverterCommand.class
                .getResourceAsStream("AtlasShardingConverterCommandExamplesSection.txt"));
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOptionWithRequiredArgument(INPUT, INPUT_DESCRIPTION, OptionOptionality.REQUIRED,
                "/path/to/atlases");
        registerOptionWithRequiredArgument(OUTPUT, OUTPUT_DESCRIPTION, OptionOptionality.REQUIRED,
                "/path/to/output");
        registerOptionWithRequiredArgument(INPUT_SHARDING, INPUT_SHARDING_DESCRIPTION,
                OptionOptionality.REQUIRED, "type@parameter");
        registerOptionWithRequiredArgument(OUTPUT_SHARDING, OUTPUT_SHARDING_DESCRIPTION,
                OptionOptionality.REQUIRED, "type@parameter");
        super.registerOptionsAndArguments();
    }
}
