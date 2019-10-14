package org.openstreetmap.atlas.utilities.command.subcommands;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.streaming.resource.InputStreamResource;
import org.openstreetmap.atlas.streaming.resource.OutputStreamWritableResource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.MultipleOutputCommand;
import org.openstreetmap.atlas.utilities.testing.TestAtlasHandler;

/**
 * Convert an .osm file of various types to an atlas.
 *
 * @author jklamer
 */
public class OsmToAtlasCommand extends MultipleOutputCommand
{

    private static final String INPUT_OSM_FILE = "input-osm-file";
    private static final String JOSM = "josm";
    private static final String NAME = "name";
    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    public static void main(final String[] args)
    {
        new OsmToAtlasCommand().runSubcommandAndExit(args);
    }

    public OsmToAtlasCommand()
    {
        super();
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    public int execute()
    {
        try
        {
            // set up the output path from the parent class
            final int code = super.execute();
            if (code != 0)
            {
                return code;
            }

            final Path absoluteOsmPath = this.optionAndArgumentDelegate
                    .getUnaryArgument(INPUT_OSM_FILE).map(Paths::get)
                    .orElseThrow(AtlasShellToolsException::new).toAbsolutePath();

            if (!absoluteOsmPath.toFile().isDirectory())
            {
                final Atlas atlas = TestAtlasHandler.getAtlasFromJsomOsmResource(
                        this.optionAndArgumentDelegate.hasOption(JOSM),
                        new InputStreamResource(() -> new File(absoluteOsmPath.toString()).read()),
                        absoluteOsmPath.getFileName().toString());
                final WritableResource outputResource = new OutputStreamWritableResource(
                        this.getOutputFile(absoluteOsmPath).write());
                atlas.save(outputResource);
            }
            else
            {
                throw new CoreException("{} is a directory", absoluteOsmPath.toString());
            }
        }
        catch (final Exception exception)
        {
            this.outputDelegate.printlnErrorMessage("Exception during execution:");
            if (this.optionAndArgumentDelegate.hasVerboseOption())
            {
                exception.printStackTrace(System.out); // NOSONAR
                System.out.println(); // NOSONAR
            }
            else
            {
                this.outputDelegate.printlnErrorMessage(exception.getMessage());
            }
            return 1;
        }
        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "osm2atlas";
    }

    public File getOutputFile(final Path osmPath)
    {
        final Path outputDirectory = getOutputPath();
        final String outputFileName = this.optionAndArgumentDelegate.getOptionArgument(NAME).map(
                nameString -> nameString.replace(FileSuffix.ATLAS.toString(), StringUtils.EMPTY))
                .orElse(osmPath.getFileName().toString().replace(FileSuffix.OSM.toString(),
                        StringUtils.EMPTY))
                .concat(FileSuffix.ATLAS.toString());
        return new File(outputDirectory.toAbsolutePath().toString()).child(outputFileName);
    }

    @Override
    public String getSimpleDescription()
    {
        return "Convert a .osm file into an Atlas file";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection(DESCRIPTION, OsmToAtlasCommand.class
                .getResourceAsStream("OsmToAtlasCommandDescriptionSection.txt"));
        addManualPageSection(EXAMPLES, OsmToAtlasCommand.class
                .getResourceAsStream("OsmToAtlasCommandExamplesSection.txt"));
        super.registerManualPageSections();
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerArgument(INPUT_OSM_FILE, ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
        registerOptionWithRequiredArgument(NAME, "Name of output atlas file",
                OptionOptionality.OPTIONAL, "output-name");
        registerOption(JOSM, "osm file in JOSM format", OptionOptionality.OPTIONAL);
        super.registerOptionsAndArguments();
    }
}
