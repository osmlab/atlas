package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.List;

import org.openstreetmap.atlas.utilities.command.AbstractAtlasShellToolsCommand;
import org.openstreetmap.atlas.utilities.command.AtlasShellToolsException;
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

    public static void main(final String[] args)
    {
        new AtlasShellToolsDemoCommand().runSubcommandAndExit(args);
    }

    @Override
    public int execute()
    {
        // Check if the parser context detected the breakfast usage
        if (getParserContext() == BREAKFAST_CONTEXT)
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

        setVersion("1.0.0");

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
        final String breakfast = getUnaryArgument("favoriteBreakfastFood")
                .orElse("Default waffles :(");
        printlnStdout("Using special breakfast mode:");
        printlnStdout(breakfast, TTYAttribute.BOLD);
    }

    private void executeLunchDinnerContext()
    {
        // We registered favoriteFoods as variadic so it comes back as a List.
        final List<String> foods = getVariadicArgument("favoriteFoods");

        // We registered favoriteMeal as REQUIRED so it is safe to unwrap the Optional.
        // The orElseThrow is just there to stop Sonar from complaining.
        final String meal = getUnaryArgument("favoriteMeal")
                .orElseThrow(AtlasShellToolsException::new);

        printStdout("I like meal ");
        printStdout(meal, TTYAttribute.MAGENTA, TTYAttribute.BOLD, TTYAttribute.BLINK);
        printlnStdout(" the best");

        final int repeatDefault = 1;
        final int repeat = getOptionArgument("repeat", value ->
        {
            final int parsed;
            try
            {
                parsed = Integer.parseInt(value);
            }
            catch (final Exception exception)
            {
                printlnWarnMessage("failed to parse repeat argument, using default");
                return null;
            }
            return parsed;
        }).orElse(repeatDefault);

        printlnStdout("Favorite foods are:");
        for (int index = 0; index < repeat; index++)
        {
            for (final String food : foods)
            {
                String mutableFood = food;
                if (hasOption("capitalize"))
                {
                    mutableFood = mutableFood.toUpperCase();
                }
                printlnStdout(mutableFood, TTYAttribute.BOLD);
            }
        }

        if (hasOption("cheese"))
        {
            printlnStdout("Using " + getOptionArgument("cheese").orElse("cheddar") + " cheese");
        }

        if (hasOption("beer"))
        {
            printlnStdout("Also ordering a beer, "
                    + getOptionArgument("beer").orElseThrow(AtlasShellToolsException::new));
        }
        else
        {
            printlnWarnMessage("beer skipped");
        }

        printStderr("Here is a closing stderr message\n", TTYAttribute.UNDERLINE);

    }
}
