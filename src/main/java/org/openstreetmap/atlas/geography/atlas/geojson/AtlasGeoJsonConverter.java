package org.openstreetmap.atlas.geography.atlas.geojson;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasLoadingCommand;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility to save an Atlas as GeoJson
 *
 * @author matthieun
 */
public class AtlasGeoJsonConverter extends AtlasLoadingCommand
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasGeoJsonConverter.class);

    private static final Switch<File> GEOJSON = new Switch<>("geojson",
            "The file where to save as GeoJson", File::new, Optionality.REQUIRED);

    public static void main(final String[] args)
    {
        new AtlasGeoJsonConverter().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final Atlas atlas = loadAtlas(command);
        final File output = (File) command.get(GEOJSON);
        atlas.saveAsGeoJson(output);
        logger.info("Saved to {}", output);
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return super.switches().with(GEOJSON);
    }
}
