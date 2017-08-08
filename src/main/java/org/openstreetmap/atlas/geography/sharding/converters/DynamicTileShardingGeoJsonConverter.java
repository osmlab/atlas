package org.openstreetmap.atlas.geography.sharding.converters;

import org.openstreetmap.atlas.geography.sharding.DynamicTileSharding;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * Save an existing serialized tree into a Geojson file.
 *
 * @author matthieun
 */
public class DynamicTileShardingGeoJsonConverter extends Command
{
    public static final Switch<WritableResource> GEOJSON = new Switch<>("geojson",
            "The resource where to save the geojson tree for debugging", File::new,
            Optionality.REQUIRED);
    public static final Switch<Resource> INPUT = new Switch<>("input",
            "The resource where to read the serialized tree", File::new, Optionality.REQUIRED);

    public static void main(final String[] args)
    {
        new DynamicTileShardingGeoJsonConverter().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final Resource input = (Resource) command.get(INPUT);
        final WritableResource geojson = (WritableResource) command.get(GEOJSON);
        new DynamicTileSharding(input).saveAsGeoJson(geojson);
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(GEOJSON, INPUT);
    }
}
