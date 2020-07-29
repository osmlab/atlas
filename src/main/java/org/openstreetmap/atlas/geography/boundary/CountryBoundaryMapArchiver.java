package org.openstreetmap.atlas.geography.boundary;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.streaming.compression.Compressor;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 * @author Yiqing Jin
 * @author james-gage
 */
public class CountryBoundaryMapArchiver extends Command
{
    // Inputs
    protected static final Switch<File> SHAPE_FILE = new Switch<>("shp", "path to the shape file",
            File::new, Optionality.OPTIONAL);
    protected static final Switch<Atlas> ATLAS = new Switch<>("atlas",
            "path to the atlas file containing boundaries",
            path -> PackedAtlas.load(new File(path)), Optionality.OPTIONAL);
    protected static final Switch<File> BOUNDARY_FILE = new Switch<>("boundaries",
            "path to the pre-existing boundary file", File::new, Optionality.OPTIONAL);
    // Outputs
    protected static final Switch<File> OUTPUT = new Switch<>("out", "The output file format",
            File::new, Optionality.REQUIRED);
    // Options
    protected static final Switch<Rectangle> BOUNDS = new Switch<>("bounds", "The bounds",
            Rectangle::forString, Optionality.OPTIONAL, Rectangle.MAXIMUM.toCompactString());
    protected static final Switch<Boolean> CREATE_SPATIAL_INDEX = new Switch<>("createSpatialIndex",
            "Indicator whether to create a spatial grid index and include that in the output.",
            Boolean::parseBoolean, Optionality.OPTIONAL, Boolean.FALSE.toString());
    protected static final Switch<Integer> OCEAN_BOUNDARY_ZOOM_LEVEL = new Switch<>(
            "oceanBoundaryZoomLevel",
            "The zoom level at which to create ocean tiles to fill in potential voids. Recommended value: 3",
            Integer::parseInt, Optionality.OPTIONAL);
    protected static final Switch<Boolean> SAVE_GEOJSON_WKT = new Switch<>("saveGeojsonWkt",
            "Save the country boundaries to Geojson and WKT", Boolean::parseBoolean,
            Optionality.OPTIONAL, Boolean.FALSE.toString());
    private static final Logger logger = LoggerFactory.getLogger(CountryBoundaryMapArchiver.class);

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

    protected CountryBoundaryMap generateOceanBoundaryMap(final CountryBoundaryMap boundaryMap,
            final Iterable<SlippyTile> allTiles)
    {
        final CountryBoundaryMap finalBoundaryMap = new CountryBoundaryMap();
        int oceanCountryCount = 0;
        // add all ocean boundaries to the new boundary map
        logger.info("Calculating ocean boundaries...");
        for (final SlippyTile tile : allTiles)
        {
            final Time start = Time.now();
            final String countryCode = String.format("O%02d", oceanCountryCount);
            final Geometry countryGeometry = geometryForShard(tile.bounds(), boundaryMap);
            if (!countryGeometry.isEmpty())
            {
                if (countryGeometry instanceof Polygon)
                {
                    finalBoundaryMap.addCountry(countryCode, (Polygon) countryGeometry);
                }
                if (countryGeometry instanceof MultiPolygon)
                {
                    finalBoundaryMap.addCountry(countryCode, (MultiPolygon) countryGeometry);
                }
                logger.info("Added Ocean Country {} in {}", countryCode, start.elapsedSince());
                oceanCountryCount++;
            }
            else
            {
                logger.info("Skipped Ocean Country {} in {}. It is land covered.", tile.getName(),
                        start.elapsedSince());
            }
        }

        // add all countries from the input boundary map to the new boundary map
        logger.info("Adding back country boundaries to the new ocean boundary map");
        final JtsMultiPolygonConverter multiPolyConverter = new JtsMultiPolygonConverter();
        for (final String country : boundaryMap.allCountryNames())
        {
            for (final CountryBoundary countryBoundary : boundaryMap.countryBoundary(country))
            {
                final GeometryFactory factory = new GeometryFactory();
                final Set<org.locationtech.jts.geom.Polygon> boundaryPolygons = multiPolyConverter
                        .convert(countryBoundary.getBoundary());
                final org.locationtech.jts.geom.MultiPolygon countryGeometry = factory
                        .createMultiPolygon(
                                boundaryPolygons.toArray(new Polygon[boundaryPolygons.size()]));
                finalBoundaryMap.addCountry(country, countryGeometry);
            }
        }
        return finalBoundaryMap;
    }

