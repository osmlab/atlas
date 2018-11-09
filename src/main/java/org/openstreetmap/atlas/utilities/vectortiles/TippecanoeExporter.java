package org.openstreetmap.atlas.utilities.vectortiles;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.geojson.LineDelimitedGeoJsonConverter;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.runtime.RunScript;
import org.openstreetmap.atlas.utilities.runtime.SingleLineMonitor;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This CLI will take a directory full of atlases and export it into an MBTiles file full of Mapbox
 * vector tiles. It creates intermediary line-delimited GeoJSON, and then it ultimately drives
 * tippecanoe to do the vector tile creation.
 *
 * @author hallahan
 */
public final class TippecanoeExporter extends LineDelimitedGeoJsonConverter
{
    private static final int EXIT_FAILURE = 1;

    private static final Logger logger = LoggerFactory.getLogger(TippecanoeExporter.class);

    private static final Switch<Path> MBTILES = new Switch<>("mbtiles",
            "The MBTiles file to which tippecanoe will write vector tiles.", Paths::get,
            Optionality.REQUIRED);

    private TippecanoeExporter()
    {
        this.setJsonMutator(TippecanoeSettings.JSON_MUTATOR);
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
            logger.error(
                    "Your system does not have a valid installation of tippecanoe installed in its path.");
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
        final String[] commandArray = new String[] { "tippecanoe", "--version" };
        try
        {
            RunScript.run(commandArray, Collections.singletonList(MONITOR));
        }
        // When you look up the version, tippecanoe exits with 1, so getting here is normal.
        // We want to get into this catch.
        catch (final CoreException exception)
        {
            final Optional<String> result = MONITOR.getResult();
            if (result.isPresent())
            {
                final String outputString = result.get();
                final String[] versionArray = outputString.split("\n")[0].split("tippecanoe v");
                // Here we extract the version.
                if (versionArray.length == 2)
                {
                    final String versionString = versionArray[1];
                    final DefaultArtifactVersion version = new DefaultArtifactVersion(
                            versionString);
                    if (TippecanoeSettings.MIN_VERSION.compareTo(version) <= 0)
                    {
                        return true;
                    }
                    else
                    {
                        logger.error(
                                "Your version of tippecanoe is too old! The minimum version is {}",
                                TippecanoeSettings.MIN_VERSION);
                    }
                }
            }
        }
        return false;
    }

    private static final SingleLineMonitor MONITOR = new SingleLineMonitor()
    {
        @Override
        protected Optional<String> parseResult(final String line)
        {
            return Optional.of(line);
        }
    };

    private void runTippecanoe(final Path geojson, final Path mbtiles, final boolean overwrite)
    {
        final Time time = Time.now();

        final List<String> commandList = new ArrayList<>();

        commandList.add("tippecanoe");
        commandList.add("-o");
        commandList.add(mbtiles.toString());

        commandList.addAll(Arrays.asList(TippecanoeSettings.ARGS));

        if (overwrite)
        {
            commandList.add("--force");
        }

        commandList.add(geojson.toString());

        final String[] commandArray = commandList.toArray(new String[0]);

        logger.info("Running tippecanoe...");

        logger.info(StringUtils.join(commandArray, " "));

        RunScript.run(commandArray);

        logger.info("tippecanoe has successfully generated vector tiles in {}", mbtiles);
        logger.info("tippecanoe took {}", time.elapsedSince());
    }

}
