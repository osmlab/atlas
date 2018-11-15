package org.openstreetmap.atlas.utilities.vectortiles;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.utilities.runtime.RunScript;
import org.openstreetmap.atlas.utilities.runtime.SingleLineMonitor;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This utility class allows you to execute the tippecanoe command line tool ultimately via Java's
 * runtime exec.
 *
 * @author hallahan
 */
public final class TippecanoeCommands
{
    private TippecanoeCommands()
    {
        // Utility class
    }

    private static final Logger logger = LoggerFactory.getLogger(TippecanoeCommands.class);
    private static final SingleLineMonitor MONITOR = new SingleLineMonitor()
    {
        @Override
        protected Optional<String> parseResult(final String line)
        {
            return Optional.of(line);
        }
    };

    /**
     * Allows you to check if you have installed tippecanoe on your system and if it satisfies the
     * minimum version that we need.
     *
     * @return Whether you have a valid tippecanoe ready to execute in your environment.
     */
    public static boolean hasValidTippecanoe()
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

    /**
     * Runs the tippecanoe CLI with the default arguments found in TippecanoeSettings.java
     *
     * @param geojson
     *            The path to a single line-delimited GeoJSON file.
     * @param mbtiles
     *            The path to write the MBTiles file.
     * @param overwrite
     *            Whether we should be able to overwrite an existing MBTiles file.
     */
    public static void runTippecanoe(final Path geojson, final Path mbtiles,
            final boolean overwrite)
    {
        runTippecanoe(geojson, mbtiles, overwrite, TippecanoeSettings.ARGS);
    }

    /**
     * Runs the tippecanoe CLI with the provided arguments.
     *
     * @param geojson
     *            The path to a single line-delimited GeoJSON file.
     * @param mbtiles
     *            The path to write the MBTiles file.
     * @param overwrite
     *            Whether we should be able to overwrite an existing MBTiles file.
     * @param args
     *            tippecanoe CLI arguments as documented in
     *            https://github.com/mapbox/tippecanoe/blob/master/README.md
     */
    public static void runTippecanoe(final Path geojson, final Path mbtiles,
            final boolean overwrite, final String[] args)
    {
        final Time time = Time.now();

        final List<String> commandList = new ArrayList<>();

        commandList.add("tippecanoe");
        commandList.add("-o");
        commandList.add(mbtiles.toString());

        commandList.addAll(Arrays.asList(args));

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
