package org.openstreetmap.atlas.geography.atlas.command;

import java.io.PrintStream;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.StreamSupport;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.tags.RouteTag;
import org.openstreetmap.atlas.tags.annotations.validation.Validators;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * Command for listing countries that have at least one ferry line
 *
 * @author cstaylor
 */
public class FerrySearchSubCommand extends AbstractAtlasSubCommand
{
    private Set<String> countries;

    public FerrySearchSubCommand()
    {
        super("ferries", "Searching for ferries");
    }

    @Override
    public void usage(final PrintStream writer)
    {
        writer.printf(AtlasCommandConstants.INPUT_PARAMETER_DESCRIPTION);
    }

    @Override
    protected int finish(final CommandMap command)
    {
        System.out.println(this.countries);
        return 0;
    }

    @Override
    protected void handle(final Atlas atlas, final CommandMap command)
    {
        if (StreamSupport.stream(atlas.entities(i -> i.getIdentifier() > 0).spliterator(), true)
                .filter(entity -> Validators.isOfType(entity, RouteTag.class, RouteTag.FERRY))
                .findFirst().isPresent())
        {
            atlas.metaData().getCountry().ifPresent(this.countries::add);
        }
    }

    @Override
    protected void start(final CommandMap command)
    {
        this.countries = new TreeSet<>();
    }
}
