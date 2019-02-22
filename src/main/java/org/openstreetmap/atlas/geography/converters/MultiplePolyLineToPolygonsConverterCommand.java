package org.openstreetmap.atlas.geography.converters;

import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.WritableResource;
import org.openstreetmap.atlas.streaming.writers.SafeBufferedWriter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.conversion.StringConverter;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * This command reads a file with delimited lists of {@link PolyLine}s in WKT format, and applies
 * logic to stitch those {@link PolyLine}s together to form one or more {@link Polygon}s.
 *
 * @author matthieun
 */
public class MultiplePolyLineToPolygonsConverterCommand extends Command
{
    private static final Switch<File> POLYLINES = new Switch<>("polylines",
            "File containing lines of semicolon separated list of WKT polylines", File::new,
            Optionality.REQUIRED);
    private static final Switch<File> POLYGONS = new Switch<>("polygons",
            "Output file that will contain lines of semicolon separated list of reconstructed polygons",
            File::new, Optionality.OPTIONAL);
    private static final Switch<String> DELIMITER = new Switch<>("delimiter",
            "The string delimiter between groups of polylines, and polygons in the output.",
            StringConverter.IDENTITY, Optionality.OPTIONAL, ";");

    private static final WktPolyLineConverter WKT_POLY_LINE_CONVERTER = new WktPolyLineConverter();
    private static final WktMultiPolyLineConverter WKT_MULTI_POLY_LINE_CONVERTER = new WktMultiPolyLineConverter();
    private static final MultiplePolyLineToPolygonsConverter MULTIPLE_POLY_LINE_TO_POLYGONS_CONVERTER = new MultiplePolyLineToPolygonsConverter();

    public static void main(final String[] args)
    {
        new MultiplePolyLineToPolygonsConverterCommand().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final String delimiter = (String) command.get(DELIMITER);
        final File input = (File) command.get(POLYLINES);
        final File output = (File) command.get(POLYGONS);
        translate(input, output, delimiter);
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(POLYLINES, POLYGONS, DELIMITER);
    }

    protected void translate(final Resource input, final WritableResource output,
            final String delimiter)
    {
        final Iterable<List<PolyLine>> inputs = Iterables.stream(input.lines())
                .map(line -> StringList.split(line, delimiter).stream()
                        .filter(wkt -> wkt != null && !wkt.isEmpty()).flatMap(wkt ->
                        {
                            final List<PolyLine> result = new ArrayList<>();
                            if (wkt.toLowerCase().contains("multi"))
                            {
                                WKT_MULTI_POLY_LINE_CONVERTER.backwardConvert(wkt)
                                        .forEach(result::add);
                            }
                            else
                            {
                                result.add(WKT_POLY_LINE_CONVERTER.backwardConvert(wkt));
                            }
                            return result.stream();
                        }).collect(Collectors.toList()));
        try (SafeBufferedWriter writer = writer(output))
        {
            for (final List<PolyLine> inputPolyLines : inputs)
            {
                writer.writeLine(new StringList(Iterables
                        .stream(MULTIPLE_POLY_LINE_TO_POLYGONS_CONVERTER.convert(inputPolyLines))
                        .map(Polygon::toWkt)).join(delimiter));
            }
        }
        catch (final Exception e)
        {
            throw new CoreException("Unable to convert polylines from {}", input.getName(), e);
        }
    }

    private SafeBufferedWriter writer(final WritableResource output)
    {
        return output != null ? output.writer()
                : new SafeBufferedWriter(new OutputStreamWriter(System.out));
    }
}
