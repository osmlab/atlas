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

/**
 * Flexible command for converting a {@link PackedAtlas} to a text Atlas
 *
 * @author cstaylor
 */
public class PackedToTextAtlasSubCommand implements FlexibleSubCommand
{
    private static final String NAME = "packed-to-text";

    private static final String DESCRIPTION = "converts a packed atlas to a text-based atlas";

    private static final Switch<Path> INPUT_PARAMETER = new Switch<>("packed-atlas",
            "Input atlas data in packed atlas format", Paths::get, Optionality.REQUIRED);

    private static final Switch<Path> OUTPUT_PARAMETER = new Switch<>("text-atlas",
            "Output text atlas data path", Paths::get, Optionality.REQUIRED);

    private Path inputPath;
    private Path outputPath;

    @Override
    public int execute(final CommandMap map)
    {
        this.inputPath = (Path) map.get(INPUT_PARAMETER);
        this.outputPath = (Path) map.get(OUTPUT_PARAMETER);
        preVerify();
        PackedAtlas.load(new File(this.inputPath.toFile()))
                .saveAsText(new File(this.outputPath.toFile()));
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
        writer.println("-text-atlas=/output/path/to/text/atlas");
        writer.println("-packed-atlas=/input/path/to/packed/atlas");
    }

    private void preVerify()
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
        catch (final IOException oops)
        {
            throw new CoreException("Error when creating directories {}",
                    this.outputPath.getParent(), oops);
        }
    }
}
