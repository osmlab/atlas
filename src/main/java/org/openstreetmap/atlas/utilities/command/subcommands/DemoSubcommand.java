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
public class DemoSubcommand extends AbstractAtlasShellToolsCommand
{
    private static final int BREAKFAST_CONTEXT = 4;

    public static void main(final String[] args)
    {
        new DemoSubcommand().runSubcommandAndExit(args);
    }

    @Override
    public int execute()
    {
        // Check if the parser context detected the breakfast usage
        if (getParserContext() == BREAKFAST_CONTEXT)
        {
            System.exit(executeBreakfastContext());
        }

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
        final String paragraph1 = "This command serves as a demo of the subcommand API and capabilities. "
                + "This example section is filled out to demonstrate the behavior of the "
                + "automatic documentation formatting code.";
        final String paragraph2 = "Here is a second follow up paragraph. Unfortunately, "
                + "Java does not provide a way to declare multi-line string literals. Due to this "
                + "shortcoming, declaring these paragraphs in code can be a pain. Thankfully, "
                + "Eclipse and IntelliJ both automatically format multi-line strings when pasted "
                + "into an empty \"\". Alternatively, you could have your subcommand read the "
                + "paragraphs from a resource file.";

        addManualPageSection("DESCRIPTION");
        addParagraphToSection("DESCRIPTION", paragraph1);
        addParagraphToSection("DESCRIPTION", paragraph2);

        addManualPageSection("EXAMPLES");
        addParagraphToSection("EXAMPLES",
                "Run a dinner command with pizza and wings. Use 805 beer and the default cheese:");
        addCodeBlockToSection("EXAMPLES", "$ demo dinner pizza wings --beer=805 --cheese");
        addParagraphToSection("EXAMPLES",
                "Run a breakfast command with some waffles and pancakes:");
        addCodeBlockToSection("EXAMPLES", "$ demo breakfast waffles pancakes");
        addParagraphToSection("EXAMPLES",
                "Run a lunch command with a salad. Use some tasty parmesan cheese:");
        addCodeBlockToSection("EXAMPLES", "$ demo lunch salad --cheese=parmesan");
    }

    @Override
    public void registerOptionsAndArguments()
    {
        final String beerDescription = "Brand of your favorite beer. "
                + "Currently making this option description really long in"
                + " order to test out the autoformatting capabilities of"
                + " the DocumentationFormatter class.";

        setVersion("1.0.0");
        registerOption("capitalize", 'c', "Capitalize the foods list.");
        registerOptionWithRequiredArgument("beer", beerDescription, "brand");
        registerOptionWithOptionalArgument("cheese", 'C',
                "Use cheese. Defaults to cheddar, but will accept a supplied alternative.", "type");
        registerOptionWithRequiredArgument("repeat", 'R', "Repeat the food list N times.", "N");
        registerArgument("favoriteMeal", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
        registerArgument("favoriteFoods", ArgumentArity.VARIADIC, ArgumentOptionality.OPTIONAL);

        registerOption("breakfast", 'b', "Use breakfast mode", BREAKFAST_CONTEXT);
        registerArgument("favoriteBreakfastFood", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED,
                BREAKFAST_CONTEXT);
    }

    private int executeBreakfastContext()
    {
        final String breakfast = getUnaryArgument("favoriteBreakfastFood")
                .orElseThrow(AtlasShellToolsException::new);
        printlnStdout("Using special breakfast mode:");
        printlnStdout(breakfast, TTYAttribute.BOLD);

        return 0;
    }
}
