package org.openstreetmap.atlas.geography.geojson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Location;
import org.openstreetmap.atlas.geography.PolyLine;
import org.openstreetmap.atlas.geography.geojson.GeoJsonBuilder.LocationIterableProperties;
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

import com.google.gson.JsonElement;

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

        final Iterable<LocationIterableProperties> result = readGeoJsonItems(mode,
                folder.listFilesRecursively(), filePrefix);
        final GeoJsonObject object = new GeoJsonBuilder().create(result);
        final JsonWriter writer = new JsonWriter(output);
        writer.write(object.jsonObject());
        writer.close();
        return 0;
    }

    protected Iterable<LocationIterableProperties> readGeoJsonItems(final Mode mode,
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

    private Iterable<LocationIterableProperties> readGeoJsonItems(final Resource resource)
    {
        final Iterable<PropertiesLocated> read = () -> new GeoJsonReader(resource);
        return Iterables.stream(read).flatMap(propertiesLocated ->
        {
            final Map<String, String> tags = new HashMap<>();
            for (final Map.Entry<String, JsonElement> entry : propertiesLocated.getProperties()
                    .entrySet())
            {
                final String key = entry.getKey();
                final String value = entry.getValue().getAsString();
                tags.put(key, value);
            }
            final List<LocationIterableProperties> result = new ArrayList<>();
            if (propertiesLocated.getItem() instanceof Location)
            {
                final LocationIterableProperties item = new LocationIterableProperties(
                        (Location) propertiesLocated.getItem(), tags);
                result.add(item);
            }
            if (propertiesLocated.getItem() instanceof PolyLine)
            {
                final LocationIterableProperties item = new LocationIterableProperties(
                        (PolyLine) propertiesLocated.getItem(), tags);
                result.add(item);
            }
            return result;
        });
    }

    private Iterable<LocationIterableProperties> readGeoJsonItems(final String line)
    {
        return readGeoJsonItems(new StringResource(line));
    }
}
