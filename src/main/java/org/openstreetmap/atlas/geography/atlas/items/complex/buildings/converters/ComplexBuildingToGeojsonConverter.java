package org.openstreetmap.atlas.geography.atlas.items.complex.buildings.converters;

import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.items.complex.buildings.ComplexBuildingFinder;
import org.openstreetmap.atlas.geography.geojson.GeoJsonSaver;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * Get all the building outlines in an Atlas to a GeoJson file.
 *
 * @author matthieun
 */
public class ComplexBuildingToGeojsonConverter extends Command
{
    public static final Switch<Atlas> ATLAS_FOLDER = new Switch<>("atlasFolder",
            "Folder containing the Atlas files to transcribe",
            value -> new AtlasResourceLoader().load(new File(value)), Optionality.REQUIRED);
    public static final Switch<File> OUTPUT = new Switch<>("output", "The output GeoJson file",
            value -> new File(value), Optionality.REQUIRED);

    public static void main(final String[] args)
    {
        new ComplexBuildingToGeojsonConverter().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final Atlas atlas = (Atlas) command.get(ATLAS_FOLDER);
        final File output = (File) command.get(OUTPUT);
        final Iterable<Polygon> shapes = Iterables.translateMulti(
                new ComplexBuildingFinder().find(atlas),
                building -> building.getOutline().outers());
        GeoJsonSaver.save(shapes, output);
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(ATLAS_FOLDER, OUTPUT);
    }
}
