package org.openstreetmap.atlas.geography.atlas.command;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.BigNodeFinder;
import org.openstreetmap.atlas.geography.atlas.items.complex.bignode.RestrictedPath;
import org.openstreetmap.atlas.streaming.resource.File;
import org.openstreetmap.atlas.utilities.runtime.Command.Optionality;
import org.openstreetmap.atlas.utilities.runtime.Command.Switch;
import org.openstreetmap.atlas.utilities.runtime.Command.SwitchList;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * List all valid turnRestriction Id's in the output file.
 *
 * @author ahsieh
 */
public class AtlasListRestrictedPathsCommand extends AbstractAtlasSubCommand
{
    private static final Switch<File> OUTPUT_PARAMETER = new Switch<>("output",
            "The output file to list all turn restriction ids", value -> new File(value),
            Optionality.OPTIONAL);

    private final TreeSet<RestrictedPath> restrictedPaths;

    public AtlasListRestrictedPathsCommand()
    {
        super("restrictedPaths", "lists restrictedPaths");
        this.restrictedPaths = new TreeSet<>((restriction1, restriction2) -> Integer
                .compare(restriction1.hashCode(), restriction2.hashCode()));
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
    }

    @Override
    protected int finish(final CommandMap command)
    {
        @SuppressWarnings("unchecked")
        final Optional<File> possibleFile = (Optional<File>) command.getOption(OUTPUT_PARAMETER);

        if (possibleFile.isPresent())
        {
            try (PrintStream out = new PrintStream(
                    new FileOutputStream(possibleFile.get().getFile(), true)))
            {
                this.restrictedPaths.forEach(value -> out.println(value));
            }
            catch (final IOException oops)
            {
                throw new CoreException("Error writing restrictedPaths to file", oops);
            }
        }
        else
        {
            try (PrintStream out = new PrintStream(System.out))
            {
                this.restrictedPaths.forEach(value -> out.println(value));
            }
        }

        return 0;
    }

    @Override
    protected void handle(final Atlas atlas, final CommandMap command)
    {
        StreamSupport.stream(new BigNodeFinder().find(atlas).spliterator(), false)
                .flatMap(bigNode ->
                {
                    return bigNode.turnRestrictions().stream();
                }).forEach(this.restrictedPaths::add);
    }

}
