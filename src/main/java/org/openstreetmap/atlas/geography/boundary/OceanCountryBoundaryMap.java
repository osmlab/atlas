package org.openstreetmap.atlas.geography.boundary;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jtslab.SnapRoundOverlayFunctions;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Command that takes a completed country boundary map and writes a new boundary map that is the
 * same as the first, but also contains ocean countries.
 *
 * @author james-gage
 */
public class OceanCountryBoundaryMap extends Command
{
    private static final Switch<File> BOUNDARY_MAP = new Switch<>("boundaryMap",
            "The country boundary map", File::new, Optionality.REQUIRED);
    private static final Switch<File> OUTPUT = new Switch<>("output",
            "The ocean country boundary file to be output", File::new, Optionality.REQUIRED);
    // ocean boundary slippytile zoom level
    private static final int OCEAN_BOUNDARY_ZOOM_LEVEL = 3;
    private static final double JTS_SNAP_PRECISION = .000000000000001;

    private static final Logger logger = LoggerFactory.getLogger(OceanCountryBoundaryMap.class);

    public static CountryBoundaryMap generateOceanBoundaryMap(final CountryBoundaryMap boundaryMap,
            final Iterable<SlippyTile> allTiles)
    {
        final CountryBoundaryMap finalBoundaryMap = new CountryBoundaryMap();
        int oceanCountryCount = 0;
        // add all ocean boundaries to the new boundary map
        logger.info("Calculating ocean boundaries");
        for (final SlippyTile tile : allTiles)
        {
            final String countryCode = String.format("O%02d", oceanCountryCount);
            final Geometry countryMP = geometryForShard(tile.bounds(), boundaryMap);
            if (!countryMP.isEmpty())
            {
                if (countryMP instanceof Polygon)
                {
                    finalBoundaryMap.addCountry(countryCode, (Polygon) countryMP);
                }
                if (countryMP instanceof MultiPolygon)
                {
                    finalBoundaryMap.addCountry(countryCode, (MultiPolygon) countryMP);
                }
                oceanCountryCount++;
            }
        }

        // add all countries from the input boundary map to the new boundary map
        logger.info("Adding country boundaries");
        final JtsMultiPolygonConverter multiPolyConverter = new JtsMultiPolygonConverter();
        for (final String country : boundaryMap.allCountryNames())
        {
            for (final CountryBoundary countryBoundary : boundaryMap.countryBoundary(country))
            {
                final GeometryFactory factory = new GeometryFactory();
                final Set<org.locationtech.jts.geom.Polygon> boundaryPolygons = multiPolyConverter
                        .convert(countryBoundary.getBoundary());
                final org.locationtech.jts.geom.MultiPolygon countryMP = factory.createMultiPolygon(
                        boundaryPolygons.toArray(new Polygon[boundaryPolygons.size()]));
                finalBoundaryMap.addCountry(country, countryMP);
            }
        }
        return finalBoundaryMap;
    }

    public static Geometry geometryForShard(final Rectangle shardBounds,
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
            final Set<org.locationtech.jts.geom.Polygon> boundaryPolygons = multiPolyConverter
                    .convert(boundary.getBoundary());
            final org.locationtech.jts.geom.MultiPolygon countryMP = factory.createMultiPolygon(
                    boundaryPolygons.toArray(new Polygon[boundaryPolygons.size()]));
            final org.locationtech.jts.geom.Geometry geom = SnapRoundOverlayFunctions
                    .difference(shardPolyJts, countryMP, JTS_SNAP_PRECISION);
            shardPolyJts = geom;
        }
        return shardPolyJts;
    }

    public static void main(final String[] args)
    {
        new OceanCountryBoundaryMap().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final File boundaryFile = (File) command.get(BOUNDARY_MAP);
        final CountryBoundaryMap boundaryMap = CountryBoundaryMap.fromPlainText(boundaryFile);
        final File outputFile = (File) command.get(OUTPUT);
        final Iterable<SlippyTile> allTiles = SlippyTile.allTiles(OCEAN_BOUNDARY_ZOOM_LEVEL);
        final CountryBoundaryMap finalBoundaryMap = generateOceanBoundaryMap(boundaryMap, allTiles);
        try
        {
            finalBoundaryMap.writeToFile(outputFile);
            return 0;
        }
        catch (final IOException e)
        {
            logger.error("Error while writing the boundary map to file", e);
            return 1;
        }
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(BOUNDARY_MAP, OUTPUT);
    }
}
