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
        final List<String> foods = getVariadicArgument("favoriteFoods");
        final String meal = getUnaryArgument("favoriteMeal").get();

        printStdout("I like meal ");
        printStdout(meal, TTYAttribute.MAGENTA, TTYAttribute.BOLD, TTYAttribute.BLINK);
        printStdout(" the best\n");

        printVerboseStdout(
                "PS, I really like to talk, but only if you supply the \'--verbose\' option\n");

        printStdout("Favorite foods are:\n");
        for (final String food : foods)
        {
            String mutableFood = food;
            if (hasOption("capitalize"))
            {
                mutableFood = mutableFood.toUpperCase();
            }
            printStdout(mutableFood + "\n", TTYAttribute.BOLD);
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

    }

    @Override
    public void registerOptionsAndArguments()
    {
        final String beerDescription = "Brand of your favorite beer. "
                + "Currently making this option description really long in"
                + " order to test out the autoformatting capabilities of"
                + " the DocumentationFormatter class.";

        setVersion("1.0.0");
        registerOption("capitalize", 'c', "Capitalize the foods list");
        registerOptionWithRequiredArgument("beer", beerDescription, "brand");
        registerOptionWithOptionalArgument("cheese",
                "Use cheese, optionally ask for LIGHT or EXTRA", "amount");
        registerArgument("favoriteMeal", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
        registerArgument("favoriteFoods", ArgumentArity.VARIADIC, ArgumentOptionality.OPTIONAL);
    }
}
