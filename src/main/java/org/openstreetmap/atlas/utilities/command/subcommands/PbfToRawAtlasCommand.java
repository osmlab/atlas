package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas.AtlasSerializationFormat;
import org.openstreetmap.atlas.geography.atlas.raw.creation.RawAtlasGenerator;
import org.openstreetmap.atlas.geography.sharding.CountryShard;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.MultipleOutputCommand;

/**
 * @author samgass
 */
public class PbfToRawAtlasCommand extends MultipleOutputCommand
{
    // The hint for the input path for the PBF file(s) to convert
    private static final String PBF_PATH_HINT = "pbf";

    // The country name for the country shards Atlas file(s) to output
    private static final String COUNTRY_NAME = "countryName";

    private static final String COUNTRY_NAME_DESCRIPTION = "The country for the shard to build";

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    private List<File> pbfs;

    public static void main(final String[] args)
    {
        new PbfToRawAtlasCommand().runSubcommandAndExit(args);
    }

    public PbfToRawAtlasCommand()
    {
        super();
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    public int execute()
    {
        // set up the output path from the parent class
        final int code = super.execute();
        if (code != 0)
        {
            return code;
        }

        getInputPBFs();
        final String countryName = this.optionAndArgumentDelegate.getOptionArgument(COUNTRY_NAME)
                .orElseThrow(AtlasShellToolsException::new);
        this.pbfs.forEach(pbf ->
        {
            final PackedAtlas rawAtlas = (PackedAtlas) new RawAtlasGenerator(pbf).build();
            final String pbfName = pbf.getName().replace(FileSuffix.PBF.toString(), "");
            final String rawAtlasFilename = String.format("%s%s%s%s", countryName,
                    CountryShard.COUNTRY_SHARD_SEPARATOR, pbfName, FileSuffix.ATLAS);
            rawAtlas.setSaveSerializationFormat(AtlasSerializationFormat.PROTOBUF);
            final Path concatenatedPath;
            if (this.optionAndArgumentDelegate
                    .hasOption(MultipleOutputCommand.OUTPUT_DIRECTORY_OPTION_LONG))
            {
                // save atlas to user specified output directory
                concatenatedPath = Paths.get(getOutputPath().toAbsolutePath().toString(),
                        rawAtlasFilename);
            }
            else
            {
                // save atlas in place
                concatenatedPath = Paths.get(
                        Paths.get(pbf.getAbsolutePath()).getParent().toAbsolutePath().toString(),
                        rawAtlasFilename);
            }
            this.outputDelegate.printlnStdout(concatenatedPath.toAbsolutePath().toString());
            final File outputFile = new File(concatenatedPath.toAbsolutePath().toString());
            rawAtlas.save(outputFile);
        });
        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "pbf2raw";
    }

    @Override
    public String getSimpleDescription()
    {
        return "generate raw Atlas file(s) from the given PBF shard(s)";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION", PbfToRawAtlasCommand.class
                .getResourceAsStream("PbfToRawAtlasDescriptionSection.txt"));
        addManualPageSection("EXAMPLES", PbfToRawAtlasCommand.class
                .getResourceAsStream("PbfToRawAtlasCommandExamplesSection.txt"));
    }

    @Override
    public void registerOptionsAndArguments()
    {
        final Integer[] contexts = this.optionAndArgumentDelegate.getFilteredRegisteredContexts()
                .toArray(new Integer[0]);
        this.registerArgument(PBF_PATH_HINT, ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED,
                contexts);
        this.registerOptionWithRequiredArgument(COUNTRY_NAME, COUNTRY_NAME_DESCRIPTION,
                OptionOptionality.REQUIRED, COUNTRY_NAME);
        super.registerOptionsAndArguments();
    }

    /**
     * Get a list of input PBF resources from the input switch
     *
     * @return A list of PBF files
     */
    private List<File> getInputPBFs()
    {
        if (this.pbfs == null)
        {
            this.pbfs = new ArrayList<>();
        }
        else
        {
            return this.pbfs;
        }

        final List<String> inputPbfPaths = this.optionAndArgumentDelegate
                .getVariadicArgument(PBF_PATH_HINT);

        inputPbfPaths.stream().forEach(path ->
        {
            final File file = new File(path, false);
            if (!file.exists())
            {
                this.outputDelegate.printlnWarnMessage("file not found: " + path);
            }
            else if (file.isDirectory())
            {
                this.outputDelegate.printlnWarnMessage("skipping directory: " + path);
            }
            else
            {
                if (this.optionAndArgumentDelegate.hasVerboseOption())
                {
                    this.outputDelegate.printlnCommandMessage("loading " + path);
                }

                this.pbfs.add(file);
            }
        });

        return this.pbfs;
    }
}
