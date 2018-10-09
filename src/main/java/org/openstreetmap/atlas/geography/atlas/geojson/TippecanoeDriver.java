package org.openstreetmap.atlas.geography.atlas.geojson;

import static org.openstreetmap.atlas.geography.atlas.geojson.TippecanoeUtils.fetchGeoJsonFilesInDirectory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.io.FileUtils;
import org.openstreetmap.atlas.streaming.resource.File;
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
            }
            catch (final IOException noDelete)
            {
                logger.warn(
                        "Tried to delete MBTiles output directory {} for overwrite, but unable.",
                        mbtilesDirectory);
            }
        }

        final List<File> geojsonFiles = fetchGeoJsonFilesInDirectory(geojsonDirectory);
        logger.info("About to convert {} GeoJSON files into MBTiles with tippecanoe...",
                geojsonFiles.size());

        // OK, now implement the tippecanoe CLI stuff...
        final CommandLine commandLine = CommandLine.parse("java");
        commandLine.addArgument("-version");
        final DefaultExecutor executor = new DefaultExecutor();
        try
        {
            final int exitCode = executor.execute(commandLine);
            logger.info("exit code: {}", exitCode);
        }
        catch (final IOException ioException)
        {
            logger.error("Unable to run tippecanoe.", ioException);
        }

        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(GEOJSON_DIRECTORY, MBTILES_DIRECTORY, OVERWRITE, PROCESSES);
    }
}
