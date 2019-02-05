package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.locale.IsoCountry;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.terminal.TTYAttribute;

/**
 * @author lcram
 */
public class IsoCountryCodeCommand extends AbstractAtlasShellToolsCommand
{
    private static final String QUERY_HINT = "query";

    private final OptionAndArgumentDelegate optionAndArgumentDelegate;
    private final CommandOutputDelegate outputDelegate;

    public static void main(final String[] args)
    {
        new IsoCountryCodeCommand().runSubcommandAndExit(args);
    }

    public IsoCountryCodeCommand()
    {
        this.optionAndArgumentDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    public int execute()
    {
        final List<String> queries = this.optionAndArgumentDelegate.getVariadicArgument(QUERY_HINT);

        for (final String query : queries)
        {
            final Optional<IsoCountry> forCode = IsoCountry.forCountryCode(query);
            final Optional<IsoCountry> forDisplay = IsoCountry.forDisplayCountryClosestMatch(query);

            // check for exact country code first
            if (forCode.isPresent())
            {
                this.outputDelegate.printlnStdout("ISO code \'" + query + "\' matched: ");
                this.outputDelegate.printlnStdout(
                        forCode.get().toString() + ", " + forCode.get().getIso3CountryCode(),
                        TTYAttribute.BOLD);
            }
            else if (forDisplay.isPresent())
            {
                this.outputDelegate
                        .printlnStdout("Display country name \'" + query + "\' matched: ");
                this.outputDelegate.printlnStdout(
                        forDisplay.get().toString() + ", " + forDisplay.get().getIso3CountryCode(),
                        TTYAttribute.BOLD);
            }
            else
            {
                this.outputDelegate.printlnErrorMessage("unmatchable query " + query);
            }
        }

        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "iso-country-code";
    }

    @Override
    public String getSimpleDescription()
    {
        return "convert ISO country codes to countries and back again";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION", IsoCountryCodeCommand.class
                .getResourceAsStream("IsoCountryCodeCommandDescriptionSection.txt"));
        addManualPageSection("EXAMPLES", IsoCountryCodeCommand.class
                .getResourceAsStream("IsoCountryCodeCommandExamplesSection.txt"));
    }

    @Override
    public void registerOptionsAndArguments()
    {
        registerArgument(QUERY_HINT, ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED);
        super.registerOptionsAndArguments();
    }
}
