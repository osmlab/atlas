package org.openstreetmap.atlas.vectortiles;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.util.StringUtils;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.openstreetmap.atlas.geography.atlas.geojson.LineDelimitedGeoJsonConverter;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.time.Time;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TippecanoeExporter extends LineDelimitedGeoJsonConverter
{
    private static final int EXIT_FAILURE = 1;

    private static final Switch<Path> MBTILES = new Switch<>("mbtiles", "The MBTiles file to which tippecanoe will write vector tiles.", Paths::get, Optionality.REQUIRED);

    private TippecanoeExporter()
    {
        this.jsonMutator = TippecanoeSettings.JSON_MUTATOR;
    }

    public static void main(final String[] args)
    {
        new TippecanoeExporter().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        super.onRun(command);

        if (!hasValidTippecanoe())
        {
            logger.error("Your system does not have a valid installation of tippecanoe installed in its path.");
            logger.error("https://github.com/mapbox/tippecanoe");

            System.exit(EXIT_FAILURE);
        }

        final Path mbtiles = (Path) command.get(MBTILES);

        final Path geojsonDirectory = (Path) command.get(GEOJSON_DIRECTORY);
        final Path geojson = geojsonDirectory.resolve(TippecanoeSettings.GEOJSON);

        final Boolean overwrite = (Boolean) command.get(OVERWRITE);

        runTippecanoe(geojson, mbtiles, overwrite);

        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return super.switches().with(MBTILES);
    }


    private boolean hasValidTippecanoe()
    {
        final CommandLine commandLine = CommandLine.parse("tippecanoe").addArgument("--version");
        logger.info("cmd: {}", commandLine);
        final DefaultExecutor executor = new DefaultExecutor();
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
        executor.setStreamHandler(streamHandler);
        try
        {
            executor.execute(commandLine);
        }
        // When you look up the version, it tippecanoe exits with 1, so getting here is normal.
        // We want to get into this catch.
        catch (IOException ioException)
        {
            final String outputString = outputStream.toString();
            logger.info(outputString);
            String[] versionArr = outputString.split("\n")[0].split("tippecanoe v");
            // Here we extract the version.
            if (versionArr.length == 2)
            {
                final String versionString = versionArr[1];
                final DefaultArtifactVersion version = new DefaultArtifactVersion(versionString);
                if (TippecanoeSettings.MIN_VERSION.compareTo(version) <= 0)
                {
                    return true;
                }
                else
                {
                    logger.error("Your version of tippecanoe is too old!");
                }
            }

        }
        return false;
    }


    private void runTippecanoe(final Path geojson, final Path mbtiles, boolean overwrite)
    {
        final Time time = Time.now();

        final CommandLine commandLine = CommandLine.parse("tippecanoe")
                .addArgument("-o").addArgument(mbtiles.toString())
                .addArguments(TippecanoeSettings.ARGS);

        if (overwrite)
        {
            commandLine.addArgument("--force");
        }

        commandLine.addArgument(geojson.toString());

        logger.info("Running tippecanoe...");

        logger.info(StringUtils.toString(commandLine.toStrings(), " "));

        final DefaultExecutor executor = new DefaultExecutor();
        try {
            executor.execute(commandLine);
            logger.info("tippecanoe has successfully generated vector tiles in {}", mbtiles);
        }
        catch (final IOException ioException)
        {
            logger.error("Unable to execute tippecanoe.", ioException);
        }
        logger.info("tippecanoe took {}", time.elapsedSince());
    }

}
