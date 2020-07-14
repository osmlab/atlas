package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.openstreetmap.atlas.locale.IsoCountry;
import org.openstreetmap.atlas.locale.IsoCountryFuzzyMatcher;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
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

    private static final char ALL_OPTION_SHORT = 'a';
    private static final String ALL_OPTION_LONG = "all";
    private static final String ALL_OPTION_DESCRIPTION = "Show the entire ISO country listing.";
    private static final Integer ALL_OPTION_CONTEXT = 4;

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
        if (this.optionAndArgumentDelegate.getParserContext() == ALL_OPTION_CONTEXT)
        {
            return allExecute();
        }

        final List<String> queries = this.optionAndArgumentDelegate.getVariadicArgument(QUERY_HINT);

        for (int i = 0; i < queries.size(); i++)
        {
            final String query = queries.get(i);
            final Optional<IsoCountry> forIsoCode = IsoCountry.forCountryCode(query);
            final Optional<IsoCountry> forDisplayNameExact = IsoCountry.forDisplayCountry(query);
            final List<IsoCountry> forDisplayNameTopMatches = IsoCountryFuzzyMatcher
                    .forDisplayCountryTopMatches(this.optionAndArgumentDelegate
                            .getOptionArgument(NUMBER_OPTION_LONG, Integer::parseInt)
                            .orElse(DEFAULT_MATCH_NUMBER), query.toLowerCase());

            if (forIsoCode.isEmpty() && IsoCountry.forCountryCode(query.toUpperCase()).isPresent())
            {
                this.outputDelegate.printlnWarnMessage(
                        "did you mean case-sensitive ISO code '" + query.toUpperCase() + "'?");
            }

            // check for exact country code first
            if (forIsoCode.isPresent())
            {
                this.outputDelegate.printlnStdout("ISO code '" + query + "' matched: ",
                        TTYAttribute.BOLD);
                printCountry(forIsoCode.get());
            }
            else if (forDisplayNameExact.isPresent())
            {
                this.outputDelegate.printlnStdout("Display country name '" + query + "' matched: ",
                        TTYAttribute.BOLD);
                printCountry(forDisplayNameExact.get());
            }
            else if (!forDisplayNameTopMatches.isEmpty())
            {
                this.outputDelegate.printlnStdout(
                        "Display country name '" + query + "' had no exact matches. "
                                + forDisplayNameTopMatches.size() + " closest matches are:",
                        TTYAttribute.BOLD);
                for (final IsoCountry country : forDisplayNameTopMatches)
                {
                    printCountry(country);
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
        registerArgument(QUERY_HINT, ArgumentArity.VARIADIC, ArgumentOptionality.REQUIRED,
                AbstractAtlasShellToolsCommand.DEFAULT_CONTEXT);
        registerOptionWithRequiredArgument(NUMBER_OPTION_LONG, NUMBER_OPTION_SHORT,
                NUMBER_OPTION_DESCRIPTION, OptionOptionality.OPTIONAL, NUMBER_OPTION_HINT,
                AbstractAtlasShellToolsCommand.DEFAULT_CONTEXT);
        registerOption(ALL_OPTION_LONG, ALL_OPTION_SHORT, ALL_OPTION_DESCRIPTION,
                OptionOptionality.REQUIRED, ALL_OPTION_CONTEXT);
        super.registerOptionsAndArguments();
    }

    private int allExecute()
    {
        final List<String> countries = new ArrayList<>(IsoCountry.allCountryCodes());
        Collections.sort(countries);

        this.outputDelegate.printlnStdout("Displaying all countries:", TTYAttribute.BOLD);
        for (final String country : countries)
        {
            final Optional<IsoCountry> forCode = IsoCountry.forCountryCode(country);
            if (forCode.isEmpty())
            {
                throw new AtlasShellToolsException();
            }
            printCountry(forCode.get());
        }
        return 0;
    }

    private void printCountry(final IsoCountry country)
    {
        this.outputDelegate.printlnStdout(country.getCountryCode() + "   "
                + country.getIso3CountryCode() + "   " + country.toString(), TTYAttribute.GREEN);
    }
}
