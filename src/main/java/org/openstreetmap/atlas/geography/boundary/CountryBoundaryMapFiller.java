package org.openstreetmap.atlas.geography.boundary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.clipping.Clip;
import org.openstreetmap.atlas.geography.clipping.Clip.ClipType;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonConverter;
import org.openstreetmap.atlas.geography.converters.jts.JtsPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * Take a country boundary map and fill it with a Polygon with all the spaces that do not have any
 * country assigned
 *
 * @author matthieun
 */
public class CountryBoundaryMapFiller extends Command
{
    public static final Switch<File> INPUT = new Switch<>("input", "The input boundary file",
            File::new, Optionality.REQUIRED);
    public static final Switch<File> OUTPUT = new Switch<>("output", "The output boundary file",
            File::new, Optionality.REQUIRED);

    public static void main(final String[] args)
    {
        new CountryBoundaryMapFiller().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final File input = (File) command.get(INPUT);
        final File output = (File) command.get(OUTPUT);
        final CountryBoundaryMap map = CountryBoundaryMap.fromPlainText(input);
        final MultiMap<Polygon, Polygon> outerToInners = new MultiMap<>();
        final MultiMap<String, org.locationtech.jts.geom.Polygon> resultMap = new MultiMap<>();
        for (final String countryName : map.allCountryNames())
        {
            for (final org.locationtech.jts.geom.Polygon boundary : map
                    .countryBoundary(countryName))
            {
                resultMap.add(countryName, boundary);
                final MultiPolygon multiPolygon = new JtsPolygonToMultiPolygonConverter()
                        .convert(boundary);
                for (final Polygon outer : multiPolygon.outers())
                {
                    outerToInners.put(outer, new ArrayList<>());
                    for (final Polygon inner : multiPolygon.innersOf(outer))
                    {
                        outerToInners.add(outer, inner);
                    }
                }
            }
        }
        final MultiPolygon allCountries = new MultiPolygon(outerToInners);
        final Clip clip = MultiPolygon.forPolygon(Rectangle.MAXIMUM).clip(allCountries,
                ClipType.NOT);
        final MultiPolygon result = clip.getClipMultiPolygon();
        final List<org.locationtech.jts.geom.Polygon> results = new ArrayList<>();
        results.addAll(new JtsMultiPolygonConverter().convert(result));
        resultMap.put(ISOCountryTag.COUNTRY_MISSING, results);
        try
        {
            CountryBoundaryMap.fromBoundaryMap(resultMap).writeToFile(output);
        }
        catch (final IOException e)
        {
            throw new CoreException("Could not write new boundaries file to {}", output, e);
        }
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(INPUT, OUTPUT);
    }
}