    protected Geometry geometryForShard(final Rectangle shardBounds,
            final CountryBoundaryMap boundaryMap)
    {
        final JtsMultiPolygonConverter multiPolyConverter = new JtsMultiPolygonConverter();
        final JtsPolygonConverter polyConverter = new JtsPolygonConverter();
        final GeometryFactory factory = new GeometryFactory();
        final List<CountryBoundary> boundaries = boundaryMap.boundaries(shardBounds);
        // jts version of the initial shard bounds
        org.locationtech.jts.geom.Geometry shardPolyJts = polyConverter.convert(shardBounds);
        // remove country boundaries from the ocean tile one by one
        for (final CountryBoundary boundary : boundaries)
        {
            final Set<Polygon> boundaryPolygons = multiPolyConverter
                    .convert(boundary.getBoundary());
            final org.locationtech.jts.geom.MultiPolygon countryGeometry = factory
                    .createMultiPolygon(
                            boundaryPolygons.toArray(new Polygon[boundaryPolygons.size()]));
            shardPolyJts = shardPolyJts.difference(countryGeometry);
        }
        return shardPolyJts;
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        // Read inputs
        final File shapeFile = (File) command.get(SHAPE_FILE);
        final Atlas atlas = (Atlas) command.get(ATLAS);
        final File boundaries = (File) command.get(BOUNDARY_FILE);
        final File output = (File) command.get(OUTPUT);
        output.setCompressor(Compressor.GZIP);
        final Rectangle bounds = (Rectangle) command.get(BOUNDS);
        final boolean createIndex = (Boolean) command.get(CREATE_SPATIAL_INDEX);
        final Integer oceanBoundaryZoomLevel = (Integer) command.get(OCEAN_BOUNDARY_ZOOM_LEVEL);
        final boolean saveGeojsonWkt = (Boolean) command.get(SAVE_GEOJSON_WKT);

        // Create boundary map
        final Time timer = Time.now();
        CountryBoundaryMap map = new CountryBoundaryMap(bounds);
        if (atlas != null)
        {
            map.readFromAtlas(atlas);
        }
        else if (shapeFile != null)
        {
            map.readFromShapeFile(shapeFile.getFile());
        }
        else if (boundaries != null)
        {
            map.readFromPlainText(boundaries);
        }
        else
        {
            throw new CoreException("No input data was specified to build a Country Boundary Map");
        }

        // Add oceans
        if (oceanBoundaryZoomLevel != null)
        {
            final Iterable<SlippyTile> allTiles = SlippyTile.allTiles(oceanBoundaryZoomLevel);
            map = generateOceanBoundaryMap(map, allTiles);
        }

        // Create index
        if (createIndex)
        {
            logger.info("Building Grid Index...");
            final Time startTime = Time.now();
            final Set<String> loadedCountries = map.getLoadedCountries();
            map.initializeGridIndex(loadedCountries);
            logger.info("Finished building Grid Index in {}", startTime.elapsedSince());
        }

        // Save
        try
        {
            logger.info("Saving CountryBoundaryMap to {}.", output);
            map.writeToFile(output);
        }
        catch (final IOException e)
        {
            throw new CoreException("Could not write CountryBoundaryMap.", e);
        }

        // Use printer for Geojson and WKT
        if (saveGeojsonWkt)
        {
            new CountryBoundaryMapPrinter().print(output);
        }

        logger.info("CountryBoundaryMap creation took {}.", timer.elapsedSince());
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(SHAPE_FILE, ATLAS, BOUNDARY_FILE, OUTPUT, BOUNDS,
                CREATE_SPATIAL_INDEX, OCEAN_BOUNDARY_ZOOM_LEVEL, SAVE_GEOJSON_WKT);
    }
}
