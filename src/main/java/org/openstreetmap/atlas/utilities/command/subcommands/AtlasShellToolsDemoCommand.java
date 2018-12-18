package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.List;

import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
import org.openstreetmap.atlas.utilities.command.abstractcommand.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.abstractcommand.CommandOutputDelegate;
import org.openstreetmap.atlas.utilities.command.abstractcommand.OptionAndArgumentFetcher;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.terminal.TTYAttribute;

/**
 * @author lcram
 */
public class AtlasShellToolsDemoCommand extends AbstractAtlasShellToolsCommand
{
    private static final int BREAKFAST_CONTEXT = 4;
    private static final String DESCRIPTION_SECTION = "AtlasShellToolsDemoCommandDescriptionSection.txt";
    private static final String EXAMPLES_SECTION = "AtlasShellToolsDemoCommandExamplesSection.txt";

    private final OptionAndArgumentFetcher fetcher;
    private final CommandOutputDelegate output;

    public static void main(final String[] args)
    {
        new AtlasShellToolsDemoCommand().runSubcommandAndExit(args);
    }

    public AtlasShellToolsDemoCommand()
    {
        this.fetcher = this.getOptionAndArgumentFetcher();
        this.output = this.getCommandOutputDelegate();
    }

    @Override
    public int execute()
    {
        // Check if the parser context detected the breakfast usage
        if (this.fetcher.getParserContext() == BREAKFAST_CONTEXT)
        {
            executeBreakfastContext();
        }
        else
        {
            executeLunchDinnerContext();
        }

        return 0;
    }

    @Override
    public String getCommandName()
    {
        return "ast-demo";
    }

    @Override
    public String getSimpleDescription()
    {
        return "a demo of the Atlas Shell Tools subcommand API and features";
    }

    @Override
    public void registerManualPageSections()
    {
        addManualPageSection("DESCRIPTION",
                AtlasShellToolsDemoCommand.class.getResourceAsStream(DESCRIPTION_SECTION));
        addManualPageSection("EXAMPLES",
                AtlasShellToolsDemoCommand.class.getResourceAsStream(EXAMPLES_SECTION));
    }

    @Override
    public void registerOptionsAndArguments()
    {
        /*
         * Generally, it's better practice to declare option forms, descriptions, and hints in
         * static final Strings at the top of your class. However, this demo command declares them
         * using literals just for ease of tutorial.
         */

        final String beerDescription = "Brand of your favorite beer. "
                + "Currently making this option description really long in"
                + " order to test out the autoformatting capabilities of"
                + " the DocumentationFormatter class.";

        setVersion("0.0.1");

        // Register options/arguments for default lunch/dinner context
        registerOption("capitalize", 'c', "Capitalize the foods list.");
        registerOptionWithRequiredArgument("beer", beerDescription, "brand");
        registerOptionWithOptionalArgument("cheese", 'C',
                "Use cheese. Defaults to cheddar, but will accept a supplied alternative.", "type");
        registerOptionWithRequiredArgument("repeat", 'R', "Repeat the food list N times.", "N");
        registerArgument("favoriteMeal", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
        registerArgument("favoriteFoods", ArgumentArity.VARIADIC, ArgumentOptionality.OPTIONAL);

        // Register options/arguments for an alternate breakfast use case
        registerOption("breakfast", 'b', "Use breakfast mode", BREAKFAST_CONTEXT);
        registerArgument("favoriteBreakfastFood", ArgumentArity.UNARY, ArgumentOptionality.OPTIONAL,
                BREAKFAST_CONTEXT);
    }

    private void executeBreakfastContext()
    {
        final String breakfast = this.fetcher.getUnaryArgument("favoriteBreakfastFood")
                .orElse("Default waffles :(");
        this.output.printlnStdout("Using special breakfast mode:");
        this.output.printlnStdout(breakfast, TTYAttribute.BOLD);
    }

    private void executeLunchDinnerContext()
    {
        // We registered favoriteFoods as variadic so it comes back as a List.
        final List<String> foods = this.fetcher.getVariadicArgument("favoriteFoods");

        // We registered favoriteMeal as REQUIRED so it is safe to unwrap the Optional.
        // The orElseThrow is just there to stop Sonar from complaining.
        final String meal = this.fetcher.getUnaryArgument("favoriteMeal")
                .orElseThrow(AtlasShellToolsException::new);

        this.output.printStdout("I like meal ");
        this.output.printStdout(meal, TTYAttribute.MAGENTA, TTYAttribute.BOLD, TTYAttribute.BLINK);
        this.output.printlnStdout(" the best");

        final int repeatDefault = 1;
        final int repeat = this.fetcher.getOptionArgument("repeat", value ->
        {
            final int parsed;
            try
            {
                parsed = Integer.parseInt(value);
            }
            catch (final Exception exception)
            {
                this.output.printlnWarnMessage("failed to parse repeat argument, using default");
                return null;
            }
            return parsed;
        }).orElse(repeatDefault);

        this.output.printlnStdout("Favorite foods are:");
        for (int index = 0; index < repeat; index++)
        {
            for (final String food : foods)
            {
                String mutableFood = food;
                if (this.fetcher.hasOption("capitalize"))
                {
                    mutableFood = mutableFood.toUpperCase();
                }
                this.output.printlnStdout(mutableFood, TTYAttribute.BOLD);
            }
        }

        if (this.fetcher.hasOption("cheese"))
        {
            this.output.printlnStdout("Using "
                    + this.fetcher.getOptionArgument("cheese").orElse("cheddar") + " cheese");
        }

        if (this.fetcher.hasOption("beer"))
        {
            this.output.printlnStdout("Also ordering a beer, " + this.fetcher
                    .getOptionArgument("beer").orElseThrow(AtlasShellToolsException::new));
        }
        else
        {
            this.output.printlnWarnMessage("beer skipped");
        }

        this.output.printStderr("Here is a closing stderr message\n", TTYAttribute.UNDERLINE);
    }
}
