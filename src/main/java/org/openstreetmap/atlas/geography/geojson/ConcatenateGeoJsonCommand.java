package org.openstreetmap.atlas.geography.geojson;

import java.util.Collections;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.streaming.readers.GeoJsonReader;
import org.openstreetmap.atlas.streaming.readers.json.serializers.PropertiesLocated;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.FileSuffix;
import org.openstreetmap.atlas.streaming.resource.Resource;
import org.openstreetmap.atlas.streaming.resource.StringResource;
import org.openstreetmap.atlas.streaming.writers.JsonWriter;
import org.openstreetmap.atlas.utilities.collections.Iterables;
import org.openstreetmap.atlas.utilities.conversion.StringConverter;
import org.openstreetmap.atlas.utilities.runtime.Command;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * Utility class to concatenate GeoJson objects.
 *
 * @author matthieun
 * @author sid
 */
public class ConcatenateGeoJsonCommand extends Command
{
    /**
     * Mode specifies the format of the input geoJSON objects. Each file can be a GeoJSON object or
     * file can contain multiple geoJSON objects (one per line)
     *
     * @author Sid
     */
    public enum Mode
    {
        // Each file is a geoJSON object
        FILE,
        // Each line of the file is a geoJSON object
        LINE;
    }

    public static final Switch<File> PATH = new Switch<>("path",
            "The folder containing the geojson files to concatenate", File::new,
            Optionality.REQUIRED);
    public static final Switch<File> OUTPUT = new Switch<>("output",
            "The file to write the concatenated geojson to", File::new, Optionality.REQUIRED);
    public static final Switch<Mode> MODE = new Switch<>("mode",
            "The mode of the input geoJSON objects (FILE or LINE)", value -> Mode.valueOf(value),
            Optionality.REQUIRED);
    public static final Switch<String> FILE_PREFIX = new Switch<>("filePrefix",
            "The prefix of the input geoJSON file in LINE mode", StringConverter.IDENTITY,
            Optionality.OPTIONAL, "part-");

    public static void main(final String[] args)
    {
        new ConcatenateGeoJsonCommand().run(args);
    }

    @Override
    protected int onRun(final CommandMap command)
    {
        final File folder = (File) command.get(PATH);
        final File output = (File) command.get(OUTPUT);
        final Mode mode = (Mode) command.get(MODE);
        final String filePrefix = (String) command.get(FILE_PREFIX);

        // processing the files in sorted order makes testing easier
        final List<File> files = folder.listFilesRecursively();
        Collections.sort(files);
        final Iterable<PropertiesLocated> jsonItems = readGeoJsonItems(mode, files, filePrefix);
        final GeoJsonObject result = new GeoJsonBuilder()
                .createFeatureCollectionFromPropertiesLocated(jsonItems);
        final JsonWriter writer = new JsonWriter(output);
        writer.write(result.jsonObject());
        writer.close();
        return 0;
    }

    protected Iterable<PropertiesLocated> readGeoJsonItems(final Mode mode,
            final Iterable<File> files, final String filePrefix)
    {
        switch (mode)
        {
            case FILE:
                return Iterables.stream(files)
                        .filter(file -> file.getName().endsWith(FileSuffix.GEO_JSON.toString()))
                        .flatMap(this::readGeoJsonItems);
            case LINE:
                return Iterables.stream(files).filter(file -> file.getName().startsWith(filePrefix))
                        .flatMap(file -> file.lines()).map(line -> line.trim())
                        .filter(line -> !line.isEmpty()).flatMap(this::readGeoJsonItems);
            default:
                throw new CoreException("Invalid Mode");
        }
    }

    @Override
    protected SwitchList switches()
    {
        return new SwitchList().with(PATH, OUTPUT, MODE, FILE_PREFIX);
    }

    private Iterable<PropertiesLocated> readGeoJsonItems(final Resource resource)
    {
        final Iterable<PropertiesLocated> iterableOfPropertiesLocated = () -> new GeoJsonReader(
                resource);
        return iterableOfPropertiesLocated;
    }

    private Iterable<PropertiesLocated> readGeoJsonItems(final String line)
    {
        return readGeoJsonItems(new StringResource(line));
    }
}
