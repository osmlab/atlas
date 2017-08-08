package org.openstreetmap.atlas.geography.atlas.command;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.streaming.resource.OutputStreamWritableResource;
import org.openstreetmap.atlas.streaming.writers.JsonWriter;
import org.openstreetmap.atlas.utilities.runtime.Command.Optionality;
import org.openstreetmap.atlas.utilities.runtime.Command.Switch;
import org.openstreetmap.atlas.utilities.runtime.Command.SwitchList;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * Outputs GeoJSON data to stdout or to an optional file
 *
 * @author cstaylor
 */
public class AtlasGeoJSONSubCommand extends AbstractAtlasSubCommand
{
    private static final Switch<Path> OUTPUT_PARAMETER = new Switch<>("output",
            "The geojson file to save the Atlas as geojson to: otherwise sent to stdout",
            Paths::get, Optionality.OPTIONAL);

    private JsonWriter writer;

    public AtlasGeoJSONSubCommand()
    {
        super("geojson", "outputs the atlas as geojson data");
    }

    @Override
    public SwitchList switches()
    {
        return super.switches().with(OUTPUT_PARAMETER);
    }

    @Override
    public void usage(final PrintStream writer)
    {
        writer.printf(AtlasCommandConstants.INPUT_PARAMETER_DESCRIPTION);
        writer.printf(
                "-output=/path/to/output/geojson/file: the path to the output geojson file\n");
        writer.printf(
                "-combine : merge all of the atlas files into a MultiAtlas before outputting geojson\n");
    }

    @Override
    protected int finish(final CommandMap command)
    {
        this.writer.close();
        return 0;
    }

    @Override
    protected void handle(final Atlas atlas, final CommandMap command)
    {
        getWriter(command).write(atlas.asGeoJson(x -> true).jsonObject());
    }

    @SuppressWarnings("unchecked")
    private JsonWriter getWriter(final CommandMap map)
    {
        if (this.writer == null)
        {
            ((Optional<Path>) map.getOption(OUTPUT_PARAMETER)).ifPresent(path ->
            {
                try
                {
                    Files.createDirectories(path.getParent());
                    this.writer = new JsonWriter(new File(path.toString()));
                }
                catch (final IOException oops)
                {
                    throw new CoreException("Error when creating output stream", oops);
                }
            });

            if (this.writer == null)
            {
                this.writer = new JsonWriter(new OutputStreamWritableResource(System.out));
            }
        }
        return this.writer;
    }

}
