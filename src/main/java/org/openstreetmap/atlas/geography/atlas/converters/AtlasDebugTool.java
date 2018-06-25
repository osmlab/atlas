package org.openstreetmap.atlas.geography.atlas.converters;

import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.AtlasResourceLoader;
import org.openstreetmap.atlas.geography.atlas.pbf.AtlasLoadingOption;
import org.openstreetmap.atlas.geography.atlas.pbf.OsmPbfLoader;
import org.openstreetmap.atlas.geography.atlas.routing.AStarRouter;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.boundary.converters.CountryListTwoWayStringConverter;
import org.openstreetmap.atlas.geography.converters.MultiPolygonStringConverter;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.scalars.Distance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Convert an Atlas to GeoJson
 *
 * @author matthieun
 */
public class AtlasDebugTool extends Command
{
    private static final Logger logger = LoggerFactory.getLogger(AtlasDebugTool.class);

    private static final Switch<File> PBF = new Switch<>("pbf", "The protobuf file",
            path -> new File(path), Optionality.OPTIONAL);
    private static final Switch<File> ATLAS = new Switch<>("atlas", "The atlas file",
            path -> new File(path), Optionality.REQUIRED);
    private static final Switch<File> GEOJSON = new Switch<>("geojson", "The geojson file",
            path -> new File(path), Optionality.OPTIONAL);
    private static final Switch<File> TEXT = new Switch<>("text", "The text file",
            path -> new File(path), Optionality.OPTIONAL);
    private static final Switch<java.io.File> BOUNDARY = new Switch<>("boundary",
            "The country boundary file", path -> new java.io.File(path), Optionality.OPTIONAL);
    private static final Switch<String> COUNTRY = new Switch<>("country",
            "The country name which will be loaded", name -> name, Optionality.OPTIONAL);
    private static final Switch<List<Location>> ROUTE = new Switch<>("route",
            "The lat,lon:lat,lon representing a start and end points to get a route", value ->
            {
                final StringList split = StringList.split(value, ":");
                final List<Location> result = new ArrayList<>();
                result.add(Location.forString(split.get(0)));
                result.add(Location.forString(split.get(1)));
                return result;
            }, Optionality.OPTIONAL);
    private static final Switch<Rectangle> BOUND = new Switch<>("bound",
            "Data will be loaded only in this bounding box", value -> Rectangle.forString(value));
    private static final Switch<MultiPolygon> MULTIPOLYGON = new Switch<>("multipolygon",
            "Data will be loaded only in this multipolygon",
            value -> new MultiPolygonStringConverter().convert(value));

    public static void main(final String[] args)
    {
        new AtlasDebugTool().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final File pbf = (File) command.get(PBF);
        final File atlasFile = (File) command.get(ATLAS);
        final File geojson = (File) command.get(GEOJSON);
        final File text = (File) command.get(TEXT);
        final java.io.File boundaryFile = (java.io.File) command.get(BOUNDARY);
        final String country = (String) command.get(COUNTRY);
        final Rectangle bound = (Rectangle) command.get(BOUND);
        final MultiPolygon inputMultipolygon = (MultiPolygon) command.get(MULTIPOLYGON);
        @SuppressWarnings("unchecked")
        final List<Location> startEndRoute = (List<Location>) command.get(ROUTE);

        final Atlas atlas;
        if (pbf != null && pbf.exists())
        {
            final AtlasLoadingOption option;
            MultiPolygon multiPolygon = MultiPolygon.forPolygon(Rectangle.MAXIMUM);
            if (boundaryFile != null)
            {
                final CountryBoundaryMap boundaryMap = CountryBoundaryMap
                        .fromShapeFile(boundaryFile);
                option = AtlasLoadingOption.createOptionWithAllEnabled(boundaryMap);
                if (country != null)
                {
                    if (new CountryListTwoWayStringConverter().convert(country).size() == 1)
                    {
                        multiPolygon = boundaryMap.countryBoundary(country).get(0).getBoundary();
                    }
                    option.setAdditionalCountryCodes(country);
                }
            }
            else
            {
                option = AtlasLoadingOption.createOptionWithNoSlicing();
            }
            if (bound != null)
            {
                multiPolygon = MultiPolygon.forPolygon(bound);
            }
            if (inputMultipolygon != null)
            {
                multiPolygon = inputMultipolygon;
            }
            atlas = new OsmPbfLoader(pbf, multiPolygon, option).read();
            atlas.save(atlasFile);
        }
        else if (atlasFile != null && atlasFile.exists())
        {
            atlas = new AtlasResourceLoader().load(atlasFile);
        }
        else
        {
            logger.error("Must have at least one source, -pbf or -atlas");
            atlas = null;
            System.exit(1);
        }
        logger.info("Loaded {}", atlas.summary());
        if (geojson != null)
        {
            atlas.saveAsGeoJson(geojson);
        }
        if (text != null)
        {
            atlas.saveAsText(text);
        }
        if (startEndRoute != null)
        {
            logger.info("Route between {} and {} = {}", startEndRoute.get(0), startEndRoute.get(1),
                    AStarRouter.dijkstra(atlas, Distance.TEN_MILES).route(startEndRoute.get(0),
                            startEndRoute.get(1)));
        }
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(ATLAS, GEOJSON, TEXT, PBF, BOUNDARY, COUNTRY, ROUTE, BOUND,
                MULTIPOLYGON);
    }

}
