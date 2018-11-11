package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.List;

import org.openstreetmap.atlas.utilities.command.AbstractOSMSubcommand;
import org.openstreetmap.atlas.utilities.command.output.TTYAttribute;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentArity;
import org.openstreetmap.atlas.utilities.command.parsing.ArgumentOptionality;

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
        printStdout(meal, TTYAttribute.MAGENTA, TTYAttribute.BOLD);
        printStdout(" the best\n");

        printVerboseStdout(
                "PS, I really like to talk, but only if you supply the \'--verbose\' option\n");

        printStdout("Favorite foods are:\n");
        for (String food : foods)
        {
            if (hasOption("capitalize"))
            {
                food = food.toUpperCase();
            }
            printStdout(food + "\n", TTYAttribute.BOLD);
        }

        if (hasOption("cheese"))
        {
            printStdout("Using " + getLongOptionArgument("cheese").orElse("NORMAL") + " cheese\n");
        }

        if (hasOption("beer"))
        {
            printStdout("Also ordering a beer, " + getLongOptionArgument("beer").get() + "\n");
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
    public void registerOptionsAndArguments()
    {
        setVersion("1.0.0");
        registerOption("capitalize", 'c', "Capitalize the foods list");
        registerOptionWithRequiredArgument("beer", "Favorite beer", "beer");
        registerOptionWithOptionalArgument("cheese",
                "Use cheese, optionally ask for LIGHT or EXTRA", "amount");
        registerArgument("favoriteMeal", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
        registerArgument("favoriteFoods", ArgumentArity.VARIADIC, ArgumentOptionality.OPTIONAL);
    }
}
