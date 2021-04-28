package org.openstreetmap.atlas.utilities.archive;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * Demonstrates the Extractor class
 *
 * @author cstaylor
 */
public class UnzipperCommand extends Command
{
    private static final Switch<Path> OUTPUT_FILE_PARAMETER = new Switch<>("output",
            "Output directory to store zip file entries", Paths::get, Optionality.REQUIRED);

    private static final Switch<Path> INPUT_ZIP_FILE_PARAMETER = new Switch<>("zip",
            "Zip file to extract all of the data", Paths::get, Optionality.REQUIRED);

    public static void main(final String... args)
    {
        new UnzipperCommand().runWithoutQuitting(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final Path inputPath = prepareInput((Path) command.get(INPUT_ZIP_FILE_PARAMETER));
        final Path outputFile = prepareOutput((Path) command.get(OUTPUT_FILE_PARAMETER));
        try
        {
            Extractor.extractZipArchive(outputFile).extract(inputPath);
        }
        catch (final Exception oops)
        {
            throw new CoreException("Error when extracting: {} -> {}", inputPath, outputFile, oops);
        }
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(OUTPUT_FILE_PARAMETER, INPUT_ZIP_FILE_PARAMETER);

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
        if (Files.exists(outputFile) && !Files.isDirectory(outputFile))
        {
            throw new CoreException("{} already exists and is not a directory", outputFile);
        }
        try
        {
            Files.createDirectories(outputFile);
        }
        catch (final Exception oops)
        {
            throw new CoreException("Can't create {}", outputFile, oops);
        }
        return outputFile;
    }
}
