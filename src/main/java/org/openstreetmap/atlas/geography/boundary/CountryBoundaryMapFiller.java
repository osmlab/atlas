package org.openstreetmap.atlas.geography.boundary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.clipping.Clip;
import org.openstreetmap.atlas.geography.clipping.Clip.ClipType;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.tags.ISOCountryTag;
import org.openstreetmap.atlas.utilities.maps.MultiMap;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.tuples.Tuple;

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
        final Map<String, MultiPolygon> resultMap = new HashMap<>();
        map.allCountryNames().stream()
                .map(country -> new Tuple<>(country,
                        map.countryBoundary(country).iterator().next().getBoundary()))
                .forEach(tuple ->
                {
                    final String countryName = tuple.getFirst();
                    final MultiPolygon multiPolygon = tuple.getSecond();
                    resultMap.put(countryName, multiPolygon);
                    for (final Polygon outer : multiPolygon.outers())
                    {
                        outerToInners.put(outer, new ArrayList<>());
                        for (final Polygon inner : multiPolygon.innersOf(outer))
                        {
                            outerToInners.add(outer, inner);
                        }
                    }
                });
        final MultiPolygon allCountries = new MultiPolygon(outerToInners);
        final Clip clip = MultiPolygon.forPolygon(Rectangle.MAXIMUM).clip(allCountries,
                ClipType.NOT);
        final MultiPolygon result = clip.getClipMultiPolygon();
        resultMap.put(ISOCountryTag.COUNTRY_MISSING, result);
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
