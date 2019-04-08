package org.openstreetmap.atlas.geography.boundary;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.WKTWriter;
import org.locationtech.jtslab.SnapRoundOverlayFunctions;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonConverter;
import org.openstreetmap.atlas.geography.sharding.SlippyTile;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.LineWriter;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * Command that takes a boundary map and adds ocean boundaries.
 *
 * @author james-gage
 */
public class OceanCountryBoundaryMap extends Command
{
    private static final Switch<File> BOUNDARY_MAP = new Switch<>("boundaryMap",
            "The country boundary map", File::new, Optionality.REQUIRED);
    private static final Switch<File> OUTPUT = new Switch<>("output",
            "The ocean country boundary file to be output", File::new, Optionality.REQUIRED);
    // start ocean names (e.g. O10) at first double digit number
    private static final int initialOceanIndex = 10;

    public static void main(final String[] args)
    {
        new OceanCountryBoundaryMap().run(args);
    }

    public static org.locationtech.jts.geom.Polygon[] toArray(
            final Set<org.locationtech.jts.geom.Polygon> polygons)
    {
        final org.locationtech.jts.geom.Polygon[] polyArray = new org.locationtech.jts.geom.Polygon[polygons
                .size()];
        final Iterator<org.locationtech.jts.geom.Polygon> polyIterator = polygons.iterator();
        for (int i = 0; i < polygons.size(); i++)
        {
            polyArray[i] = polyIterator.next();
        }
        return polyArray;
    }

    public static void writeBoundariesForShard(final Rectangle shardBounds,
            final CountryBoundaryMap boundaryMap, final LineWriter writer, final String countryCode)
    {
        final WKTWriter wktWriter = new WKTWriter();

        final JtsMultiPolygonConverter multiPolyConverter = new JtsMultiPolygonConverter();
        final JtsPolygonConverter polyConverter = new JtsPolygonConverter();
        final GeometryFactory factory = new GeometryFactory();

        final List<CountryBoundary> boundaries = boundaryMap.boundaries(shardBounds);

        // jts version of the initial shard bounds
        org.locationtech.jts.geom.Geometry shardPolyJts = polyConverter.convert(shardBounds);

        for (final CountryBoundary boundary : boundaries)
        {
            final Set<org.locationtech.jts.geom.Polygon> boundaryPolygons = multiPolyConverter
                    .convert(boundary.getBoundary());
            final org.locationtech.jts.geom.MultiPolygon countryMP = factory
                    .createMultiPolygon(toArray(boundaryPolygons));
            // compute the clip
            final org.locationtech.jts.geom.Geometry geom = SnapRoundOverlayFunctions
                    .difference(shardPolyJts, countryMP, .000000000000001);
            shardPolyJts = geom;
        }
        if (!shardPolyJts.isEmpty())
        {
            writer.writeLine(countryCode + "||" + wktWriter.write(shardPolyJts) + "#");
        }
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final File boundaryFile = (File) command.get(BOUNDARY_MAP);
        final CountryBoundaryMap boundaryMap = CountryBoundaryMap.fromPlainText(boundaryFile);
        final File outputFile = (File) command.get(OUTPUT);
        final LineWriter writer = new LineWriter(outputFile);
        final Iterable<SlippyTile> allTiles = SlippyTile.allTiles(3);
        int oceanCountryCount = initialOceanIndex;
        for (final SlippyTile tile : allTiles)
        {
            final String countryCode = "O" + oceanCountryCount;
            writeBoundariesForShard(tile.bounds(), boundaryMap, writer, countryCode);
            oceanCountryCount++;
        }
        // after writing all ocean countries, fill in the rest of the bounday map
        boundaryFile.lines().forEach(line -> writer.writeLine(line));
        try
        {
            writer.close();
            return 0;
        }
        catch (final IOException e)
        {
            e.printStackTrace();
            return 1;
        }
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(BOUNDARY_MAP, OUTPUT);
    }
}
