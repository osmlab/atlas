package org.openstreetmap.atlas.vectortiles;

import org.openstreetmap.atlas.geography.atlas.geojson.LineDelimitedGeoJsonConverter;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

import java.nio.file.Path;
import java.nio.file.Paths;

public class TippecanoeExporter extends LineDelimitedGeoJsonConverter
{
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

        return 0;
    }

    // TODO: Check to see if tippecanoe is installed.

    @Override
    protected SwitchList switches()
    {
        return super.switches().with(MBTILES);
    }
}
