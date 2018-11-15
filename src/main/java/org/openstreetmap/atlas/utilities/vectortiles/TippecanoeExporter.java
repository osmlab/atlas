package org.openstreetmap.atlas.utilities.vectortiles;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.openstreetmap.atlas.geography.atlas.geojson.LineDelimitedGeoJsonConverter;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
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

        if (!TippecanoeCommands.hasValidTippecanoe())
        {
            logger.error(
                    "Your system does not have a valid installation of tippecanoe installed in its path.");
            logger.error("https://github.com/mapbox/tippecanoe");

            System.exit(EXIT_FAILURE);
        }

        final Path mbtiles = (Path) command.get(MBTILES);

        final Path geojsonDirectory = (Path) command.get(GEOJSON_DIRECTORY);
        final Path geojson = geojsonDirectory.resolve(TippecanoeSettings.EVERYTHING_GEOJSON);

        final Boolean overwrite = (Boolean) command.get(OVERWRITE);

        TippecanoeCommands.runTippecanoe(geojson, mbtiles, overwrite);

        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return super.switches().with(MBTILES);
    }

}
