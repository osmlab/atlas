package org.openstreetmap.atlas.utilities.command.subcommands;

import java.util.List;

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
public class AtlasShellToolsDemoCommand extends AbstractAtlasShellToolsCommand
{
    private static final int BREAKFAST_CONTEXT = 4;
    private static final String DESCRIPTION_SECTION = "AtlasShellToolsDemoCommandDescriptionSection.txt";
    private static final String EXAMPLES_SECTION = "AtlasShellToolsDemoCommandExamplesSection.txt";

    private final OptionAndArgumentDelegate optargDelegate;
    private final CommandOutputDelegate outputDelegate;

    public static void main(final String[] args)
    {
        new AtlasShellToolsDemoCommand().runSubcommandAndExit(args);
    }

    public AtlasShellToolsDemoCommand()
    {
        this.optargDelegate = this.getOptionAndArgumentDelegate();
        this.outputDelegate = this.getCommandOutputDelegate();
    }

    @Override
    public int execute()
    {
        // Check if the parser context detected the breakfast usage
        if (this.optargDelegate.getParserContext() == BREAKFAST_CONTEXT)
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
        registerOption("capitalize", 'c', "Capitalize the foods list.", OptionOptionality.OPTIONAL);
        registerOptionWithRequiredArgument("beer", beerDescription, OptionOptionality.OPTIONAL,
                "brand");
        registerOptionWithOptionalArgument("cheese", 'C', // NOSONAR
                "Use cheese. Defaults to cheddar, but will accept a supplied alternative.",
                OptionOptionality.OPTIONAL, "type");
        registerOptionWithRequiredArgument("repeat", 'R', "Repeat the food list N times.",
                OptionOptionality.OPTIONAL, "N");
        registerArgument("favoriteMeal", ArgumentArity.UNARY, ArgumentOptionality.REQUIRED);
        registerArgument("favoriteFoods", ArgumentArity.VARIADIC, ArgumentOptionality.OPTIONAL);

        // Register options/arguments for an alternate breakfast use case
        registerOption("breakfast", 'b', "Use breakfast mode", OptionOptionality.REQUIRED,
                BREAKFAST_CONTEXT);
        registerArgument("favoriteBreakfastFood", ArgumentArity.UNARY, ArgumentOptionality.OPTIONAL,
                BREAKFAST_CONTEXT);

        /*
         * Always call super.registerOptionsAndArguments last. Some superclasses will attempt to
         * register options to all available parser contexts. You want to ensure that each super
         * class has access to the full set of parser contexts within its class hierarchy. For
         * example, the global superclass AbstractAtlasShellToolsCommand registers a '--verbose'
         * option to all parser contexts. By calling super.register... last, you ensure that the
         * '--verbose' registry code runs for every context you registered above. Had you called
         * super.register... first, the '--verbose' registry code would have missed any additional
         * contexts you registered here.
         */
        super.registerOptionsAndArguments();
    }

    private void executeBreakfastContext()
    {
        final String breakfast = this.optargDelegate.getUnaryArgument("favoriteBreakfastFood")
                .orElse("Default waffles :(");
        this.outputDelegate.printlnStdout("Using special breakfast mode:");
        this.outputDelegate.printlnStdout(breakfast, TTYAttribute.BOLD);
    }

    private void executeLunchDinnerContext()
    {
        // We registered favoriteFoods as variadic so it comes back as a List.
        final List<String> foods = this.optargDelegate.getVariadicArgument("favoriteFoods");

        // We registered favoriteMeal as REQUIRED so it is safe to unwrap the Optional.
        // The orElseThrow is just there to stop Sonar from complaining.
        final String meal = this.optargDelegate.getUnaryArgument("favoriteMeal")
                .orElseThrow(AtlasShellToolsException::new);

        this.outputDelegate.printStdout("I like meal ");
        this.outputDelegate.printStdout(meal, TTYAttribute.MAGENTA, TTYAttribute.BOLD,
                TTYAttribute.BLINK);
        this.outputDelegate.printlnStdout(" the best");

        final int repeatDefault = 1;
        final int repeat = this.optargDelegate.getOptionArgument("repeat", value ->
        {
            final int parsed;
            try
            {
                parsed = Integer.parseInt(value);
            }
            catch (final Exception exception)
            {
                this.outputDelegate
                        .printlnWarnMessage("failed to parse repeat argument, using default");
                return null;
            }
            return parsed;
        }).orElse(repeatDefault);

        this.outputDelegate.printlnStdout("Favorite foods are:");
        for (int index = 0; index < repeat; index++)
        {
            for (final String food : foods)
            {
                String mutableFood = food;
                if (this.optargDelegate.hasOption("capitalize"))
                {
                    mutableFood = mutableFood.toUpperCase();
                }
                this.outputDelegate.printlnStdout(mutableFood, TTYAttribute.BOLD);
            }
        }

        if (this.optargDelegate.hasOption("cheese"))
        {
            this.outputDelegate.printlnStdout(
                    "Using " + this.optargDelegate.getOptionArgument("cheese").orElse("cheddar")
                            + " cheese");
        }

        if (this.optargDelegate.hasOption("beer"))
        {
            this.outputDelegate.printlnStdout("Also ordering a beer, " + this.optargDelegate
                    .getOptionArgument("beer").orElseThrow(AtlasShellToolsException::new));
        }
        else
        {
            this.outputDelegate.printlnWarnMessage("beer skipped");
        }

        this.outputDelegate.printStderr("Here is a closing stderr message\n",
                TTYAttribute.UNDERLINE);
    }
}
