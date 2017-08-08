package org.openstreetmap.atlas.geography.atlas.command;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.Rectangle;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasCloner;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.runtime.Command.Flag;
import org.openstreetmap.atlas.utilities.runtime.Command.Optionality;
import org.openstreetmap.atlas.utilities.runtime.Command.Switch;
import org.openstreetmap.atlas.utilities.runtime.Command.SwitchList;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * Create a {@link MultiAtlas} from the set of input atlas files, creates a {@link PackedAtlas} from
 * the {@link MultiAtlas}, and then writes that {@link PackedAtlas} to the specified output file.
 *
 * @author mnahoum
 */
public class SubAtlasSubCommand extends AbstractAtlasSubCommand
{
    private static final Switch<Path> OUTPUT = new Switch<>("output",
            "The file to save the Atlas to", Paths::get, Optionality.OPTIONAL);
    private static final Switch<Rectangle> SUB = new Switch<>("sub",
            "The rectangle to soft-cut this Atlas to", Rectangle::forString, Optionality.REQUIRED);
    private static final Switch<Path> GEOJSON = new Switch<>("geojson",
            "The geojson file to save this sub atlas to", Paths::get, Optionality.OPTIONAL);
    private static final Flag SAVE_MEMORY = new Flag("saveMemory",
            "Reduce momery cost if this flag is existed");

    private final List<Atlas> atlases = new ArrayList<>();

    public SubAtlasSubCommand()
    {
        super("sub", "Subs an Atlas into another smaller Atlas");
    }

    @Override
    public SwitchList switches()
    {
        return super.switches().with(OUTPUT, SUB, GEOJSON, SAVE_MEMORY);
    }

    @Override
    public void usage(final PrintStream writer)
    {
        writer.printf(AtlasCommandConstants.INPUT_PARAMETER_DESCRIPTION);
        writer.printf(
                "-output=/path/to/atlas/output/to/save : the path to the output atlas file\n");
        writer.printf(
                "-geojson=/path/to/atlas/geojson/to/save : the path to the output geojson file (optional)\n");
        writer.printf("-sub=minLat,minLon:maxLat,maxLon : the rectangle to sub the Atlas with\n");
        writer.printf("-saveMemory : a flag to save memory\n");
    }

    @Override
    protected int finish(final CommandMap command)
    {
        final Atlas atlas = new MultiAtlas(this.atlases);
        final Rectangle rectangle = (Rectangle) command.get(SUB);
        try
        {
            final Atlas saveMe = new PackedAtlasCloner().cloneFrom(atlas).subAtlas(rectangle)
                    .orElseThrow(
                            () -> new CoreException("There are no features in the sub rectangle."));
            final Path path = (Path) command.get(OUTPUT);
            Files.createDirectories(path.getParent());
            saveMe.save(new File(path.toString()));
            final Path path2 = (Path) command.get(GEOJSON);
            if (path2 != null)
            {
                Files.createDirectories(path2.getParent());
                saveMe.saveAsGeoJson(new File(path2.toString()));
            }
            return 0;
        }
        catch (final IOException oops)
        {
            throw new CoreException("Error when saving packed atlas", oops);
        }
    }

    @Override
    protected void handle(final Atlas atlas, final CommandMap command)
    {
        final boolean saveMemory = (boolean) command.get(SAVE_MEMORY);
        final Rectangle rectangle = (Rectangle) command.get(SUB);
        if (!saveMemory || rectangle.overlaps(atlas.bounds()))
        {
            this.atlases.add(atlas);
        }
    }
}
