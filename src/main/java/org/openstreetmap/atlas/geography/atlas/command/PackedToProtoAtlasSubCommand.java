package org.openstreetmap.atlas.geography.atlas.command;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.runtime.Command.Optionality;
import org.openstreetmap.atlas.utilities.runtime.Command.Switch;
import org.openstreetmap.atlas.utilities.runtime.Command.SwitchList;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.runtime.FlexibleSubCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command for converting a serialized PackedAtlas to a serialized ProtoAtlas
 *
 * @author lcram
 */
public class PackedToProtoAtlasSubCommand implements FlexibleSubCommand
{
    private static final Logger logger = LoggerFactory
            .getLogger(PackedToProtoAtlasSubCommand.class);

    private static final String NAME = "packed-to-proto";
    private static final String DESCRIPTION = "converts a packed atlas to a naive proto-based atlas";
    private static final String PACKED_SWITCH_TEXT = "packed-atlas";
    private static final String PROTO_SWITCH_TEXT = "proto-atlas";

    private static final Switch<Path> INPUT_PARAMETER = new Switch<>(PACKED_SWITCH_TEXT,
            "Input atlas data in text atlas format", Paths::get, Optionality.REQUIRED);

    private static final Switch<Path> OUTPUT_PARAMETER = new Switch<>(PROTO_SWITCH_TEXT,
            "Output atlas data path", Paths::get, Optionality.REQUIRED);

    private Path inputPath;
    private Path outputPath;

    @Override
    public int execute(final CommandMap map)
    {
        this.inputPath = (Path) map.get(INPUT_PARAMETER);
        this.outputPath = (Path) map.get(OUTPUT_PARAMETER);
        verifyArguments();
        PackedAtlas.load(new File(this.inputPath.toFile()))
                .saveAsProto(new File(this.outputPath.toFile()));

        return 0;
    }

    @Override
    public String getDescription()
    {
        return DESCRIPTION;
    }

    @Override
    public String getName()
    {
        return NAME;
    }

    @Override
    public SwitchList switches()
    {
        return new SwitchList().with(INPUT_PARAMETER, OUTPUT_PARAMETER);
    }

    @Override
    public void usage(final PrintStream writer)
    {
        writer.println("-" + PACKED_SWITCH_TEXT + "=/input/path/to/packed/atlas");
        writer.println("-" + PROTO_SWITCH_TEXT + "=/output/path/to/proto/atlas");
    }

    private void verifyArguments()
    {
        if (!Files.isRegularFile(this.inputPath))
        {
            throw new CoreException("{} is not a readable file", this.inputPath);
        }

        try
        {
            if (Files.isDirectory(this.outputPath))
            {
                throw new CoreException("{} is a directory.  Aborting", this.outputPath);
            }
            Files.createDirectories(this.outputPath.getParent());
        }
        catch (final IOException exception)
        {
            throw new CoreException("Error when creating directories {}",
                    this.outputPath.getParent(), exception);
        }
    }
}
