package org.openstreetmap.atlas.geography.boundary;

import java.io.IOException;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.streaming.compression.Compressor;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.LineFilteredResource;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
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

    private static final Switch<java.io.File> SHAPE_FILE = new Switch<>("shp",
            "path to the shape file", java.io.File::new, Optionality.OPTIONAL);
    private static final Switch<Atlas> ATLAS = new Switch<>("atlas",
            "path to the atlas file containing boundaries",
            path -> PackedAtlas.load(new File(path)), Optionality.OPTIONAL);
    private static final Switch<File> OUT = new Switch<>("out", "The output file format",
            path -> new File(path));
    private static final Switch<Rectangle> BOUNDS = new Switch<>("bounds", "The bounds",
            rectangle -> Rectangle.forString(rectangle), Optionality.OPTIONAL,
            Rectangle.MAXIMUM.toCompactString());
    private static final Switch<Boolean> CREATE_SPATIAL_INDEX = new Switch<>("createSpatialIndex",
            "Default true, performance optimization to create and serialize a spatial index.",
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
        return new CountryBoundaryMap(resource);
    }

    /**
     * @param resource
     *            The {@link Resource} to read the {@link CountryBoundaryMap} from
     * @param countries
     *            The countries we want included in the resulting {@link CountryBoundaryMap}
     * @return the created {@link CountryBoundaryMap}
     */
    public CountryBoundaryMap read(final Resource resource, final Iterable<String> countries)
    {
        return new CountryBoundaryMap(new LineFilteredResource(resource,
                CountryBoundaryMap.COUNTRY_FILTER_GENERATOR.apply(countries)));
    }

    /**
     * Saves the given {@link CountryBoundaryMap} to the provided {@link Resource}, including the
     * grid index.
     *
     * @param map
     *            The {@link CountryBoundaryMap} to save
     * @param resource
     *            The {@link Resource} to save to
     * @param gridIndexParts
     *            The {@link GridIndexParts} used to save the index
     */
    public void save(final CountryBoundaryMap map, final GridIndexParts gridIndexParts,
            final WritableResource resource)
    {
        try
        {
            logger.info("Saving CountryBoundaryMap to {}", resource);
            map.writeBoundariesAndGridIndexAsText(resource, gridIndexParts);
            map.boundaries(Rectangle.MAXIMUM).forEach(boundary -> logger
                    .info("Loaded boundary for country {}", boundary.getCountryName()));
        }
        catch (final IOException e)
        {
            throw new CoreException("Could not write CountryBoundaryMap.");
        }
    }

    /**
     * Saves the given {@link CountryBoundaryMap} to the provided {@link Resource}, will NOT save
     * the grid index.
     *
     * @param map
     *            The {@link CountryBoundaryMap} to save
     * @param resource
     *            The {@link Resource} to save to
     */
    public void save(final CountryBoundaryMap map, final WritableResource resource)
    {
        try
        {
            logger.info("Saving CountryBoundaryMap to {}", resource);
            map.writeBoundariesAsText(resource);
            map.boundaries(Rectangle.MAXIMUM).forEach(boundary -> logger
                    .info("Loaded boundary for country {}", boundary.getCountryName()));
        }
        catch (final IOException e)
        {
            throw new CoreException("Could not write CountryBoundaryMap.");
        }
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final java.io.File shapeFile = (java.io.File) command.get(SHAPE_FILE);
        final Atlas atlas = (Atlas) command.get(ATLAS);
        final File out = (File) command.get(OUT);
        out.setCompressor(Compressor.GZIP);
        final Rectangle bounds = (Rectangle) command.get(BOUNDS);
        final boolean createIndex = (Boolean) command.get(CREATE_SPATIAL_INDEX);
        final CountryBoundaryMap map;
        if (atlas != null)
        {
            map = new CountryBoundaryMap(atlas, bounds);
        }
        else
        {
            map = new CountryBoundaryMap(shapeFile, bounds);
        }

        if (createIndex)
        {
            logger.info("Building Grid Index...");
            final Time startTime = Time.now();
            final AbstractGridIndexBuilder builder = map.createGridIndex(map.getLoadedCountries(),
                    true);
            logger.info("Finished building Grid Index in {}", startTime.elapsedSince());

            save(map, new GridIndexParts(builder.getSpatialIndexCells(), builder.getEnvelope()),
                    out);
            return 0;
        }

        save(map, out);
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(SHAPE_FILE, ATLAS, OUT, BOUNDS, CREATE_SPATIAL_INDEX);
    }
}
