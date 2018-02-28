package org.openstreetmap.atlas.geography.boundary;

import java.io.IOException;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.streaming.compression.Compressor;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 * @author Yiqing Jin
 */
public class CountryBoundaryMapArchiver extends Command
{
    private static final Logger logger = LoggerFactory.getLogger(CountryBoundaryMapArchiver.class);

    // Parameters
    private static final Switch<File> SHAPE_FILE = new Switch<>("shp", "path to the shape file",
            File::new, Optionality.OPTIONAL);
    private static final Switch<Atlas> ATLAS = new Switch<>("atlas",
            "path to the atlas file containing boundaries",
            path -> PackedAtlas.load(new File(path)), Optionality.OPTIONAL);
    private static final Switch<File> OUTPUT = new Switch<>("out", "The output file format",
            path -> new File(path));
    public static final Switch<StringList> COUNTRIES = new Switch<>("countries",
            "Comma separated list of countries to be included in the boundary file",
            value -> StringList.split(value, ","), Optionality.OPTIONAL);
    private static final Switch<Rectangle> BOUNDS = new Switch<>("bounds", "The bounds",
            rectangle -> Rectangle.forString(rectangle), Optionality.OPTIONAL,
            Rectangle.MAXIMUM.toCompactString());
    private static final Switch<Boolean> CREATE_SPATIAL_INDEX = new Switch<>("createSpatialIndex",
            "Indicator whether to create a spatial grid index and include that in the output.",
            Boolean::parseBoolean, Optionality.OPTIONAL, Boolean.FALSE.toString());

    public static void main(final String[] args)
    {
        new CountryBoundaryMapArchiver().run(args);
    }

    /**
     * @param resource
     *            The {@link Resource} to read the {@link CountryBoundaryMap} from
     * @return the created {@link CountryBoundaryMap}
     */
    public CountryBoundaryMap read(final Resource resource)
    {
        return CountryBoundaryMap.fromPlainText(resource);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        // Read inputs
        final File shapeFile = (File) command.get(SHAPE_FILE);
        final Atlas atlas = (Atlas) command.get(ATLAS);
        final File output = (File) command.get(OUTPUT);
        output.setCompressor(Compressor.GZIP);
        final Rectangle bounds = (Rectangle) command.get(BOUNDS);
        final boolean createIndex = (Boolean) command.get(CREATE_SPATIAL_INDEX);

        // Create boundary map
        final Time timer = Time.now();
        final CountryBoundaryMap map = new CountryBoundaryMap(bounds);
        if (atlas != null)
        {
            map.readFromAtlas(atlas);
        }

        if (shapeFile != null)
        {
            map.readFromShapeFile(shapeFile.getFile());
        }

        // Create index
        if (createIndex)
        {
            logger.info("Building Grid Index...");
            final Time startTime = Time.now();
            map.initializeGridIndex(map.getLoadedCountries());
            logger.info("Finished building Grid Index in {}", startTime.elapsedSince());
        }

        // Save
        try
        {
            logger.info("Saving CountryBoundaryMap to {}.", output);
            map.writeToFile(output);
            map.boundaries(Rectangle.MAXIMUM).forEach(boundary -> logger
                    .info("Loaded boundary for country {}", boundary.getCountryName()));
        }
        catch (final IOException e)
        {
            throw new CoreException("Could not write CountryBoundaryMap.");
        }

        logger.info("CountryBoundaryMap creation took {}.", timer.elapsedSince());
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(SHAPE_FILE, ATLAS, OUTPUT, BOUNDS, CREATE_SPATIAL_INDEX);
    }
}
