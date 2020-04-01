package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas.AtlasSerializationFormat;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.raw.creation.RawAtlasGenerator;
import org.openstreetmap.atlas.geography.atlas.raw.sectioning.WaySectionProcessor;
import org.openstreetmap.atlas.geography.sharding.Shard;
import org.openstreetmap.atlas.streaming.compression.Decompressor;
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
 * @author matthieun
 */
public class PbfToAtlasCommand extends MultipleOutputCommand
{
    // The hint for the input path for the PBF file(s) to convert
    private static final String PBF_PATH_HINT = "pbf";

    // The country name for the country shards Atlas file(s) to output
    private static final String COUNTRY_NAME = "countryName";

    // The file containing the WKT polygon to constrain the loading
    private static final String BOUNDS = "bounds";

    // Whether or not to stop at the raw atlas
    private static final String RAW = "raw";

    private static final String COUNTRY_NAME_DESCRIPTION = "The country for the shard to build";
    private static final String BOUNDS_DESCRIPTION = "The file containing WKT bounds to restrain the loading.";
    private static final String RAW_DESCRIPTION = "Whether or not to stop at the raw atlas. If this is enabled, way-sectioning will not happen";

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    private List<File> pbfs;

    public static void main(final String[] args)
    {
        new PbfToAtlasCommand().runSubcommandAndExit(args);
    }

    public PbfToAtlasCommand()
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
            PackedAtlas atlas = (PackedAtlas) new RawAtlasGenerator(pbf,
                    AtlasLoadingOption.createOptionWithOnlySectioning(), getBounds()).build();
            final String pbfName = pbf.getName().replace(FileSuffix.PBF.toString(), "");
            final String rawAtlasFilename = String.format("%s%s%s%s", countryName,
                    Shard.SHARD_DATA_SEPARATOR, pbfName, FileSuffix.ATLAS);
            if (!stopAtRaw())
            {
                final WaySectionProcessor waySectionProcessor = new WaySectionProcessor(atlas,
                        AtlasLoadingOption.createOptionWithNoSlicing());
                atlas = (PackedAtlas) waySectionProcessor.run();
            }
            atlas.setSaveSerializationFormat(AtlasSerializationFormat.PROTOBUF);
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
            atlas.save(outputFile);
        });
        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "pbf2atlas";
    }

    @Override
    public String getSimpleDescription()
    {
        return "Generate way-sectioned Atlas file(s) from the given PBF shard(s)";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION",
                PbfToAtlasCommand.class.getResourceAsStream("PbfToAtlasDescriptionSection.txt"));
        addManualPageSection("EXAMPLES", PbfToAtlasCommand.class
                .getResourceAsStream("PbfToAtlasCommandExamplesSection.txt"));
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
        this.registerOptionWithRequiredArgument(BOUNDS, BOUNDS_DESCRIPTION,
                OptionOptionality.OPTIONAL, BOUNDS);
        this.registerOption(RAW, RAW_DESCRIPTION, OptionOptionality.OPTIONAL);
        super.registerOptionsAndArguments();
    }

    private MultiPolygon getBounds()
    {
        final Optional<String> boundsFilePathOption = this.optionAndArgumentDelegate
                .getOptionArgument(BOUNDS);
        if (boundsFilePathOption.isPresent())
        {
            final String wktFileName = boundsFilePathOption.get();
            final File wktFile = new File(wktFileName);
            if (wktFileName.endsWith(FileSuffix.GZIP.toString()))
            {
                wktFile.setDecompressor(Decompressor.GZIP);
            }
            final String wkt = wktFile.firstLine();
            return MultiPolygon.wkt(wkt);
        }
        else
        {
            return MultiPolygon.MAXIMUM;
        }
    }

    /**
     * Get a list of input PBF resources from the input switch
     */
    private void getInputPBFs()
    {
        if (this.pbfs == null)
        {
            this.pbfs = new ArrayList<>();
        }
        else
        {
            return;
        }

        final List<String> inputPbfPaths = this.optionAndArgumentDelegate
                .getVariadicArgument(PBF_PATH_HINT);

        inputPbfPaths.forEach(path ->
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
    }

    private boolean stopAtRaw()
    {
        return this.optionAndArgumentDelegate.hasOption(RAW);
    }
}
