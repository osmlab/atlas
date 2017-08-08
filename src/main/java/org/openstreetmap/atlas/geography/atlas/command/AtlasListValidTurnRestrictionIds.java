package org.openstreetmap.atlas.geography.atlas.command;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.TreeSet;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.exception.CoreException;
import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.geography.atlas.items.complex.restriction.ComplexTurnRestrictionFinder;
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
public class AtlasListValidTurnRestrictionIds extends AbstractAtlasSubCommand
{
    private static final Switch<File> OUTPUT_PARAMETER = new Switch<>("output",
            "The output file to list all turn restriction ids", value -> new File(value),
            Optionality.REQUIRED);

    private final TreeSet<Long> turnRestrictions;

    public AtlasListValidTurnRestrictionIds()
    {
        super("turnRestrictions", "lists turn restriction OsmIds");
        this.turnRestrictions = new TreeSet<>();
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
        final File file = (File) command.get(OUTPUT_PARAMETER);

        try (PrintStream out = new PrintStream(new FileOutputStream(file.getFile(), true)))
        {
            this.turnRestrictions.forEach(value -> out.println(value));
        }
        catch (final IOException oops)
        {
            throw new CoreException("Error writing turnRestriction ids to file", oops);
        }

        return 0;
    }

    @Override
    protected void handle(final Atlas atlas, final CommandMap command)
    {
        StreamSupport.stream(new ComplexTurnRestrictionFinder().find(atlas).spliterator(), false)
                .filter(turnRestriction -> turnRestriction.isValid())
                .map(turnRestriction -> turnRestriction.getOsmIdentifier())
                .forEach(this.turnRestrictions::add);
    }

}
