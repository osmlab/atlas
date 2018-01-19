package org.openstreetmap.atlas.geography.converters;

import java.util.List;
import java.util.stream.Collectors;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.Polygon;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.writers.SafeBufferedWriter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.collections.StringList;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * @author matthieun
 */
public class MultiplePolyLineToPolygonsConverterCommand extends Command
{
    private static final Switch<File> POLYLINES = new Switch<>("polylines",
            "File containing lines of semicolon separated list of WKT polylines", File::new,
            Optionality.REQUIRED);
    private static final Switch<File> POLYGONS = new Switch<>("polygons",
            "Output file that will contain lines of semicolon separated list of reconstructed polygons",
            File::new, Optionality.REQUIRED);

    public static void main(final String[] args)
    {
        new MultiplePolyLineToPolygonsConverterCommand().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final File inputFile = (File) command.get(POLYLINES);
        final Iterable<List<PolyLine>> inputs = Iterables.stream(inputFile.lines())
                .map(line -> StringList.split(line, ";").stream()
                        .map(wkt -> new WktPolyLineConverter().backwardConvert(wkt))
                        .collect(Collectors.toList()));
        try (SafeBufferedWriter writer = ((File) command.get(POLYGONS)).writer())
        {
            for (final List<PolyLine> input : inputs)
            {
                writer.writeLine(new StringList(
                        Iterables.stream(new MultiplePolyLineToPolygonsConverter().convert(input))
                                .map(Polygon::toWkt)).join(";"));
            }
        }
        catch (final Exception e)
        {
            throw new CoreException("Unable to convert polylines from {}", inputFile.getName(), e);
        }
        return 0;
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(POLYLINES, POLYGONS);
    }
}
