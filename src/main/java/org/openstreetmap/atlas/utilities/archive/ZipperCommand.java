package org.openstreetmap.atlas.utilities.archive;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.compress.archivers.ArchiveException;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * Demonstrates the Archiver class
 *
 * @author cstaylor
 */
public class ZipperCommand extends Command
{
    private static final Switch<Path> INPUT_FILE_PARAMETER = new Switch<>("input",
            "Input files to store in a zip file", Paths::get, Optionality.REQUIRED);

    private static final Switch<Path> OUTPUT_ZIP_FILE_PARAMETER = new Switch<>("zip",
            "Zip file to store all of the data", Paths::get, Optionality.REQUIRED);

    private static final Flag COMPRESSION_FLAG = new Flag("compress",
            "Enable compression of all files");

    public static void main(final String... args)
    {
        new ZipperCommand().runWithoutQuitting(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final Path inputPath = prepareInput((Path) command.get(INPUT_FILE_PARAMETER));
        final Path outputFile = prepareOutput((Path) command.get(OUTPUT_ZIP_FILE_PARAMETER));
        try
        {
            Archiver.createZipArchiver(outputFile).compress(inputPath);
        }
        catch (final IOException | ArchiveException oops)
        {
            throw new CoreException("Error when archiving: {} -> {}", inputPath, outputFile, oops);
        }
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(INPUT_FILE_PARAMETER, OUTPUT_ZIP_FILE_PARAMETER,
                COMPRESSION_FLAG);

    }

    private Path prepareInput(final Path inputPath)
    {
        if (!Files.isReadable(inputPath))
        {
            throw new CoreException("Can't read {} or it doesn't exist", inputPath);
        }
        return inputPath;
    }

    private Path prepareOutput(final Path outputFile)
    {
        if (Files.exists(outputFile))
        {
            throw new CoreException("{} already exists. Aborting", outputFile);
        }
        try
        {
            Files.createDirectories(outputFile.getParent());
        }
        catch (final IOException oops)
        {
            throw new CoreException("Can't create parent directories for {}", outputFile, oops);
        }
        return outputFile;
    }
}
