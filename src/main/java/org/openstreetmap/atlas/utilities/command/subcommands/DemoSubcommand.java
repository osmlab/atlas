package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.List;

import org.openstreetmap.atlas.utilities.command.AbstractOSMSubcommand;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;
import org.openstreetmap.atlas.utilities.command.terminal.TTYAttribute;

/**
 * @author lcram
 */
public class DemoSubcommand extends AbstractOSMSubcommand
{
    public static void main(final String[] args)
    {
        new DemoSubcommand().runSubcommandAndExit(args);
    }

    @Override
    public int execute()
    {
        // We registered favoriteFoods as variadic so it comes back as a List.
        final List<String> foods = getVariadicArgument("favoriteFoods");

        // We registered favoriteMeal as REQUIRED so it is safe to unwrap the Optional.
        final String meal = getUnaryArgument("favoriteMeal").get();

        printStdout("I like meal ");
        printStdout(meal, TTYAttribute.MAGENTA, TTYAttribute.BOLD, TTYAttribute.BLINK);
        printStdout(" the best\n");

        printVerboseStdout(
                "PS, I really like to talk, but only if you supply the \'--verbose\' option\n");

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

        printStdout("Favorite foods are:\n");
        for (int index = 0; index < repeat; index++)
        {
            for (final String food : foods)
            {
                String mutableFood = food;
                if (hasOption("capitalize"))
                {
                    mutableFood = mutableFood.toUpperCase();
                }
                printStdout(mutableFood + "\n", TTYAttribute.BOLD);
            }
        }

        if (hasOption("cheese"))
        {
            printStdout("Using " + getOptionArgument("cheese").orElse("NORMAL") + " cheese\n");
        }

        if (hasOption("beer"))
        {
            printStdout("Also ordering a beer, " + getOptionArgument("beer").get() + "\n");
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
        return "demo";
    }

    @Override
    public String getSimpleDescription()
    {
        return "a demo of the OSM subcommand API and features";
    }

    @Override
    public void registerManualPageSections()
    {
        final String paragraph1 = "This example section is filled out to demonstrate the capabilities of the "
                + "automatic documentation formatting code. Below are some examples of how to "
                + "run the demo command:";
        final String paragraph2 = "Here is a follow up paragraph that comes after the code example. Unfortunately,\n"
                + "Java does not provide a way to declare multi-line string literals. Due to this\n"
                + "shortcoming, declaring these paragraphs in code can be a pain. Thankfully,\n"
                + "Eclipse and IntelliJ both automatically format multi-line strings when pasted\n"
                + "into an empty \"\". Alternatively, you could have your subcommand read the\n"
                + "paragraphs from a resource file.";
        addManualPageSection("DEMO SECTION");
        addParagraphToSection("DEMO SECTION", paragraph1);
        addCodeBlockToSection("DEMO SECTION",
                "$ demo dinner pizza wings --beer=805 --cheese=cheddar");
        addCodeBlockToSection("DEMO SECTION", "$ demo breakfast waffles pancakes");
        addCodeBlockToSection("DEMO SECTION", "$ demo lunch salad --cheese=parmesan");
        addParagraphToSection("DEMO SECTION", paragraph2);
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
        registerOptionWithOptionalArgument("cheese",
                "Use cheese, optionally ask for LIGHT or EXTRA.", "amount");
        registerOptionWithRequiredArgument("repeat", "Repeat the food list N times.", "N");
        registerArgument("favoriteMeal", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
        registerArgument("favoriteFoods", ArgumentArity.VARIADIC, ArgumentOptionality.OPTIONAL);
    }
}
