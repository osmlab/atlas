package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.locale.IsoCountry;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentDelegate;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.parsing.OptionOptionality;
import org.openstreetmap.atlas.utilities.command.terminal.TTYAttribute;

/**
 * @author lcram
 */
public class IsoCountryCodeCommand extends AbstractAtlasShellToolsCommand
{
    private static final int DEFAULT_MATCH_NUMBER = 3;

    private static final char NUMBER_OPTION_SHORT = 'n';
    private static final String NUMBER_OPTION_LONG = "number";
    private static final String NUMBER_OPTION_DESCRIPTION = "The number of matches to display. Defaults to "
            + DEFAULT_MATCH_NUMBER + ".";
    private static final String NUMBER_OPTION_HINT = "n";

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

        for (int i = 0; i < queries.size(); i++)
        {
            final String query = queries.get(i);
            final Optional<IsoCountry> forCode = IsoCountry.forCountryCode(query);
            final Optional<IsoCountry> forDisplayExact = IsoCountry
                    .forDisplayCountryIgnoreCase(query);
            final List<IsoCountry> forDisplayTopMatches = IsoCountry
                    .forDisplayCountryTopMatches(this.optionAndArgumentDelegate
                            .getOptionArgument(NUMBER_OPTION_LONG, Integer::parseInt)
                            .orElse(DEFAULT_MATCH_NUMBER), query);

            // check for exact country code first
            if (forCode.isPresent())
            {
                this.outputDelegate.printlnStdout("ISO code \'" + query + "\' matched: ",
                        TTYAttribute.BOLD);
                this.outputDelegate.printlnStdout(
                        forCode.get().toString() + ", " + forCode.get().getIso3CountryCode(),
                        TTYAttribute.GREEN);
            }
            else if (forDisplayExact.isPresent())
            {
                this.outputDelegate.printlnStdout(
                        "Display country name \'" + query + "\' matched: ", TTYAttribute.BOLD);
                this.outputDelegate.printlnStdout(forDisplayExact.get().toString() + ", "
                        + forDisplayExact.get().getIso3CountryCode(), TTYAttribute.GREEN);
            }
            else if (!forDisplayTopMatches.isEmpty())
            {
                this.outputDelegate.printlnStdout(
                        "Display country name \'" + query + "\' had no exact matches. "
                                + forDisplayTopMatches.size() + " closest matches are:",
                        TTYAttribute.BOLD);
                for (final IsoCountry country : forDisplayTopMatches)
                {
                    this.outputDelegate.printlnStdout(
                            country.toString() + ", " + country.getIso3CountryCode(),
                            TTYAttribute.GREEN);
                }
            }
            else
            {
                this.outputDelegate.printlnErrorMessage("unmatchable query " + query);
            }

            if (i < queries.size() - 1)
            {
                this.outputDelegate.printlnStdout("");
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
        registerOptionWithRequiredArgument(NUMBER_OPTION_LONG, NUMBER_OPTION_SHORT,
                NUMBER_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL, NUMBER_OPTION_HINT);
        super.registerOptionsAndArguments();
    }
}
