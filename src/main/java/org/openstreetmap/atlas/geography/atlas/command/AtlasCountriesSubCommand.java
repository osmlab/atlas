package org.openstreetmap.atlas.geography.atlas.command;

import java.io.PrintStream;
import java.util.Set;
import java.util.TreeSet;

import org.openstreetmap.atlas.geography.atlas.Atlas;
import org.openstreetmap.atlas.utilities.runtime.CommandMap;

/**
 * Lists all of the countries found in the metadata within the input atlas files
 *
 * @author cstaylor
 */
public class AtlasCountriesSubCommand extends AbstractAtlasSubCommand
{
    private final Set<String> countries;

    public AtlasCountriesSubCommand()
    {
        super("countries", "lists all of the countries found in alphabetical order");
        this.countries = new TreeSet<>();
    }

    @Override
    public void usage(final PrintStream writer)
    {
        writer.printf(AtlasCommandConstants.INPUT_PARAMETER_DESCRIPTION);
    }

    @Override
    protected int finish(final CommandMap command)
    {
        System.out.printf("Countries: %d\n", this.countries.size());
        this.countries.forEach(System.out::println);
        return 0;
    }

    @Override
    protected void handle(final Atlas atlas, final CommandMap command)
    {
        atlas.metaData().getCountry().ifPresent(this.countries::add);
    }
}
