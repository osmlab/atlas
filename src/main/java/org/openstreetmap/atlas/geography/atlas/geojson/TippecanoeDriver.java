package org.openstreetmap.atlas.geography.atlas.geojson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Drives tippecanoe to create MapboxVectorTiles with GeoJSON.
 *
 * @author hallahan
 */
public class TippecanoeDriver extends Command
{
    private static final int DEFAULT_PROCESSES = 8;

    private static final Logger logger = LoggerFactory.getLogger(TippecanoeDriver.class);

    private static final Switch<Path> GEOJSON_DIRECTORY = new Switch<>("geojsonDirectory",
            "The directory to write tippecanoe GeoJSON.", Paths::get, Optionality.REQUIRED);

    private static final Switch<Path> MBTILES_DIRECTORY = new Switch<>("mbtilesDirectory",
            "The directory to write our MBTiles files from tippecanoe.", Paths::get,
            Optionality.REQUIRED);

    private static final Switch<Boolean> OVERWRITE = new Switch<>("overwrite",
            "Choose to automatically overwrite your MBTiles.", Boolean::new, Optionality.OPTIONAL,
            "false");

    private static final Switch<Integer> PROCESSES = new Switch<>("processes",
            "The number of processes of tippecanoe to run in parallel.", Integer::valueOf,
            Optionality.OPTIONAL, String.valueOf(DEFAULT_PROCESSES));

    public static void main(final String[] args)
    {
        new TippecanoeDriver().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final Time time = Time.now();
        final Path geojsonDirectory = (Path) command.get(GEOJSON_DIRECTORY);
        final Path mbtilesDirectory = (Path) command.get(MBTILES_DIRECTORY);
        final Boolean overwrite = (Boolean) command.get(OVERWRITE);
        final int processes = (Integer) command.get(PROCESSES);

        if (overwrite)
        {
            try
            {
                FileUtils.deleteDirectory(mbtilesDirectory.toFile());
                Files.createDirectory(mbtilesDirectory);
            }
            catch (final IOException noDelete)
            {
                logger.warn(
                        "Tried to delete MBTiles output directory {} for overwrite, but unable.",
                        mbtilesDirectory);
            }
        }

        List<File> geojsonFiles = fetchGeoJsonFilesInDirectory(geojsonDirectory);
        logger.info("About to convert {} GeoJSON files into MBTiles with tippecanoe...",
                geojsonFiles.size());

        while (true)
        {
            final int size = geojsonFiles.size();
            if (processes <= size)
            {
                process(geojsonFiles.subList(0, processes), mbtilesDirectory);
                geojsonFiles = geojsonFiles.subList(processes, size);
            }
            else
            {
                process(geojsonFiles, mbtilesDirectory);
                break;
            }
        }

        logger.info(
                "Finished converting directory of GeoJSON to MBTiles in {}.\ngeojson: {}\nmbtiles: {}",
                time.elapsedSince(), geojsonDirectory, mbtilesDirectory);
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(GEOJSON_DIRECTORY, MBTILES_DIRECTORY, OVERWRITE, PROCESSES);
    }

    private void process(final List<File> geojsonFiles, final Path mbtilesDirectory)
    {
        final List<DefaultExecuteResultHandler> handlers = new ArrayList<>();
        for (final File geojson : geojsonFiles)
        {
            final DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
            handlers.add(handler);
            final String name = FilenameUtils.removeExtension(geojson.getName());
            final Path mbtiles = mbtilesDirectory.resolve(name + ".mbtiles");

            final CommandLine commandLine = CommandLine.parse("tippecanoe").addArgument("-o")
                    .addArgument(mbtiles.toString(), true).addArgument("-Z10").addArgument("-z14")
                    .addArgument("--drop-densest-as-needed")
                    .addArgument(geojson.getAbsolutePath(), true);
            logger.info("Executing: {}", commandLine);

            final DefaultExecutor executor = new DefaultExecutor();
            try
            {
                executor.execute(commandLine, handler);
                logger.info("Started execution: {}", mbtiles.getFileName());
            }
            catch (final IOException ioException)
            {
                logger.error("{} failed.", commandLine, ioException);
            }
        }

        // Wait for the handlers to all be done before moving on...
        for (final DefaultExecuteResultHandler handler : handlers)
        {
            try
            {
                handler.waitFor();
            }
            catch (final InterruptedException interruptedException)
            {
                logger.error("tippecanoe interrupted", interruptedException);
            }
        }

    }

    private static List<File> fetchGeoJsonFilesInDirectory(final Path directory)
    {
        return new File(directory.toFile()).listFilesRecursively().stream()
                .filter(FileSuffix.resourceFilter(FileSuffix.GEO_JSON))
                .collect(Collectors.toList());
    }
}
