package org.openstreetmap.atlas.geography.boundary;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.conversion.StringConverter;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;
import org.openstreetmap.atlas.utilities.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author matthieun
 * @author tony
 * @deprecated Use CountryBoundaryMapPrinterCommand within the atlas-shell-tools utility instead:
 *             <p>
 *             {@code atlas boundary-itemizer --boundary=/path/to/boundary.txt.gz}
 */
@Deprecated(since = "6.3.1")
public class CountryBoundaryMapPrinter extends Command
{
    private static final Logger logger = LoggerFactory.getLogger(CountryBoundaryMapPrinter.class);

    private static final Switch<String> COUNTRIES = new Switch<>("countries",
            "The countries to extract as geojson (csv list)", StringConverter.IDENTITY,
            Optionality.OPTIONAL);
    private static final Switch<File> INPUT = new Switch<>("input", "The input boundaries file",
            File::new, Optionality.REQUIRED);

    public static void main(final String[] args)
    {
        new CountryBoundaryMapPrinter().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final File input = (File) command.get(INPUT);
        final String countries = (String) command.get(COUNTRIES);
        print(input, countries);
        return 0;
    }

    protected void print(final File input)
    {
        print(input, null);
    }

    protected void print(final File input, final String countries)
    {
        String inputName = input.getName();
        inputName = inputName.substring(0, inputName.indexOf('.'));
        final File geojson = input.parent().child(inputName + "-geojson");
        geojson.mkdirs();
        final File wkt = input.parent().child(inputName + "-wkt");
        wkt.mkdirs();
        final Set<String> countrySet;
        final CountryBoundaryMap map = CountryBoundaryMap.fromPlainText(input);
        if (countries == null)
        {
            countrySet = map.countryCodesOverlappingWith(Rectangle.MAXIMUM).stream()
                    .collect(Collectors.toSet());
        }
        else
        {
            countrySet = StringList.split(countries, ",").stream().collect(Collectors.toSet());
        }
        for (final String country : countrySet)
        {
            final Time start = Time.now();
            final List<CountryBoundary> boundaries = map.countryBoundary(country);
            for (int i = 0; i < boundaries.size(); i++)
            {
                String name = country;
                if (i > 0)
                {
                    name += "_" + i;
                }
                final MultiPolygon multiPolygon = boundaries.get(i).getBoundary();
                save(wkt.child(country + FileSuffix.WKT), multiPolygon.toWkt());
                final File countryFile = geojson.child(name + FileSuffix.GEO_JSON);
                multiPolygon.asGeoJsonFeatureCollection().save(countryFile);
            }
            logger.info("Saved {} in {}", country, start.elapsedSince());
        }
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(INPUT, COUNTRIES);
    }

    private void save(final WritableResource output, final String string)
    {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(output.write(), StandardCharsets.UTF_8)))
        {
            writer.write(string);
        }
        catch (final Exception e)
        {
            throw new CoreException("Could not save file {}", output.getName(), e);
        }
    }
}
