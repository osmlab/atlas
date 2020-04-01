package org.openstreetmap.atlas.geography.atlas.command;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.multi.MultiAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlas;
import org.openstreetmap.atlas.geography.atlas.packed.PackedAtlasCloner;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.runtime.Command.Optionality;
import org.openstreetmap.atlas.utilities.runtime.Command.Switch;
import org.openstreetmap.atlas.utilities.runtime.Command.SwitchList;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * Create a MultiAtlas from the set of input Atlas files, creates a PackedAtlas from the MultiAtlas,
 * and then writes that PackedAtlas to the specified output file. Input files are defined through
 * the -input parameter as directory of Atlas shards. All shards are joined by default. Only select
 * shards can be joined by using the -atlases parameter.
 *
 * @author cstaylor
 * @author bbreithaupt
 */
public class AtlasJoinerSubCommand extends AbstractAtlasSubCommand
{
    private static final Switch<Path> OUTPUT_PARAMETER = new Switch<>("output",
            "The Atlas file to save to", Paths::get, Optionality.REQUIRED);

    private static final Switch<Set<String>> ATLAS_NAMES_PARAMETER = new Switch<>("atlases",
            "A comma separated List of Atlas files to join",
            atlasNames -> Stream.of(atlasNames.split(",")).collect(Collectors.toSet()),
            Optionality.OPTIONAL);

    private final List<Atlas> atlases = new ArrayList<>();

    public AtlasJoinerSubCommand()
    {
        super("join", "joins multiple atlas files into a single packed atlas");
    }

    @Override
    public SwitchList switches()
    {
        return super.switches().with(OUTPUT_PARAMETER, ATLAS_NAMES_PARAMETER);
    }

    @Override
    public void usage(final PrintStream writer)
    {
        writer.printf(AtlasCommandConstants.INPUT_PARAMETER_DESCRIPTION);
        writer.printf(
                "-output=/path/to/atlas/output/to/save : the path to the output atlas file\n");
        writer.printf(
                "-atlases=example.atlas,example2.atlas : comma separated list of atlas file names\n");
    }

    @Override
    protected int finish(final CommandMap command)
    {
        final Atlas atlas = new MultiAtlas(this.atlases);
        try
        {
            final PackedAtlas saveMe = new PackedAtlasCloner().cloneFrom(atlas);
            final Path path = (Path) command.get(OUTPUT_PARAMETER);
            Files.createDirectories(path.getParent());
            saveMe.save(new File(path.toString()));
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
        final Optional atlasNames = command.getOption(ATLAS_NAMES_PARAMETER);
        if (atlasNames.isPresent() && ((Set<String>) atlasNames.get()).contains(atlas.getName()))
        {
            this.atlases.add(atlas);
        }
        else if (!atlasNames.isPresent())
        {
            this.atlases.add(atlas);
        }
    }

}
