package org.openstreetmap.atlas.utilities.command.subcommands;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.MultiPolygon;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.boundary.CountryBoundary;
import org.openstreetmap.atlas.geography.boundary.CountryBoundaryMap;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.time.Time;

/**
 * @author matthieun
 */
public class CountryBoundaryMapPrinterCommand extends AbstractAtlasShellToolsCommand
{
    private static final String BOUNDARY_OPTION_LONG = "country-boundary";

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
        final Optional<CountryBoundaryMap> boundariesOption = loadCountryBoundaryMap();
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
        addManualPageSection("DESCRIPTION", CountryBoundaryMapPrinterCommand.class
                .getResourceAsStream("CountryBoundaryMapPrinterCommandDescriptionSection.txt"));
        addManualPageSection("EXAMPLES", CountryBoundaryMapPrinterCommand.class
                .getResourceAsStream("CountryBoundaryMapPrinterCommandExamplesSection.txt"));
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerOptionWithRequiredArgument(BOUNDARY_OPTION_LONG, 'b', "Path to the boundary file",
                OptionOptionality.REQUIRED, "boundary-file");
        super.registerOptionsAndArguments();
    }

    private File getBoundaryFile()
    {
        return new File(getOptionAndArgumentDelegate().getOptionArgument(BOUNDARY_OPTION_LONG)
                .orElseThrow(AtlasShellToolsException::new), this.getFileSystem());
    }

    private Optional<CountryBoundaryMap> loadCountryBoundaryMap()
    {
        final Optional<CountryBoundaryMap> countryBoundaryMap;
        final File boundaryMapFile = getBoundaryFile();
        if (!boundaryMapFile.exists())
        {
            getCommandOutputDelegate().printlnErrorMessage(
                    "boundary file " + boundaryMapFile.getAbsolutePathString() + " does not exist");
            return Optional.empty();
        }
        if (getOptionAndArgumentDelegate().hasVerboseOption())
        {
            getCommandOutputDelegate().printlnCommandMessage("loading country boundary map...");
        }
        countryBoundaryMap = Optional.of(CountryBoundaryMap.fromPlainText(boundaryMapFile));
        if (getOptionAndArgumentDelegate().hasVerboseOption())
        {
            getCommandOutputDelegate().printlnCommandMessage("loaded boundary map");
        }
        return countryBoundaryMap;
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
