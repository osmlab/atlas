package org.openstreetmap.atlas.utilities.vectortiles;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.openstreetmap.atlas.geography.atlas.geojson.LineDelimitedGeoJsonConverter;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simple CLI that will take tippecanoe line-delimited GeoJSON output and convert it into
 * vector tiles with tippecanoe. If you would like the full end-to-end conversion of atlas files
 * into an MBTiles file, use TippecanoeExporter. If you would like to just convert atlas files into
 * line-delimited GeoJSON, use LineDelimitedGeoJsonConverter.
 *
 * @author hallahan
 */
public class TippecanoeConverter extends Command
{
    private static final Logger logger = LoggerFactory.getLogger(TippecanoeConverter.class);

    private static final Command.Switch<Path> GEOJSON_DIRECTORY = new Command.Switch<>(
            "geojsonDirectory", "The directory to read line-delimited GeoJSON.", Paths::get,
            Command.Optionality.REQUIRED);

    private static final Switch<Path> MBTILES = new Switch<>("mbtiles",
            "The MBTiles file to which tippecanoe will write vector tiles.", Paths::get,
            Optionality.REQUIRED);

    private static final Command.Switch<Boolean> OVERWRITE = new Command.Switch<>("overwrite",
            "Choose to automatically overwrite an MBTiles file if it exists at the given path.",
            Boolean::new, Command.Optionality.OPTIONAL, "false");

    public static void main(final String[] args)
    {
        new TippecanoeConverter().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        if (!TippecanoeCommands.hasValidTippecanoe())
        {
            logger.error(
                    "Your system does not have a valid installation of tippecanoe installed in its path.");
            logger.error("https://github.com/mapbox/tippecanoe");

            System.exit(LineDelimitedGeoJsonConverter.EXIT_FAILURE);
        }

        final Path mbtiles = (Path) command.get(MBTILES);
        final Path geojsonDirectory = (Path) command.get(GEOJSON_DIRECTORY);
        final Path geojson = geojsonDirectory.resolve(LineDelimitedGeoJsonConverter.EVERYTHING);
        final Boolean overwrite = (Boolean) command.get(OVERWRITE);

        TippecanoeCommands.decompress(geojsonDirectory);
        TippecanoeCommands.concatenate(geojsonDirectory);

        TippecanoeCommands.runTippecanoe(geojson, mbtiles, overwrite, TippecanoeSettings.ARGS);

        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(GEOJSON_DIRECTORY, MBTILES, OVERWRITE);
    }
}
