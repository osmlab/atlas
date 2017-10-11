package org.openstreetmap.atlas.geography.boundary;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.conversion.StringConverter;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * @author matthieun
 * @author tony
 */
public class CountryBoundaryMapPrinter extends Command
{
    public static final Switch<String> COUNTRIES = new Switch<>("countries",
            "The countries to extract as geojson (csv list)", StringConverter.IDENTITY,
            Optionality.REQUIRED);
    public static final Switch<File> INPUT = new Switch<>("input", "The input boundaries file",
            value -> new File(value), Optionality.REQUIRED);
    public static final Switch<File> OUTPUT = new Switch<>("output", "The output folder",
            value -> new File(value), Optionality.REQUIRED);
    public static final Flag OUTPUT_WKT = new Flag("wkt",
            "The optional flag to output in wkt format", Optionality.OPTIONAL);

    private static final JtsMultiPolygonToMultiPolygonConverter JTS_MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER = new JtsMultiPolygonToMultiPolygonConverter();

    public static void main(final String[] args)
    {
        new CountryBoundaryMapPrinter().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final File input = (File) command.get(INPUT);
        final File output = (File) command.get(OUTPUT);
        output.mkdirs();
        final boolean outputWKT = (Boolean) command.get(OUTPUT_WKT);
        final String countries = (String) command.get(COUNTRIES);
        StringList countryList = new StringList();
        final CountryBoundaryMap map = new CountryBoundaryMap(input);
        if ("*".equals(countries))
        {
            countryList = map.countryCodesOverlappingWith(Rectangle.MAXIMUM);
        }
        else
        {
            countryList = StringList.split(countries, ",");
        }
        for (final String country : countryList)
        {
            final List<CountryBoundary> boundaries = map.countryBoundary(country);
            for (int i = 0; i < boundaries.size(); i++)
            {
                final MultiPolygon multiPolygon = boundaries.get(i).getBoundary();
                if (outputWKT)
                {
                    save(output.child(country + FileSuffix.WKT),
                            JTS_MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER
                                    .backwardConvert(multiPolygon).toText());
                }
                else
                {
                    String name = country;
                    if (i > 0)
                    {
                        name += "_" + i;
                    }
                    final File countryFile = output.child(name + "_boundary" + FileSuffix.GEO_JSON);
                    multiPolygon.asGeoJson().save(countryFile);
                }
            }
        }
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(INPUT, OUTPUT, COUNTRIES, OUTPUT_WKT);
    }

    private void save(final WritableResource output, final String string)
    {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(output.write(), StandardCharsets.UTF_8));)
        {
            writer.write(string);
        }
        catch (final Exception e)
        {
            throw new CoreException("Could not save wkt file {}", output.getName(), e);
        }
    }
}
