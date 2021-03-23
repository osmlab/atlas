package org.openstreetmap.atlas.utilities.command.subcommands;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.geography.converters.jts.JtsMultiPolygonToMultiPolygonConverter;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.subcommands.templates.CountryBoundaryMapTemplate;
import org.openstreetmap.atlas.utilities.time.Time;

/**
 * @author matthieun
 */
public class CountryBoundaryMapPrinterCommand extends AbstractAtlasShellToolsCommand
{
    public static final String BOUNDARY_OPTION_LONG = "country-boundary";

    public static void main(final String[] args)
    {
        new CountryBoundaryMapPrinterCommand().runSubcommandAndExit(args);
    }

    @Override
    public int execute()
    {
        final File boundaryFile = getBoundaryFile();
        String boundaryFileName = boundaryFile.getName();
        boundaryFileName = boundaryFileName.substring(0, boundaryFileName.indexOf('.'));
        final Optional<CountryBoundaryMap> boundariesOption = CountryBoundaryMapTemplate
                .getCountryBoundaryMap(this);
        final File outputFolder = boundaryFile.parent();
        final File geojson = outputFolder.child(boundaryFileName + "-geojson");
        geojson.mkdirs();
        final File wkt = outputFolder.child(boundaryFileName + "-wkt");
        wkt.mkdirs();
        if (boundariesOption.isEmpty())
        {
            getCommandOutputDelegate().printlnErrorMessage("Could not read boundary file!");
            return 1;
        }
        final CountryBoundaryMap map = boundariesOption.get();
        final Set<String> countrySet = map.countryCodesOverlappingWith(Rectangle.MAXIMUM).stream()
                .collect(Collectors.toSet());
        final GeometryFactory geometryFactory = new GeometryFactory();
        for (final String country : countrySet)
        {
            final Time start = Time.now();
            final Polygon[] polygons = map.countryBoundary(country).toArray(new Polygon[0]);
            final MultiPolygon multiPolygon = new MultiPolygon(polygons, geometryFactory);
            saveGeometry(wkt, geojson, country, multiPolygon);
            if (getOptionAndArgumentDelegate().hasVerboseOption())
            {
                getCommandOutputDelegate()
                        .printlnCommandMessage("Saved " + country + " in " + start.elapsedSince());
            }
        }
        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "boundary-itemizer";
    }

    @Override
    public String getSimpleDescription()
    {
        return "Read a CountryBoundaryMap file and print each country to geojson and wkt";
    }

    @Override
    public void registerManualPageSections()
    {
        registerManualPageSectionsFromTemplate(new CountryBoundaryMapTemplate());
        addManualPageSection("DESCRIPTION", CountryBoundaryMapPrinterCommand.class
                .getResourceAsStream("CountryBoundaryMapPrinterCommandDescriptionSection.txt"));
        addManualPageSection("EXAMPLES", CountryBoundaryMapPrinterCommand.class
                .getResourceAsStream("CountryBoundaryMapPrinterCommandExamplesSection.txt"));
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOptionsAndArgumentsFromTemplate(new CountryBoundaryMapTemplate());
        super.registerOptionsAndArguments();
    }

    private File getBoundaryFile()
    {
        return new File(getOptionAndArgumentDelegate()
                .getOptionArgument(CountryBoundaryMapTemplate.COUNTRY_BOUNDARY_OPTION_LONG)
                .orElseThrow(AtlasShellToolsException::new), this.getFileSystem());
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

    private void saveGeometry(final File wkt, final File geojson, final String name,
            final MultiPolygon multiPolygon)
    {
        save(wkt.child(name + FileSuffix.WKT), multiPolygon.toText());
        final File countryFile = geojson.child(name + FileSuffix.GEO_JSON);
        new JtsMultiPolygonToMultiPolygonConverter().convert(multiPolygon)
                .saveAsGeoJson(countryFile);
    }
}
