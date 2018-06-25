package org.openstreetmap.atlas.geography.boundary;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

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
    private static final StringConverter<Optional<File>> OUTPUT_GETTER = value ->
    {
        if (!value.isEmpty())
        {
            return Optional.of(new File(value));
        }
        return Optional.empty();
    };

    public static final Switch<String> COUNTRIES = new Switch<>("countries",
            "The countries to extract as geojson (csv list)", StringConverter.IDENTITY,
            Optionality.REQUIRED);
    public static final Switch<File> INPUT = new Switch<>("input", "The input boundaries file",
            File::new, Optionality.REQUIRED);
    public static final Switch<Optional<File>> OUTPUT_GEOJSON = new Switch<>("geojson",
            "The output folder", OUTPUT_GETTER, Optionality.OPTIONAL, "");
    public static final Switch<Optional<File>> OUTPUT_WKT = new Switch<>("wkt",
            "The output folder for WKT", OUTPUT_GETTER, Optionality.OPTIONAL, "");

    private static final JtsMultiPolygonToMultiPolygonConverter JTS_MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER = new JtsMultiPolygonToMultiPolygonConverter();

    public static void main(final String[] args)
    {
        new CountryBoundaryMapPrinter().run(args);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected int onRun(final CommandMap command)
    {
        final File input = (File) command.get(INPUT);
        final Optional<File> geojson = (Optional<File>) command.get(OUTPUT_GEOJSON);
        geojson.ifPresent(File::mkdirs);
        final Optional<File> wkt = (Optional<File>) command.get(OUTPUT_WKT);
        wkt.ifPresent(File::mkdirs);
        if (!geojson.isPresent() && !wkt.isPresent())
        {
            return 0;
        }
        final String countries = (String) command.get(COUNTRIES);
        StringList countryList = new StringList();
        final CountryBoundaryMap map = CountryBoundaryMap.fromPlainText(input);
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
                String name = country;
                if (i > 0)
                {
                    name += "_" + i;
                }
                final MultiPolygon multiPolygon = boundaries.get(i).getBoundary();
                if (wkt.isPresent())
                {
                    save(wkt.get().child(country + FileSuffix.WKT),
                            JTS_MULTI_POLYGON_TO_MULTI_POLYGON_CONVERTER
                                    .backwardConvert(multiPolygon).toText());
                }
                if (geojson.isPresent())
                {
                    final File countryFile = geojson.get()
                            .child(name + "_boundary" + FileSuffix.GEO_JSON);
                    multiPolygon.asGeoJsonFeatureCollection().save(countryFile);
                }
            }
        }
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(INPUT, OUTPUT_GEOJSON, COUNTRIES, OUTPUT_WKT);
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
